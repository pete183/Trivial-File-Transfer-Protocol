package server;

import java.util.ArrayList;
import java.util.Random;

public class SocketConstants extends Thread{

    protected final String MODE = "octet";
    protected final int DATA_LENGTH = 512;

    public SocketConstants(String name){
        super(name);
    }

    public SocketConstants(){
    }

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
}
