import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
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

            // Perform 10 measurements randomly
            Random random = new Random();
            long[] roundTripTimes = new long[10];
            long[] dnsLookupTimes = new long[10];

            for (int i = 0; i < 10; i++) {
                // Generate a random meme number between 1 and 10
                int memeNumber = random.nextInt(10) + 1;
                String userInput = String.valueOf(memeNumber);

                long startTime = System.nanoTime();

                // Measure DNS lookup time
                long dnsStartTime = System.nanoTime();
                InetAddress.getByName(SERVER_ADDRESS);
                long dnsEndTime = System.nanoTime();
                dnsLookupTimes[i] = dnsEndTime - dnsStartTime;

                byte[] sendData = userInput.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                long endTime = System.nanoTime();
                roundTripTimes[i] = endTime - startTime;

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Client> Received from server: \"" + response + "\"");
            }

            // Send "bye" command to server
            String byeCommand = "bye";
            byte[] sendData = byeCommand.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            // Receive server's response
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if ("disconnected".equalsIgnoreCase(response)) {
                System.out.println("Client> exit");
            }

            // Calculate and display statistics
            long minRoundTrip = min(roundTripTimes);
            long maxRoundTrip = max(roundTripTimes);
            double meanRoundTrip = mean(roundTripTimes);
            double stdDevRoundTrip = stdDev(roundTripTimes);

            // Calculate and display DNS lookup statistics
            long minDnsLookup = min(dnsLookupTimes);
            long maxDnsLookup = max(dnsLookupTimes);
            double meanDnsLookup = mean(dnsLookupTimes);
            double stdDevDnsLookup = stdDev(dnsLookupTimes);

            // Display statistics
            System.out.println("\nRound Trip Time Statistics (in nanoseconds):");
            System.out.println("Minimum: " + minRoundTrip);
            System.out.println("Maximum: " + maxRoundTrip);
            System.out.println("Mean: " + meanRoundTrip);
            System.out.println("Standard Deviation: " + stdDevRoundTrip);

            // Display DNS lookup statistics
            System.out.println("\nDNS Lookup Time Statistics (in nanoseconds):");
            System.out.println("Minimum: " + minDnsLookup);
            System.out.println("Maximum: " + maxDnsLookup);
            System.out.println("Mean: " + meanDnsLookup);
            System.out.println("Standard Deviation: " + stdDevDnsLookup);

        } catch (UnknownHostException ex) {
            System.out.println("Client> Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Client> I/O error: " + ex.getMessage());
        }
    }

    // Helper methods for calculating min
    private static long min(long[] values) {
        long min = Long.MAX_VALUE;
        for (long value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }
    
    // Helper methods for calculating max
    private static long max(long[] values) {
        long max = Long.MIN_VALUE;
        for (long value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    // Helper methods for calculating mean
    private static double mean(long[] values) {
        double sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    // Helper methods for calculating standard deviation
    private static double stdDev(long[] values) {
        double mean = mean(values);
        double sumSquaredDiff = 0;
        for (long value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }
}