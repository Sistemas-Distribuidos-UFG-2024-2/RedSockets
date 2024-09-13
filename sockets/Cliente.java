import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    public static void main(String[] args) {

        List<ServerInfo> servers = new ArrayList<>();
        servers.add(new ServerInfo("localhost", 12345));  // Servidor 1
        servers.add(new ServerInfo("localhost", 12346));  // Servidor 2
        servers.add(new ServerInfo("localhost", 12347));  // Servidor 3

        boolean connected = false;
        Socket socket = null;


        for (ServerInfo server : servers) {
            try {
                System.out.println("Tentando conectar ao servidor: " + server.host + ":" + server.port);
                socket = new Socket(server.host, server.port);
                connected = true;
                System.out.println("Conectado ao servidor: " + server.host + ":" + server.port);
                break;  
            } catch (UnknownHostException e) {
                System.err.println("Host desconhecido: " + server.host);
            } catch (IOException e) {
                System.err.println("Não foi possível conectar ao servidor: " + server.host + ":" + server.port);
            }
        }

        if (!connected) {
            System.out.println("Não foi possível conectar a nenhum servidor.");
            return;
        }

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Hello");
            System.out.println("Mensagem enviada ao servidor: Hello");

            String serverMessage = in.readLine();
            System.out.println("Resposta recebida do servidor: " + serverMessage);

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
