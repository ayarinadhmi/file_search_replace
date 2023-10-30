import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

public class FileProcessorXml implements FileProcessor {
    public   FileProcessorXml(){

    }
    public void process( int numThreads  ,String inputFile,String outputFile,String textToReplace,String toPut) {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream(inputFile));
            BlockingQueue<String> currentChunk = new LinkedBlockingQueue<>();
            BlockingQueue<String> mergedChunks = new LinkedBlockingQueue<>();
            String f = processEvent(reader.getEventType(), reader,textToReplace,toPut);
            currentChunk.add(f);

            while (reader.hasNext()) {

                int event = reader.next();
                String fragment = processEvent(event, reader,textToReplace,toPut);
                currentChunk.add(fragment);
                if (currentChunk.size() > 10000) {
                    BlockingQueue<String> finalCurrentChunk = currentChunk;
                    executor.execute(() -> {
                        mergedChunks.add(processAndMergeChunk(finalCurrentChunk));
                    });
                    currentChunk = new LinkedBlockingQueue<>();
                }
            }
            BlockingQueue<String> finalCurrentChunk1 = currentChunk;
            executor.execute(() -> {
                mergedChunks.add(processAndMergeChunk(finalCurrentChunk1));
            });

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            writeMergedChunksToFile(mergedChunks, outputFile);
        } catch (XMLStreamException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String processEvent(int event, XMLStreamReader reader, String textToReplace,String toPut) {
        switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                StringBuilder modifiedFragment = new StringBuilder("<" + reader.getLocalName());
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    String attrName = reader.getAttributeLocalName(i);
                    String attrValue = reader.getAttributeValue(i);
                    if ( attrValue.equals(textToReplace)) {
                        modifiedFragment.append(" " + attrName + "=\"" + toPut + "\"");
                    } else {
                        modifiedFragment.append(" " + attrName + "=\"" + attrValue + "\"");
                    }
                }
                modifiedFragment.append(">");
                return modifiedFragment.toString();
            case XMLStreamConstants.END_ELEMENT:
                return "</" + reader.getLocalName() + ">";
            case XMLStreamConstants.CHARACTERS:
                return reader.getText();
            case XMLStreamConstants .START_DOCUMENT:
                 return "<?xml version=" + reader.getVersion()+ " encoding="+ reader.getEncoding()+ "?>"    ;
        }
        return "";
    }

    private static String processAndMergeChunk(BlockingQueue<String> chunk) {
        StringBuilder mergedChunk = new StringBuilder();
        while (!chunk.isEmpty()) {
            mergedChunk.append(chunk.poll());
        }
        return mergedChunk.toString();
    }

    private static void writeMergedChunksToFile(BlockingQueue<String> mergedChunks, String outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            while (!mergedChunks.isEmpty()) {
                writer.write(mergedChunks.poll());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
