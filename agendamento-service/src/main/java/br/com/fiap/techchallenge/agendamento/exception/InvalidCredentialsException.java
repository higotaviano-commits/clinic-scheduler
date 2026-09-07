package br.com.fiap.techchallenge.agendamento.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Login ou senha inválidos");
    }
}
