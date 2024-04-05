import java.io.*;
import java.net.*;

public class TCPserver {

    public static void main(String[] args) {
        // Check if the port number is provided as an argument
        if (args.length != 1) {
            System.out.println("Usage: java TCPserver <port>");
            return;
        }

        // Parse the port number from the command-line arguments
        int PORT = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Continuously listen for client connections
            while (true) {
                Socket socket = serverSocket.accept();  // Accept a client connection
                System.out.println("Server> Got connection request from " + socket.getInetAddress().getHostAddress());
                // Start a new thread to handle the client connection
                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Inner class for handling client connections
    private static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;  // Store the socket connection for this client
        }

        @Override
        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                String text;
                // Read messages from the client until 'bye' is received
                while ((text = input.readUTF()) != null) {
                    if ("bye".equalsIgnoreCase(text)) {
                        System.out.println("Server> Client disconnected.");
                        break;
                    }

                    // Parse the requested meme number and prepare the file path
                    int memeNumber = Integer.parseInt(text);
                    String memeFile = "memes/meme" + memeNumber + ".jpg";
                    File file = new File(memeFile);

                    // Check if the file exists and send it to the client
                    if (file.exists()) {
                        long startTime = System.nanoTime();
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] memeData = new byte[(int) file.length()];
                            fileInputStream.read(memeData);
                            output.writeUTF(memeFile);
                            output.writeInt(memeData.length);
                            output.write(memeData);
                            output.flush();
                        }
                        long endTime = System.nanoTime();
                        // Log the time taken to access and send the file
                        System.out.println("Server> File sent: " + memeFile + ". Access time: " + (endTime - startTime) + " ns.");
                    } else {
                        System.out.println("Server> File not found: " + memeFile);
                        output.writeUTF("File not found");
                        output.writeInt(0);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server> Connection error: " + ex.getMessage());
            } finally {
                try {
                    // Close the socket when done
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    System.out.println("Server> Error closing socket: " + ex.getMessage());
                }
            }
        }
    }
}
