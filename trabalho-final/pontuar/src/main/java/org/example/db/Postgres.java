package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Postgres {
    private Connection conexao;

    public Postgres() {
        conectar();
        criarTabela();
    }

    private void conectar() {
        try {
            // Configure a URL, o usuário e a senha conforme a sua instalação do PostgreSQL
            String url = "jdbc:postgresql://localhost:5433/pontuar";
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
                id SERIAL PRIMARY KEY,
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
}

