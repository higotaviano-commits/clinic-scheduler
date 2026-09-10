package br.com.fiap.techchalleger.historicoservice.dto;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long pacienteId,
        Long medicoId,
        LocalDateTime dataHora,
        String status,
        String descricao,
        String diagnostico
) {
}
