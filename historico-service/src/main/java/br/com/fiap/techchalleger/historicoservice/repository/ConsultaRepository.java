package br.com.fiap.techchalleger.historicoservice.repository;

import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteIdOrderByDataHoraDesc(Long pacienteId);

    List<Consulta> findByMedicoIdOrderByDataHoraDesc(Long medicoId);

    List<Consulta> findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
            Long pacienteId,
            LocalDateTime dataHora);
}
