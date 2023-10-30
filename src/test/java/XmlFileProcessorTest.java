import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlFileProcessorTest {

    @Test
    public void shouldProcesseElementAndReplaceText() throws XMLStreamException {

        //given
        int eventType = XMLStreamConstants.START_ELEMENT;
        XMLEventFactory factory = XMLEventFactory.newFactory();
        StartElement element = factory.createStartElement("test","","entry",
                getAttributes().iterator(),new ArrayList<Namespace>().iterator());
        XMLStreamReader reader = startElementToXMLStreamReader(element);
         //when
        String result = FileProcessorXml.processEvent(reader.next(),reader,"trace","error");

        /* then */
        assertEquals(result,"<entry test=\"notReplaced\" level=\"error\">");
     }
    private List<Attribute> getAttributes( ) {
        XMLEventFactory factory = XMLEventFactory.newFactory();
        List< Attribute> attributes = new ArrayList<>();
        attributes.add(factory.createAttribute("level","trace")  );
        attributes.add(factory.createAttribute("test","notReplaced")  );
        return attributes;
    }
    private   XMLStreamReader startElementToXMLStreamReader(StartElement startElement) throws XMLStreamException {
        // Create an XMLStreamReader from the StartElement
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String startElementXML = startElement.toString();
        return inputFactory.createXMLStreamReader(new StringReader(startElementXML));
    }

}
