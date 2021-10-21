package scc.srv.Resources;


import scc.data.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/message")
public class MessageResource {
    CosmosDBLayer db = CosmosDBLayer.getInstance();

    /**
     * Post a new message.The id of the message is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Message create(Message m) {
        MessageDAO messageDAO = new MessageDAO(m);
        db.putMessage(messageDAO);
        return m;
    }

    /**
     * Return the message with the id.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Message getById(@PathParam("id") String id) {
        MessageDAO m = db.getMessageById(id).stream().findFirst().get();
        return m.toMessage();
        //throw new ServiceUnavailableException();
    }

    /**
     * Lists the ids of all messages.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAll() {
        List<String> ids = new ArrayList<>();

        for (MessageDAO m : db.getMessages()){
            ids.add(m.getId());
        }
        return ids;
    }

}
