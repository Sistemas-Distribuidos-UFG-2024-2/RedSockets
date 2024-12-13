package com.servico.FechamentoFolha.services;

import com.servico.FechamentoFolha.entities.FuncPonto;
import com.servico.FechamentoFolha.repositories.FuncPontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FuncionarioService {


    @Autowired
    private FuncPontoRepository funcPontoRepository;

    public double calcularSalario(String matricula) {
        List<FuncPonto> pontos = funcPontoRepository.findByMatricula(matricula);

        // Mapa para armazenar os pontos por dia
        Map<String, List<LocalDateTime>> pontosPorDia = new HashMap<>();

        // Formato do horário
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Organize os pontos por dia (entrada e saída)
        for (FuncPonto ponto : pontos) {
            String data = ponto.getHorario().substring(0, 10);  // extrai apenas a data (YYYY-MM-DD)
            LocalDateTime horario = LocalDateTime.parse(ponto.getHorario(), formatter);

            pontosPorDia.putIfAbsent(data, new ArrayList<>());
            pontosPorDia.get(data).add(horario);
        }

        double salario = 0.0;
        double valorHora = 20.0;  // Valor da hora

        // Agora calcule o salário, levando em consideração as horas trabalhadas
        for (Map.Entry<String, List<LocalDateTime>> entry : pontosPorDia.entrySet()) {
            List<LocalDateTime> horarios = entry.getValue();

            // Se houverem pelo menos 2 pontos por dia (entrada e saída), faça o cálculo
            if (horarios.size() >= 2) {
                // Ordena os horários (entrada primeiro, saída depois)
                horarios.sort(Comparator.naturalOrder());

                // Calcular as horas trabalhadas, considerando múltiplos pontos no dia
                Duration totalDuracao = Duration.ZERO;

                for (int i = 0; i < horarios.size() - 1; i += 2) {
                    // Verifica se há um par de horários de entrada e saída
                    if (i + 1 < horarios.size()) {
                        Duration duracao = Duration.between(horarios.get(i), horarios.get(i + 1));
                        totalDuracao = totalDuracao.plus(duracao);
                    }
                }


                long totalSegundos = totalDuracao.getSeconds();
                long horas = totalSegundos / 3600;
                long minutos = (totalSegundos % 3600) / 60;
                long segundos = totalSegundos % 60;


                System.out.println("Horas trabalhadas no dia " + entry.getKey() + ": " + horas + "h " + minutos + "m " + segundos + "s");


                double horasTrabalhadas = horas + minutos / 60.0 + segundos / 3600.0;

                // Agora, calcula o salário proporcional à quantidade de horas trabalhadas
                salario += horasTrabalhadas * valorHora;
            }
        }

        return salario;
    }
}
