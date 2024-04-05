import java.io.*;
import java.net.*;
import java.util.Random;

public class UDPclient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java UDPClient <server address> <server_port>");
            return;
        }

        String SERVER_ADDRESS = args[0];
        int SERVER_PORT = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            System.out.println("Client> Connected to the meme server.");

            Random random = new Random();
            long[] roundTripTimes = new long[10];

            for (int i = 0; i < 10; i++) {
                int memeNumber = i + 1;
                String userInput = String.valueOf(memeNumber);
                byte[] sendData = userInput.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                
                long startTime = System.nanoTime();
                socket.send(sendPacket);

                // Prepare a buffer for receiving potentially large data
                byte[] receiveData = new byte[65507];  // Maximum UDP datagram size
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                long endTime = System.nanoTime();
                roundTripTimes[i] = endTime - startTime;

                // Write the received data (image) to a file
                try (FileOutputStream fos = new FileOutputStream("received_meme_" + memeNumber + ".jpg")) {
                    fos.write(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Client> Meme image received and saved: received_meme_" + memeNumber + ".jpg");
                } catch (IOException e) {
                    System.out.println("Client> Error saving the received meme image: " + e.getMessage());
                }
            }

            // Send a "bye" message to the server
            byte[] byeData = "bye".getBytes();
            DatagramPacket byePacket = new DatagramPacket(byeData, byeData.length, serverAddress, SERVER_PORT);
            socket.send(byePacket);

            // Await confirmation of disconnection
            byte[] byeResponse = new byte[1024];
            DatagramPacket byeReceivePacket = new DatagramPacket(byeResponse, byeResponse.length);
            socket.receive(byeReceivePacket);
            String response = new String(byeReceivePacket.getData(), 0, byeReceivePacket.getLength());
            if ("disconnected".equalsIgnoreCase(response.trim())) {
                System.out.println("Client> Disconnected from the server.");
            }

            // Output round-trip time statistics
            System.out.println("\nRound Trip Time Statistics (in nanoseconds):");
            System.out.println("Minimum: " + min(roundTripTimes));
            System.out.println("Maximum: " + max(roundTripTimes));
            System.out.println("Mean: " + mean(roundTripTimes));
            System.out.println("Standard Deviation: " + stdDev(roundTripTimes));

        } catch (UnknownHostException ex) {
            System.out.println("Client> Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Client> I/O error: " + ex.getMessage());
        }
    }

    private static long min(long[] values) {
        long min = Long.MAX_VALUE;
        for (long value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private static long max(long[] values) {
        long max = Long.MIN_VALUE;
        for (long value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private static double mean(long[] values) {
        double sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double stdDev(long[] values) {
        double mean = mean(values);
        double sumSquaredDiff = 0;
        for (long value : values) {
            sumSquaredDiff += (value - mean) * (value - mean);
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }
}
