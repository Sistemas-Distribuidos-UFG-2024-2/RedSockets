package org.example.db;

import java.sql.*;

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
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
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

    public void salvarPontoOffline(String nome, String cargo, String horario) {
        String sql = "INSERT INTO func_ponto(nome, cargo, horario) VALUES(?,?,?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cargo);
            stmt.setString(3, horario);
            stmt.executeUpdate();
            System.out.println("Ponto salvo localmente: " + nome);
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

    public void deletarPonto(int id) {
        String sql = "DELETE FROM func_ponto WHERE id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

