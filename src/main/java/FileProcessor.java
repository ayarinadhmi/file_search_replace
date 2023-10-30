import java.util.concurrent.ExecutorService;

public interface FileProcessor {
    void process(int numThreads,  String inputFile, String outputFile, String textToReplace, String toPut);

}
