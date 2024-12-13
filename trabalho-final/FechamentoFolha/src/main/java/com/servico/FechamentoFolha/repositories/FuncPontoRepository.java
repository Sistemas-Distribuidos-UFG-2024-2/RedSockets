package com.servico.FechamentoFolha.repositories;

import com.servico.FechamentoFolha.entities.FuncPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FuncPontoRepository extends JpaRepository<FuncPonto, UUID> {
    List<FuncPonto> findByMatricula(String matricula);
}
