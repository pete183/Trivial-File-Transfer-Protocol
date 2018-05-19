package tcpclient;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;

public class TCPClient extends SocketConstants{

    /**
     * Private address
     */
    private String address;
    private int portNumber;


    // The client takes two user arguments as input: 1) Server IP address in dotted-decimal format, 2) Server TCP port
    // Change the default running configuration in Netbeans to pass the arguments.
    // Given that both processes will run on the same machine for this lab session,
    // the IP address can be the loopback address 127.0.0.1
    public static void main(String[] args) throws IOException {
        new TCPClient(args);
//        Socket echoSocket;
//        String adress;
//        int portNumber;
//        String userInput;
//        String serverResponse;
//
//        // All Input/Output from/to the socket will take place through the writer/reader objects defined below.
//        // We will not access sockets directly in this example.
//        PrintWriter out;
//        BufferedReader in;
//        // A buffered reader to read characters typed by the user (i.e. from the standard input)
//        BufferedReader stdIn;
//
//
//        // Check that both required input arguments are passed.
//        if (args.length != 2) {
//            System.err.println("Usage: java EchoClient <address> <port>");
//            System.exit(1);
//        }
//
//        // Assign the first argument to the hostName String object
//        adress = args[0];
//        // Assign the second argument to the portNumber variable
//        portNumber = Integer.parseInt(args[1]);
//
//        try {
//            // *******************************************************************************************
//            // TODO:
//            // Add a line of code that will instantiate the client Socket (echoSocket) and connect it to the server (must be running)
//            // Alternatively, you can use the default constructor to first instantiate the Socket object
//            // and then explicitly connect it to the server.
//            // *******************************************************************************************
//            echoSocket = new Socket(adress, portNumber);
//
//
//            // We get an OutputStream from the client socket and use it to construct a PrintWriter object
//            // to write formatted representations of objects to the socket (to send messages to the server through TCP)
//            out = new PrintWriter(echoSocket.getOutputStream(), true);
//
//            // We get an InputStream from the client socket and use it to construct a BufferedReader object
//            // to read a character input stream from the socket (sent from the server through TCP)
//            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
//
//            // We create an InputStreamReader and wrap it with a BufferedReader object to read characters coming through the standard input (typed by the user).
//            stdIn = new BufferedReader(new InputStreamReader(System.in));
//
//            // Keep reading from the standard input until the user types 'exit'
//            // When the client is running, you will type in characters in the consle below
//            // A line terminates with a carriage return (i.e. after you pressed enter)
//            while (((userInput = stdIn.readLine()).compareTo("exit")) != 0) {
//                System.out.println("Requesting to echo: " + userInput + ", which is " + userInput.length() +  " characters long..");
//
//                // *******************************************************************************************
//                // TODO:
//                // Add a line that sends the line of characters to the server
//                // This is a bloking call; i.e. the process will block until the whole line (terminated by a carriage return) is written to the stream (i.e. sent through the socket)
//                // Don't use the socket directly - we have a wrapped a PrintWriter object around it (check above)
//                // *******************************************************************************************
//                out.println(userInput);
//
//                // *******************************************************************************************
//                // TODO:
//                // Add a line that reads a line of characters sent from the server (the server will echo back what we sent above)
//                // This is a bloking call; i.e. the process will block until a whole line (terminated by a carriage return) is read
//                // Don't use the socket directly - we have a wrapped a PrintWriter object around it (check above)
//                // *******************************************************************************************
//                serverResponse = in.readLine();
//
//                // Print the echoed line
//                System.out.println("echo: " + serverResponse);
//            }
//
//            // User typed 'exit'. Close the socket (effectively tearing down the TCP connection)
//            echoSocket.close();
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host " + adress);
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection to " + adress + ". Is the server running?");
//            System.exit(1);
//        }
    }


    public TCPClient(String[] args){

        Socket echoSocket;
        String fileName = "";
        byte[] sendBuffer;
        ByteArray recieveBuffer = new ByteArray();
        ByteArray wholeFile = new ByteArray();
        byte[] sendFileData;


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
            echoSocket = new Socket(address, portNumber);

            boolean userInput = true;
            boolean packetLoop = true;
            String userOption = null;
            while (userInput) {
                userOption = askUserInputOption();
                SerialisePacket serialisePacket = new SerialisePacket();
                switch (ClientOption.get(userOption)) {
                    case Read:
                        fileName = askUserFileName();
                        sendBuffer = serialisePacket.getRequestBuffer(Opcode.Read, fileName);
                        userInput = false;

                        byte[] finalSend = new byte[PACKET_LENGTH];
                        System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
                        echoSocket.getOutputStream().write(finalSend);


                        break;
                    case Write:
                        fileName = askUserFileName();
                        if (new File(fileName).exists()) {
                            sendBuffer = serialisePacket.getRequestBuffer(Opcode.Write, fileName);
                            userInput = false;
                            finalSend = new byte[PACKET_LENGTH];
                            System.arraycopy(sendBuffer, 0, finalSend, 0, sendBuffer.length);
                            echoSocket.getOutputStream().write(finalSend);
                            File file = new File("./" + fileName);
                            wholeFile.addBytes(Files.readAllBytes(file.toPath()));

                            int block = 1;
                            int length = 0;
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
                            System.out.println(fileName + " does not exists");
                            System.out.println("Please start again.");
                        }

                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }

            }


            while(packetLoop){

                InputStream stream = echoSocket.getInputStream();
                byte[] bigBuffer = new byte[516];
                int count = stream.read(bigBuffer);
                byte[] recievedBuffer = new byte[count];
                System.arraycopy(bigBuffer, 0, recievedBuffer, 0, count);
                DeserialisePacket deserialisePacket = new DeserialisePacket(recievedBuffer);
                SerialisePacket serialisePacket = new SerialisePacket();

                switch(ClientOption.get(userOption)){
                    case Read:

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
        String answer = scanFile.nextLine();
        return answer;
    }
}
