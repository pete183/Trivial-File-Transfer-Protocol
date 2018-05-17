package server;


public class SerialisePacket extends SocketConstants {

    public byte[] getRequestBuffer(Opcode opcode, String fileName){
        return convertToBytes(new ByteArray(){{
            addInt(opcode.getValue());
            addString(fileName);
            addZero();
            addString(MODE);
            addZero();
        }});
    }

    public byte[] getDataBuffer(int blockNumber, byte[] data){
        return convertToBytes(new ByteArray(){{
            addInt(Opcode.Data.getValue());
            addInt(blockNumber);
            addBytes(data);
        }});
    }

    public byte[] getAckBuffer(int blockNumber){
        return convertToBytes(new ByteArray(){{
            addInt(Opcode.Ack.getValue());
            addInt(blockNumber);
        }});
    }

    public byte[] getErrorBuffer() {
        return convertToBytes(new ByteArray() {{
            addInt(Opcode.Error.getValue());
            addInt(1);
            addString("File not found");
            addZero();
        }});
    }
}
