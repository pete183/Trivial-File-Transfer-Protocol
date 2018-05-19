package mttcpserver;

import java.util.ArrayList;
import java.util.Random;

/**
 * SocketConstants
 * Extends Thread
 */
public class SocketConstants extends Thread{

    /**
     * Protected final Mode set to "octet"
     * Protected final data length set to 512
     * Protected final packet length set to 516 e.g. 512 + 4
     * Protected timeout is set to 5 seconds
     */
    protected final String MODE = "octet";
    protected final int DATA_LENGTH = 512;
    protected final int PACKET_LENGTH = 516;
    protected final int TIME_OUT = 5000;


    /**
     * SocketConstants
     * Constructor which implements Thread
     * @param name
     */
    public SocketConstants(String name){
        super(name);
    }

    /**
     * SocketConstants
     * Non threaded Constructor
     */
    public SocketConstants(){
    }

    /**
     * Opcode enum
     */
    protected enum Opcode{
        Read(1),
        Write(2),
        Data(3),
        Ack(4),
        Error(5);

        private int value;

        Opcode(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Opcode get(int value){
            Opcode option = Opcode.Error;
            for(Opcode code: Opcode.values()){
                if(code.value == value){
                    option = code;
                }
            }
            return option;
        }

        @Override
        public String toString() {
            return String.valueOf(this.getValue());
        }

    }

    /**
     * convertToBytes
     * Converts an ArrayList<Byte> into a primitive byte[]
     * @param bytes ArrayList<Byte>
     * @return byte[]
     */
    protected byte[] convertToBytes(ArrayList<Byte> bytes){
        byte[] buffer = new byte[bytes.size()];
        for(int i = 0; i < bytes.size(); i++){
            buffer[i] = bytes.get(i);
        }
        return buffer;
    }

    /**
     * generateTID
     * Parameter port is included so the server / client
     * does not have the same port
     * @param port
     * @return int
     */
    protected int generateTID(int port){
        int random;
        do {
            Random r = new Random();
            int Low = 1025;
            int High = 65534;
            random =  r.nextInt(High-Low) + Low;
        }
        while(random == port);
        return random;
    }

    /**
     * generateTID
     * @return int
     */
    protected int generateTID(){
        Random r = new Random();
        int Low = 1025;
        int High = 65534;
        return r.nextInt(High-Low) + Low;
    }
}
