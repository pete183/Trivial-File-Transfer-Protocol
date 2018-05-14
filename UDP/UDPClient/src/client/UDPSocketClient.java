package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UDPSocketClient extends GenericUDPSocket {


    // the client will take the IP Address of the server (in dotted decimal format as an argument)
    // given that for this tutorial both the client and the server will run on the same machine, you can use the loopback address 127.0.0.1
    public static void main(String[] args) throws IOException {



        String option = optionInput();

        DatagramSocket socket;
        DatagramPacket packet;

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

        socket.setSoTimeout(50);

        // Add source code below to get the address from args[0], the argument handed in when the process is started.
        // In Netbeans, add a command line argument by changing the running configuration.
        // The address must be transfomed from a String to an InetAddress (an IP addresse object in Java).
        InetAddress address = InetAddress.getByName(args[0]);


        switch(Integer.parseInt(option)){
            case 1:
                System.out.println("Read files");
                buf = createReadBuffer();
                break;
            case 2:
                System.out.println("Write files");
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
        packet = new DatagramPacket(buf, LENGTH);
        packet.setAddress(address);
        packet.setPort(9000);


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
            String received = new String(packet.getData());

            Opcode opOption = Opcode.get(getOpcode(packet.getData()));

            if(packet.getData()[LENGTH - 1] == 0){
                whileLoop = false;
            }

            switch(opOption){
                case Data:

                    System.out.println(received.substring(4, packet.getLength()));
                    buf = addOpToBuffer(Opcode.Ack.getValue());

                    buf = addBlockToBuffer(buf, packet.getData());



                    packet = new DatagramPacket(buf, LENGTH);
                    packet.setAddress(address);
                    packet.setPort(9000);

                    // Send the datagram packet to the server (this is a blocking call) - we do not care about the data that the packet carries.
                    // The server will respond to any kind of request (i.e. regardless of the packet payload)
                    socket.send(packet);


                    break;
                case Error:

                    System.out.println(received.substring(3, packet.getLength()));
                    System.out.println("Error");
                    break;

            }

        }

        // display response

        socket.close();
    }




    public static String optionInput(){
        System.out.println("press 1 to read a file, press 2 to write a file");

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
        String fileName = scanFile.nextLine();



        if(fileName.length() > (LENGTH - TFTPMODE.length() - 4)){
            System.out.println("File name is too long");
        }

        byte[] bytes = fileName.getBytes();

        for(int i = 2; i < fileName.length() + 2; i++ ){
            buf[i] = bytes[i - 2];
        }


        buf[fileName.length()+2] = 0;


        byte[] mode = TFTPMODE.getBytes();

        for(int i = fileName.length()+3; i < mode.length + fileName.length()+3; i++ ){
            buf[i] = mode[i - (fileName.length()+3)];
        }

        return buf;
    }
    
}
