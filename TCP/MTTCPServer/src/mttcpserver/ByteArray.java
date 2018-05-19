package mttcpserver;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * ByteArray
 * Extends ArrayList<Byte>
 */
public class ByteArray extends ArrayList<Byte> {

    /**
     * addInt
     * Add integer to buffer
     * @param number
     */
    protected void addInt(int number){
        BigInteger bigInt = BigInteger.valueOf(number);
        byte[] buffer = new byte[2];
        System.arraycopy(bigInt.toByteArray(), 0, buffer, 1, 1);
        addBytes(buffer);
    }

    /**
     * addString
     * Add string to buffer
     * @param string
     */
    protected void addString(String string){
        addBytes(string.getBytes());
    }

    /**
     * addZero
     * Add a zero byte to buffer
     */
    protected void addZero(){
        byte[] buffer = new byte[1];
        buffer[0] = 0;
        addBytes(buffer);
    }

    /**
     * addBytes
     * Add primitive byte array to buffer
     * @param array
     */
    protected void addBytes(byte[] array){
        for(byte byte1: array){
            this.add((Byte) byte1);
        }
    }

}
