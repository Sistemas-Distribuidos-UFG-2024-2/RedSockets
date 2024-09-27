import java.io.*;
import java.net.Socket;

public class Cliente {
    private int idade;
    private int tempoMinimoTrabalhado;

    // Construtor para passar os parâmetros diretamente
    public Cliente(int idade, int tempoMinimoTrabalhado) {
        this.idade = idade;
        this.tempoMinimoTrabalhado = tempoMinimoTrabalhado;
    }

    public void conectar() {
        try (Socket socket = new Socket("localhost", 9000)) {  // Conecta ao balanceador de carga
            System.out.println("Conectado ao Load Balancer!");

            // Cria streams para comunicação
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envia os dados de idade e tempo diretamente ao servidor
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

    // Método para executar o cliente em uma nova thread
    public void executar() {
        new Thread(this::conectar).start();
    }

    // Método principal para testar a rotina de múltiplos clientes
    public static void main(String[] args) {
        // Simulando 10 clientes com idades e tempos de trabalho diferentes
        for (int i = 0; i < 100; i++) {
            // Valores de idade e tempo variando
            int idade = 30 + i;  // Exemplo: 30, 31, 32, ..., 39
            int tempoTrabalhado = 10 + i;  // Exemplo: 10, 11, 12, ..., 19
            Cliente cliente = new Cliente(idade, tempoTrabalhado);
            cliente.executar();  // Executa o cliente em uma nova thread
        }
    }
}
