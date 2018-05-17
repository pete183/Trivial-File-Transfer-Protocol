package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;

public class UDPSocketServer extends SocketConstants {

    protected DatagramSocket socket = null;


    public UDPSocketServer() throws SocketException {
        this("UDPSocketServer");
    }

    public UDPSocketServer(String name) throws SocketException {
        super(name);
        // **********************************************
        // Add a line here to instantiate a DatagramSocket for the socket field defined above.
        // Bind the socket to port 9000 (any port over 1024 would be ok as long as no other application uses it).
        // Ports below 1024 require administrative rights when running the applications.
        // Take a note of the port as the client needs to send its datagram to an IP address and port to which this server socket is bound.
        //***********************************************

        socket = new DatagramSocket(9000);

    }



    @Override
    public void run() {

        byte[] recvBuf = new byte[516];     // a byte array that will store the data received by the client
        byte[] sendFileData = new byte[516];
        ByteArray wholeFile = new ByteArray();
        String readFileName = "";
        String writeFileName = "";
        ByteArray recieveBuffer = new ByteArray();



        try {
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

            DatagramSocket threadSocket = new DatagramSocket(generateTID(packet.getPort()));

            // run forever
            while (true) {

                socket.receive(packet);

                DeserialisePacket deserialisePacket = new DeserialisePacket(packet);
                int opcode = deserialisePacket.getOpcode();

                SerialisePacket serialisePacket = new SerialisePacket();
                switch(Opcode.get(opcode)){
                    case Read:

                        readFileName = deserialisePacket.getFileName();


                        File file = new File("./"+ readFileName);

                        if(file.exists()){
                            wholeFile = new ByteArray();
                            wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                            int fileSendLength = (wholeFile.size() < DATA_LENGTH ) ? wholeFile.size() : (DATA_LENGTH);

                            byte[] dataSend = new byte[fileSendLength];
                            System.arraycopy(convertToBytes(wholeFile), 0, dataSend, 0, fileSendLength);


                            sendFileData = serialisePacket.getDataBuffer(0, dataSend);
                            packet.setData(sendFileData);
                            packet.setAddress(packet.getAddress());
                            packet.setPort(packet.getPort());
                            socket.send(packet);




                        } else {
                            // Send error
                            System.out.println(readFileName + " doesn't exist");

                            sendFileData = serialisePacket.getErrorBuffer();


                            packet.setData(sendFileData);
                            packet.setAddress(packet.getAddress());
                            packet.setPort(packet.getPort());
                            socket.send(packet);


                        }

                        break;
                    case Write:


                        writeFileName = deserialisePacket.getFileName();
                        sendFileData = serialisePacket.getAckBuffer(1);
                        byte[] senderBuffer = new byte[516];
                        System.arraycopy(sendFileData, 0, senderBuffer, 0, sendFileData.length);
                        packet.setData(senderBuffer);
                        packet.setAddress(packet.getAddress());
                        packet.setPort(packet.getPort());
                        socket.send(packet);


                        break;
                    case Data:


                        recieveBuffer.addBytes(deserialisePacket.getData());


                        if (packet.getData()[packet.getData().length - 1] == 0) {
                            try (FileOutputStream fos = new FileOutputStream(writeFileName)) {

                                byte[] fileBytesArray = new byte[recieveBuffer.size()];
                                for (int i = 0; i < recieveBuffer.size(); i++) {
                                    fileBytesArray[i] = recieveBuffer.get(i);
                                }
                                fos.write(fileBytesArray);
                                fos.close();
                                System.out.println("File has been saved to server's directory as " + writeFileName);
                            }
                        }

                        byte[] sendBuffer = serialisePacket.getAckBuffer(deserialisePacket.getBlockNumber());

                        senderBuffer = new byte[516];
                        System.arraycopy(sendBuffer, 0, senderBuffer, 0, sendBuffer.length);
                        packet.setData(senderBuffer);
                        packet.setAddress(packet.getAddress());
                        packet.setPort(packet.getPort());
                        socket.send(packet);
                        break;
                    case Ack:
                        int block = deserialisePacket.getBlockNumber() + 1;
                        int length = (wholeFile.size() - (block*DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - (block * DATA_LENGTH) : (DATA_LENGTH);

                        if(length > 0){


                            byte[] smallFileData = new byte[512];

                            System.arraycopy(convertToBytes(wholeFile), block * DATA_LENGTH, smallFileData, 0, length);

                            SerialisePacket serialisePacket2 = new SerialisePacket();
                            sendFileData = serialisePacket2.getDataBuffer(block, smallFileData);




                            packet.setData(sendFileData);
                            packet.setAddress(packet.getAddress());
                            packet.setPort(packet.getPort());
                            socket.send(packet);

                        }
                        break;

                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        new UDPSocketServer().start();
        System.out.println("Time Server Started");
    }

}
