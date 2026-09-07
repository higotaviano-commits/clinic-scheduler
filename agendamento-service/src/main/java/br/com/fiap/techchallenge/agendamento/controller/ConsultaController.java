package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaUpdateRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.usecase.ConsultaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Regras de acesso (Fase 3):
 * - Médicos: visualizam e editam o histórico de consultas.
 * - Enfermeiros: registram novas consultas e acessam o histórico.
 * - Pacientes: visualizam apenas as próprias consultas (checagem feita no ConsultaService).
 */
@RestController
@RequestMapping("/api/v1/consultas")
@Tag(name = "Consultas", description = "Agendamento e histórico de consultas")
public class ConsultaController {

    private final ConsultaUseCase consultaUseCase;

    public ConsultaController(ConsultaUseCase consultaUseCase) {
        this.consultaUseCase = consultaUseCase;
    }

    @Operation(summary = "Agendar consulta")
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<ConsultaResponse> create(@RequestBody @Valid ConsultaRequest request) {
        ConsultaResponse response = consultaUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar consulta (data/status/observações)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ConsultaResponse> update(@PathVariable Long id, @RequestBody ConsultaUpdateRequest request) {
        ConsultaResponse response = consultaUseCase.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar consulta por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<ConsultaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(consultaUseCase.findById(id));
    }

    @Operation(summary = "Listar todas as consultas")
    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<ConsultaResponse>> findAll() {
        return ResponseEntity.ok(consultaUseCase.findAll());
    }

    @Operation(summary = "Histórico de consultas de um paciente")
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<List<ConsultaResponse>> findByPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(consultaUseCase.findByPaciente(pacienteId));
    }
}
