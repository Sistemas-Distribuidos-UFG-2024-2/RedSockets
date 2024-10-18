package exercicios.rmi.service;

import exercicios.rmi.Cliente;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SalarioService extends Remote {
    String reajustarSalario(Cliente cliente) throws RemoteException;
}
