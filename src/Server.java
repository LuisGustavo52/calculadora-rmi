import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static final int RMI_DEFAULT_PORT = 1099;
    private static Registry registry;
    public Server() {
        try {
            Calculator calculator = new Calculator();
// Cria e inicializa o registro
// Poderia ser feito via linha de comando
// No Windows "start rmiregistry"
            registry = LocateRegistry.createRegistry(RMI_DEFAULT_PORT);
// Cria o stub para o objeto remoto (exporta o objeto)
            Operations stub = (Operations) UnicastRemoteObject.exportObject(
                    calculator, RMI_DEFAULT_PORT);
// Liga o stub exportado a um nome de servic ̧o (no RMI registry)
            registry.rebind("CalculatorService", stub);
            System.out.println("Servic ̧o da calculadora rodando...");
        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao inicializar o servidor.");
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        new Server();
    }
}