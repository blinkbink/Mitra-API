package id.co.keriss.consolidate.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class ExtractXml {
	static int i;
	static String arr[];
	
	public int doExtract(String param, String[] arr_result) {
		System.out.println("Function do Extract XML...");
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		byte buff[] = param.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(buff);
		BufferedInputStream f = new BufferedInputStream(in);
		int j = 0;
		int k = 0;
		
		i = 0;
		arr = new String[255];
		Arrays.fill(arr, new String());
				
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new BufferedInputStream(f));
			int eventType = reader.getEventType();
			while (reader.hasNext()) {
				eventType = reader.next();
			    printText(reader);
			}
			System.out.println("i array : "+i);
			for(j = 0; j < i; j++) {
                System.out.println("arr["+j+"] : "+arr[j]);
                if(arr[j].charAt(0) != '\n') {
                    arr_result[k] = arr[j];
                    //System.out.println("arr_result["+k+"] : "+arr_result[k]);
                    k++;
                }
            }
			
            /*while(arr[j] != null) {
                if(arr[j].charAt(1) != ' ') {
                    //System.out.println("arr["+j+"] : "+arr[j]);
                    arr_result[k] = arr[j];
                    System.out.println("arr_result["+k+"] : "+arr_result[k]);
                    k++;
                }
                j++;
                
           }*/
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;
	}
	
	public static final String getEventTypeString(int eventType) {
        switch (eventType) {
        case XMLStreamConstants.START_ELEMENT:
            return "START_ELEMENT";

        case XMLStreamConstants.END_ELEMENT:
            return "END_ELEMENT";

        case XMLStreamConstants.PROCESSING_INSTRUCTION:
            return "PROCESSING_INSTRUCTION";

        case XMLStreamConstants.CHARACTERS:
            return "CHARACTERS";

        case XMLStreamConstants.COMMENT:
            return "COMMENT";

        case XMLStreamConstants.START_DOCUMENT:
            return "START_DOCUMENT";

        case XMLStreamConstants.END_DOCUMENT:
            return "END_DOCUMENT";

        case XMLStreamConstants.ENTITY_REFERENCE:
            return "ENTITY_REFERENCE";

        case XMLStreamConstants.ATTRIBUTE:
            return "ATTRIBUTE";

        case XMLStreamConstants.DTD:
            return "DTD";

        case XMLStreamConstants.CDATA:
            return "CDATA";

        case XMLStreamConstants.SPACE:
            return "SPACE";
        }

        return "UNKNOWN_EVENT_TYPE , " + eventType;
    }

    private static void printEventType(int eventType) {
        System.out.println("EVENT TYPE(" + eventType + ") = " + getEventTypeString(eventType));
    }

    private static void printStartDocument(XMLStreamReader xmlr) {
        if (XMLStreamConstants.START_DOCUMENT == xmlr.getEventType()) {
            System.out.println(
                    "<?xml version=\"" + xmlr.getVersion() + "\""
                    + " encoding=\"" + xmlr.getCharacterEncodingScheme() + "\""
                    + "?>");
        }
    }

    private static void printComment(XMLStreamReader xmlr) {
        if (xmlr.getEventType() == XMLStreamConstants.COMMENT) {
            System.out.print("<!--" + xmlr.getText() + "-->");
        }
    }

    private static void printText(XMLStreamReader xmlr) {

        if (xmlr.hasText()) {
            //System.out.print(xmlr.getText());
            arr[i] = xmlr.getText();
            i++;
        }
    }

    private static void printPIData(XMLStreamReader xmlr) {
        if (xmlr.getEventType() == XMLStreamConstants.PROCESSING_INSTRUCTION) {
            System.out.print(
                    "<?" + xmlr.getPITarget() + " " + xmlr.getPIData() + "?>");
        }
    }

    private static void printStartElement(XMLStreamReader xmlr) {
        if (xmlr.isStartElement()) {
            System.out.print("<" + xmlr.getName().toString());
            printAttributes(xmlr);
            System.out.print(">");
        }
    }

    private static void printEndElement(XMLStreamReader xmlr) {
        if (xmlr.isEndElement()) {
            System.out.print("</" + xmlr.getName().toString() + ">");
        }
    }

    private static void printAttributes(XMLStreamReader xmlr) {
        int count = xmlr.getAttributeCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                System.out.print(" ");
                System.out.print(xmlr.getAttributeName(i).toString());
                System.out.print("=");
                System.out.print("\"");
                System.out.print(xmlr.getAttributeValue(i));
                System.out.print("\"");
            }
        }

        count = xmlr.getNamespaceCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                System.out.print(" ");
                System.out.print("xmlns");

                if (xmlr.getNamespacePrefix(i) != null) {
                    System.out.print(":" + xmlr.getNamespacePrefix(i));
                }

                System.out.print("=");
                System.out.print("\"");
                System.out.print(xmlr.getNamespaceURI(i));
                System.out.print("\"");
            }
        }
    }
}
