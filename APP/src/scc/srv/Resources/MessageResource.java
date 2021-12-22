package scc.srv.Resources;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.*;
import scc.data.Channel.Channel;
import scc.data.Channel.ChannelDAO;
import scc.data.Garbage.Garbage;
import scc.data.Message.Message;
import scc.data.Message.MessageDAO;
import scc.data.User.User;
import scc.data.User.UserDAO;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Path("/messages")
public class MessageResource {
    MongoDB db = MongoDB.getInstance();
    RedisCache cache = RedisCache.getCachePool();

    /**
     * Post a new message.The id of the message is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("scc:session") Cookie session, Message m) throws JsonProcessingException {
        String cookie = GetObjects.getCookie(session);
        if (db.getMessageById(m.getId()) != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Quotes.MESSAGE_EXISTS).build();
        }
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS+ ":NO COOKIE").build();
        User user = GetObjects.getUserIfExists(m.getUser());
        if (user == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            if (user.getChannelIds().contains(m.getChannel())) {
                if (verifyMsgExists(m.getReplyTo()) || m.getReplyTo().equals("")) {
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

        User user = GetObjects.getUserIfExists(message.getUser());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            log.info("getById Action Requested at Message Resource");
            return Response.status(Response.Status.OK).entity(message).build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();


        //throw new ServiceUnavailableException();
    }

    /**
     * Delete message by id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("deleteById Action Requested at Message Resource");
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        MessageDAO message = getMessageFromDb(id);
        if (message == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.MESSAGE_NOT_FOUND).build();

        User user = GetObjects.getUserIfExists(message.getUser());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.USER_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            if (user.getChannelIds().contains(message.getChannelId())) {
                db.delMessage(message);
                cache.deleteMessage(message.getId());
                db.putGarbage(new Garbage("MESSAGE", message.getId()));
                return Response.status(Response.Status.OK).entity(message).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
    }

    /**
     * Updates the message with the given id
     * Return the message with the id.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Message newMessage) {
        try {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            MessageDAO m = getMessageFromDb(id);
            if (m == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            User user = GetObjects.getUserIfExists(m.getUser());
            if (user == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            if (cache.verifySessionCookie(cookie, user.getId())) {
                log.info("getById Action Requested at Message Resource");
                log.info("updateById Action Requested at Channel Resource");

                if (newMessage.getId() != null || !newMessage.getId().equals("")) {
                    m.setId(newMessage.getId());
                }
                if (newMessage.getText() != null || !newMessage.getText().equals("")) {
                    m.setText(newMessage.getText());
                }
                if (newMessage.getImageId() != null) {
                    m.setImageId(newMessage.getImageId());
                }
                db.updateMessage(id, m);
                if (m != null) return Response.status(Response.Status.OK).entity(m.toMessage()).build();
                else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        } catch (Exception e) {
            throw new ServiceUnavailableException();
        }
    }

    /**
     * Lists the ids of all messages.
     */

    /*@GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("st") int offset, @QueryParam("len") int limit) {
        log.info("getAll Action Requested at Message Resource");
        List<String> ids = new ArrayList<>();

        for (MessageDAO m : db.getMessages(offset, limit)) {
            ids.add(m.getId());
        }
        if (!ids.isEmpty()) {
            return Response.status(Response.Status.OK).entity(ids).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.MESSAGE_NOT_FOUND).build();
        }
    }
*/


    private boolean verifyMsgExists(String id) {
        if (cache.getMessage(id) != null)
            return true;
        if (db.getMessageById(id) != null)
            return true;
        return false;
    }


    private MessageDAO getMessageFromDb(String idMessage) throws JsonProcessingException {
        MessageDAO message = null;
        MessageDAO m = db.getMessageById(idMessage);

        if (m!=null) {
            message = m;
            cache.setMessage(message.toMessage());
        }
        return message;
    }


}
