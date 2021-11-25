package scc.srv.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.Channel;
import scc.data.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
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
    public Response create(@CookieParam("scc:session") Cookie session,Channel channel) throws JsonProcessingException {
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        User user = GetObjects.getUserIfExistsByName(channel.getOwner());
        if (user == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (cache.verifySessionCookie(cookie, user.getName())) {
            Channel exists = GetObjects.getChannelIfExists(channel.getId());
            if (exists==null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_EXISTS).build();
            ChannelDAO channelDAO = new ChannelDAO(channel);
            db.putChannel(channelDAO);
            cache.setChannel(channelDAO.toChannel());
            log.info("create Action Requested at Channel Resource");
            return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
    }

    /**
     * Delete the channel with the given id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        log.info("deleteById Action Requested at Channel Resource");
        if (db.getUserById(id) != null) {
            db.delChannelById(id);
            return Response.status(Response.Status.OK).entity(id).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
        }
    }


    /**
     * Add user with Id to channel with Id
     */
    @PUT
    @Path("/{id}/add/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserToChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @PathParam("userId") String idUser) throws JsonProcessingException {
        log.info("addUserToChannel Action Requested at Channel Resource");

        User userToAdd = GetObjects.getUserIfExists(idUser);
        if (userToAdd == null)
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel=GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();


        if (channel.isChannelPublic()) {
            db.addChannelToUser(idUser, idChannel);
            db.addUserToChannel(idChannel, idUser);
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            User userOwner = GetObjects.getUserIfExistsByName(channel.getOwner());
            if (userOwner == null)
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            if (cache.verifySessionCookie(cookie, userOwner.getName())) {
                db.addChannelToUser(idUser, idChannel);
                db.addUserToChannel(idChannel, idUser);
                return Response.status(Response.Status.OK).entity(channel).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("getById Action Requested at Channel Resource");

        Channel channel=GetObjects.getChannelIfExists(id);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        if (channel.isChannelPublic()) {
            return Response.status(Response.Status.OK).entity(channel).build();
        } else {
            String cookie = GetObjects.getCookie(session);
            if (cookie.equals(""))
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

            for (String idU : channel.getMemberIds()) {
                User u  = GetObjects.getUserIfExists(idU);
                if (cache.verifySessionCookie(cookie, u.getName())) {
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
