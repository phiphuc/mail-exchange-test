package com.phiphuc.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.notification.PushSubscription;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.phiphuc.transaction.TransactionBo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/payment")
public class PaymentService {
    private final Log log = LogFactory.getLog(PaymentService.class);

    @Autowired
    TransactionBo transactionBo;

    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @GET
    @Path("/phiphuc")
    public Response savePayment() {
        log.debug("START FOLLOW MAIL EXCHANGE");
        try {
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
            service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
            ExchangeCredentials credentials = new WebCredentials("15dh110100@st.huflit.edu.vn", "Josemourinho26011963", "outlook.office365.com");
            service.setCredentials(credentials);
            WellKnownFolderName wkFolder = WellKnownFolderName.Inbox;
            FolderId folderId = new FolderId(wkFolder);
            List<FolderId> folder = new ArrayList<FolderId>();
            folder.add(folderId);

            URI callback = new URI("http://node11.codenvy.io:39367/rest/payment/incomingevent");
            log.debug("START SUBSCRIPTION MAIL EXCHANGE");
            PushSubscription pushSubscription = service.subscribeToPushNotifications(
                    folder,
                    callback /* The endpoint of the listener. */,
                    1/* Get a status event every 5 minutes if no new events are available. */, null  /* watermark: null to start a new subscription. */,
                    EventType.NewMail);
            log.debug("SUBSCRIPTION MAIL EXCHANGE SUCCESS ID: " + pushSubscription.getId() + " WATERMARK :" + pushSubscription.getWaterMark());
        } catch (Exception e) {
            log.debug("SUBSCRIPTION MAIL EXCHANGE ERROR ");
            e.printStackTrace();
        }
        String result = transactionBo.save();

        return Response.status(200).entity(result).build();

    }

    @Path("/incomingevent")
    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    public Response onNotificationReceived(@Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
        System.out.println("RECEIVED EWS NOTIFICATION SUCCESS");

        try {
            JSONObject xmlJSONObj = XML.toJSONObject(IOUtils.toString(request.getInputStream()));
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
        /*System.out.println("ROOT ELEMENT :" + doc.getDocumentElement().getNodeName());*/
        // Deserialize the document
        return Response.ok(null).build();
    }
    private Document loadXML(String rawXML) {
        Document doc = null;
        try {
            System.out.println("Incoming request input stream : " + rawXML);

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

            // turn on this flag in order to resolve manually the namespaces of the document
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            doc = (Document) builder.parse(new InputSource(new ByteArrayInputStream(rawXML.getBytes("UTF-8"))));
        } catch (ParserConfigurationException e) {
            System.out.println("Unable to create a new DocumentBuilder");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported Encoding: UTF-8");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Error parsing XML");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
        return doc;
    }
}