package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.db.Postgres;
import org.example.db.Sincronizacao;
import org.example.db.Sqlite;
import org.json.JSONObject;

public class Cliente {

    public static void main(String[] args) {
        String enderecoServidor = "localhost";  // Endereço do servidor
        int porta = 8080;  // Porta do servidor

        Sqlite sqlite = new Sqlite();
        Postgres postgres = new Postgres();
        Sincronizacao sincronizacao = new Sincronizacao(sqlite, postgres);
        ServidorChecker servidorChecker = new ServidorChecker(enderecoServidor, porta);

        servidorChecker.start();

        while(true){
            if(servidorChecker.isServidorOnline()){
                sincronizacao.sincronizar();
            } else {
                JSONObject json = new JSONObject();
                json.put("nome", "Marcos");
                json.put("cargo", "Desenvolvedor");
                json.put("horario", "08:30");
                sqlite.salvarPontoOffline(json.getString("nome"),json.getString("cargo"),json.getString("horario"));

            }
            try{
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

//    public static void sincPontos(Sqlite sqlite, String endServidor, int porta){
//        try(ResultSet pontos = sqlite.recuperarPontosOffline()){
//            while (pontos.next()){
//                int id = pontos.getInt("id");
//                String nome = pontos.getString("nome");
//                String cargo = pontos.getString("cargo");
//                String horario = pontos.getString("horario");
//
//                //ENVIAR JSON PARA SERVIDOR
//                JSONObject json = new JSONObject();
//                json.put("nome", nome);
//                json.put("cargo", cargo);
//                json.put("horario", horario);
//
//                try(Socket socket = new Socket(endServidor, porta)){
//                    PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
//                    saida.println(json.toString());
//
//                    //CONFIRMAR ENVIO BEM SUCEDIDO E DELETAR DO BANCO LOCAL
//                    sqlite.deletarPonto(id);
//                    System.out.printf("Ponto do(a) " + nome + " sincronizado com o servidor e removido do banco local.");
//
//                } catch (IOException e){
//                    System.out.println("Erro ao sincronizar ponto com o servidor.");
//                }
//            }
//        } catch (SQLException e){
//            e.printStackTrace();
//        }
//    }

}
