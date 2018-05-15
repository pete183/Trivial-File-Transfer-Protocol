package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UDPSocketClient extends GenericUDPSocket {

    private static String fileName;
    // the client will take the IP Address of the server (in dotted decimal format as an argument)
    // given that for this tutorial both the client and the server will run on the same machine, you can use the loopback address 127.0.0.1
    public static void main(String[] args) throws IOException {



        String option = optionInput();


        DatagramSocket socket;
        DatagramPacket packet;

        ArrayList<Byte> recieveBuffer = new ArrayList<>();
        byte[] totalByteArray = new byte[LENGTH];


        if (args.length != 1) {
            System.out.println("the hostname of the server is required");
            return;
        }

        byte[] buf = new byte[LENGTH];


        //****************************************************************************************
        // add a line below to instantiate the DatagramSocket socket object
        // bind the socket to some port over 1024
        // Note: this is NOT the port we set in the server
        // If you put the same port you will get an exception because
        // the server is also listening to this port and both processes run on the same machine!
        //****************************************************************************************
        socket = new DatagramSocket(4000);

        //socket.setSoTimeout(50);

        // Add source code below to get the address from args[0], the argument handed in when the process is started.
        // In Netbeans, add a command line argument by changing the running configuration.
        // The address must be transfomed from a String to an InetAddress (an IP addresse object in Java).
        InetAddress address = InetAddress.getByName(args[0]);


        switch(Integer.parseInt(option)){
            case 1:
                buf = createReadBuffer();
                break;
            case 2:
                buf = createWriteBuffer();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }

        //************************************************************
        // Add a line to instantiate a packet using the buf byte array
        // Set the IP address and port fields in the packet so that the packet is sent to the server
        //************************************************************
        packet = createPacket(buf, address);


        // Send the datagram packet to the server (this is a blocking call) - we do not care about the data that the packet carries.
        // The server will respond to any kind of request (i.e. regardless of the packet payload)
        socket.send(packet);

        //**************************************************************************************
        // add a line of code below to receive a packet containing the server's response
        // we can reuse the DatagramPacket instantiated above - all settable values will be overriden when the receive call completes.
        //**************************************************************************************
        boolean whileLoop = true;
        while(whileLoop){
            socket.receive(packet);


            Opcode opOption = Opcode.get(getOpcode(packet.getData()));

            if(packet.getData()[LENGTH - 1] == 0){
                whileLoop = false;
            }

            switch(opOption){
                case Read:
                    break;
                case Write:
                    break;
                case Data:
                    for(byte bytes: getData(packet.getData())){
                        recieveBuffer.add(bytes);
                    }

                    if(!whileLoop) {
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

                    packet = createPacket(buf, address);

                    // Send the datagram packet to the server (this is a blocking call) - we do not care about the data that the packet carries.
                    // The server will respond to any kind of request (i.e. regardless of the packet payload)
                    socket.send(packet);

                    break;
                case Ack:

                    File file = new File("./"+ fileName);



                    if(file.exists()){
                        System.out.println("Reading file: " + fileName);
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
                        System.out.println(fileName + " does not exist");
                        byte[] errorSend = addOpToBuffer(Opcode.Error.getValue());
                        errorSend = addErrorToBuffer(errorSend, 40);
                        errorSend = addErrorMessageToBuffer(errorSend, fileName + " does not exist");

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
                case Error:
                    System.out.println(getErrorMessage(packet.getData()));
                    whileLoop = false;
                    break;

            }
        }

        // display response
        socket.close();
    }




    public static String optionInput(){
        System.out.println("press 1 to retrieve a file from the server, press 2 to store a file on the server");

        Scanner scan = new Scanner(System.in);
        return scan.nextLine();
    }

    public static byte[] createReadBuffer(){
        return createBuffer(Opcode.Read.getValue());
    }

    public static byte[] createWriteBuffer(){
        return createBuffer(Opcode.Write.getValue());
    }


    public static byte[] createBuffer(int opcode){
        byte[] buf = addOpToBuffer(opcode);


        System.out.println("Enter the filename");

        Scanner scanFile = new Scanner(System.in);
        fileName = scanFile.nextLine();



        if(fileName.length() > (LENGTH - TFTPMODE.length() - 4)){
            System.out.println("File name is too long");
        }

        byte[] bytes = fileName.getBytes();

        System.arraycopy(bytes, 0, buf, 2, fileName.length());


        buf[fileName.length()+2] = 0;

        byte[] mode = TFTPMODE.getBytes();

        System.arraycopy(mode, 0, buf, fileName.length() + 3, mode.length);

        return buf;
    }
    
}
