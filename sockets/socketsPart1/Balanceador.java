package socketsPart1;

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
//                System.out.println("Cliente conectado: " + clientConnection.getInetAddress());

                // Cria uma nova thread para cada conexão de cliente
                new Thread(() -> {
                    ServerInfo server = getNextServer();

                    if (server != null) {
                        System.out.println("Encaminhando a requisição para o servidor: " + server.host + ":" + server.port);
                        forwardRequestToServer(clientConnection, server);
                    } else {
                        System.out.println("Nenhum servidor disponível no momento.");
                    }
                }).start();  // Inicia a thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void forwardRequestToServer(Socket clientConnection, ServerInfo server) {
        try (Socket serverSocket = new Socket(server.host, server.port)) {
            // Incrementa as conexões ativas no servidor
            server.incrementConnections();
            System.out.println("Conexões ativas no servidor " + server.host + ":" + server.port + ": " + server.activeConnections);

            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientConnection.getOutputStream(), true);

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

            // Envia a mensagem do cliente para o servidor
            String clientMessage = clientIn.readLine();
//            System.out.println("Mensagem do cliente: " + clientMessage);
            serverOut.println(clientMessage);

            // Recebe a resposta do servidor e envia de volta ao cliente
            String serverMessage = serverIn.readLine();
            System.out.println("Resposta do servidor: " + serverMessage);
            clientOut.println(serverMessage);

            // Adicione a impressão do servidor para o qual a requisição foi encaminhada
            System.out.println("Requisição encaminhada para o servidor: " + server.host + ":" + server.port);

            // Atraso de 5 segundos para simular o tempo que o servidor leva para liberar a conexão
            System.out.println("Aguardando 5 segundos antes de liberar a conexão...");
            Thread.sleep(5000);  // Simula a espera

            // Removido a impressão de desconexão do cliente
        } catch (IOException | InterruptedException e) {
            System.out.println("Erro ao conectar ao servidor " + server.host + ":" + server.port);
            e.printStackTrace();
        } finally {
            // Decrementa as conexões ativas no servidor
            server.decrementConnections();
            System.out.println("Conexões ativas no servidor " + server.host + ":" + server.port + " após desconexão: " + server.activeConnections);
        }
    }



    // Método para retornar o servidor com menos conexões ativas
    private static ServerInfo getNextServer() {
        if (servers.isEmpty()) {
            return null;
        }

        // Encontra o servidor com menos conexões ativas
        ServerInfo leastConnectedServer = null;
        int leastConnections = Integer.MAX_VALUE;

        for (ServerInfo server : servers) {
            if (server.activeConnections < leastConnections) {
                leastConnections = server.activeConnections;
                leastConnectedServer = server;
            }
        }

        // Se há mais de um servidor com o mesmo número de conexões, aplica o round robin
        List<ServerInfo> leastConnectedServers = new ArrayList<>();
        for (ServerInfo server : servers) {
            if (server.activeConnections == leastConnections) {
                leastConnectedServers.add(server);
            }
        }

        // Usa round robin se há mais de um servidor com o mesmo número de conexões
        if (!leastConnectedServers.isEmpty()) {
            leastConnectedServer = leastConnectedServers.get(currentServerIndex % leastConnectedServers.size());
            currentServerIndex = (currentServerIndex + 1) % leastConnectedServers.size();  // Round Robin
        }

        return leastConnectedServer;
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
                System.out.println("Servidores Disponíveis: ");
                if (servers.isEmpty()) {
                    System.out.println("Nenhum servidor disponível.\n");
                } else {
                    for (ServerInfo serverInfo : servers) {
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
        int activeConnections; // Número de conexões ativas

        ServerInfo(String host, int port) {
            this.host = host;
            this.port = port;
            this.activeConnections = 0;  // Inicializa com 0 conexões
        }

        // Incrementa o número de conexões ativas
        public void incrementConnections() {
            activeConnections++;
        }

        // Decrementa o número de conexões ativas
        public void decrementConnections() {
            activeConnections--;
        }
    }
}
