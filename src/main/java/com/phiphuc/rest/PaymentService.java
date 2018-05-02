package com.phiphuc.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.notification.PushSubscription;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.phiphuc.transaction.TransactionBo;
import sun.rmi.transport.tcp.TCPEndpoint;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/payment")
public class PaymentService {

    @Autowired
    TransactionBo transactionBo;

    private TCPEndpoint tcpEndpoint;

    @GET
    @Path("/phiphuc")
    public Response savePayment() {
        try {
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
            service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
            ExchangeCredentials credentials = new WebCredentials("15dh110100@st.huflit.edu.vn", "Josemourinho26011963", "outlook.office365.com");
            service.setCredentials(credentials);
            WellKnownFolderName wkFolder = WellKnownFolderName.Inbox;
            FolderId folderId = new FolderId(wkFolder);
            List<FolderId> folder = new ArrayList<FolderId>();
            folder.add(folderId);

            URI callback = new URI("http://localhost:8082/rest/payment/incomingevent");

            PushSubscription pushSubscription = service.subscribeToPushNotifications(
                    folder,
                    callback /* The endpoint of the listener. */,
                    1/* Get a status event every 5 minutes if no new events are available. */, null  /* watermark: null to start a new subscription. */,
                    EventType.NewMail);
            System.out.println("PushSubscription = " + pushSubscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = transactionBo.save();

        return Response.status(200).entity(result).build();

    }

    @Path("/incomingevent")
    @POST()
    @Produces(MediaType.TEXT_XML)
    public Response onNotificationReceived() throws Exception {
        System.out.println("received EWS notification success");
        File file = new File("C:\\Users\\XYZ\\workspace\\POC\\ews_notification_response.xml");
        /*String responseXMLStr = IOUtils.toString(new FileInputStream(file));*/
        return Response.ok(null).build();
    }

}