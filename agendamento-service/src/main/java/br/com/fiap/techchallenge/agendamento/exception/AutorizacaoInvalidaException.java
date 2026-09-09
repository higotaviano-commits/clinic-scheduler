package br.com.fiap.techchallenge.agendamento.exception;

public class AutorizacaoInvalidaException extends RuntimeException {
    public AutorizacaoInvalidaException() {
        super("Login ou senha inválidos");
    }
}
