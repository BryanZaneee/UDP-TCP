import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPclient {
    private static final String SERVER_ADDRESS = "localhost"; // Server address
    private static final int SERVER_PORT = 5927; // Server port, matching the server's port

    // Initiates server connection, creates I/O streams, and manages user input.
    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // Initial connection message
            System.out.println("Client> Connected to the joke server. Enter a joke number (1-3) or type 'bye' to exit.");
            
            while (true) {
                System.out.print("Client> ");
                String userInput = scanner.nextLine(); // Read user input

                // Send user input to the server
                output.println(userInput); 

                if ("bye".equalsIgnoreCase(userInput)) {
                    // Handle server's response for "bye" command
                    String response = input.readLine(); 
                    if ("disconnected".equalsIgnoreCase(response)) {
                        System.out.println("Client> exit");
                    }
                    break; // Exit the loop to end client process
                }

                // Receive and print the response from the server
                String response = input.readLine();
                System.out.println("Client> Received from server: \"" + response + "\"");
            }

        } catch (UnknownHostException ex) {
            System.out.println("Client> Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Client> I/O error: " + ex.getMessage());
        }
    }
}
