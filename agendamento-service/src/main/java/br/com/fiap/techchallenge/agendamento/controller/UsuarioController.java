package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.CreateUsuarioRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.UsuarioResponse;
import br.com.fiap.techchallenge.agendamento.usecase.UsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de registro é público — necessário para criar as primeiras contas de cada perfil. */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Cadastro de médicos, enfermeiros e pacientes")
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;

    public UsuarioController(UsuarioUseCase usuarioUseCase) {
        this.usuarioUseCase = usuarioUseCase;
    }

    @Operation(summary = "Cadastrar usuário", description = "Cria um médico, enfermeiro ou paciente")
    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@RequestBody @Valid CreateUsuarioRequest request) {
        UsuarioResponse response = usuarioUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
