package client;

/**
 * SerialisePacket
 * Extends SocketConstants
 */
public class SerialisePacket extends SocketConstants {

    /**
     * getRequestBuffer
     * @param opcode
     * @param fileName
     * @return byte[]
     */
    public byte[] getRequestBuffer(Opcode opcode, String fileName){
        return convertToBytes(new ByteArray(){{
            addInt(opcode.getValue());
            addString(fileName);
            addZero();
            addString(MODE);
            addZero();
        }});
    }

    /**
     * getDataBuffer
     * @param blockNumber
     * @param data
     * @return byte[]
     */
    public byte[] getDataBuffer(int blockNumber, byte[] data){
        return convertToBytes(new ByteArray(){{
            addInt(Opcode.Data.getValue());
            addInt(blockNumber);
            addBytes(data);
        }});
    }

    /**
     * getAckBuffer
     * @param blockNumber
     * @return byte[]
     */
    public byte[] getAckBuffer(int blockNumber){
        return convertToBytes(new ByteArray(){{
            addInt(Opcode.Ack.getValue());
            addInt(blockNumber);
        }});
    }


    /**
     * getErrorBuffer
     * @return byte[]
     */
    public byte[] getErrorBuffer() {
        return convertToBytes(new ByteArray() {{
            addInt(Opcode.Error.getValue());
            addInt(1);
            addString("File not found");
            addZero();
        }});
    }
}
