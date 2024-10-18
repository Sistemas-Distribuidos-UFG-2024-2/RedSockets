package exercicios.rmi;

import exercicios.rmi.service.SalarioService;

import java.rmi.Naming;

public class ClienteRmiJava {

    public static void main(String[] args) {
        try {
            SalarioService salarioService = (SalarioService) Naming.lookup("rmi://localhost:1099/SalarioService");
            Cliente cliente = new Cliente("Guilherme","Programador",2500.0);

            String result = salarioService.reajustarSalario(cliente);
            System.out.println(result);

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
