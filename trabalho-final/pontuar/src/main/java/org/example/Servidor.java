package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {

    public static void main(String[] args) {
        int porta = 8080;  // Porta para conexão

        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
            System.out.println("Servidor aguardando conexões na porta " + porta);

            while (true) {
                // Aceita conexão do cliente
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                // Processa a requisição do cliente
                BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true);

                // Recebe e processa o JSON do cliente
                String jsonStr = entrada.readLine();
                if (jsonStr != null) {
                    JSONObject json = new JSONObject(jsonStr);
                    processarPontoFuncionario(json);
                }

                // Envia confirmação para o cliente
                saida.println("Ponto registrado com sucesso!");
                clienteSocket.close();  // Fecha a conexão com o cliente
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Processa as informações recebidas do funcionário
    private static void processarPontoFuncionario(JSONObject json) {
        String nome = json.getString("nome");
        String cargo = json.getString("cargo");
        String horario = json.getString("horario");

        System.out.println("Registro de ponto:");
        System.out.println("Nome: " + nome);
        System.out.println("Cargo: " + cargo);
        System.out.println("Horário: " + horario);
    }
}
