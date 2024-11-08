package org.example.db;

import java.sql.*;

public class Sincronizacao {

    private Sqlite sqlite;
    private Postgres postgres;

    public Sincronizacao(Sqlite bancoLocal, Postgres bancoPostgres) {
        this.sqlite = bancoLocal;
        this.postgres = bancoPostgres;
    }

    public void sincronizar() {
        try (ResultSet pontosOffline = sqlite.recuperarPontosOffline()) {
            while (pontosOffline.next()) {
                int id = pontosOffline.getInt("id");
                String nome = pontosOffline.getString("nome");
                String cargo = pontosOffline.getString("cargo");
                String horario = pontosOffline.getString("horario");

                // Inserir no PostgreSQL
                if (inserirNoPostgres(nome, cargo, horario)) {
                    // Deletar do SQLite após inserção bem-sucedida
                    sqlite.deletarPonto(id);
                    System.out.println("Dados sincronizados e removidos do SQLite: " + nome);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean inserirNoPostgres(String nome, String cargo, String horario) {
        String sql = "INSERT INTO func_ponto (nome, cargo, horario) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = postgres.getConexao().prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cargo);
            stmt.setString(3, horario);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir no PostgreSQL: " + e.getMessage());
            return false;
        }
    }
}

