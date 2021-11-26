package scc.srv.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.*;
import scc.utils.Hash;
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

    /**
     * Post a new channel.The id of the channel is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam("scc:session") Cookie session, ChannelCreation channel) throws JsonProcessingException {

        String newId = Hash.of(channel.getName());

        Channel exists = GetObjects.getChannelIfExists(newId);
        if (exists != null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_EXISTS).build();

        if (channel.isChannelPublic()) {
            ChannelDAO channelDAO = createComputation(newId, channel);
            log.info("create Action Requested at Channel Resource");
            return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            User user = GetObjects.getUserIfExistsByName(channel.getOwner());
            if (user == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            if (cache.verifySessionCookie(cookie, user.getId())) {
                ChannelDAO channelDAO = createComputation(newId, channel);
                addToMembersComputation(user.getId(),newId,user,channelDAO.toChannel());
                log.info("create Action Requested at Channel Resource");
                return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
    }

    private ChannelDAO createComputation(String newId, ChannelCreation channel) throws JsonProcessingException {
        ChannelDAO channelDAO = new ChannelDAO(newId, channel);
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

        if (channel.isChannelPublic()) {
            db.delChannelById(channel.getId());
            cache.deleteChannel(channel.getId());
            return Response.status(Response.Status.OK).entity(channel.toChannel()).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            User user = GetObjects.getUserIfExistsByName(channel.getOwner());
            if (user == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            if (cache.verifySessionCookie(cookie, user.getId())) {
                db.delChannelById(channel.getId());
                cache.deleteChannel(channel.getId());
                return Response.status(Response.Status.OK).entity(channel.toChannel()).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
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

        User userToAdd = GetObjects.getUserIfExists(idUser);
        if (userToAdd == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (channel.isChannelPublic()) {
            addToMembersComputation(idUser, idChannel, userToAdd, channel);
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            User userOwner = GetObjects.getUserIfExistsByName(channel.getOwner());
            if (userOwner == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            if (cache.verifySessionCookie(cookie, userOwner.getId())) {
                addToMembersComputation(idUser, idChannel, userToAdd, channel);
                return Response.status(Response.Status.OK).entity(channel).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        }
    }

    private void addToMembersComputation(String idUser, String idChannel, User userToAdd, Channel channel) throws JsonProcessingException {
        db.addChannelToUser(idUser, idChannel);
        db.addUserToChannel(idChannel, idUser);
        cache.setUser(userToAdd);
        cache.setChannel(channel);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("getById Action Requested at Channel Resource");

        Channel channel = GetObjects.getChannelIfExists(id);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (channel.isChannelPublic()) {
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            for (String idU : channel.getMemberIds()) {
                User u = GetObjects.getUserIfExists(idU);
                if (cache.verifySessionCookie(cookie, u.getId())) {
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
        log.info("updateById Action Requested at Channel Resource");
        ChannelDAO c = db.getChannelById(id).stream().findFirst().get();
        if (newChannel.getId() != null || !newChannel.getId().equals("")) {
            c.setId(newChannel.getId());
        }
        if (newChannel.getName() != null || !newChannel.getName().equals("")) {
            c.setName(newChannel.getName());
        }
        c.setIsPublic(newChannel.isChannelPublic());

        if (newChannel.getMemberIds() != null) {
            c.setMemberIds(newChannel.getMemberIds());
        }
        db.updateChannel(id, c);
        if (c != null) return Response.status(Response.Status.OK).entity(c.toChannel()).build();
        else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
        //throw new ServiceUnavailableException();
    }


    /**
     * Lists the ids of all channels.
     */
    @GET
    @Path("/{id}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesOfChannelWithPagination(@PathParam("id") String idChannel, @QueryParam("st") int offset, @QueryParam("len") int limit) {
        log.info("getAll Action Requested at Channel Resource");

        List<String> idsMessages = new ArrayList<>();

        for (MessageDAO m : db.getMessages(offset, limit, idChannel)) {
            idsMessages.add(m.getId());
        }
        return Response.status(Response.Status.OK).entity(idsMessages).build();
    }

    /**
     * Lists the ids of all channels.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        log.info("getAll Action Requested at Channel Resource");
        List<String> ids = new ArrayList<>();

        for (ChannelDAO c : db.getChannels()) {
            ids.add(c.getId());
        }
        if (!ids.isEmpty()) return Response.status(Response.Status.OK).entity(ids).build();
        else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
    }


}
