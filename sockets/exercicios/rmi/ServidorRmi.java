package exercicios.rmi;

import exercicios.rmi.service.SalarioService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ServidorRmi extends UnicastRemoteObject implements SalarioService {

    protected  ServidorRmi() throws RemoteException{
        super();
    }

    @Override
    public String reajustarSalario(Cliente cliente) throws RemoteException {
        double salarioReajustado = cliente.getSalario();

        if(cliente.getCargo().toLowerCase().matches("operador")){
            salarioReajustado = cliente.getSalario() * 1.20;
        } else if(cliente.getCargo().toLowerCase().matches("programador")){
            salarioReajustado = cliente.getSalario() * 1.18;
        }

        return "Funcionario: " + cliente.getNome() + "\nSalario reajustado: " + salarioReajustado;
    }

    public static void main(String[] args) {
        try{
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI iniciado na porta 1099.");

            SalarioService salarioService = new ServidorRmi();
            java.rmi.Naming.rebind("rmi://localhost:1099/SalarioService", salarioService);
            System.out.println("Servidor ativo.");
        } catch(Exception e){
            e.printStackTrace();
        }
    }


}
