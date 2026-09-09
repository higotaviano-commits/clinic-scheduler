package br.com.fiap.techchalleger.historicoservice.exception;

public class UsuarioNaoAutenticadoException extends RuntimeException {

    public UsuarioNaoAutenticadoException() {
        super("Usuário não autenticado.");
    }
}