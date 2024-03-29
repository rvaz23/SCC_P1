package scc.srv.Resources;

import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.cache.Session;
import scc.data.*;
import scc.data.Channel.Channel;
import scc.data.Channel.ChannelDAO;
import scc.data.Garbage.Garbage;
import scc.data.User.Login;
import scc.data.User.User;
import scc.data.User.UserDAO;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;

@Log
@Path("/user")
public class UserResource {
    MongoDB db = MongoDB.getInstance();
    RedisCache cache = RedisCache.getCachePool();

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) throws JsonProcessingException {
        boolean pwdOk = false;
        // Check pwd
        User usr = GetObjects.getUserIfExists(user.getUser());
        if (usr != null) {
            if (usr.getPwd().equals(user.getPwd()))
                pwdOk = true;
        }
        if (usr == null) {
            Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND);
        }

        if (pwdOk) {
            String uid = UUID.randomUUID().toString();
            NewCookie cookie = new NewCookie("scc:session", uid, "/", null, "sessionid", 3600, false, true);
            cache.putSession(new Session(uid, user.getUser()));
            return Response.ok().cookie(cookie).build();
        } else
            return Response.status(Status.UNAUTHORIZED).entity(Quotes.INCORRECT_LOGIN).build();
    }

    /**
     * Post a new user.The id of the user is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(User user) throws JsonProcessingException {
        log.info("Create Action Requested at User Resource");
        UserDAO userDAO = new UserDAO(user);

        if (db.getUserById(user.getId()) != null) {
            return Response.status(Status.BAD_REQUEST).entity(Quotes.USER_EXISTS).build();
        }

        if (db.getUserByUsername(user.getName()) != null) {
            return Response.status(Status.BAD_REQUEST).entity(Quotes.USER_EXISTS).build();
        }

        db.putUser(userDAO);
        cache.setUser(userDAO.toUser());
        return Response.status(Response.Status.OK).entity(user).build();
    }

    /**
     * Return the user with the id.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("getById Action Requested at User Resource");
        // procurar na cache
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        User user = GetObjects.getUserIfExists(id);
        if (user == null)
            return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            return Response.status(Response.Status.OK).entity(user).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
    }

    private UserDAO getUserFromDb(String idUser) {
        UserDAO user = null;
        UserDAO u = db.getUserById(idUser);
        if (u != null) {
            user = u;
            cache.setUser(user.toUser());
        }
        return user;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, User user) throws JsonProcessingException {
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        UserDAO u = getUserFromDb(id);
        if (u != null) {
            if (cache.verifySessionCookie(cookie, u.getId())) {
                if (user.getName() != null || !user.getName().equals("")) {
                    u.setName(user.getName());
                }
                if (user.getPhotoId() != null || !user.getPhotoId().equals("")) {
                    u.setPhotoId(user.getPhotoId());
                }
                if (user.getPwd() != null || !user.getPwd().equals("")) {
                    u.setPwd(user.getPwd());
                }
                db.updateUser(id, u);
                cache.setUser(u.toUser());
                return Response.status(Response.Status.OK).entity(u.toUser()).build();

            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) throws JsonProcessingException {
        log.info("deleteById Action Requested at User Resource");
        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        UserDAO user = getUserFromDb(id);
        if (user != null) {
            if (cache.verifySessionCookie(cookie, user.getId())) {
                db.delUser(user);
                cache.deleteUser(id);
                db.putGarbage(new Garbage("USER", user.getId()));
                return Response.status(Response.Status.OK).entity(user.toUser()).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
        return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
    }


    @POST
    @Path("/{id}/subscribe/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribeToChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idUser, @PathParam("channelId") String idChannel) throws JsonProcessingException {
        log.info("addChannelToUser Action Requested at User Resource");

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity("82").build();

        User user = GetObjects.getUserIfExists(idUser);
        if (user == null)
            return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            if (channel.isPublicChannel()) { // true -> quando o channel e publico
                if (!channel.getMembers().contains(idUser)) {
                    UserDAO userChanged = subscribeComputation(idUser, idChannel);
                    cache.increment(channel);
                    return Response.status(Response.Status.OK).entity(userChanged.toUser()).build();
                }
                return Response.status(Response.Status.OK).entity(user).build();
            } else {
                User userOwner = GetObjects.getUserIfExists(channel.getOwner());
                if (userOwner == null)
                    return Response.status(Response.Status.FORBIDDEN).entity("99").build();
                if (idUser.equals(userOwner.getId())) {
                    UserDAO userChanged = subscribeComputation(idUser, idChannel);
                    cache.increment(channel);
                    return Response.status(Response.Status.OK).entity(userChanged.toUser()).build();
                }
                return Response.status(Response.Status.FORBIDDEN).entity("104").build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("107").build();
        }
    }

    private UserDAO subscribeComputation(String idUser, String idChannel) throws JsonProcessingException {
        UserDAO user1 = db.addChannelToUser(idUser, idChannel);
        ChannelDAO channel1 = db.addUserToChannel(idChannel, idUser);
        cache.setUser(user1.toUser());
        cache.setChannel(channel1.toChannel());
        return user1;
    }


    @POST
    @Path("/{id}/remove/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unsubscribeToChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idUser, @PathParam("channelId") String idChannel) throws JsonProcessingException {
        log.info("removeChannelFromUser Action Requested at User Resource");

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity("82").build();

        User user = GetObjects.getUserIfExists(idUser);
        if (user == null)
            return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        Channel channel = GetObjects.getChannelIfExists(idChannel);
        if (channel == null)
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            if (channel.getMembers().contains(idUser)) { // true -> quando o channel e publico
                UserDAO userChanged = unsubscribeComputation(idUser, idChannel);
                return Response.status(Response.Status.OK).entity(userChanged.toUser()).build();
            } else {
                Response.status(Response.Status.FORBIDDEN).entity("104").build();
            }
        }

        return Response.status(Response.Status.FORBIDDEN).entity("107").build();


    }

    private UserDAO unsubscribeComputation(String idUser, String idChannel) throws JsonProcessingException {
        ChannelDAO channel = db.getChannelById(idChannel);
        if (channel != null) {
            ArrayList<String> users = channel.getMembers();
            users.remove(idUser);
            channel.setMembers(users);
            db.updateChannel(idChannel, channel);
            cache.setChannel(channel.toChannel());
        }
        UserDAO user = db.getUserById(idUser);
        if (user != null) {
            ArrayList<String> channels = user.getChannelIds();
            channels.remove(idChannel);
            user.setChannelIds(channels);
            db.updateUser(idUser, user);
            cache.setUser(user.toUser());
            return user;
        }
        return null;
    }


    /**
     * Get channels associated to user id
     *
     * @return
     */

    @GET
    @Path("/{id}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannelsByUserId(@CookieParam("scc:session") Cookie session,
                                        @PathParam("id") String idUser) throws JsonProcessingException {
        log.info("getChannelsByUserId Action Requested at User Resource");

        String cookie = GetObjects.getCookie(session);
        if (cookie.equals(""))
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

        User user = GetObjects.getUserIfExists(idUser);
        if (user == null)
            return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        List<String> channelIds = new ArrayList<>();

        if (cache.verifySessionCookie(cookie, user.getId())) {
            channelIds = user.getChannelIds();
            return Response.status(Response.Status.OK).entity(channelIds).build();

        }
        return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

    }


    /**
     * Lists the ids of all users.
     **/
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("st") int offset, @QueryParam("len") int limit) {
        log.info("getAll Action Requested at User Resource");
        List<String> ids = new ArrayList<>();

        for (UserDAO u : db.getUsers(offset, limit)) {
            ids.add(u.getId());
        }
        return Response.status(Response.Status.OK).entity(ids).build();
    }
/*
    private UserDAO getUserFromDb(String idUser) throws JsonProcessingException {
        UserDAO user = null;
        Optional<UserDAO> op = db.getUserById(idUser).stream().findFirst();
        if (op.isPresent()) {
            user = op.get();
            cache.setUser(user.toUser());
        }
        return user;
    }

*/

}
