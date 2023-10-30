import java.util.concurrent.*;

public class Application {

    public static void main(String[] args) {

        String dataType = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        String toReplace = args[3];
        String replacement = args[4];

        int numThreads = 5;
        FileProcessor fileProcessor ;
        if ("xml".equals(dataType)) {
            fileProcessor = new FileProcessorXml();
            fileProcessor.process(numThreads, inputFile, outputFile, toReplace, replacement);
        } else if ("txt".equals(dataType)) {
            fileProcessor = new FileProcessorText();
            fileProcessor.process(numThreads, inputFile, outputFile, toReplace, replacement);
        }
    }

}
