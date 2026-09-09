package br.com.fiap.techchalleger.historicoservice.controller;

import br.com.fiap.techchalleger.historicoservice.dto.ConsultaResponse;
import br.com.fiap.techchalleger.historicoservice.exception.AcessoNegadoException;
import br.com.fiap.techchalleger.historicoservice.exception.ConsultaNotFoundException;
import br.com.fiap.techchalleger.historicoservice.service.HistoricoService;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HistoricoGraphQLController {

    private final HistoricoService historicoService;

    public HistoricoGraphQLController(
            HistoricoService historicoService) {

        this.historicoService = historicoService;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    @QueryMapping
    public List<ConsultaResponse> historicoPaciente(
            @Argument Long pacienteId) {

        return historicoService.buscarHistoricoPaciente(pacienteId);
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @QueryMapping
    public List<ConsultaResponse> historicoMedico(
            @Argument Long medicoId) {

        return historicoService.buscarHistoricoMedico(medicoId);
    }

    @QueryMapping
    public List<ConsultaResponse> consultasFuturas(
            @Argument Long pacienteId) {

        return historicoService.buscarConsultasFuturas(pacienteId);
    }

    @QueryMapping
    public ConsultaResponse consulta(
            @Argument Long id) {

        return historicoService.buscarPorId(id);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleConsultaNotFound(
            ConsultaNotFoundException exception) {

        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(exception.getMessage())
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleAcessoNegado(
            AcessoNegadoException exception) {

        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.FORBIDDEN)
                .message(exception.getMessage())
                .build();
    }
}
