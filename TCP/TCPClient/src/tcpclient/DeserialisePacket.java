package tcpclient;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * DeserialisePacket
 * Extends SocketConstants
 */
public class DeserialisePacket extends SocketConstants {

    /**
     * private DatagramPacket
     */
    private byte[] packet;

    /**
     * DeserialisePacket
     * Constructor
     * @param packet
     */
    public DeserialisePacket(byte[] packet){
        this.packet = packet;
    }

    /**
     * getOpcode
     * @return Opcode
     */
    public int getOpcode(){
        byte[] data = new byte[2];
        System.arraycopy(packet, 0, data, 0, 2);
        return getNumber(data);
    }

    /**
     * getBlockNumber
     * @return Block Number
     */
    public int getBlockNumber(){
        byte[] data = new byte[2];
        System.arraycopy(packet, 2, data, 0, 2);
        return getNumber(data);
    }

    /**
     * getErrorCode
     * @return Error Code
     */
    public int getErrorCode(){
        byte[] data = new byte[2];
        System.arraycopy(packet, 2, data, 0, 2);
        return getNumber(data);
    }


    /**
     * getNumber
     * Converts byte[] into an integer
     * @param data
     * @return int
     */
    private int getNumber(byte[] data){
        return new BigInteger(data).intValue();
    }


    /**
     * getData
     * @return byte[]
     */
    public byte[] getData(){
        int i = 4;
        ArrayList<Byte> buffer = new ArrayList<>();
        while(i < packet.length){
            buffer.add(packet[i]);
            i++;
        }
        return convertToBytes(buffer);
    }


    /**
     * getErrorMessage
     * @return String
     */
    public String getErrorMessage(){
        return getString(4);
    }

    /**
     * getFileName
     * @return String
     */
    public String getFileName(){
        return getString(2);
    }

    /**
     * getString
     * Gets a string from the packet data
     * @param offset
     * @return String
     */
    private String getString(int offset){
        int i = offset;
        ArrayList<Byte> buffer = new ArrayList<>();
        while(i < packet.length && packet[i] != 0){
            buffer.add(packet[i]);
            i++;
        }
        return new String(convertToBytes(buffer));

    }
}
