package org.example.db;

import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.UUID;

public class Sincronizacao {

    private Sqlite sqlite;
    private static PostgresProxy postgresProxy;
    private PostgresServidor postgresServidor;

    public Sincronizacao(Sqlite bancoLocal, PostgresProxy bancoPostgresProxy) {
        this.sqlite = bancoLocal;
        this.postgresProxy = bancoPostgresProxy;
    }

    public Sincronizacao(PostgresProxy postgresProxy, PostgresServidor postgresServidor){
        this.postgresProxy = postgresProxy;
        this.postgresServidor = postgresServidor;
    }
    public Sincronizacao(Sqlite sqlite){
        this.sqlite = sqlite;
    }

    public Sincronizacao(PostgresProxy postgresProxy){
        this.postgresProxy = postgresProxy;
    }

    public void inserirNoPostgres(JSONObject json) {
        String sql = "INSERT INTO func_ponto (id, matricula, nome, cargo, horario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = postgresProxy.getConexao().prepareStatement(sql)) {

            // Garante que o ID seja um UUID
            UUID id = UUID.fromString(json.getString("id"));
            stmt.setObject(1, id);
            stmt.setString(2, json.getString("matricula"));
            stmt.setString(3, json.getString("nome"));
            stmt.setString(4, json.getString("cargo"));
            stmt.setString(5, json.getString("horario"));

            stmt.executeUpdate();
            System.out.println("Registro inserido no PostgreSQL com sucesso: " + json);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir no PostgreSQL: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("ID inválido: " + e.getMessage());
        }
    }

    public void sincronizar() {
        // Verificar se há dados para sincronizar no banco SQLite
        String sql = "SELECT * FROM func_ponto"; // Busca todos os dados no SQLite
        try (Connection sqliteConexao = sqlite.getConexao();  // Conexão aberta uma vez
             PreparedStatement stmt = sqliteConexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Se houver dados para sincronizar
            while (rs.next()) {
                JSONObject dado = new JSONObject();
                dado.put("id", rs.getString("id"));
                dado.put("matricula", rs.getString("matricula"));
                dado.put("nome", rs.getString("nome"));
                dado.put("cargo", rs.getString("cargo"));
                dado.put("horario", rs.getString("horario"));

                // Enviar o dado para o servidor (PostgreSQL)
                inserirNoPostgres(dado);

                // Após sincronizar, remover o registro do SQLite
                try (PreparedStatement deleteStmt = sqliteConexao.prepareStatement("DELETE FROM func_ponto WHERE id = ?")) {
                    deleteStmt.setString(1, rs.getString("id"));
                    deleteStmt.executeUpdate();
                } catch (SQLException deleteException) {
                    System.err.println("Erro ao remover dado do SQLite: " + deleteException.getMessage());
                }
            }

            System.out.println("Sincronização completa.");

        } catch (SQLException e) {
            System.err.println("Erro ao verificar ou sincronizar dados no SQLite: " + e.getMessage());
        }
    }
    public void sincronizarPostgresParaServidor() {
        // Buscar dados na tabela do PostgresProxy
        String sql = "SELECT * FROM func_ponto";  // Tabela no PostgresProxy
        try (Connection conexaoPostgres = postgresProxy.getConexao();
             PreparedStatement stmt = conexaoPostgres.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                JSONObject dado = new JSONObject();
                dado.put("id", rs.getString("id"));
                dado.put("matricula", rs.getString("matricula"));
                dado.put("nome", rs.getString("nome"));
                dado.put("cargo", rs.getString("cargo"));
                dado.put("horario", rs.getString("horario"));

                System.out.println(dado);

                // Enviar para o servidor
                inserirNoPostgresServidor(dado);
                System.out.println("Dado sincronizado para o servidor: " + dado);

                try (PreparedStatement deleteStmt = conexaoPostgres.prepareStatement("DELETE FROM func_ponto WHERE id = ?")) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    deleteStmt.setObject(1, id);
                    deleteStmt.executeUpdate();
                } catch (SQLException deleteException) {
                    System.err.println("Erro ao remover dado do PostgresProxy: " + deleteException.getMessage());
                }
            }

            System.out.println("Sincronização completa com o servidor.");
        } catch (SQLException e) {
            System.err.println("Erro durante a sincronização com o servidor: " + e.getMessage());
        }
    }

    public void inserirNoPostgresServidor(JSONObject json) {
        String sql = "INSERT INTO func_ponto (id, matricula, nome, cargo, horario) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = postgresServidor.getConexao().prepareStatement(sql)) {

            // Garante que o ID seja um UUID
            UUID id = UUID.fromString(json.getString("id"));
            stmt.setObject(1, id);
            stmt.setString(2, json.getString("matricula"));
            stmt.setString(3, json.getString("nome"));
            stmt.setString(4, json.getString("cargo"));
            stmt.setString(5, json.getString("horario"));

            stmt.executeUpdate();
            System.out.println("Registro inserido no PostgreSQL com sucesso: " + json);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir no PostgreSQL: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("ID inválido: " + e.getMessage());
        }
    }

}

