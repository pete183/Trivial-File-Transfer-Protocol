package tcpclient;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * ByteArray
 * Extends ArrayList<Byte>
 */
public class ByteArray extends ArrayList<Byte> {

    /**
     * addInt
     * Converts integer to byte[]
     * Adds byte[] to ArrayList
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
     * Converts string to byte[]
     * Adds byte[] to ArrayList
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
     * Loops through byte[]
     * Adds each byte to ArrayList
     * @param array
     */
    protected void addBytes(byte[] array){
        for(byte byte1: array){
            this.add((Byte) byte1);
        }
    }

}
