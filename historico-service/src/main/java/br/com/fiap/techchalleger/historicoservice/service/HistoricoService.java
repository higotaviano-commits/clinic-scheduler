package br.com.fiap.techchalleger.historicoservice.service;

import br.com.fiap.techchalleger.historicoservice.dto.ConsultaResponse;
import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import br.com.fiap.techchalleger.historicoservice.exception.ConsultaNotFoundException;
import br.com.fiap.techchalleger.historicoservice.repository.ConsultaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricoService {

    private final ConsultaRepository consultaRepository;

    public HistoricoService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    public List<ConsultaResponse> buscarHistoricoPaciente(Long pacienteId) {

        return consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConsultaResponse> buscarHistoricoMedico(Long medicoId) {

        return consultaRepository
                .findByMedicoIdOrderByDataHoraDesc(medicoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConsultaResponse> buscarConsultasFuturas(Long pacienteId) {

        return consultaRepository
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        pacienteId,
                        LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ConsultaResponse buscarPorId(Long id) {

        Consulta consulta = consultaRepository
                .findById(id)
                .orElseThrow(() -> new ConsultaNotFoundException(id));

        return toResponse(consulta);
    }

    private ConsultaResponse toResponse(Consulta consulta) {

        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPacienteId(),
                consulta.getMedicoId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getDescricao(),
                consulta.getDiagnostico()
        );
    }
}
