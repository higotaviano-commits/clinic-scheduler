package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pacientes")
public class Paciente extends Usuario {
    // campos específicos do paciente podem ser adicionados aqui futuramente (ex.: convênio)
}
