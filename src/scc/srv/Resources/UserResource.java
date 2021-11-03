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
	public Response create(User user) {
		// TODO Verificar se dois users nao tem mesmo nome/ID
		log.info("Create Action Requested at User Resource");
		UserDAO userDAO = new UserDAO(user);

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
		return Response.status(Response.Status.OK).entity(user).build();
	}

	/**
	 * Add user with Id to channel with Id
	 */
	@PUT
	@Path("/{idUser}/subscribe/{idChannel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserToChannel(@PathParam("idUser") String idUser, @PathParam("idChannel") String idChannel) {
		log.info("addUserToChannel Action Requested at User Resource");
		Optional<UserDAO> csmItrU = db.getUserById(idUser).stream().findFirst();
		Optional<ChannelDAO> csmItrC = db.getChannelById(idChannel).stream().findFirst();

		if (csmItrU.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
		} else if (csmItrC.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
		} else {
			ChannelDAO c = csmItrC.get();
			if (c.isStatus()) { // verifica se o channel e privado
				db.addChannelToUser(idUser, idChannel);
				db.addUserToChannel(idChannel, idUser);
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
	@Path("/{idUser}/channels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChannelsByUserId(@CookieParam("scc:session") Cookie session,
			@PathParam("idUser") String idUser) {
		log.info("getChannelsByUserId Action Requested at User Resource");
		List<String> channelIds = new ArrayList<>();
		User user;
		try {
			user = cache.getUser(idUser);
			if (user == null) {
				Optional<UserDAO> op = db.getUserById(idUser).stream().findFirst();
				if (op.isPresent()) {
					UserDAO u = op.get();
					user=u.toUser();
					cache.setUser(user);
				}
			}
				String cookie="";
				if(session!=null) {
					cookie=session.getValue();
				}
				if (cache.verifySessionCookie(cookie, user.getName())) {
					channelIds=user.getChannelIds();
					if (channelIds.isEmpty()) {
						return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
					} else {
						return Response.status(Response.Status.OK).entity(channelIds).build();
					}
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
				}
			

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
	}

	/**
	 * Return the user with the id.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
		log.info("getById Action Requested at User Resource");
		// procurar na cache
		try {
			User user = cache.getUser(id);
			if (user != null) {
				String cookie="";
				if(session!=null) {
					cookie=session.getValue();
				}
				if (cache.verifySessionCookie(cookie, user.getName())) {
					return Response.status(Response.Status.OK).entity(user).build();
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Optional<UserDAO> op = db.getUserById(id).stream().findFirst();
		if (op.isPresent()) {
			UserDAO u = op.get();
			// add to cache
			try {
				cache.setUser(u.toUser());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (cache.verifySessionCookie(session.getValue(), u.getName())) {
				return Response.status(Response.Status.OK).entity(u.toUser()).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
			}
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
		}
		// throw new ServiceUnavailableException();
	}

	/**
	 * Updates and returns the user if id is valid.
	 */
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateById(@PathParam("id") String id, User user) {
		// cache=RedisCache.getCachePool();
		log.info("updateById Action Requested at User Resource");
		Optional<UserDAO> op = db.getUserById(id).stream().findFirst();
		if (op.isPresent()) {
			UserDAO u = op.get();
			if (user.getName() != null || !user.getName().equals("")) {
				u.setName(user.getName());
			}
			if (user.getPhotoId() != null || !user.getPhotoId().equals("")) {
				u.setPhotoId(user.getPhotoId());
			}
			db.updateUser(id, u);
			try {
				cache.setUser(u.toUser());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Response.status(Response.Status.OK).entity(u.toUser()).build();
		} else
			return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

		// throw new ServiceUnavailableException();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteById(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
		log.info("deleteById Action Requested at User Resource");
		Optional<UserDAO> op = db.getUserById(id).stream().findFirst();
		if (op.isPresent()) {
			UserDAO u = op.get();
			String cookie="";
			if(session!=null) {
				cookie=session.getValue();
			}
			if (cache.verifySessionCookie(cookie, u.getName())) {
				db.delUser(u);
				cache.deleteUser(id);
				return Response.status(Response.Status.OK).entity(u.toUser()).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
			}
		} else {
			// return
			// Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
			return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
		}
	}

	/**
	 * Lists the ids of all users.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		log.info("getAll Action Requested at User Resource");
		List<String> ids = new ArrayList<>();

		for (UserDAO u : db.getUsers()) {
			ids.add(u.getId());
		}
		return Response.status(Response.Status.OK).entity(ids).build();
	}
}
