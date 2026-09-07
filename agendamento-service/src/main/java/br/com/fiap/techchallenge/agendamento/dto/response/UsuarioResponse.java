package br.com.fiap.techchallenge.agendamento.dto.response;

import br.com.fiap.techchallenge.agendamento.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String login,
        String tipoUsuario
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getLogin(),
                usuario.getClass().getSimpleName()
        );
    }
}
