package org.example;

import java.io.IOException;
import java.net.Socket;

public class ProxyChecker extends Thread {
    private String enderecoProxy;
    private int porta;
    private boolean proxyOnline;

    public ProxyChecker(String enderecoProxy, int porta) {
        this.enderecoProxy = enderecoProxy;
        this.porta = porta;
        this.proxyOnline = false;
    }

    public boolean isProxyOnline() {
        return proxyOnline;
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket(enderecoProxy, porta)) {
                proxyOnline = true;
                System.out.println("Proxy está online");
            } catch (IOException e) {
                proxyOnline = false;
                System.out.println("Proxy está offline");
            }
            try {
                Thread.sleep(5000); // Intervalo de verificação (5 segundos)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
