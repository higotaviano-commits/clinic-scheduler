package br.com.fiap.techchallenge.agendamento.dto.response;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long pacienteId,
        Long medicoId,
        Long registradoPorId,
        LocalDateTime dataHora,
        StatusConsulta status,
        String observacoes,
        LocalDateTime criadoEm,
        LocalDateTime lastModifiedAt
) {
    public static ConsultaResponse from(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPacienteId(),
                consulta.getMedicoId(),
                consulta.getRegistradoPorId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getObservacoes(),
                consulta.getCriadoEm(),
                consulta.getLastModifiedAt()
        );
    }
}
