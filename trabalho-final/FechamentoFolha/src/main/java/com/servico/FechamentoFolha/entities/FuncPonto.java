package com.servico.FechamentoFolha.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class FuncPonto {

    @Id
    private UUID id;

    @Column(unique = true)
    private String matricula;
    private String nome;
    private String cargo;

    private String horario;

    public FuncPonto(){}

    public FuncPonto(UUID id, String matricula, String nome, String cargo, String horario) {
        this.id = id;
        this.matricula = matricula;
        this.nome = nome;
        this.cargo = cargo;
        this.horario = horario;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
