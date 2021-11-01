package scc.srv.Resources;


import lombok.extern.java.Log;
import scc.data.*;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Log
@Path("/message")
public class MessageResource {
    CosmosDBLayer db = CosmosDBLayer.getInstance();

    /**
     * Post a new message.The id of the message is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Message m) {
        log.info("create Action Requested at Message Resource");
        MessageDAO messageDAO = new MessageDAO(m);
        db.putMessage(messageDAO);
        return Response.status(Response.Status.OK).entity(m).build();
    }

    /**
     * Return the message with the id.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") String id) {
        log.info("getById Action Requested at Message Resource");
        MessageDAO m = db.getMessageById(id).stream().findFirst().get();
        if(m != null) {
            return Response.status(Response.Status.OK).entity(m.toMessage()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();
        }
        //throw new ServiceUnavailableException();
    }

    /**
     * Lists the ids of all messages.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        log.info("getAll Action Requested at Message Resource");
        List<String> ids = new ArrayList<>();

        for (MessageDAO m : db.getMessages()){
            ids.add(m.getId());
        }
        if(!ids.isEmpty()){ return Response.status(Response.Status.OK).entity(ids).build();}
            else{ return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();}
    }

    /**
     * Delete message by id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        log.info("deleteById Action Requested at Message Resource");
        Optional<MessageDAO> op =  db.getMessageById(id).stream().findFirst();
        if(op.isPresent()){
            MessageDAO m = op.get();
            db.delMessage(m);
            return Response.status(Response.Status.OK).entity(m.toMessage()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();

        }
    }
}
