import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    // initialize socket and input output streams
    private Socket socket               = null;
    private BufferedReader in           = null;
    private DataOutputStream out        = null;

    // Data to keep track of
    int clientState = 0;
    private String[] clientCommands = {"HELO", "AUTH " + System.getProperty("user.name"), "REDY", "SCHD", "QUIT", "OK", "RESC All"};
    ArrayList<ArrayList<String>> serverInfo = new ArrayList<>();

    // Execute RESC All once - Will not be used in Stage 1
    boolean obtainServerData = true;

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

        } catch(UnknownHostException u) { System.out.println(u);
        } catch(IOException i) { System.out.println(i); }

        String userInput = clientCommands[clientState]; // Send HELO

        while (userInput != "EXIT PROGRAM") {

            // Send the command
            sendCommand(userInput);

            // Wait for reply
            String serverResponse = waitForResponse();
            System.out.println("RCVD: "+serverResponse);

            // Process reply
            userInput = processServerResponse(serverResponse);

        }

        try {

            out.close();
            socket.close();
            in.close();

        } catch(IOException i) { System.out.println(i); }

    }

    // Send a command to the server
    public void sendCommand(String argument){

        try {

            // Send the command to the server
            System.out.println("SENT: " +argument);
            out.write((argument+"\n").getBytes());

        } catch(IOException i) {  System.out.println(i); }

    };

    public String processServerResponse(String argument) {

        // Split string into array of words
        String[] arr = argument.split(" ");

        if(arr[0].equals("OK")) {

            if(clientState == 3) { // Response from SCHD
                clientState = 2; // Reply REDY
            } else { // Response from any other command
                clientState++; // Reply next command
            }

        } else if(arr[0].equals("JOBN")) {

            if(!obtainServerData) {
                clientState = 6;
                obtainServerData = true;
            } else {
                clientState++; // Reply SCHD JobID ServerSize ServerID
                return clientCommands[clientState] + " " + arr[2] + " " + "large 0";
            }

        } else if(arr[0].equals("NONE")) {

            clientState = 4; // Reply QUIT

        } else if(arr[0].equals("QUIT")) {

            return "EXIT PROGRAM";

        } else {

            if(arr[0].equals(".") || arr[0].equals("DATA")) { // Response from end of RESC All

                clientState = 2; // Reply REDY

            } else {

                clientState = 5; // Reply OK

                // Record Server Details
                ArrayList<String> temp = new ArrayList<>();
                for(int i = 0; i < arr.length; i++) {
                    temp.add(arr[i]);
                }
                serverInfo.add(temp);

            }

        }

        // Return string for next command
        return clientCommands[clientState];

    };

    public String waitForResponse() {

        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("ERR: Server response was interrupted.");
        }

        System.out.println("ERR: No Response from Server.");
        return null;

    };

    public static void main(String args[]) {

        Client client = new Client("127.0.0.1", 8096);

    }

}
