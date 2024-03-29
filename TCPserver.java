import java.io.*;
import java.net.*;

public class TCPserver {
    private static final int PORT = 5927;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server> Got connection request from " + socket.getInetAddress().getHostAddress());
                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                String text;
                while ((text = input.readUTF()) != null) {
                    if (text.equalsIgnoreCase("bye")) {
                        System.out.println("Server> Client disconnected.");
                        break;
                    }

                    try {
                        int memeNumber = Integer.parseInt(text);
                        String memeFile = "memes/meme" + memeNumber + ".jpg";
                        File file = new File(memeFile);
                        if (file.exists()) {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            byte[] memeData = new byte[(int) file.length()];
                            fileInputStream.read(memeData);
                            fileInputStream.close();
                            output.write(memeData);
                            output.flush();
                            System.out.println("Server> Client requested: \"Meme " + memeNumber + "\", returning: \"" + memeFile + "\" file");
                        } else {
                            output.write(new byte[0]);
                            output.flush();
                            System.out.println("Server> Invalid meme request received.");
                        }
                    } catch (NumberFormatException | IOException e) {
                        output.write(new byte[0]);
                        output.flush();
                        System.out.println("Server> Invalid input received.");
                    }
                }

                socket.close();
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}