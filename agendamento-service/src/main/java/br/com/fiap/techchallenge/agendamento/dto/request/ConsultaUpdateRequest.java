package br.com.fiap.techchallenge.agendamento.dto.request;

import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaUpdateRequest(
        LocalDateTime dataHora,
        StatusConsulta status,
        String observacoes
) {
}
