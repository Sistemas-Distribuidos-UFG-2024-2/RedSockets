package org.example;

import java.io.IOException;
import java.net.Socket;

public class ServidorChecker extends Thread {
    private String enderecoServidor;
    private int porta;
    private boolean servidorOnline;

    public ServidorChecker(String enderecoServidor, int porta) {
        this.enderecoServidor = enderecoServidor;
        this.porta = porta;
        this.servidorOnline = false;
    }

    public boolean isServidorOnline() {
        return servidorOnline;
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket(enderecoServidor, porta)) {
                servidorOnline = true;
                System.out.println("Servidor está online");
            } catch (IOException e) {
                servidorOnline = false;
                System.out.println("Servidor está offline");
            }
            try {
                Thread.sleep(5000); // Intervalo de verificação (5 segundos)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
