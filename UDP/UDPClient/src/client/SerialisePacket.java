package client;

/**
 * SerialisePacket
 * Extends SocketConstants
 */
public class SerialisePacket extends SocketConstants {

    /**
     * getRequestBuffer
     * Adds an opcode
     * Adds a filename
     * Adds a Zero
     * Adds a mode e.g. "octet"
     * Adds a Zero
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
     * Adds an opcode
     * Adds a block number
     * Adds the data
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
     * Adds an opcode
     * Adds a block number
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
     * Adds an opcode
     * Adds an error code e.g. 1
     * Adds an error message e.g. File not found
     * Adds a Zero
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
