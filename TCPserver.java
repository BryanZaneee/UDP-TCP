import java.io.*;
import java.net.*;

public class TCPserver {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java TCPserver <port>");
            return;
        }

        int PORT = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Continuously accept new client connections
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server> Connected to client from " + socket.getInetAddress().getHostAddress());
                
                // Delegate the client handling to a new thread
                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Dedicated thread to handle client communication
    private static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                String text;

                // Handle incoming messages until "bye" is received
                while ((text = input.readUTF()) != null) {
                    if ("bye".equalsIgnoreCase(text)) {
                        System.out.println("Server> Disconnection requested by client.");
                        break;
                    }

                    try {
                        int memeNumber = Integer.parseInt(text);
                        String memeFile = "memes/meme" + memeNumber + ".jpg";
                        File file = new File(memeFile);

                        // Check file existence and send data
                        if (file.exists()) {
                            // Read and send file content
                            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                                byte[] memeData = new byte[(int) file.length()];
                                fileInputStream.read(memeData);

                                output.writeUTF(memeFile); // Optionally used by client
                                output.writeInt(memeData.length); // Crucial for client data handling
                                output.write(memeData);
                                output.flush();
                            }
                            System.out.println("Server> Sent meme: " + memeFile);
                        } else {
                            // Inform client of missing file
                            System.out.println("Server> Meme not found: " + memeFile);
                            output.writeUTF("File not found");
                            output.writeInt(0);
                        }
                    } catch (NumberFormatException e) {
                        // Handle invalid meme number format
                        output.writeUTF("Invalid request");
                        output.writeInt(0);
                        System.out.println("Server> Received invalid meme request: " + text);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server> Communication error: " + ex.getMessage());
            } finally {
                // Ensure socket closure on thread end
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("Server> Error closing socket: " + ex.getMessage());
                }
            }
        }
    }
}
