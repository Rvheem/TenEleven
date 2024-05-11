package Admin;

import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Facerecog {

    public static void embedFace(String imagePath) {
        try {
            String embeddingCommand = "java -jar C:\\PROJECTS\\face-recognition-java\\cli\\target\\face-recognition-cli-0.3.1.jar embed -p " +
                    imagePath + " -e C:\\PROJECTS\\face-recognition-java\\embeddings.dat";
            executeCommand(embeddingCommand);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String[] recognizeFace(String testImagePath) {
        try {
            // Execute prediction command to generate result
            String predictCommand = "java -jar C:\\PROJECTS\\face-recognition-java\\cli\\target\\face-recognition-cli-0.3.1.jar predict -e C:\\PROJECTS\\face-recognition-java\\embeddings.dat"
                    + " -p " + testImagePath;
            executeCommand(predictCommand);

            // Read the contents of result.txt
            String resultFilePath = "C:\\PROJECTS\\result.txt";
            StringBuilder predictResult = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(resultFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    predictResult.append(line);
                }
            }

            // Split the contents of result.txt to extract name and probability
            String[] parts = predictResult.toString().split(" - ");
            if (parts.length == 3) {
                return new String[]{parts[0], parts[1]};
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String executeCommand(String command) throws IOException, InterruptedException {
    // Split the command string by spaces
    String[] commandParts = command.split("\\s+");

    ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
        System.err.println("Command execution failed with exit code: " + exitCode);
    }

    return output.toString();
}


    public static void main(String[] args) {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Test face recognition
        String testImagePath = "C:\\PROJECTS\\cam.jpg"; // Path to a test image
        String[] recognizedName = recognizeFace(testImagePath);
        if (recognizedName != null) {
            System.out.println("Recognized face: " + recognizedName[0]);
        } else {
            System.out.println("Face recognition failed.");
        }
    }
}
