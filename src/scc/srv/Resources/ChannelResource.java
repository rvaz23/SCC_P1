package scc.srv.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.*;
import scc.data.Channel.Channel;
import scc.data.Channel.ChannelCreation;
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
@Path("/channel")
public class ChannelResource {
    CosmosDBLayer db = CosmosDBLayer.getInstance();
    RedisCache cache = RedisCache.getCachePool();
    CognitiveSearch cogSearch = CognitiveSearch.getInstance();

    /**
     * Post a new channel.The id of the channel is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("scc:session") Cookie session, ChannelCreation channel) throws JsonProcessingException {

        User user = GetObjects.getUserIfExists(channel.getOwner());
        if (user == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            ChannelDAO channelDAO = createComputation(channel);

            addToMembersComputation(user.getId(), channelDAO.getId());
            log.info("create Action Requested at Channel Resource");
            return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
    }

    private ChannelDAO createComputation(ChannelCreation channel) throws JsonProcessingException {
        ChannelDAO channelDAO = new ChannelDAO(channel);
        db.putChannel(channelDAO);
        cache.setChannel(channelDAO.toChannel());

        return channelDAO;
    }

    /**
     * Delete the channel with the given id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("deleteById Action Requested at Channel Resource");

        ChannelDAO channel = getChannelFromDb(id);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        User user = GetObjects.getUserIfExists(channel.getOwner());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            db.delChannelById(channel.getId());
            cache.deleteChannel(channel.getId());
            db.putGarbage(new Garbage("CHANNEL", channel.getId()));
            return Response.status(Response.Status.OK).entity(channel.toChannel()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

    }

    private ChannelDAO getChannelFromDb(String idChannel) throws JsonProcessingException {
        ChannelDAO channel = null;
        Optional<ChannelDAO> op = db.getChannelById(idChannel).stream().findFirst();
        if (op.isPresent()) {
            channel = op.get();
            cache.setChannel(channel.toChannel());
        }
        return channel;
    }


    /**
     * Add user with Id to channel with Id
     */
    @POST
    @Path("/{id}/add/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserToChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @PathParam("userId") String idUser) throws JsonProcessingException {
        log.info("addUserToChannel Action Requested at Channel Resource");

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity("no cookie").build();

        User userToAdd = GetObjects.getUserIfExists(idUser);
        if (userToAdd == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        User userOwner = GetObjects.getUserIfExists(channel.getOwner());
        if (userOwner == null)
            return Response.status(Response.Status.FORBIDDEN).entity("Null owner").build();

        if (cache.verifySessionCookie(cookie, userOwner.getId())) {
            if (!channel.getMembers().contains(idUser)) {
                ChannelDAO channelDAO = addToMembersComputation(idUser, idChannel);
                return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
            }
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Last").build();
        }
    }

    private ChannelDAO addToMembersComputation(String idUser, String idChannel) throws JsonProcessingException {
        UserDAO userDAO = db.addChannelToUser(idUser, idChannel).getItem();
        ChannelDAO channelDAO = db.addUserToChannel(idChannel, idUser).getItem();
        cache.setUser(userDAO.toUser());
        cache.setChannel(channelDAO.toChannel());
        return channelDAO;
    }


    /**
     * Remove user from channel
     */
    @POST
    @Path("/{id}/remove/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @PathParam("userId") String idUser) throws JsonProcessingException {
        log.info("removeUserFromChannel Action Requested at Channel Resource");

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity("no cookie").build();

        User userToRemove = GetObjects.getUserIfExists(idUser);
        if (userToRemove == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();

        User userOwner = GetObjects.getUserIfExists(channel.getOwner());
        if (userOwner == null)
            return Response.status(Response.Status.FORBIDDEN).entity("Null owner").build();

        if (cache.verifySessionCookie(cookie, userOwner.getId())) {
            if (channel.getMembers().contains(idUser)){
                ChannelDAO channelDAO =removeFromMembersComputation(idUser, idChannel);
                return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
            }
            return Response.status(Response.Status.OK).entity(channel).build();
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
    }

    private ChannelDAO removeFromMembersComputation(String idUser, String idChannel) throws JsonProcessingException {
        Optional<UserDAO> optional = db.getUserById(idUser).stream().findFirst();
        if (optional.isPresent()){
            UserDAO user = optional.get();
            ArrayList<String> channels =user.getChannelIds();
            channels.remove(idChannel);
            user.setChannelIds(channels);
            db.updateUser(idUser,user);
            cache.setUser(user.toUser());
        }
        Optional<ChannelDAO> optional1 = db.getChannelById(idChannel).stream().findFirst();
        if (optional1.isPresent()){
            ChannelDAO channel = optional1.get();
            ArrayList<String> users = channel.getMembers();
            users.remove(idUser);
            channel.setMembers(users);
            db.updateChannel(idChannel,channel);
            cache.setChannel(channel.toChannel());
            return  channel;
        }
        return null;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("getById Action Requested at Channel Resource");

        Channel channel = GetObjects.getChannelIfExists(id);

        if (channel == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (channel.isPublicChannel()) {
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            for (String idU : channel.getMembers()) {
                User u = GetObjects.getUserIfExists(idU);
                if (u != null && cache.verifySessionCookie(cookie, u.getId())) {
                    return Response.status(Response.Status.OK).entity(channel).build();
                }
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }


    }

    /**
     * Updates the channel with the given id
     * Return the channel with the id.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response UpdateById(@PathParam("id") String id, Channel newChannel) {
        try {
            log.info("updateById Action Requested at Channel Resource");
            ChannelDAO c = db.getChannelById(id).stream().findFirst().get();
            if (newChannel.getId() != null || !newChannel.getId().equals("")) {
                c.setId(newChannel.getId());
            }
            if (newChannel.getName() != null || !newChannel.getName().equals("")) {
                c.setName(newChannel.getName());
            }
            c.setPublicChannel(newChannel.isPublicChannel());

            if (newChannel.getMembers() != null) {
                c.setMembers(newChannel.getMembers());
            }
            db.updateChannel(id, c);
            if (c != null) return Response.status(Response.Status.OK).entity(c.toChannel()).build();
            else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();

        } catch (Exception e) {
            throw new ServiceUnavailableException();
        }
    }

    @GET
    @Path("/{id}/messages/{expression}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesOfChannelByWordSearch(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @PathParam("expression") String expression) throws JsonProcessingException {

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (channel.isPublicChannel()) {
            return findByWordMsgComputation(expression, idChannel);
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            for (String idU : channel.getMembers()) {
                User u = GetObjects.getUserIfExists(idU);
                if (u != null && cache.verifySessionCookie(cookie, u.getId())) {
                    return findByWordMsgComputation(expression, idChannel);
                }
            }
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
    }

    private Response findByWordMsgComputation(String expression, String idChannel) {
        List<String> foundMessages = cogSearch.findMessagesWithWordInChannel(expression, idChannel);
        if (foundMessages != null || foundMessages.size() > 0) {
            return Response.status(Response.Status.OK).entity(foundMessages).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.EXPRESSION_NOT_FOUND).build();
        }
    }

    /**
     * Lists the ids of all channels.
     */
    @GET
    @Path("/{id}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesOfChannelWithPagination(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @QueryParam("st") int offset, @QueryParam("len") int limit) throws JsonProcessingException {
        log.info("getAll Action Requested at Channel Resource");

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (channel.isPublicChannel()) {
            List<Message> messages = getMessagesOfChannelComputation(offset, limit, idChannel);
            return Response.status(Response.Status.OK).entity(messages).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            for (String idU : channel.getMembers()) {
                User u = GetObjects.getUserIfExists(idU);
                if (u != null && cache.verifySessionCookie(cookie, u.getId())) {
                    List<Message> messages = getMessagesOfChannelComputation(offset, limit, idChannel);
                    if (messages.isEmpty()) {
                        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
                    }
                    return Response.status(Response.Status.OK).entity(messages).build();
                }
            }
        }

        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

    }

    public List<Message> getMessagesOfChannelComputation(int offset, int limit, String idChannel) {
        List<Message> messages = new ArrayList<>();

        for (MessageDAO m : db.getMessages(offset, limit, idChannel)) {
            messages.add(m.toMessage());
        }
        return messages;
    }

    /**
     * Lists the ids of all channels.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("st") int offset, @QueryParam("len") int limit) {
        log.info("getAll Action Requested at Channel Resource");
        List<String> ids = new ArrayList<>();

        for (ChannelDAO c : db.getChannels(offset, limit)) {
            ids.add(c.getId());
        }
        if (!ids.isEmpty()) return Response.status(Response.Status.OK).entity(ids).build();
        else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
    }


}
