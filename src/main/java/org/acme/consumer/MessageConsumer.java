package org.acme.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class MessageConsumer {

    @Incoming("messages-in")
    public void receive(String message){
        System.out.println("Received message: " + message);
    }
}
