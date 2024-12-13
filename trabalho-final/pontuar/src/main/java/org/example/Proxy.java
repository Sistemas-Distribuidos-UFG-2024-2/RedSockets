package org.example;

import org.example.db.Sincronizacao;
import org.example.db.Sqlite;
import org.example.db.PostgresProxy;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Proxy {

    private static final String SERVIDOR_HOST = "localhost";
    private static final int SERVIDOR_PORTA = 8082;
    private static final int PROXY_PORTA = 8081;

    public static void main(String[] args) {
        Sqlite sqlite = new Sqlite();
        PostgresProxy postgresProxy = new PostgresProxy();

        Queue<JSONObject> fila = new LinkedList<>();

        ServidorChecker servidorChecker = new ServidorChecker(SERVIDOR_HOST, SERVIDOR_PORTA);
        servidorChecker.start();

        try (ServerSocket proxySocket = new ServerSocket(PROXY_PORTA)) {
            System.out.println("Proxy iniciado na porta: " + PROXY_PORTA);
            Sincronizacao sinc = new Sincronizacao(sqlite, postgresProxy);
            sinc.sincronizar();

            while (true) {
                try (Socket clienteSocket = proxySocket.accept()) {
                    System.out.println("Conexão recebida do cliente.");

                    BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                    PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true);

                    String jsonStr = entrada.readLine();

                    if (jsonStr != null) {
                        JSONObject json = new JSONObject(jsonStr);
                        if (servidorChecker.isServidorOnline()) {
                            try(Socket servidorSocket = new Socket(SERVIDOR_HOST, SERVIDOR_PORTA)){
                                PrintWriter servidorSaida = new PrintWriter(servidorSocket.getOutputStream(), true);
                                servidorSaida.println(json.toString());
                                System.out.println("Dado enviado ao servidor diretamente: " + json);
                            } catch (IOException e){
                                System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
                            }

                        } else {

                            System.out.println("Servidor offline. Salvando os dados localmente no SQLite.");
                            postgresProxy.salvarPontoOffline(json);
                        }
                    }

                    saida.println("Ponto registrado com sucesso!");
                } catch (IOException e) {
                    System.err.println("Erro ao processar a requisição do cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o Proxy: " + e.getMessage());
        }
    }
}
