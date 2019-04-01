import java.io.*;
import java.util.*;
import javax.xml.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {

        if(args.length != 1)
            throw new RuntimeException("The name of the XML file is required.");

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(new File(args[0])));



        while(reader.hasNext()) {

            int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT: {

                    System.out.print("node: ");
                    System.out.print(reader.getLocalName());
                    for(int i = 0; i < reader.getAttributeCount(); i++)
                        System.out.print(" attr: "+reader.getAttributeName(i)+"=\""+reader.getAttributeValue(i)+"\"");
                    System.out.println();

                }
            }

        }

    }

}
