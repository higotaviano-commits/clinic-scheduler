package br.com.fiap.techchalleger.historicoservice.exception;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Usuário não possui permissão para acessar este recurso.");
    }

    public AcessoNegadoException(String message) {
        super(message);
    }
}
