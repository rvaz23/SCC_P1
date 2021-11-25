package scc.srv.Resources;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.*;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Path("/message")
public class MessageResource {
    CosmosDBLayer db = CosmosDBLayer.getInstance();
    RedisCache cache = RedisCache.getCachePool();

    /**
     * Post a new message.The id of the message is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("scc:session") Cookie session, Message m) throws JsonProcessingException {
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        User user = GetObjects.getUserIfExists(m.getSenderId());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getName())) {
            if (user.getChannelIds().contains(m.getChannelId())) {
                if (verifyMsgExists(m.getRepliesToId())) {
                    log.info("create Action Requested at Message Resource");
                    MessageDAO messageDAO = new MessageDAO(m);
                    db.putMessage(messageDAO);
                    cache.setMessage(messageDAO.toMessage());
                    return Response.status(Response.Status.OK).entity(m).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();
                }
            }
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
    }

    /**
     * Return the message with the id.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        Message message = GetObjects.getMessageIfExists(id);
        if (message == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        User user = GetObjects.getUserIfExists(message.getSenderId());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getName())) {
            log.info("getById Action Requested at Message Resource");
            return Response.status(Response.Status.OK).entity(message).build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();


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

        for (MessageDAO m : db.getMessages()) {
            ids.add(m.getId());
        }
        if (!ids.isEmpty()) {
            return Response.status(Response.Status.OK).entity(ids).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();
        }
    }

    /**
     * Delete message by id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        log.info("deleteById Action Requested at Message Resource");
        Optional<MessageDAO> op = db.getMessageById(id).stream().findFirst();
        if (op.isPresent()) {
            MessageDAO m = op.get();
            db.delMessage(m);
            return Response.status(Response.Status.OK).entity(m.toMessage()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();

        }
    }

    private boolean verifyMsgExists(String id) {
        if (cache.getMessage(id) != null)
            return true;
        if (db.getUserById(id).stream().count() > 0)
            return true;
        return false;
    }


    private UserDAO getUserFromDb(String idUser) throws JsonProcessingException {
        UserDAO user = null;
        Optional<UserDAO> op = db.getUserById(idUser).stream().findFirst();
        if (op.isPresent()) {
            user = op.get();
            cache.setUser(user.toUser());
        }
        return user;
    }

}
