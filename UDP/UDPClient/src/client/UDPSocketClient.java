package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Scanner;


public class UDPSocketClient extends SocketConstants {

    private final int TID;
    private InetAddress address;

    private enum ClientOption {
        Read("1"),
        Write("2"),
        Error("3");

        private String value;

        ClientOption(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ClientOption get(String value){
            for(ClientOption code: ClientOption.values()){
                if(code.value.equals(value)){
                    return code;
                }
            }
            return ClientOption.Error;
        }


        @Override
        public String toString() {
            return this.getValue();
        }
    }


    public UDPSocketClient(String[] args) throws IOException {
        TID = generateTID();
        System.out.println(TID);
        DatagramSocket socket;
        DatagramPacket packet;

        String fileName = "";
        byte[] sendBuffer = new byte[516];
        ByteArray recieveBuffer = new ByteArray();
        ByteArray wholeFile = new ByteArray();
        byte[] sendFileData = new byte[516];


        socket = new DatagramSocket(TID);
        address = InetAddress.getByName(args[0]);

        if (args.length != 1) {
            System.out.println("the hostname of the server is required");
            return;
        }


        boolean userInput = true;
        while(userInput) {
            String userOption = askUserInputOption();
            fileName = askUserFileName();
            SerialisePacket serialisePacket = new SerialisePacket();
            switch (ClientOption.get(userOption)) {
                case Read:

                    sendBuffer = serialisePacket.getRequestBuffer(Opcode.Read, fileName);
                    userInput = false;
                    break;
                case Write:
                    sendBuffer = serialisePacket.getRequestBuffer(Opcode.Write, fileName);
                    userInput = false;
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }

        }
        byte[] finalSend = new byte[516];
        System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
        packet = createPacket(finalSend, 9000);
        socket.send(packet);



        boolean packetLoop = true;
        while(packetLoop){
            socket.receive(packet);

            DeserialisePacket deserialisePacket = new DeserialisePacket(packet);

            int opcode = deserialisePacket.getOpcode();
            SerialisePacket serialisePacket = new SerialisePacket();
            switch (Opcode.get(opcode)) {
                case Read:
                    System.out.println("Read");
                    break;
                case Write:
                    System.out.println("Write");
                    break;
                case Data:
                    if(packet.getData()[packet.getData().length - 1] == 0) {
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

                    sendBuffer = serialisePacket.getAckBuffer(deserialisePacket.getBlockNumber()+1);


                    byte[] senderBuffer = new byte[516];
                    System.arraycopy(sendBuffer, 0, senderBuffer, 0, sendBuffer.length);
                    packet.setData(senderBuffer);
                    packet.setAddress(packet.getAddress());
                    packet.setPort(packet.getPort());
                    socket.send(packet);
                    break;
                case Ack:
                    System.out.println("Ack");

                    int blockNumber = deserialisePacket.getBlockNumber();
                    File file = new File("./"+ fileName);

                    if(file.exists()){
                        wholeFile = new ByteArray();
                        wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                        int fileSendLength = (wholeFile.size() < DATA_LENGTH ) ? wholeFile.size() : (DATA_LENGTH);

                        byte[] dataSend = new byte[fileSendLength];
                        System.arraycopy(convertToBytes(wholeFile), 0, dataSend, 0, fileSendLength);


                        sendFileData = serialisePacket.getDataBuffer(blockNumber+1, dataSend);
                        System.out.println(new String(sendFileData));
                        packet.setData(sendFileData);
                        packet.setAddress(packet.getAddress());
                        packet.setPort(packet.getPort());
                        socket.send(packet);




                    } else {
                        // Send error
                        System.out.println(fileName + " doesn't exist");

                        sendFileData = serialisePacket.getErrorBuffer();


                        packet.setData(sendFileData);
                        packet.setAddress(packet.getAddress());
                        packet.setPort(packet.getPort());
                        socket.send(packet);


                    }

                    break;
                case Error:
                    System.out.println(deserialisePacket.getErrorMessage() + " - Error Code: " + deserialisePacket.getErrorCode());
                    packetLoop = false;
                    break;
            }

        }
        socket.close();
    }

    // the client will take the IP Address of the server (in dotted decimal format as an argument)
    // given that for this tutorial both the client and the server will run on the same machine, you can use the loopback address 127.0.0.1
    public static void main(String[] args) throws IOException {
        new UDPSocketClient(args);
    }

    private DatagramPacket createPacket(byte[] buffer, int port){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        packet.setAddress(address);
        packet.setPort(port);
        return packet;
    }


    private String askUserInputOption(){
        System.out.println("press 1 to retrieve a file from the server, press 2 to store a file on the server");
        Scanner scan = new Scanner(System.in);
        return scan.nextLine();
    }

    private String askUserFileName(){
        System.out.println("Enter the filename");


        Scanner scanFile = new Scanner(System.in);
        String answer = scanFile.nextLine();
        return answer;
    }


//    public DatagramPacket createInitalDatagram(byte[] buffer){
//        byte[] sendBuffer = new byte[LENGTH];
//        System.arraycopy(buffer,0, sendBuffer, 0, buffer.length);
//        DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length);
//        packet.setAddress(address);
//        packet.setPort(9000);
//        return packet;
//    }








}
