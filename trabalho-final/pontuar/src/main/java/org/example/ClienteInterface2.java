package org.example;

import org.example.db.PostgresProxy;
import org.example.db.Sincronizacao;
import org.example.db.Sqlite;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ClienteInterface2 {

    public static void main(String[] args) {
        Sqlite sqlite = new Sqlite();
        PostgresProxy postgresProxy = new PostgresProxy();
        Sincronizacao sincronizacao = new Sincronizacao(sqlite, postgresProxy);

        sincronizacao.sincronizar();
        SwingUtilities.invokeLater(ClienteInterface2::criarInterface);
    }

    private static void criarInterface() {
        final String enderecoProxy = "localhost";
        final int portaProxy = 8081;

        ProxyChecker proxyChecker = new ProxyChecker(enderecoProxy, portaProxy);
        proxyChecker.start();

        JFrame frame = new JFrame("Cliente - Envio de Dados");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);

        JPanel panel = new JPanel(new GridLayout(5, 2));

        JTextField matriculaField = new JTextField();
        JTextField nomeField = new JTextField();
        JTextField cargoField = new JTextField();

        JButton enviarButton = new JButton("Enviar");
        JButton verificarSalarioButton = new JButton("Verificar Salário");

        panel.add(new JLabel("Matrícula:"));
        panel.add(matriculaField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Cargo:"));
        panel.add(cargoField);
        panel.add(new JLabel());  // Placeholder
        panel.add(enviarButton);
        panel.add(new JLabel());  // Placeholder
        panel.add(verificarSalarioButton);

        frame.add(panel);
        frame.setVisible(true);

        enviarButton.addActionListener(e -> enviarDados(matriculaField, nomeField, cargoField, enderecoProxy, portaProxy, proxyChecker, frame));

        verificarSalarioButton.addActionListener(e -> criarTelaVerificacaoSalario());
    }

    private static void enviarDados(JTextField matriculaField, JTextField nomeField, JTextField cargoField, String enderecoProxy, int portaProxy, ProxyChecker proxyChecker, JFrame frame) {
        String matricula = matriculaField.getText();
        String nome = nomeField.getText();
        String cargo = cargoField.getText();

        if (matricula.isEmpty() || nome.isEmpty() || cargo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Todos os campos devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        JSONObject json = new JSONObject();
        json.put("id", UUID.randomUUID());
        json.put("matricula", matricula);
        json.put("nome", nome);
        json.put("cargo", cargo);
        json.put("dataHora", dataHora);

        Sqlite sqlite = new Sqlite();
        Sincronizacao sincronizacao = new Sincronizacao(sqlite);

        if (proxyChecker.isProxyOnline()) {
            sincronizacao.sincronizar();
            try (Socket socket = new Socket(enderecoProxy, portaProxy);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println(json.toString());
                System.out.println("Dados enviados: " + json.toString());

                // Exibe mensagem de sucesso e limpa os campos
                JOptionPane.showMessageDialog(frame, "Dados enviados com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                limparCampos(matriculaField, nomeField, cargoField);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Erro de comunicação com o servidor.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            sqlite.salvarPontoOffline(json);
            JOptionPane.showMessageDialog(frame, "Proxy offline. Dados salvos localmente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            limparCampos(matriculaField, nomeField, cargoField);
        }
    }

    private static void limparCampos(JTextField matriculaField, JTextField nomeField, JTextField cargoField) {
        matriculaField.setText("");
        nomeField.setText("");
        cargoField.setText("");
    }

    private static void criarTelaVerificacaoSalario() {
        JFrame verificaSalarioFrame = new JFrame("Verificar Salário");
        verificaSalarioFrame.setSize(400, 200);
        verificaSalarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField matriculaField = new JTextField();

        JButton enviarButton = new JButton("Verificar Salário");

        panel.add(new JLabel("Matrícula:"));
        panel.add(matriculaField);
        panel.add(new JLabel());  // Placeholder
        panel.add(enviarButton);

        verificaSalarioFrame.add(panel);
        verificaSalarioFrame.setVisible(true);

        enviarButton.addActionListener(e -> verificarSalario(matriculaField.getText(), verificaSalarioFrame));
    }

    private static void verificarSalario(String matricula, JFrame verificaSalarioFrame) {
        String enderecoServico = "http://localhost:8080/api/folha/calcular-salario";  // URL do serviço de cálculo de salário

        try {
            // Codifica a matrícula para a URL
            String urlString = enderecoServico + "?matricula=" + URLEncoder.encode(matricula, "UTF-8");
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lê a resposta do servidor
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    System.out.println("Resposta do servidor: " + response);

                    String responseText = response.toString().trim();

                    try {
                        double salario = Double.parseDouble(responseText);
                        JOptionPane.showMessageDialog(
                                verificaSalarioFrame,
                                "O salário do funcionário é: R$ " + String.format("%.2f", salario),
                                "Verificação de Salário",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (NumberFormatException e) {
                        // Caso a resposta não seja um número, exibe erro
                        JOptionPane.showMessageDialog(
                                verificaSalarioFrame,
                                "Resposta inesperada do servidor: " + responseText,
                                "Erro",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        verificaSalarioFrame,
                        "Erro ao verificar o salário. Código HTTP: " + responseCode,
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (IOException e) {
            System.err.println("Erro ao verificar salário: " + e.getMessage());
            JOptionPane.showMessageDialog(
                    verificaSalarioFrame,
                    "Erro ao verificar salário: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
