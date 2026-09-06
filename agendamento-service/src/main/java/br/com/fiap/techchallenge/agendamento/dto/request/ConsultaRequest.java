package br.com.fiap.techchallenge.agendamento.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConsultaRequest(

        @NotNull(message = "pacienteId é obrigatório")
        Long pacienteId,

        @NotNull(message = "medicoId é obrigatório")
        Long medicoId,

        @NotNull(message = "dataHora é obrigatória")
        @Future(message = "dataHora deve estar no futuro")
        LocalDateTime dataHora,

        String observacoes
) {
}
