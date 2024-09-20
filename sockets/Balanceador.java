import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Balanceador {
    private static CopyOnWriteArrayList<ServerInfo> servers = new CopyOnWriteArrayList<>();
    private static int currentServerIndex = 0;

    public static void main(String[] args) {
        // Thread servidores
        new Thread(Balanceador::listenForServers).start();

        // Thread clientes
        new Thread(Balanceador::listenForClients).start();

        // Thread para exibir a lista de servidores disponíveis
        new Thread(Balanceador::pingAndPrintServersAvailable).start();
    }

    // Método para ouvir as notificações dos servidores (porta 9001)
    private static void listenForServers() {
        try (ServerSocket serverNotificationSocket = new ServerSocket(9001)) {
            System.out.println("Load Balancer aguardando notificações de servidores...");

            while (true) {
                Socket serverSocket = serverNotificationSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

                String serverMessage = in.readLine();

                if (serverMessage != null && serverMessage.startsWith("Online:")) {
                    int serverPort = Integer.parseInt(serverMessage.split(":")[1]);
                    servers.add(new ServerInfo("localhost", serverPort));  // Adiciona o servidor à lista
                    System.out.println("Servidor adicionado: localhost:" + serverPort);
                }

                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para ouvir as conexões dos clientes (porta 9000)
    private static void listenForClients() {
        try (ServerSocket clientSocket = new ServerSocket(9000)) {
            System.out.println("Load Balancer aguardando conexões de clientes...");

            while (true) {
                Socket clientConnection = clientSocket.accept();
                System.out.println("Cliente conectado: " + clientConnection.getInetAddress());

                ServerInfo server = getNextServer();

                if (server != null) {
                    forwardRequestToServer(clientConnection, server);
                } else {
                    System.out.println("Nenhum servidor disponível no momento.");
                }

                clientConnection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para encaminhar a requisição do cliente para o servidor
    private static void forwardRequestToServer(Socket clientConnection, ServerInfo server) {
        try (Socket serverSocket = new Socket(server.host, server.port)) {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientConnection.getOutputStream(), true);

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

            // Envia a mensagem do cliente para o servidor
            String clientMessage = clientIn.readLine();
            System.out.println("Mensagem do cliente: " + clientMessage);
            serverOut.println(clientMessage);

            // Recebe a resposta do servidor e envia de volta ao cliente
            String serverMessage = serverIn.readLine();
            System.out.println("Resposta do servidor: " + serverMessage);
            clientOut.println(serverMessage);

            System.out.println("Cliente " + clientConnection.getInetAddress() + ":" + server.port);
            System.out.println("\n");

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor " + server.host + ":" + server.port);
            e.printStackTrace();
        }
    }

    // Método para retornar o próximo servidor usando Round Robin
    private static ServerInfo getNextServer() {
        if (servers.isEmpty()) {
            return null;
        }

        ServerInfo server = servers.get(currentServerIndex);
        currentServerIndex = (currentServerIndex + 1) % servers.size();  // Round Robin
        return server;
    }

    private static void pingAndPrintServersAvailable() {
        while (true) {
            try {
                Thread.sleep(10000);
                for (ServerInfo server : servers) {
                    if (!pingServer(server)) {
                        System.out.println("Servidor offline: " + server.host + ":" + server.port);
                        servers.remove(server);
                    }
                }
                System.out.println("Servidores Disponiveis: ");
                if(servers.isEmpty()){
                    System.out.println("Nenhuma servidor disponivel.\n");
                } else {
                    for(ServerInfo serverInfo: servers){
                        System.out.println("Servidor: " + serverInfo.host + ":" + serverInfo.port);
                    }
                    System.out.println("\n");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para pingar um servidor e verificar se ele está online
    private static boolean pingServer(ServerInfo server) {
        try (Socket serverSocket = new Socket(server.host, server.port)) {
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            out.println("PING");

            // Espera resposta "PONG" dentro de 2 segundos
            serverSocket.setSoTimeout(2000);
            String response = in.readLine();

            if ("PONG".equals(response)) {
                System.out.println("Servidor respondeu com PONG: " + server.host + ":" + server.port);
                return true;
            }

        } catch (IOException e) {
            System.out.println("Erro ao pingar o servidor " + server.host + ":" + server.port);
        }
        return false;  // Retorna false se o servidor não respondeu ou ocorreu erro
    }

    static class ServerInfo {
        String host;
        int port;

        ServerInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
