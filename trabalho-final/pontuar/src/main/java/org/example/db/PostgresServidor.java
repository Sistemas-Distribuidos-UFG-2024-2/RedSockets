package org.example.db;

import org.json.JSONObject;

import java.sql.*;
import java.util.UUID;

public class PostgresServidor {
    private Connection conexao;

    public PostgresServidor() {
        conectar();
        criarTabela();
    }

    private void conectar() {
        try {
            // Configure a URL, o usuário e a senha conforme a sua instalação do PostgreSQL
            String url = "jdbc:postgresql://localhost:5434/pontuar_servidor";
            String usuario = "postgres";
            String senha = "12345";

            conexao = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conectado ao banco PostgreSQL.");
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao PostgreSQL: " + e.getMessage());
        }
    }
    private void criarTabela() {
        String sql = """
            CREATE TABLE IF NOT EXISTS func_ponto (
                id UUID PRIMARY KEY,
                matricula VARCHAR(100) NOT NULL,
                nome VARCHAR(100) NOT NULL,
                cargo VARCHAR(50) NOT NULL,
                horario VARCHAR(50) NOT NULL
            );
            """;

        try (Statement stmt = conexao.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'pontos' verificada/criada com sucesso no PostgreSQL.");
        } catch (SQLException e) {
            System.out.println("Erro ao criar a tabela no PostgreSQL: " + e.getMessage());
        }
    }

    public Connection getConexao() {
        return conexao;
    }


    public void inserirBancoProd(JSONObject json) {
        String sql = "INSERT INTO func_ponto (id, matricula, nome, cargo, horario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // Converte a string do JSON para UUID
            UUID id = UUID.fromString(json.getString("id")); // Converte String para UUID

            stmt.setObject(1, id); // Insere o UUID diretamente
            stmt.setString(2, json.getString("matricula"));
            stmt.setString(3, json.getString("nome"));
            stmt.setString(4, json.getString("cargo"));
            stmt.setString(5, json.getString("dataHora"));

            stmt.executeUpdate();
            System.out.println("Ponto salvo localmente: " + json);

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco local: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("ID inválido: " + e.getMessage());
        }
    }
}

