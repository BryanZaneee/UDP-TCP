import java.io.*;
import java.net.*;

public class UDPserver {
    private static final int PORT = 5927;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String text = new String(receivePacket.getData(), 0, receivePacket.getLength());

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Handle "bye" command
                if (text.equalsIgnoreCase("bye")) {
                    String response = "disconnected";
                    sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                    System.out.println("Server> Client disconnected.");
                    continue;
                }

                try {
                    // Parse joke number and send corresponding meme image
                    int jokeNumber = Integer.parseInt(text);
                    String jokeFile = "memes/meme" + jokeNumber + ".jpg";
                    File file = new File(jokeFile);
                    if (file.exists()) {
                        // Read meme image file and send it to the client
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] imageData = new byte[(int) file.length()];
                        fileInputStream.read(imageData);
                        fileInputStream.close();
                        DatagramPacket sendPacket = new DatagramPacket(imageData, imageData.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);
                        System.out.println("Server> Client requested: \"Meme " + jokeNumber + "\", returning: \"" + jokeFile + "\" file");
                    } else {
                        String response = "Meme not found.";
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);
                        System.out.println("Server> Invalid meme request received.");
                    }
                } catch (NumberFormatException | IOException e) {
                    String response = "Invalid input.";
                    sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                    System.out.println("Server> Invalid input received.");
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}