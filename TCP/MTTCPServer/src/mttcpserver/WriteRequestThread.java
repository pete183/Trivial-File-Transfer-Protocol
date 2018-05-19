package mttcpserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;

public class WriteRequestThread extends RequestThread {

    byte[] intitalBuffer;

    public WriteRequestThread(Socket socket, byte[] recievedBuffer){
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
            String fileName = deserialisePacket.getFileName();
            ByteArray recieveBuffer = new ByteArray();





                boolean packetLoop = true;

                do{
                    InputStream stream = slaveSocket.getInputStream();
                    byte[] bigBuffer = new byte[516];
                    int count = stream.read(bigBuffer);
                    recievedBuffer = new byte[count];
                    System.arraycopy(bigBuffer, 0, recievedBuffer, 0, count);
                    deserialisePacket = new DeserialisePacket(recievedBuffer);

                    if (count < PACKET_LENGTH) {
                        packetLoop = false;
                    }
                    recieveBuffer.addBytes(deserialisePacket.getData());
                    if (!packetLoop) {
                        try (FileOutputStream fos = new FileOutputStream(fileName)) {

                            byte[] fileBytesArray = new byte[recieveBuffer.size()];
                            for (int i = 0; i < recieveBuffer.size(); i++) {
                                fileBytesArray[i] = recieveBuffer.get(i);
                            }
                            fos.write(fileBytesArray);
                            fos.close();
                            System.out.println("File has been saved to client's directory as " + fileName);
                        }
                    }
                }while(packetLoop);



            slaveSocket.close();
            System.out.println("Slave socket has closed");
        } catch (IOException e) {
            System.err.println(e);
        }

    }

}
