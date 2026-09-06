package br.com.fiap.techchallenge.agendamento.usecase;

import br.com.fiap.techchallenge.agendamento.dto.request.CreateUsuarioRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.UsuarioResponse;

public interface UsuarioUseCase {

    UsuarioResponse create(CreateUsuarioRequest request);
}
