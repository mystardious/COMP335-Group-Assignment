import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    // User Arguments
    static boolean manualInput = false; // Allow manual input of commands
    static boolean help        = false; // Display program usage

    // Automation variables
    static int algorithm = 0; // 0 = AllToLargest, 1 = First-Fit, 2 = Best-Fit, 3 = Worst-Fit

    // Server data
    ArrayList<ArrayList<String>> allServerInfo = new ArrayList<>();
    ArrayList<ArrayList<String>> initialAllServerInfo = new ArrayList<>();

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

        // Save an initial copy of all server info
        RESCAll();
        initialAllServerInfo = allServerInfo;

        // Save another copy so that data in both ArrayLists are not linked
        RESCAll();
        int indexOfLargestServer = 0;
        for(int i = 0; i < allServerInfo.size(); i++) {
            if( Integer.parseInt(allServerInfo.get(indexOfLargestServer).get(4)) < Integer.parseInt(allServerInfo.get(i).get(4)) )
                indexOfLargestServer = i;
        }

        while(!currentJob.equals("NONE")) {

            String[] currentJobDetails = currentJob.split(" ");
            String serverType = "";
            String serverID = "";

            // Collect information on all servers
            RESCAll();

            // AllToLargest
            if (algorithm == 0) {
                serverType = allServerInfo.get(indexOfLargestServer).get(0);
                serverID = "0";
            }

            // First-Fit
            else if (algorithm == 1) {

                ArrayList<String> firstFitServer = findFirstFit(currentJobDetails);
                serverType = firstFitServer.get(0);
                serverID = firstFitServer.get(1);

            }

            // Best-Fit
            else if (algorithm == 2) {

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
            sendCommand("SCHD " +
                            currentJobDetails[2] + " " +
                                serverType + " " +
                                    serverID);

            // Goto next job
            currentJob = sendCommand("REDY");


        }

    }

    public void ClientQuit() { sendCommand("QUIT"); }

    // Client Scheduler Algorithms

    /**
     * First-Fit Algorithm
     * @return the first active server with sufficient initial resource capacity to run the job
     */
    public ArrayList<String> findFirstFit(String[] currentJob) {

        System.out.println("Please define first-fit algorithm.");

        return null;

    }

    /**
     * Best-Fit Algorithm
     * @return the best-fit server or if none have sufficient resources, return the best-fit active server based on initial resources
     *
     * A server is the best fit when the difference between the server's number of cores and the jobs required cores is
     * the smallest.
     *
     *  e.g.    Anything not important has been replaced with the letter x
     *
     *  Some Servers
     *          server_type server_ID server_state available_time #CPU_cores memory disk_space
     *          xlarge,     x,        x,           x,             16,        x,     x
     *          large,      x,        x,           x,             8,         x,     x
     *
     *  The current job
     *                submit_time job_ID estimated_runtime #CPU_cores memory disk_space
     *          JOBN, x,          x,     x,                7,         x,     x
     *
     *  The fitness value of the two severs xlarge and large are:
     *          xlarge: 9
     *          large:  1
     *
     *  The server with the best fit is the one with the lowest fitness value which is the large server. So the job should
     *  be scheduled to large. However if there are no servers that have sufficient resources to run the job then it is
     *  assigned to the server with the best fit based on the first RESCAll() call. Which will use the first server of every
     *  type depending on the job's required cores.
     *
     */
    public ArrayList<String> findBestFit(String[] currentJob) {

        // DONE 1 Create helper method for finding if a server has sufficient resource to process a job
        // DONE 2 Create helper method for finding fitness value
        // DONE 3 Write note for definition of best-fit
        // DONE 4 Based on note complete the findBestFit() method.

        ArrayList<String> bestFitServer = findBestFitServer(allServerInfo, currentJob);

        if(bestFitServer != null)
            return bestFitServer;
        else
            return findBestFitServer(initialAllServerInfo, currentJob);

    }

    /**
     * Worst-Fit Algorithm
     * @return the worst-fit active server based on resource capacity
     */
    public ArrayList<String> findWorstFit(String[] currentJob) {

        System.out.println("Please define worst-fit algorithm.");

        return null;

    }

    // Algorithm Helper Methods

    /**
     * Find the server with the closest number of cores to the job with sufficient resources (must have more cores than job)
     * @return Best Fit Server based on list of server data
     */
    public ArrayList<String> findBestFitServer(ArrayList<ArrayList<String>> serverList, String[] currentJob) {

        int bestFit = Integer.MAX_VALUE, minAvail = Integer.MAX_VALUE;
        ArrayList<String> bestFitServer = null;

        for(ArrayList<String> server: serverList) {

            if(hasSufficientResources(server, currentJob)) {

                int fitnessValue = calculateFitnessValue(server, currentJob);
                int serverAvailTime = Integer.parseInt(server.get(3));

                if( (fitnessValue < bestFit) || (fitnessValue == bestFit && serverAvailTime < minAvail) ) {

                    bestFit = fitnessValue;
                    minAvail = serverAvailTime;
                    bestFitServer = server;

                }

            }

        }

        return bestFitServer;

    }

    /**
     * Does this server have sufficient resources?
     * @return true if the server has equal or more cores than the job requires
     */
    public boolean hasSufficientResources(ArrayList<String> server, String[] currentJob) {

        int noRequiredCores = Integer.parseInt(currentJob[4]);
        int serverCores = Integer.parseInt(server.get(4));

        if(noRequiredCores <= serverCores)
            return true;

        return false;

    }

    /**
     * Calculate fitness value of a job to a server
     * @return the difference between the number of cores the job requires and that in the server
     */
    public int calculateFitnessValue(ArrayList<String> server, String[] currentJob) {

        int noRequiredCores = Integer.parseInt(currentJob[4]);
        int serverCores = Integer.parseInt(server.get(4));

        return serverCores - noRequiredCores;

    }

    // Client Command Methods

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

    // Client Usage

    public static void clientUsage() {

        System.out.println("ds-sim COMP335@MQ, S1-27Apr, 2019");
        System.out.println("Usage:");
        System.out.println("    java Client [-h] [-m] [-a]");

    }

}
