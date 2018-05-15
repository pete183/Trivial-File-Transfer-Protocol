package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UDPSocketServer extends GenericUDPSocket {

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

        byte[] totalByteArray = new byte[LENGTH];
        byte[] recvBuf = new byte[LENGTH];     // a byte array that will store the data received by the client
        ArrayList<Byte> recieveBuffer = new ArrayList<>();
        String fileName = "";

        try {
            // run forever
            while (true) {
                //**************************************
                // Add source code below to: 
                // 1) create a DatagramPacket called packet. Use the byte array above to construct the datagram
                // 2) wait until a client sends something (a blocking call).
                //**************************************

                DatagramPacket packet = new DatagramPacket(recvBuf, LENGTH);

                socket.receive(packet);


                Opcode option = Opcode.get(getOpcode(packet.getData()));
                switch(option){
                    case Read:
                        //Read command from client


                        String readFileName = getFilename(packet.getData());

                        File file = new File("./"+ readFileName);



                        if(file.exists()){
                            System.out.println("Reading file: " + readFileName);
                            // Send data
                            totalByteArray = Files.readAllBytes(file.toPath());


                            byte[] send = addOpToBuffer(Opcode.Data.getValue());
                            send = addBlockToBuffer(send);

                            int length = (totalByteArray.length < (LENGTH - 4)) ? totalByteArray.length : (LENGTH - 4);

                            System.arraycopy(totalByteArray, 0, send, 4, length);


                            //****************************************
                            // Extract the IP address (an InetAddress object) and source port (int) from the received packet
                            // They will be both used to send back the response (which is now in the buf byte array -- see above)
                            //****************************************
                            InetAddress addr = packet.getAddress();
                            int srcPort = packet.getPort();

                            // set the buf as the data of the packet (let's re-use the same packet object)
                            packet.setData(send);

                            // set the IP address and port extracted above as destination IP address and port in the packet to be sent
                            packet.setAddress(addr);
                            packet.setPort(srcPort);

                            //*****************************************
                            // Send the packet (a blocking call)
                            //*****************************************
                            socket.send(packet);



                        } else {
                            // Send error
                            System.out.println(readFileName + " does not exist");
                            byte[] errorSend = addOpToBuffer(Opcode.Error.getValue());
                            errorSend = addErrorToBuffer(errorSend, 40);
                            errorSend = addErrorMessageToBuffer(errorSend, readFileName + " does not exist");

                            //****************************************
                            // Add source code below to extract the IP address (an InetAddress object) and source port (int) from the received packet
                            // They will be both used to send back the response (which is now in the buf byte array -- see above)
                            //****************************************
                            InetAddress addr = packet.getAddress();
                            int srcPort = packet.getPort();

                            // set the buf as the data of the packet (let's re-use the same packet object)
                            packet.setData(errorSend);

                            // set the IP address and port extracted above as destination IP address and port in the packet to be sent
                            packet.setAddress(addr);
                            packet.setPort(srcPort);

                            //*****************************************
                            // Send the packet (a blocking call)
                            //*****************************************
                            socket.send(packet);

                        }



                        break;
                    case Write:
                        //Write command from client
                        fileName = getFilename(packet.getData());
                        System.out.println("Writing file: " + fileName);

                        // Send Ack

                        byte[] buf = addOpToBuffer(Opcode.Ack.getValue());

                        buf = addBlockToBuffer(buf, packet.getData());



                        //****************************************
                        // Extract the IP address (an InetAddress object) and source port (int) from the received packet
                        // They will be both used to send back the response (which is now in the buf byte array -- see above)
                        //****************************************
                        InetAddress addr = packet.getAddress();
                        int srcPort = packet.getPort();

                        // set the buf as the data of the packet (let's re-use the same packet object)
                        packet.setData(buf);

                        // set the IP address and port extracted above as destination IP address and port in the packet to be sent
                        packet.setAddress(addr);
                        packet.setPort(srcPort);

                        //*****************************************
                        // Send the packet (a blocking call)
                        //*****************************************
                        socket.send(packet);




                        break;
                    case Data:

                        for(byte bytes: getData(packet.getData())){
                            recieveBuffer.add(bytes);
                        }

                        if(packet.getData()[LENGTH - 1] == 0) {
                            try (FileOutputStream fos = new FileOutputStream(fileName)) {

                                byte[] fileBytesArray = new byte[recieveBuffer.size()];
                                for(int i = 0; i < recieveBuffer.size(); i++){
                                    fileBytesArray[i] = recieveBuffer.get(i);
                                }
                                fos.write(fileBytesArray);
                                System.out.println("File has been saved to client's directory as " + fileName);
                            }
                        }

                        buf = addOpToBuffer(Opcode.Ack.getValue());

                        buf = addBlockToBuffer(buf, packet.getData());

                        InetAddress addrData = packet.getAddress();
                        packet = createPacket(buf, addrData);

                        // Send the datagram packet to the server (this is a blocking call) - we do not care about the data that the packet carries.
                        // The server will respond to any kind of request (i.e. regardless of the packet payload)
                        socket.send(packet);
                        break;
                    case Ack:
                        int block = getBlock(packet.getData()) + 1;

                        byte[] send = addOpToBuffer(3);
                        send = addBlockToBuffer(send, block);



                        int length = (totalByteArray.length - (block*(LENGTH - 4)) < (LENGTH - 4)) ? totalByteArray.length - (block * (LENGTH - 4)) : (LENGTH - 4);
                        if(length > 0){
                            System.arraycopy(totalByteArray, block * 508, send, 4, length);
                            //System.arraycopy(byteArray, fromPos, dest, toPos, length);

                            //****************************************6
                            // Extract the IP address (an InetAddress object) and source port (int) from the received packet
                            // They will be both used to send back the response (which is now in the buf byte array -- see above)
                            //****************************************
                            addr = packet.getAddress();
                            srcPort = packet.getPort();

                            // set the buf as the data of the packet (let's re-use the same packet object)
                            packet.setData(send);

                            // set the IP address and port extracted above as destination IP address and port in the packet to be sent
                            packet.setAddress(addr);
                            packet.setPort(srcPort);

                            //*****************************************
                            // Send the packet (a blocking call)
                            //*****************************************
                            socket.send(packet);
                        }
                        break;
                    case Error:
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
