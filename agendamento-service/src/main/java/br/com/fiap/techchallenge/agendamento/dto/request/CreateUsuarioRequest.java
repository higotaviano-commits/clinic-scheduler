package br.com.fiap.techchallenge.agendamento.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUsuarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Login é obrigatório")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotBlank(message = "Tipo é obrigatório. Use MEDICO, ENFERMEIRO ou PACIENTE")
        String tipoUsuario
) {
}
