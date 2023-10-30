import java.io.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessorText implements FileProcessor {


        public   FileProcessorText(){

        }
        public  void process( int numThreads ,String inputFile,String outputFile,String textToReplace,String toPut) {

        try {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            int chunkSize = 1000;
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                 final String finalLine = line;
                Future<String> future = executor.submit(() -> replaceText(finalLine, textToReplace, toPut));

                writer.write(future.get());
                writer.newLine();

                if (lineNumber % chunkSize == 0) {
                    executor.shutdown();
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    executor = Executors.newFixedThreadPool(numThreads);
                }
            }

            reader.close();
            writer.close();
            executor.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static String replaceText(String input, String searchPattern, String replacement) {
        Pattern pattern = Pattern.compile(searchPattern);
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll(replacement);
    }
}
