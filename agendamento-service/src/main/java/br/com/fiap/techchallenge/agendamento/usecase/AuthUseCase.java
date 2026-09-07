package br.com.fiap.techchallenge.agendamento.usecase;

import br.com.fiap.techchallenge.agendamento.dto.request.LoginRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.AuthResponse;

public interface AuthUseCase {

    AuthResponse authenticate(LoginRequest request);
}
