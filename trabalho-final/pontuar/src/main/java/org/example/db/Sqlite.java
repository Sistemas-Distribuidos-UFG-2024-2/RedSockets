package org.example.db;

import org.json.JSONObject;

import java.sql.*;
import java.util.UUID;

public class Sqlite {

    private Connection conexao;

    public Sqlite() {
        conectar();
        criarTabela();
    }

    private void conectar() {
        try {
            conexao = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\guilh\\OneDrive\\Área de Trabalho\\SISTEMAS_DISTRIBUIDO\\Grupo-6\\trabalho-final\\pontuar\\src\\main\\resources\\sqlite.db");
            System.out.println("Banco de dados local conectado.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS func_ponto ("
                + "id UUID PRIMARY KEY,"
                + "matricula TEXT NOT NULL,"
                + "nome TEXT NOT NULL,"
                + "cargo TEXT NOT NULL,"
                + "horario TEXT NOT NULL"
                + ");";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void salvarPontoOffline(JSONObject json) {
        String sql = "INSERT INTO func_ponto(id, matricula, nome, cargo, horario) VALUES(?,?,?,?,?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            UUID id = (UUID) json.get("id");
            stmt.setObject(1, id);
            stmt.setString(2, json.getString("matricula"));
            stmt.setString(3, json.getString("nome"));
            stmt.setString(4, json.getString("cargo"));
            stmt.setString(5, json.getString("dataHora"));
            stmt.executeUpdate();
            System.out.println("Ponto salvo localmente: " + json);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public ResultSet recuperarPontosOffline() {
        String sql = "SELECT * FROM func_ponto";
        try {
            Statement stmt = conexao.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void deletarPonto(String id) {
        String sql = "DELETE FROM func_ponto WHERE id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getConexao() {
        return conexao;
    }
}

