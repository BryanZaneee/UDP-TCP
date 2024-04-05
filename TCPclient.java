import java.io.*;
import java.net.*;
import java.util.Random;

public class TCPclient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TCPclient <server address> <server_port>");
            return;
        }

        String SERVER_ADDRESS = args[0];
        int SERVER_PORT = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            System.out.println("Client> Connected to the server.");

            Random random = new Random();
            long[] roundTripTimes = new long[10];

            for (int i = 0; i < 10; i++) {
                int memeNumber = random.nextInt(10) + 1;
                String userInput = String.valueOf(memeNumber);

                long startTime = System.nanoTime();

                output.writeUTF(userInput);
                output.flush();

                byte[] memeData = new byte[1024 * 1024];
                int bytesRead = input.read(memeData);

                long endTime = System.nanoTime();
                roundTripTimes[i] = endTime - startTime;

                if (bytesRead > 0) {
                    String memeFilename = "received_meme_" + memeNumber + ".jpg";
                    FileOutputStream fileOutput = new FileOutputStream(memeFilename);
                    fileOutput.write(memeData, 0, bytesRead);
                    fileOutput.close();
                    System.out.println("Client> Received meme: " + memeFilename);
                } else {
                    System.out.println("Client> Received empty response.");
                }
            }

            output.writeUTF("bye");
            output.flush();

            long minRoundTrip = min(roundTripTimes);
            long maxRoundTrip = max(roundTripTimes);
            double meanRoundTrip = mean(roundTripTimes);
            double stdDevRoundTrip = stdDev(roundTripTimes);

            System.out.println("\nRound Trip Time Statistics (in nanoseconds):");
            System.out.println("Minimum: " + minRoundTrip);
            System.out.println("Maximum: " + maxRoundTrip);
            System.out.println("Mean: " + meanRoundTrip);
            System.out.println("Standard Deviation: " + stdDevRoundTrip);

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
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }
}