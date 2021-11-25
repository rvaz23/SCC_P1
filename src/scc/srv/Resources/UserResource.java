package scc.srv.Resources;

import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.cache.Session;
import scc.data.*;
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
    CosmosDBLayer db = CosmosDBLayer.getInstance();
    RedisCache cache = RedisCache.getCachePool();

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {
        boolean pwdOk = false;
        // Check pwd
        Optional<UserDAO> userD = db.getUserByUsername(user.getUser()).stream().findFirst();
        if (userD.isPresent()) {
            if (userD.get().getPwd().equals(user.getPwd()))
                pwdOk = true;
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
        // TODO Verificar se dois users nao tem mesmo nome/ID
        log.info("Create Action Requested at User Resource");
        UserDAO userDAO = new UserDAO(user);

        if (db.getUserById(user.getId()).stream().count() > 0) {
            return Response.status(Status.BAD_REQUEST).entity(Quotes.USER_EXISTS).build();
        }

        if (db.getUserByUsername(user.getName()).stream().count() > 0) {
            return Response.status(Status.BAD_REQUEST).entity(Quotes.USER_EXISTS).build();
        }

        // adiciona user ao canal
        for (String id : user.getChannelIds()) {
            Optional<ChannelDAO> csmItr = db.getChannelById(id).stream().findFirst();
            if (!csmItr.isEmpty()) {
                ChannelDAO c = csmItr.get();
                c.addUserToChannel(user.getId());
                db.updateChannel(c.getId(), c);
            }
        }
        db.putUser(userDAO);
        cache.setUser(userDAO.toUser());
        return Response.status(Response.Status.OK).entity(user).build();
    }

    /**
     * Add user with Id to channel with Id
     */
    @PUT
    @Path("/{id}/subscribe/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addChannelToUser(@PathParam("id") String idUser, @PathParam("channelId") String idChannel) throws JsonProcessingException {
        log.info("addChannelToUser Action Requested at User Resource");
        Optional<UserDAO> csmItrU = db.getUserById(idUser).stream().findFirst();
        Optional<ChannelDAO> csmItrC = db.getChannelById(idChannel).stream().findFirst();

        if (csmItrU.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
        } else if (csmItrC.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
        } else {
            ChannelDAO c = csmItrC.get();
            if (c.isChannelPublic()) { // true -> quando o channel e publico
                UserDAO user = db.addChannelToUser(idUser, idChannel).getItem();
                ChannelDAO channel = db.addUserToChannel(idChannel, idUser).getItem();
                cache.setUser(user.toUser());
                cache.setChannel(channel.toChannel());
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.CHANNEL_IS_PRIVATE).build();
            }
        }
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

        List<String> channelIds;

        if (cache.verifySessionCookie(cookie, user.getName())) {
            channelIds = user.getChannelIds();
            if (channelIds.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
            } else {
                return Response.status(Response.Status.OK).entity(channelIds).build();
            }
        }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();

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
        if (user==null)
            return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

        if (cache.verifySessionCookie(cookie, user.getName())) {
            return Response.status(Response.Status.OK).entity(user).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
    }

    /**
     * Updates and returns the user if id is valid.
     */
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
            if (cache.verifySessionCookie(cookie, u.getName())) {
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
            if (cache.verifySessionCookie(cookie, user.getName())) {
                db.delUser(user);
                cache.deleteUser(id);
                return Response.status(Response.Status.OK).entity(user.toUser()).build();
            }
            return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
        }
        return Response.status(Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
    }

    /**
     * Lists the ids of all users.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("offset") int offset, @QueryParam("limit") int limit) {
        log.info("getAll Action Requested at User Resource");
        List<String> ids = new ArrayList<>();

        for (UserDAO u : db.getUsers(offset, limit)) {
            ids.add(u.getId());
        }
        return Response.status(Response.Status.OK).entity(ids).build();
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
