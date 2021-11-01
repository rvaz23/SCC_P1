package scc.srv.Resources;


import scc.data.*;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * Delete message by id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        Optional<MessageDAO> op =  db.getMessageById(id).stream().findFirst();
        if(op.isPresent()){
            MessageDAO m = op.get();
            db.delMessage(m);
            return Response.status(Response.Status.OK).entity(m.toMessage()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        }
    }
}
