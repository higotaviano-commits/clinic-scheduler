package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private Long registradoPorId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
