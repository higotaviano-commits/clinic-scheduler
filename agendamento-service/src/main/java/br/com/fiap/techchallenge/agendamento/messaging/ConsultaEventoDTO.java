package br.com.fiap.techchallenge.agendamento.messaging;

import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaEventoDTO(
        Long consultaId,
        Long pacienteId,
        Long medicoId,
        LocalDateTime dataHora,
        StatusConsulta status,
        String observacoes,
        LocalDateTime ocorridoEm
) {
}
