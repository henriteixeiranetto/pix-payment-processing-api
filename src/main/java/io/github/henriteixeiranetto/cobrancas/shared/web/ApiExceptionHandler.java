package io.github.henriteixeiranetto.cobrancas.shared.web;

import io.github.henriteixeiranetto.cobrancas.cobranca.domain.CobrancaNaoEncontradaException;
import io.github.henriteixeiranetto.cobrancas.shared.domain.ConsultaInvalidaException;
import io.github.henriteixeiranetto.cobrancas.shared.domain.RegraDeNegocioException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte exceções em respostas de erro no formato RFC 9457 (application/problem+json),
 * com os mesmos tipos de erro e a lista de {@code violacoes} usados pela API Pix.
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para também padronizar os erros que o
 * próprio Spring MVC lança (JSON malformado, parâmetro ausente, tipo inválido etc.).
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static final String TIPOS_DE_ERRO = "https://pix.bcb.gov.br/api/v2/error/";

    @ExceptionHandler(ConsultaInvalidaException.class)
    ProblemDetail consultaInvalida(ConsultaInvalidaException ex) {
        return consultaInvalida(List.of(new Violacao(ex.razao(), ex.propriedade())));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail operacaoInvalida(RegraDeNegocioException ex) {
        return operacaoInvalida(List.of(new Violacao(ex.razao(), "cob." + ex.propriedade())));
    }

    @ExceptionHandler(CobrancaNaoEncontradaException.class)
    ProblemDetail naoEncontrada(CobrancaNaoEncontradaException ex) {
        return problema(HttpStatus.NOT_FOUND, "CobNaoEncontrado", "Cobrança não encontrada.", ex.getMessage());
    }

    /**
     * Duas requisições alteraram a mesma cobrança ao mesmo tempo (versão desatualizada), ou duas
     * tentaram criar o mesmo txid (chave primária duplicada). Repetir a requisição resolve.
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail conflito(RuntimeException ex) {
        log.warn("Conflito de concorrência ao gravar cobrança: {}", ex.getMessage());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A cobrança foi alterada por outra requisição ao mesmo tempo. Consulte a cobrança e tente novamente.");
        problema.setTitle("Conflito de concorrência.");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "ErroInternoDoServidor", "Erro interno do servidor.",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Violacao> violacoes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .sorted()
                .map(campo -> new Violacao("O campo cob." + campo + " não respeita o schema.", "cob." + campo))
                .toList();
        return ResponseEntity.badRequest().body(operacaoInvalida(violacoes));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "CobOperacaoInvalida",
                "Cobrança inválida.", "O corpo da requisição não é um JSON válido para este recurso."));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String parametro = ex.getParameterName();
        return ResponseEntity.badRequest()
                .body(consultaInvalida(List.of(new Violacao("O parâmetro " + parametro + " é obrigatório.", parametro))));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String parametro = ex.getPropertyName();
        return ResponseEntity.badRequest().body(consultaInvalida(
                List.of(new Violacao("O parâmetro " + parametro + " não respeita o schema.", parametro))));
    }

    private static ProblemDetail operacaoInvalida(List<Violacao> violacoes) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "CobOperacaoInvalida", "Cobrança inválida.",
                "A requisição que busca alterar ou criar uma cobrança para pagamento imediato "
                        + "não respeita o schema ou está semanticamente errada.");
        problema.setProperty("violacoes", violacoes);
        return problema;
    }

    private static ProblemDetail consultaInvalida(List<Violacao> violacoes) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "CobConsultaInvalida", "Consulta inválida.",
                "Os parâmetros de consulta à lista de cobranças não respeitam o schema "
                        + "ou não fazem sentido semanticamente.");
        problema.setProperty("violacoes", violacoes);
        return problema;
    }

    private static ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create(TIPOS_DE_ERRO + tipo));
        problema.setTitle(titulo);
        return problema;
    }
}
