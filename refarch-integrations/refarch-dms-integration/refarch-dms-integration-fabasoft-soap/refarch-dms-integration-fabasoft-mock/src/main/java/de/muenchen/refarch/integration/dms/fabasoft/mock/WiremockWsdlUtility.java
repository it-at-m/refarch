package de.muenchen.refarch.integration.dms.fabasoft.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.function.Predicate;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

public final class WiremockWsdlUtility {
    private WiremockWsdlUtility() {
    }

    /**
     * Accepts a WebService response object (as defined in the WSDL) and marshals
     * to a SOAP envelope String.
     */
    public static <T> String serializeObject(final T object) {
        final ByteArrayOutputStream byteArrayOutputStream;
        final Class clazz = object.getClass();
        final String responseRootTag = StringUtils.uncapitalize(clazz.getSimpleName());
        final QName payloadName = new QName("your_namespace_URI", responseRootTag, "namespace_prefix");

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Marshaller objectMarshaller = jaxbContext.createMarshaller();

            final JAXBElement<T> jaxbElement = new JAXBElement<>(payloadName, clazz, null, object);
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            objectMarshaller.marshal(jaxbElement, document);

            final SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            final SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
            body.addDocument(document);

            byteArrayOutputStream = new ByteArrayOutputStream();
            soapMessage.saveChanges();
            soapMessage.writeTo(byteArrayOutputStream);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception trying to serialize [%s] to a SOAP envelope", object), e);
        }

        return byteArrayOutputStream.toString();
    }

    /**
     * Accepts a WebService request object (as defined in the WSDL) and unmarshals
     * to the supplied type.
     */
    public static <T> T deserializeSoapRequest(final String soapRequest, final Class<T> clazz) {

        final XMLInputFactory xif = XMLInputFactory.newFactory();
        final JAXBElement<T> jb;
        try {
            final XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(soapRequest));

            // Advance the tag iterator to the tag after Body, eg the start of the SOAP payload object
            do {
                xsr.nextTag();
            } while (!"Body".equals(xsr.getLocalName()));
            xsr.nextTag();

            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            jb = unmarshaller.unmarshal(xsr, clazz);
            xsr.close();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to deserialize request to type: %s. Request \n %s", clazz, soapRequest), e);
        }

        return jb.getValue();
    }

    public static <T> void stubOperation(final String operation, final Class<T> clazz, final Predicate<T> predicate, final Object response) {
        stubFor(requestMatching(new SoapObjectMatcher<>(clazz, operation, predicate))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody(serializeObject(response))));
    }

    public static <T> void stubOperation(final WireMockServer server, final String operation, final Class<T> clazz, final Predicate<T> predicate,
            final Object response) {
        server.stubFor(requestMatching(new SoapObjectMatcher<>(clazz, operation, predicate))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody(serializeObject(response))));
    }
}
