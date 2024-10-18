package exercicios.socket;

import java.io.*;
import java.net.*;

public class ServerJava {
    public static void main(String[] args) {
        int port = 12345; // Porta de escuta

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor aguardando conexão na porta " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                    // Lê os dados do cliente
                    String nome = in.readLine();
                    String sexo = in.readLine();
                    String idadeStr = in.readLine();
                    int idade = Integer.parseInt(idadeStr);

                    // Determina a maioridade
                    String resultado;
                    if (sexo.equalsIgnoreCase("masculino")) {
                        resultado = (idade >= 18) ? nome + " já atingiu a maioridade." : nome + " não atingiu a maioridade.";
                    } else if (sexo.equalsIgnoreCase("feminino")) {
                        resultado = (idade >= 21) ? nome + " já atingiu a maioridade." : nome + " não atingiu a maioridade.";
                    } else {
                        resultado = "Sexo inválido.";
                    }

                    // Envia o resultado ao cliente
                    out.println(resultado);
                } catch (IOException e) {
                    System.out.println("Erro ao tratar cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
