package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "enfermeiros")
public class Enfermeiro extends Usuario {
    // campos específicos do enfermeiro podem ser adicionados aqui futuramente
}
