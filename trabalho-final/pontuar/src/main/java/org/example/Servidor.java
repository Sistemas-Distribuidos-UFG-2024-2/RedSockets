package org.example;

import org.example.db.PostgresProxy;
import org.example.db.PostgresServidor;
import org.example.db.Sincronizacao;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;


public class Servidor {
    private PostgresServidor postgresServidor;

    public Servidor(PostgresServidor postgresServidor){
        this.postgresServidor = postgresServidor;
    }

    public static void main(String[] args) {
        PostgresServidor postgresServidor = new PostgresServidor();
        Servidor servidor = new Servidor(postgresServidor);
        PostgresProxy postgresProxy = new PostgresProxy();
        Sincronizacao sinc = new Sincronizacao(postgresProxy ,postgresServidor);


        try (ServerSocket serverSocket = new ServerSocket(8082)) {
            System.out.println("Servidor iniciado na porta 8082.");
            sinc.sincronizarPostgresParaServidor();

            while (true) {
                try (Socket clienteSocket = serverSocket.accept()) {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                    String jsonStr = entrada.readLine();

                    if (jsonStr != null) {
                        JSONObject json = new JSONObject(jsonStr);
                        System.out.println(json);
                        servidor.salvarNoBanco(json);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao processar requisição: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }

    }

    private void salvarNoBanco(JSONObject json) {
        postgresServidor.inserirBancoProd(json);
        System.out.println("Dado salvo no banco de dados: " + json);

    }
}

