package client;

import java.math.BigInteger;
import java.util.ArrayList;

public class ByteArray extends ArrayList<Byte> {


    protected void addInt(int number){
        BigInteger bigInt = BigInteger.valueOf(number);
        byte[] buffer = new byte[2];
        System.arraycopy(bigInt.toByteArray(), 0, buffer, 1, 1);
        addBytes(buffer);
    }

    protected void addString(String string){
        addBytes(string.getBytes());
    }

    protected void addZero(){
        byte[] buffer = new byte[1];
        buffer[0] = 0;
        addBytes(buffer);
    }

    protected void addBytes(byte[] array){
        for(byte byte1: array){
            this.add((Byte) byte1);
        }
    }

}
