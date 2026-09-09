package br.com.fiap.techchallenge.agendamento.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void deveHandlearIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Valor inválido");

        ProblemDetail result = exceptionHandler.handleIllegalArgument(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Valor inválido", result.getDetail());
        assertEquals("Requisição inválida", result.getTitle());
        assertTrue(result.getType().toString().contains("invalid-request"));
    }

    @Test
    void deveHandlearInvalidCredentialsException() {
        AutorizacaoInvalidaException ex = new AutorizacaoInvalidaException();

        ProblemDetail result = exceptionHandler.handleInvalidCredentials(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
        assertEquals("Credenciais inválidas", result.getTitle());
        assertTrue(result.getType().toString().contains("unauthorized"));
    }

    @Test
    void deveHandlearEntidadeNaoEncontradaException() {
        EntidadeNaoEncontradaException ex = new EntidadeNaoEncontradaException("Usuário não encontrado");

        ProblemDetail result = exceptionHandler.handleNotFound(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Usuário não encontrado", result.getDetail());
        assertEquals("Recurso não encontrado", result.getTitle());
        assertTrue(result.getType().toString().contains("not-found"));
    }

    @Test
    void deveHandlearForbiddenOperationException() {
        OperacaoInvalidaException ex = new OperacaoInvalidaException("Acesso negado");

        ProblemDetail result = exceptionHandler.handleForbidden(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("Acesso negado", result.getDetail());
        assertEquals("Acesso negado", result.getTitle());
        assertTrue(result.getType().toString().contains("forbidden"));
    }

    @Test
    void deveHandlearAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Sem permissão");

        ProblemDetail result = exceptionHandler.handleForbidden(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("Sem permissão para acessar este recurso", result.getDetail());
        assertEquals("Acesso negado", result.getTitle());
        assertTrue(result.getType().toString().contains("forbidden"));
    }

    @Test
    void deveHandlearJwtTokenException() {
        JwtTokenException ex = new JwtTokenException("Token inválido");

        ProblemDetail result = exceptionHandler.handleJwtToken(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
        assertEquals("Token inválido", result.getDetail());
        assertEquals("Erro na geração de token JWT", result.getTitle());
        assertTrue(result.getType().toString().contains("jwt-error"));
    }

    @Test
    void deveHandlearGenericException() {
        Exception ex = new Exception("Erro inesperado");

        ProblemDetail result = exceptionHandler.handleGeneric(ex);

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Erro interno do servidor", result.getDetail());
        assertEquals("Erro interno", result.getTitle());
        assertTrue(result.getType().toString().contains("internal"));
    }

    @Test
    void deveHandlearIllegalArgumentExceptionComMensagem() {
        String mensagem = "Email já cadastrado";
        IllegalArgumentException ex = new IllegalArgumentException(mensagem);

        ProblemDetail result = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(mensagem, result.getDetail());
    }

    @Test
    void deveHandlearEntidadeNaoEncontradaExceptionComMensagem() {
        String mensagem = "Consulta com ID 123 não encontrada";
        EntidadeNaoEncontradaException ex = new EntidadeNaoEncontradaException(mensagem);

        ProblemDetail result = exceptionHandler.handleNotFound(ex);

        assertEquals(mensagem, result.getDetail());
    }

    @Test
    void deveRetornarHttpStatusCorretoParaIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Erro");

        ProblemDetail result = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(400, result.getStatus());
    }

    @Test
    void deveRetornarHttpStatusCorretoParaNotFound() {
        EntidadeNaoEncontradaException ex = new EntidadeNaoEncontradaException("Não encontrado");

        ProblemDetail result = exceptionHandler.handleNotFound(ex);

        assertEquals(404, result.getStatus());
    }

    @Test
    void deveRetornarHttpStatusCorretoParaForbidden() {
        OperacaoInvalidaException ex = new OperacaoInvalidaException("Proibido");

        ProblemDetail result = exceptionHandler.handleForbidden(ex);

        assertEquals(403, result.getStatus());
    }

    @Test
    void deveRetornarHttpStatusCorretoParaUnauthorized() {
        AutorizacaoInvalidaException ex = new AutorizacaoInvalidaException();

        ProblemDetail result = exceptionHandler.handleInvalidCredentials(ex);

        assertEquals(401, result.getStatus());
    }

    @Test
    void deveRetornarHttpStatusCorretoParaInternalServerError() {
        Exception ex = new Exception("Erro interno");

        ProblemDetail result = exceptionHandler.handleGeneric(ex);

        assertEquals(500, result.getStatus());
    }

    @Test
    void devePossuirTypeUnicoParaCadaExcecao() {
        IllegalArgumentException ex1 = new IllegalArgumentException("Erro");
        EntidadeNaoEncontradaException ex2 = new EntidadeNaoEncontradaException("Erro");
        OperacaoInvalidaException ex3 = new OperacaoInvalidaException("Erro");

        ProblemDetail result1 = exceptionHandler.handleIllegalArgument(ex1);
        ProblemDetail result2 = exceptionHandler.handleNotFound(ex2);
        ProblemDetail result3 = exceptionHandler.handleForbidden(ex3);

        assertNotEquals(result1.getType(), result2.getType());
        assertNotEquals(result2.getType(), result3.getType());
        assertNotEquals(result1.getType(), result3.getType());
    }

}
