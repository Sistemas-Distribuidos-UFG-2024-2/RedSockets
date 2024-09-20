import java.io.*;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9000)) {  // Conecta ao balanceador de carga

            System.out.println("Conectado ao Load Balancer!");

            // Cria streams para comunicação
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Informe a idade: ");
            String idade = userInput.readLine();

            System.out.println("Informe o tempo minimo de trabalho (anos): ");
            String tempoMinimoTrabalhado = userInput.readLine();

            out.println(idade + "," + tempoMinimoTrabalhado);

            // Recebe a resposta do servidor através do balanceador
            String serverMessage = in.readLine();
            System.out.println("Resposta recebida do servidor: " + serverMessage);

            // Fecha os recursos
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
