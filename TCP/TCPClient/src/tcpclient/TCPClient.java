package tcpclient;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * TCPClient
 * Extends SocketConstants
 */
public class TCPClient extends SocketConstants{

    /**
     * Private address
     * Private port number
     */
    private String address;
    private int portNumber;

    /**
     * main
     *
     * The client takes two user arguments as input: 1) Server IP address in dotted-decimal format, 2) Server TCP port
     * Change the default running configuration in Netbeans to pass the arguments.
     * Given that both processes will run on the same machine for this lab session,
     * the IP address can be the loopback address 127.0.0.1
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new TCPClient(args);
    }

    /**
     * TCPClient
     * Constructor
     *
     * @param args
     */
    public TCPClient(String[] args){

        // Initialises working variables
        Socket echoSocket;
        String fileName = "";

        // Buffer byte[]
        byte[] sendBuffer;
        byte[] sendFileData;

        // Stores received and sending files in bytes
        ByteArray receiveBuffer = new ByteArray();
        ByteArray wholeFile = new ByteArray();

        //Check that both required input arguments are passed.
        if (args.length != 2) {
            System.err.println("Usage: java EchoClient <address> <port>");
            System.exit(1);
        }

        // Assign the first argument to the hostName String object
        address = args[0];
        // Assign the second argument to the portNumber variable
        portNumber = Integer.parseInt(args[1]);

        try {
            // Initialises an echo socket
            echoSocket = new Socket(address, portNumber);

            boolean userInput = true;
            boolean packetLoop = true;
            String userOption = null;
            while (userInput) {

                // Asks user to either read or write to the server
                userOption = askUserInputOption();
                SerialisePacket serialisePacket = new SerialisePacket();

                switch (ClientOption.get(userOption)) {
                    case Read:
                        // Asks the user which file they want to read from the server with the file name the user gave
                        fileName = askUserFileName();
                        // Creates a Read buffer
                        sendBuffer = serialisePacket.getRequestBuffer(Opcode.Read, fileName);
                        userInput = false;

                        // Sends the buffer to the server
                        byte[] finalSend = new byte[PACKET_LENGTH];
                        System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
                        echoSocket.getOutputStream().write(finalSend);


                        break;
                    case Write:
                        // Asks the user which file they want to write to the server
                        fileName = askUserFileName();
                        // If the file exists, send the file in blocks of 512 bytes
                        if (new File(fileName).exists()) {
                            // Create a buffer for a write request with the file name the user gave
                            sendBuffer = serialisePacket.getRequestBuffer(Opcode.Write, fileName);
                            userInput = false;
                            finalSend = new byte[PACKET_LENGTH];
                            System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
                            echoSocket.getOutputStream().write(finalSend);

                            // Open the users file on the client side
                            File file = new File("./" + fileName);
                            wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                            int block = 1;
                            int length = 0;

                            // Send the file in blocks of 512 bytes
                            // If there is less than 512 bytes to send, only send the bytes which are left
                            do {
                                length = (wholeFile.size() - ((block -1) * DATA_LENGTH) < DATA_LENGTH) ? wholeFile.size() - ((block-1) * DATA_LENGTH) : (DATA_LENGTH);
                                byte[] dataSend = new byte[length];
                                System.arraycopy(convertToBytes(wholeFile), (block-1) * DATA_LENGTH, dataSend, 0, length);

                                sendFileData = serialisePacket.getDataBuffer(block, dataSend);

                                echoSocket.getOutputStream().write(sendFileData);
                                block++;
                            }while(length>=DATA_LENGTH);
                            packetLoop = false;
                            System.out.println("File has been saved to server's directory as " + fileName);
                        } else {
                            // Print an error to the user
                            System.out.println(fileName + " does not exists");
                            System.out.println("Please start again.");
                        }

                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }

            }

            // Loop until packet loop is false
            while(packetLoop){

                // Wait for the server to send a packet
                // Once a packet has been received, add it to a byte[]
                InputStream stream = echoSocket.getInputStream();
                byte[] bigBuffer = new byte[516];
                int count = stream.read(bigBuffer);
                byte[] receivedBuffer = new byte[count];
                System.arraycopy(bigBuffer, 0, receivedBuffer, 0, count);
                DeserialisePacket deserialisePacket = new DeserialisePacket(receivedBuffer);

                switch(ClientOption.get(userOption)){
                    case Read:
                        // Within the read case, add the byte[] received to a ByteArray until there is none left
                        if (count < PACKET_LENGTH) {
                            packetLoop = false;
                        }
                        receiveBuffer.addBytes(deserialisePacket.getData());
                        if (!packetLoop) {

                            // When it's the last data packet
                            // Save the ByteArray to the file
                            try (FileOutputStream fos = new FileOutputStream(fileName)) {

                                byte[] fileBytesArray = new byte[receiveBuffer.size()];
                                for (int i = 0; i < receiveBuffer.size(); i++) {
                                    fileBytesArray[i] = receiveBuffer.get(i);
                                }
                                fos.write(fileBytesArray);
                                fos.close();
                                System.out.println("File has been saved to client's directory as " + fileName);
                            }
                        }
                        break;
                    case Write:
                        packetLoop = false;
                        break;
                    case Error:
                        System.out.println(deserialisePacket.getErrorMessage() + " - Error Code: " + deserialisePacket.getErrorCode());
                        packetLoop = false;
                        break;
                }
            }
            // Close the connection
            echoSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + address);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + address + ". Is the server running?");
            System.exit(1);
        }
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
        return scanFile.nextLine();
    }
}
