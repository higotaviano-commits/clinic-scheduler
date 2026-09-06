package br.com.fiap.techchallenge.agendamento.usecase;

import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaUpdateRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.ConsultaResponse;

import java.util.List;

public interface ConsultaUseCase {

    ConsultaResponse create(ConsultaRequest request);

    ConsultaResponse update(Long id, ConsultaUpdateRequest request);

    ConsultaResponse findById(Long id);

    List<ConsultaResponse> findAll();

    List<ConsultaResponse> findByPaciente(Long pacienteId);
}
