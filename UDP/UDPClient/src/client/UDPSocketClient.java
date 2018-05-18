package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * UDPSocketClient
 * Extends SocketConstants
 */
public class UDPSocketClient extends SocketConstants {

    /**
     * Private address
     */
    private InetAddress address;


    /**
     * UDPSocketClient
     * Constructor
     * @param args
     * @throws IOException
     */
    public UDPSocketClient(String[] args) throws IOException {


        DatagramSocket socket;
        DatagramPacket packet;

        String fileName = "";
        byte[] sendBuffer = new byte[PACKET_LENGTH];
        ByteArray recieveBuffer = new ByteArray();
        ByteArray wholeFile = new ByteArray();
        byte[] sendFileData = new byte[PACKET_LENGTH];
        DatagramPacket sentPacket;


        socket = new DatagramSocket(generateTID());
        //TODO uncomment
        //socket.setSoTimeout(TIME_OUT);
        address = InetAddress.getByName(args[0]);

        if (args.length != 1) {
            System.out.println("the hostname of the server is required");
            return;
        }


        boolean userInput = true;
        while(userInput) {
            String userOption = askUserInputOption();
            SerialisePacket serialisePacket = new SerialisePacket();
            switch (ClientOption.get(userOption)) {
                case Read:
                    fileName = askUserFileName();
                    sendBuffer = serialisePacket.getRequestBuffer(Opcode.Read, fileName);
                    userInput = false;
                    break;
                case Write:
                    fileName = askUserFileName();
                    if(new File(fileName).exists()){
                        sendBuffer = serialisePacket.getRequestBuffer(Opcode.Write, fileName);
                        userInput = false;
                    } else {
                        System.out.println(fileName + " does not exists");
                        System.out.println("Please start again.");
                    }

                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }

        }
        byte[] finalSend = new byte[PACKET_LENGTH];
        System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
        packet = createPacket(finalSend, 9000);
        sentPacket = packet;
        socket.send(packet);



        boolean packetLoop = true;
        while(packetLoop){
            try {
                socket.receive(packet);
            } catch(SocketTimeoutException e){
                socket.send(sentPacket);
            }

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
                    if(packet.getLength() < PACKET_LENGTH){
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

                    sendBuffer = serialisePacket.getAckBuffer(deserialisePacket.getBlockNumber());


                    byte[] senderBuffer = new byte[PACKET_LENGTH];
                    System.arraycopy(sendBuffer, 0, senderBuffer, 0, sendBuffer.length);
                    packet.setData(senderBuffer);
                    packet.setAddress(packet.getAddress());
                    packet.setPort(packet.getPort());
                    sentPacket = packet;
                    socket.send(packet);
                    break;
                case Ack:


                    int blockNumber = deserialisePacket.getBlockNumber();
                    File file = new File("./"+ fileName);

                    if(file.exists()) {
                        wholeFile = new ByteArray();
                        wholeFile.addBytes(Files.readAllBytes(file.toPath()));


                        int length = (wholeFile.size() - ((blockNumber) * DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - ((blockNumber) * DATA_LENGTH) : (DATA_LENGTH);
                        if(length < 512){
                            packetLoop = false;
                            System.out.println("File has been saved to server's directory as " + fileName);
                        }
                        byte[] dataSend = new byte[length];
                        System.arraycopy(convertToBytes(wholeFile), (blockNumber) * DATA_LENGTH, dataSend, 0, length);


                        sendFileData = serialisePacket.getDataBuffer(blockNumber + 1, dataSend);
                        packet.setData(sendFileData);
                        packet.setAddress(packet.getAddress());
                        packet.setPort(packet.getPort());
                        sentPacket = packet;
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

    /**
     * main
     * @param args e.g. 127.0.0.1
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new UDPSocketClient(args);
    }

    /**
     * createPacket
     * @param buffer
     * @param port
     * @return DatagramPacket
     */
    private DatagramPacket createPacket(byte[] buffer, int port){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        packet.setAddress(address);
        packet.setPort(port);
        return packet;
    }

    /**
     * askUserInputOption
     * @return String user's option
     */
    private String askUserInputOption(){
        System.out.println("press 1 to retrieve a file from the server, press 2 to store a file on the server");
        Scanner scan = new Scanner(System.in);
        return scan.nextLine();
    }

    /**
     * askUserFileName
     * @return String filename
     */
    private String askUserFileName(){
        System.out.println("Enter the filename");
        Scanner scanFile = new Scanner(System.in);
        String answer = scanFile.nextLine();
        return answer;
    }
}
