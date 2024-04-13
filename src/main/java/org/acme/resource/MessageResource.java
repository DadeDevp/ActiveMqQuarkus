package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.consumer.MessageConsumer;
import org.acme.produtor.MessageProducer;

@Path("/message")
public class MessageResource {
    @Inject
    MessageProducer messageProducer;

    @Inject
    MessageConsumer messageConsumer;

    @GET
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendMessage(){
        messageProducer.send("Hello! Im from message producer!!");
        return "Message Sent";
    }
    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMessage(String message){
        messageProducer.send(message);
        return Response.ok("Mensagem Sent!").build();
    }
}
