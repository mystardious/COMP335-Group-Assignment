import java.io.*;
import java.net.*;

public class Client {

    // initialize socket and input output streams
    private Socket socket               = null;
    private BufferedReader in           = null;
    private DataOutputStream out        = null;

    private String[] clientCommands = {"HELO", "AUTH comp335", "REDY", "SCHD", "QUIT"};
    int clientState = 0;

    // constructor to put ip address and port
    public Client(String address, int port)
    {
        // establish a connection
        try {

            // Connect to the server
            socket = new Socket(address, port);
            System.out.println("Connected");

            // Commands received from server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Commands sent to the server
            out = new DataOutputStream(socket.getOutputStream());

        } catch(UnknownHostException u) {

            System.out.println(u);

        } catch(IOException i) {

            System.out.println(i);

        }

        // Establish connection to Server
        sendCommand(clientCommands[0]);

        // close the connection
        try {

            System.out.println("Connection to Server Terminated");
            out.close();
            socket.close();
            in.close();

        } catch(IOException i) {

            System.out.println(i);

        }

    }

    // Send a command the the server
    public void sendCommand(String argument){

        try {

            // Send the command
            System.out.println("SENT: " +argument);
            out.write(argument.getBytes());

            waitForResponse();

        } catch(IOException i) {

            System.out.println(i);

        }

    };

    public void receivedCommand(String argument) {

        String[] arr = argument.split(" ");

        System.out.println("RCVD: "+argument);

        if(arr[0].equals("OK")) {
            if(clientState == 3) {
                clientState = 2;
                sendCommand(clientCommands[clientState]);
            } else {
                clientState++;
                sendCommand(clientCommands[clientState]); // Send next command
            }
        } else if(arr[0].equals("JOBN")) {
            clientState++;
            sendCommand(clientCommands[clientState]+" "+arr[2]+" "+"large 0"); // Send next command
        } else if(arr[0].equals("NONE")) {
            clientState = 4;
            sendCommand(clientCommands[clientState]); // Send next command
        }

    };

    public void waitForResponse() {

        try {
            boolean loop = true;
            StringBuffer recievedCommand = new StringBuffer();
            int ch = 0;
            // Response from server
            while (loop) {
                ch = in.read();
                recievedCommand.append((char) ch);
                if (ch != 0 && !in.ready()) {
                    loop = false;
                }
            }

            receivedCommand(recievedCommand.toString());
        } catch (IOException e) {
            System.out.println(e);
        }

    };

    public static void main(String args[]) {

        Client client = new Client("127.0.0.1", 8096);

    }

}
