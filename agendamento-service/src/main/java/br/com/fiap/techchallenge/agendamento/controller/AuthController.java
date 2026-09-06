package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.LoginRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.AuthResponse;
import br.com.fiap.techchallenge.agendamento.usecase.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @Operation(summary = "Realizar login", description = "Autentica com login e senha e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authUseCase.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
