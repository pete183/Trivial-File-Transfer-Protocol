package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * GenericUDPSocket extends Thread
 */
public class GenericUDPSocket {


    public static final int LENGTH = 512;
    public static final String TFTPMODE = "octet";

    public enum Opcode{
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

    public static int getOpcode(byte[] data){
        return getConcatNumber(data[0], data[1]);
    }

    public static String getFilename(byte[] data){
        return getString(data, 2);
    }


    public static int getBlock(byte[] data){
        return getSecondSection(data);
    }

    public static int getErrorCode(byte[] data){
        return getSecondSection(data);
    }

    private static int getSecondSection(byte[] data){
        return getConcatNumber(data[2], data[3]);
    }

    public static byte[] getData(byte[] data){
        List<Byte> fileNameBytes = new ArrayList<>();

        int i = 4;
        while(i < 512 && data[i] != 0){
            fileNameBytes.add(data[i]);
            i++;
        }

        byte[] fileNameBytesPrim = new byte[fileNameBytes.size()];


        for(int j = 0; j < fileNameBytes.size(); j++){
            fileNameBytesPrim[j] = fileNameBytes.get(j);
        }
        return fileNameBytesPrim;

    }


    public static String getErrorMessage(byte[] data){
        return getString(data, 3);
    }


    public static String getString(byte[] data, int offset){
        List<Byte> fileNameBytes = new ArrayList<>();

        int i = offset;
        while(i < 512 && data[i] != 0){
            fileNameBytes.add(data[i]);
            i++;
        }

        byte[] fileNameBytesPrim = new byte[fileNameBytes.size()];


        for(int j = 0; j < fileNameBytes.size(); j++){
            fileNameBytesPrim[j] = fileNameBytes.get(j);
        }
        return new String(fileNameBytesPrim);
    }




    public static int getConcatNumber(byte byte1, byte byte2){
        String number = byte1 + byte2 + "";
        return Integer.parseInt(number);
    }


    public static byte[] addOpToBuffer(int number){
        byte[] buf = new byte[LENGTH];
        return addOpToBuffer(buf, number);

    }

    public static byte[] addOpToBuffer(byte[] buf, int number){
        buf[0] = 0;
        buf[1] = (byte) number;
        return buf;
    }


    public static byte[] addBlockToBuffer(byte[] buf, byte[] packetData){
        buf[2] = packetData[2];
        buf[3] = packetData[3];
        return buf;
    }

    public static byte[] addSecondToBuffer(byte[] buf, int number){

        buf[2] = (byte) (((10 % number) == 0 ) ? 1 : 0);
        buf[3] = (byte) (number % 10) ;
        return buf;
    }


    public static byte[] addBlockToBuffer(byte[] buf){
        buf[2] = 0;
        buf[3] = 0;
        return buf;
    }


    public static byte[] addBlockToBuffer(byte[] buf, int number){
        return addSecondToBuffer(buf, number);

    }

    public static byte[] addErrorToBuffer(byte[] buf, int number){
        return addSecondToBuffer(buf, number);
    }

    public static byte[] addErrorMessageToBuffer(byte[] buf, String string){
        int length = (string.getBytes().length < LENGTH) ? string.getBytes().length : (LENGTH - 5);
        System.arraycopy(string.getBytes(), 0, buf, 3, length);
        return buf;
    }

    public static DatagramPacket createPacket(byte[] buf, InetAddress address){
        DatagramPacket packet = new DatagramPacket(buf, LENGTH);
        packet.setAddress(address);
        packet.setPort(9000);
        return packet;
    }
}