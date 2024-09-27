import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor3 {
    private static final String LOAD_BALANCER_HOST = "localhost";
    private static final int LOAD_BALANCER_PORT = 9001; // Porta do balanceador de carga

    public static void main(String[] args) {
        int serverPort = 8081; // Porta onde o servidor vai ouvir

        // Notifica o Load Balancer que o servidor está online
        notifyLoadBalancer(serverPort);

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Servidor aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão recebida de " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();  // Inicia a thread para o cliente
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para notificar o Load Balancer
    private static void notifyLoadBalancer(int serverPort) {
        try (Socket lbSocket = new Socket(LOAD_BALANCER_HOST, LOAD_BALANCER_PORT)) {
            PrintWriter out = new PrintWriter(lbSocket.getOutputStream(), true);
            out.println("Online:" + serverPort);  // Envia mensagem "Online:<port>" ao load balancer
            System.out.println("Servidor notificado ao Load Balancer na porta " + serverPort);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao Load Balancer.");
            e.printStackTrace();
        }
    }

    // Classe para lidar com o cliente
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String clientMessage;

                while ((clientMessage = in.readLine()) != null) {
                    if ("PING".equals(clientMessage)) {
                        out.println("PONG");  // Responde com PONG ao ping
                        System.out.println("Respondeu PONG ao Load Balancer.");
                    } else {
                        String[] data = clientMessage.split(",");
                        int idade = Integer.parseInt(data[0]);
                        int tempoTrabalhado = Integer.parseInt(data[1]);

                        if(idade>=65 || tempoTrabalhado >= 30){
                            out.println("Parabens, voce ja pode aposentar.");
                        } else if( idade>=60 && tempoTrabalhado>=25){
                            out.println("Parabens, voce ja pode aposentar.");
                        } else {
                            out.println("Infelizmente voce nao pode se aposentar ainda.");
                        }

                    }
                }

            } catch (IOException e) {
                System.out.println("Erro na comunicação com o cliente.");
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
