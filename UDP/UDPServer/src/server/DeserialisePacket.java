package server;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.util.ArrayList;

/**
 * DeserialisePacket
 * Extends SocketConstants
 */
public class DeserialisePacket extends SocketConstants {

    /**
     * private DatagramPacket
     */
    private DatagramPacket packet;

    /**
     * DeserialisePacket
     * Constructor
     * @param packet
     */
    public DeserialisePacket(DatagramPacket packet){
        this.packet = packet;
    }

    /**
     * getOpcode
     * @return Opcode
     */
    public int getOpcode(){
        byte[] data = new byte[2];
        System.arraycopy(packet.getData(), 0, data, 0, 2);
        return getNumber(data);
    }

    /**
     * getBlockNumber
     * @return Block Number
     */
    public int getBlockNumber(){
        byte[] data = new byte[2];
        System.arraycopy(packet.getData(), 2, data, 0, 2);
        return getNumber(data);
    }

    /**
     * getErrorCode
     * @return Error Code
     */
    public int getErrorCode(){
        byte[] data = new byte[2];
        System.arraycopy(packet.getData(), 2, data, 0, 2);
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
        while(i < packet.getData().length && packet.getData()[i] != 0){
            buffer.add(packet.getData()[i]);
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
        while(i < packet.getData().length && packet.getData()[i] != 0){
            buffer.add(packet.getData()[i]);
            i++;
        }
        return new String(convertToBytes(buffer));

    }
}
