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
        log(x, "-", y, result);
        return result;
    }
    public Number mul(Number x, Number y) {
        Number result = x.doubleValue() * y.doubleValue();
        log(x, "*", y, result);
        return result;
    }
    public Number div(Number x, Number y) {
        Number result = Double.NaN;
        if (y.doubleValue() != 0)
            result = x.doubleValue() / y.doubleValue();
        log(x, "/", y, result);
        return result;
    }

    @Override
    public Number pow(Number exponent, Number base) throws RemoteException {
        Number result = Math.pow(base.doubleValue(), exponent.doubleValue());
        log(base, "^", exponent, result);
        return result;
    }

    @Override
    public Number root(Number exponent, Number base) throws RemoteException {
        Number result = Math.pow(base.doubleValue(), 1.0 / exponent.doubleValue());
        log(base, "root", exponent, result);
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
        log(x, "%", y, result);
        return result;
    }

    @Override
    public Number factorial(Number x) throws RemoteException {
        long n = x.longValue();
        if (n < 0) throw new RemoteException("Factorial is not defined for negative numbers");
        long result = 1;
        for (long i = 2; i <= n; i++) result *= i;
        System.out.printf("%s! = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number decimalToBinary(Number x) throws RemoteException {
        long result = Long.parseLong(Long.toBinaryString(x.longValue()));
        System.out.printf("decToBin(%s) = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number binaryToDecimal(Number x) throws RemoteException {
        long result = Long.parseLong(x.toString(), 2);
        System.out.printf("binToDec(%s) = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number decimalToHex(Number x) throws RemoteException {
        long result = Long.parseLong(Long.toHexString(x.longValue()), 16);
        System.out.printf("decToHex(%s) = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number hexToDecimal(Number x) throws RemoteException {
        long result = Long.parseLong(x.toString(), 16);
        System.out.printf("hexToDec(%s) = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number binaryToHex(Number x) throws RemoteException {
        long decimal = Long.parseLong(x.toString(), 2);
        long result = Long.parseLong(Long.toHexString(decimal), 16);
        System.out.printf("binToHex(%s) = %s at %s\n", x, result, new Date());
        return result;
    }

    @Override
    public Number hexToBinary(Number x) throws RemoteException {
        long decimal = Long.parseLong(x.toString(), 16);
        long result = Long.parseLong(Long.toBinaryString(decimal));
        System.out.printf("hexToBin(%s) = %s at %s\n", x, result, new Date());
        return result;
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
}