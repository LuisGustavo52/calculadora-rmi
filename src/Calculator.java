import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Calculator implements Operations {

    private List<String> lastOperations = new ArrayList<String>();

    public Number sum(Number x, Number y) {
        Number result = x.doubleValue() + y.doubleValue();
        log(x, "+", y, result);
        return result;
    }
    public Number sub(Number x, Number y) {
        Number result = x.doubleValue() - y.doubleValue();
        log(x, "−", y, result);
        return result;
    }
    public Number mul(Number x, Number y) {
        Number result = x.doubleValue() * y.doubleValue();
        log(x, "×", y, result);
        return result;
    }
    public Number div(Number x, Number y) {
        Number result = Double.NaN;
        if (y.doubleValue() != 0)
            result = x.doubleValue() / y.doubleValue();
        log(x, "÷", y, result);
        return result;
    }

    @Override
    public Number pow(Number exponent, Number base) throws RemoteException {
        Number result = Math.pow(base.doubleValue(), exponent.doubleValue());
        log(base, "xʸ", exponent, result);
        return result;
    }

    @Override
    public Number root(Number exponent, Number base) throws RemoteException {
        Number result = Math.pow(base.doubleValue(), 1.0 / exponent.doubleValue());
        log(base, "ʸ√x", exponent, result);
        return result;
    }

    @Override
    public Number percent(Number x, Number percent) throws RemoteException {
        Number result = x.doubleValue() * percent.doubleValue() / 100.0;
        log(x, "%", percent, result);
        return result;
    }

    @Override
    public Number mod(Number x, Number y) throws RemoteException {
        Number result = x.longValue() % y.longValue();
        log(x, "mod", y, result);
        return result;
    }

    @Override
    public Number factorial(Number x) throws RemoteException {
        long n = x.longValue();
        if (n < 0) throw new RemoteException("Factorial is not defined for negative numbers");
        long result = 1;
        for (long i = 2; i <= n; i++) result *= i;
        logUnary("n!", x.toString(), String.valueOf(result));
        return result;
    }

    @Override
    public String convertBase(String value, int fromBase, int toBase) throws RemoteException {
        long decimalValue = Long.parseLong(value, fromBase);
        String result = Long.toString(decimalValue, toBase).toUpperCase();
        
        String fromStr = getBaseName(fromBase);
        String toStr = getBaseName(toBase);
        String opName = "Conversão (" + fromStr + " -> " + toStr + ")";
        
        logUnary(opName, value, result);
        return result;
    }

    private String getBaseName(int base) {
        if (base == 2) return "Binário";
        if (base == 10) return "Decimal";
        if (base == 16) return "Hexadecimal";
        return String.valueOf(base);
    }

    public List<String> lastOperations(int howMany) {
        if (lastOperations.size() < howMany)
            return lastOperations();
        return new ArrayList<String>(lastOperations.subList(
                lastOperations.size() - howMany, lastOperations.size()));
    }
    
    public List<String> lastOperations() {
        return lastOperations;
    }
    
    private void log(Number operatorOne, String operation, Number operatorTwo,
                     Number result) {

        String formattedOperation = String.format("%s %s %s = %s",
                operatorOne.toString(), operation, operatorTwo.toString(),
                result.toString());
        lastOperations.add(formattedOperation);
        System.out.printf("%s at %s\n", formattedOperation, new Date());
    }

    private void logUnary(String operation, String input, String result) {
        String formattedOperation = String.format("%s(%s) = %s", operation, input, result);
        lastOperations.add(formattedOperation);
        System.out.printf("%s at %s\n", formattedOperation, new Date());
    }
}