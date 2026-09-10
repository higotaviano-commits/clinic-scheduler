package br.com.fiap.techchallenge.agendamento.exception;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Requisição inválida");
        problem.setType(URI.create("https://api.hospital.com/errors/invalid-request"));
        return problem;
    }

    @ExceptionHandler(AutorizacaoInvalidaException.class)
    public ProblemDetail handleInvalidCredentials(AutorizacaoInvalidaException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Credenciais inválidas");
        problem.setType(URI.create("https://api.hospital.com/errors/unauthorized"));
        return problem;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Tipo de conteúdo não suportado"
                );

        problem.setTitle("Tipo de mídia não suportado");
        problem.setType(URI.create("https://api.hospital.com/errors/unsupported-media-type"));

        return problem;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Método HTTP não permitido para este recurso"
                );

        problem.setTitle("Método não permitido");
        problem.setType(URI.create("https://api.hospital.com/errors/method-not-allowed"));

        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Recurso não encontrado"
                );

        problem.setTitle("Recurso não encontrado");
        problem.setType(URI.create("https://api.hospital.com/errors/not-found"));

        return problem;
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ProblemDetail handleNotFound(EntidadeNaoEncontradaException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso não encontrado");
        problem.setType(URI.create("https://api.hospital.com/errors/not-found"));
        return problem;
    }

    @ExceptionHandler({OperacaoInvalidaException.class, AccessDeniedException.class})
    public ProblemDetail handleForbidden(RuntimeException ex) {
        String detail = ex instanceof OperacaoInvalidaException ? ex.getMessage() : "Sem permissão para acessar este recurso";
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
        problem.setTitle("Acesso negado");
        problem.setType(URI.create("https://api.hospital.com/errors/forbidden"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);

        problem.setTitle("Erro de validação");
        problem.setType(URI.create(
                "https://api.hospital.com/errors/validation"
        ));

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "JSON inválido ou malformado"
                );

        problem.setTitle("Requisição inválida");
        problem.setType(URI.create(
                "https://api.hospital.com/errors/invalid-request"
        ));

        return problem;
    }

    @ExceptionHandler(JwtTokenException.class)
    public ProblemDetail handleJwtToken(JwtTokenException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Erro na geração de token JWT");
        problem.setType(URI.create("https://api.hospital.com/errors/jwt-error"));
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.CONFLICT, "Dado já cadastrado no sistema");
        problem.setTitle("Conflito de dados");
        problem.setType(URI.create("https://api.hospital.com/errors/conflict"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        System.out.println(ex.getMessage());
        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
        problem.setTitle("Erro interno");
        problem.setType(URI.create("https://api.hospital.com/errors/internal"));
        return problem;
    }
}
