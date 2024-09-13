import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor aguardando conexões...");

            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado!");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String clientMessage = in.readLine();
            System.out.println("Mensagem recebida do cliente: " + clientMessage);

            if (clientMessage.toLowerCase().matches("hello")) {
                out.println("World");
                System.out.println("Resposta enviada ao cliente: World");
            }

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
