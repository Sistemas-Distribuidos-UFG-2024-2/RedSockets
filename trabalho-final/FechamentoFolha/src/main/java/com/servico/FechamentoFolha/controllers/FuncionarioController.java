package com.servico.FechamentoFolha.controllers;

import com.servico.FechamentoFolha.entities.FuncPonto;
import com.servico.FechamentoFolha.services.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/folha")
public class FuncionarioController {
    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping("/calcular-salario")
    public ResponseEntity<Double> calcularSalario(@RequestParam String matricula) {
        Double sal = funcionarioService.calcularSalario(matricula);
        return ResponseEntity.ok(sal);
    }
}
