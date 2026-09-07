package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medicos")
public class Medico extends Usuario {
    // campos específicos do médico podem ser adicionados aqui futuramente (ex.: CRM, especialidade)
}
