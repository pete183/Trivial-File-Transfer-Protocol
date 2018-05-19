package mttcpserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ReadRequestThread extends RequestThread {

    byte[] intitalBuffer;

    public ReadRequestThread(Socket socket, byte[] recievedBuffer){
        super(socket);
        this.intitalBuffer = recievedBuffer;
    }


    @Override
    // This method is called when we start a Thread object
    // Note that the MTTCPServerThread extends the Thread object
    public void run() {
        // The line that will be echoed back to the client.
        try {
            byte[] recievedBuffer = intitalBuffer;

            DeserialisePacket deserialisePacket = new DeserialisePacket(recievedBuffer);
            SerialisePacket serialisePacket = new SerialisePacket();
            String readFileName = deserialisePacket.getFileName();

            File file = new File("./" + readFileName);

            if (file.exists()) {
                ByteArray wholeFile = new ByteArray();
                wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                int block = 1;
                int length = 0;
                do {
                    length = (wholeFile.size() - ((block -1) * DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - ((block-1) * DATA_LENGTH) : (DATA_LENGTH);
                    byte[] dataSend = new byte[length];
                    System.arraycopy(convertToBytes(wholeFile), (block-1) * DATA_LENGTH, dataSend, 0, length);

                    byte[] sendFileData = serialisePacket.getDataBuffer(block, dataSend);

                    slaveSocket.getOutputStream().write(sendFileData);
                    block++;
                }while(length>=DATA_LENGTH);

            } else {
                System.out.println(readFileName + " doesn't exist");
                byte[] sendFileData = serialisePacket.getErrorBuffer();
                slaveSocket.getOutputStream().write(sendFileData);
            }
            slaveSocket.close();
            System.out.println("Slave is closed");
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
