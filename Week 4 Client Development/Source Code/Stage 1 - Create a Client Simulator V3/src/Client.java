import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Client {

    // User Arguments
    static boolean manualInput = false; // Allow manual input of commands
    static boolean help        = false; // Display program usage

    // Automation variables
    static int algorithm = 0; // 0 = AllToLargest, 1 = First-Fit, 2 = Best-Fit, 3 = Worst-Fit

    // Server data
    ArrayList<ArrayList<String>> allServerInfo = new ArrayList<>();
    ArrayList<ArrayList<String>> sortOrder = new ArrayList<>();
    boolean obtainServerData = true; // Enable request for server details

    public static void main(String args[]) {

        // Process program arguments
        for(int i = 0; i < args.length; i++) {

            if(args[i].equals("-m")) { manualInput = true; } // Enable manual input from user

            else if(args[i].equals("-h")) {  help = true; } // Display program usage

            else if(args[i].equals("-a")) { // Specify algorithm to be used

                if(args[i+1].equals("ff"))
                    algorithm = 1;
                else if(args[i+1].equals("bf"))
                    algorithm = 2;
                else if(args[i+1].equals("wf"))
                    algorithm = 3;
                else {
                    System.out.println("Please enter a valid algorithm.");
                    help = true;
                }

                i++;

            }

        }

        if(help)
            clientUsage();
        else {
            Client client = new Client("127.0.0.1", 8096);
        }

    }

    // initialize socket and input output streams
    private Socket socket               = null;
    private BufferedReader in           = null;
    private DataOutputStream out        = null;

    public Client(String address, int port) {
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

        // Manual input of commands.
        if(manualInput) {

            // Scanner initialisation for user manual input
            Scanner in = new Scanner(new InputStreamReader(System.in));
            String currentCommand = "";

            while(!currentCommand.equals("QUIT")) {

                // Enter and send a command to the Server.
                currentCommand = in.nextLine();
                sendCommand(currentCommand);

            }

        }

        // Automated input of commands.
        else {

            // Say Hello and sign in to Server.
            ClientSetup();

            // Schedule all jobs based on default algorithm unless specified otherwise.
            ClientScheduler();

            // Close connection to server once all jobs have been scheduled.
            ClientQuit();

        }

        try {

            out.close();
            socket.close();
            in.close();

        } catch(IOException i) { System.out.println(i); }

    }

    // DONE 1 Separate program into three parts: ClientSetup(), ClientBody() & ClientExit().
    // DONE 2 Remove processServerResponse once finished.

    public void ClientSetup() {

        // Say Hello and sign in
        sendCommand("HELO");
        sendCommand("AUTH "+System.getProperty("user.name"));

    }

    public void ClientScheduler() {

        String currentJob = sendCommand("REDY");
        String[] currentJobDetails = currentJob.split(" ");
        String[] status = {""};

        // Find largest server
        RESCAll();
        int indexOfLargestServer = 0;
        for(int i = 0; i < allServerInfo.size(); i++) {
            if( Integer.parseInt(allServerInfo.get(indexOfLargestServer).get(4)) < Integer.parseInt(allServerInfo.get(i).get(4)) )
                indexOfLargestServer = i;
        }

        //
        findAllServerInfoSortOrder();

        while(!currentJob.equals("NONE") && !status[0].equals("ERR:")) {

            currentJobDetails = currentJob.split(" ");
            String serverType = "";
            String serverID = "";

            // Collect information on all servers
            RESCAll();

            // AllToLargest
            if(algorithm == 0) {
                serverType = allServerInfo.get(indexOfLargestServer).get(0);
                serverID = "0";
            }

            // First-Fit
            else if(algorithm == 1) {



                // Sort Servers by type from smallest to largest
//                Collections.sort(allServerInfo, new Comparator<ArrayList<String>>() {
//                    @Override
//                    public int compare(ArrayList<String> o1, ArrayList<String> o2) {
//
//                        // Do not sort if both servers are the same type
//                        if(o1.get(0).equals(o2.get(0))) {
//                            if(Integer.parseInt(o1.get(1)) < Integer.parseInt(o2.get(1))) {
//                                return -1;
//                            } else if(Integer.parseInt(o1.get(1)) < Integer.parseInt(o2.get(1))) {
//                                return 1;
//                            } else
//                                return 0;
//                        }
//
//
//                       if(Integer.parseInt(o1.get(4)) < Integer.parseInt(o2.get(4))){ // Otherwise sort by core count
//                            return -1;
//                        } else if (Integer.parseInt(o1.get(4)) > Integer.parseInt(o2.get(4))){
//                            return 1;
//                        } else {
//                            return 0;
//                        }
//
//                    }
//                });

                sortAllServerInfo();


                ArrayList<String> firstFitServer = findFirstFit(currentJobDetails);
                serverType = firstFitServer.get(0);
                serverID = firstFitServer.get(1);

                System.out.println(firstFitServer);

            }

            // Best-Fit
            else if(algorithm == 2) {

                ArrayList<String> bestFitServer = findBestFit(currentJobDetails);
                serverType = bestFitServer.get(0);
                serverID = bestFitServer.get(1);

            }

            // Worst-Fit
            else if (algorithm == 3) {

                ArrayList<String> worstFitServer = findWorstFit(currentJobDetails);
                serverType = worstFitServer.get(0);
                serverID = worstFitServer.get(1);

            }

            // Run the job
            status = sendCommand("SCHD " +
                            currentJobDetails[2] + " " +
                                serverType + " " +
                                    serverID).split(" ");

            for(ArrayList<String> server: allServerInfo) {
                if(server.get(0).equals(serverType) && server.get(1).equals(serverID))
                    System.out.println(server);
            }

            // Goto next job
            currentJob = sendCommand("REDY");

        }

        for(ArrayList<String> x: sortOrder) {
            System.out.println(x);
        }

    }

    public void ClientQuit() { sendCommand("QUIT"); }

    // Client Scheduler Algorithms

    /**
     * First-Fit Algorithm
     * @return the first active server with sufficient initial resource capacity to run the job
     */
    public ArrayList<String> findFirstFit(String[] currentJob) {

        // DONE 1 Sort servers from smallest to largest
        // DONE 2 Traverse through all servers and select the first server that has equal or more cores than the job.

        for(int i = 0; i < allServerInfo.size(); i++) {

            // Compare number of cores required for the job against the server's number of cores.
            if(Integer.parseInt(currentJob[4]) <= Integer.parseInt(allServerInfo.get(i).get(4))) {
                return allServerInfo.get(i);
            }

        }

        // No server was found
        return null;

    }

    /**
     * Best-Fit Algorithm
     * @return the best-fit active server based on initial resource capacity
     */
    public ArrayList<String> findBestFit(String[] currentJob) {

        System.out.println("Please define best-fit algorithm.");

        return null;

    }

    /**
     * Worst-Fit Algorithm
     * @return the worst-fit active server based on resource capacity
     */
    public ArrayList<String> findWorstFit(String[] currentJob) {

        System.out.println("Please define worst-fit algorithm.");

        return null;

    }

    /**
     * Resource Information Request
     *  RESC All - The informaton of all servers, in the system, regardless of their state.
     */
    public void RESCAll() {

        allServerInfo = new ArrayList<>(); // Delete old information for new data
        sendCommandNoLog("RESC All"); // Expected Response is "DATA"

        String temp = sendCommandNoLog("OK");
        while(!temp.equals(".")) {

            // Store data for each server into array
            ArrayList<String> server = new ArrayList<>();
            String[] tempServer = temp.split(" ");
            for(String serverDetail: tempServer) {
                server.add(serverDetail);
            }

            // Add server to list
            allServerInfo.add(server);

            // Get next server
            temp = sendCommandNoLog("OK");

        }

    }

    public void sortAllServerInfo() {

        ArrayList<ArrayList<String>> temp = new ArrayList<>();

        for(int i = 0; i < sortOrder.size(); i++) {

            for(int j = 0; j < allServerInfo.size(); j++) {

                if(sortOrder.get(i).get(0).equals(allServerInfo.get(j).get(0)))
                    temp.add(allServerInfo.get(j));

            }

        }

        allServerInfo = temp;

    }

    public void findAllServerInfoSortOrder() {

        for(int i = 0; i < allServerInfo.size(); i++) {

            if(!isServerTypeInList(allServerInfo.get(i).get(0))) {
                addServerType(allServerInfo.get(i).get(0), allServerInfo.get(i).get(4));
            }

        }

    }

    public int addServerType(String serverType, String coreCount) {

        ArrayList<String> temp = new ArrayList<>();
        temp.add(serverType);
        temp.add(coreCount);

        for(int i = 0; i < sortOrder.size(); i++) {
            if(Integer.parseInt(coreCount) < Integer.parseInt(sortOrder.get(i).get(1))) {
                sortOrder.add(i, temp);
                return 0;
            }
        }

        sortOrder.add(temp);
        return 0;

    }

    // Helper method for sortAllServerInfoAlphabetically()
    public boolean isServerTypeInList(String otherServerType) {

        for(ArrayList<String> server: sortOrder) {

            if(server.get(0).equals(otherServerType))
                return true;

        }

        return false;

    }

    /**
     * Send a command to the server and display the result in the terminal.
     *  e.g. sendCommand("HELO");
     *
     *  Client Terminal
     *      SENT: HELO
     *      RCVD: OK
     *
     * @param argument - the command to be send.
     * @return String - the reply from the server.
     */
    public String sendCommand(String argument){

        try {

            // Send the command to the server
            System.out.println("SENT: " +argument);
            out.write((argument+"\n").getBytes());

            // Read and return response from the server.
            String serverResponse = in.readLine();
            System.out.println("RCVD: "+serverResponse);
            return serverResponse;

        } catch(IOException i) { System.out.println(i); }

        return "ERR: No Response from Server.";

    }

    /**
     * Sometimes we want to send a commmand and not display it in the terminal because it is not important to the user.
     * This function works exactly the same as the above but without any logging.
     * @param argument - the command to be send.
     * @return String - the reply from the server.
     */
    public String sendCommandNoLog(String argument) {

        try {

            // Send the command to the server
            out.write((argument+"\n").getBytes());

            // Return response from the server.
            return in.readLine();

        } catch(IOException i) { System.out.println(i); }

        return "ERR: No Response from Server.";

    }

    public static void clientUsage() {

        System.out.println("ds-sim COMP335@MQ, S1-27Apr, 2019");
        System.out.println("Usage:");
        System.out.println("    java Client [-h] [-m] [-a]");

    }

}
