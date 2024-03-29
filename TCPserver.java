import java.io.*;
import java.net.*;

public class TCPserver {
    // Define the port number for the server
    private static final int PORT = 5927;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Continuously listen for client connections.
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server> Got connection request from " + socket.getInetAddress().getHostAddress());
                new ServerThread(socket).start();  // Handle each client connection in a separate thread (even tho we dont need to).
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ServerThread extends Thread {
        private Socket socket;

        // Constructor to assign the client's socket to a local variable.
        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        // This is the main logic of the server thread handling client communication.
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

                String text;
                while ((text = input.readLine()) != null) {
                    // Output formatting when the client sends a "bye" request.
                    if (text.equalsIgnoreCase("bye")) {
                        output.println("disconnected");
                        System.out.println("Server> Client disconnected.");
                        break;
                    }

                    // Try to parse the joke number and send back the corresponding joke.
                    try {
                        int jokeNumber = Integer.parseInt(text);
                        String jokeFile = "joke" + jokeNumber + ".txt";
                        File file = new File(jokeFile);
                        if (file.exists()) {
                            BufferedReader fileReader = new BufferedReader(new FileReader(file));
                            StringBuilder jokeContent = new StringBuilder();
                            String line;
                            while ((line = fileReader.readLine()) != null) {
                                jokeContent.append(line).append("\n");
                            }
                            fileReader.close();
                            output.println(jokeContent.toString().trim());  // Send the joke content to the client.
                            System.out.println("Server> Client requested: \"Joke " + jokeNumber + "\", returning: \"" + jokeFile + "\" file");
                        } else {
                            output.println("Joke not found.");
                            System.out.println("Server> Invalid joke request received.");
                        }
                    } catch (NumberFormatException | IOException e) {
                        output.println("Invalid input.");
                        System.out.println("Server> Invalid input received.");
                    }
                }

                socket.close(); // Close the connection to the client.
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
