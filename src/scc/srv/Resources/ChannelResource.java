package scc.srv.Resources;
import com.azure.cosmos.util.CosmosPagedIterable;
import lombok.extern.java.Log;
import scc.cache.RedisCache;
import scc.data.Channel;
import scc.data.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
import scc.utils.Quotes;
import javax.ws.rs.*;
import scc.cache.Session;

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
    public Response create(Channel channel) {
        log.info("create Action Requested at Channel Resource");
        ChannelDAO channelDAO = new ChannelDAO(channel);
        db.putChannel(channelDAO);
        return Response.status(Response.Status.OK).entity(channelDAO.toChannel()).build();
    }

    /**
     * Delete the channel with the given id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        log.info("deleteById Action Requested at Channel Resource");
        if(db.getUserById(id)!=null){
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
    public Response addUserToChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String idChannel, @PathParam("userId") String idUser) {
        log.info("addUserToChannel Action Requested at Channel Resource");
        Optional<UserDAO> csmItrU = db.getUserById(idUser).stream().findFirst();
        Optional<ChannelDAO> csmItrC = db.getChannelById(idChannel).stream().findFirst();
        String cookie="";
        if (csmItrU.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
        } else if (csmItrC.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
        } else {
            if(session!=null) {
                cookie=session.getValue();
            }
            ChannelDAO c = csmItrC.get();
            if (c.isPublic() || ( !c.isPublic() && cache.verifySessionCookie(cookie,c.getOwner() ) )) {
                db.addChannelToUser(idUser, idChannel);
                db.addUserToChannel(idChannel, idUser);
                return Response.status(Response.Status.OK).build();
            }else{
                return Response.status(Response.Status.FORBIDDEN).entity(Quotes.FORBIDEN_ACCESS).build();
            }
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@CookieParam("scc:session") Cookie session,@PathParam("id") String id) {
        log.info("getById Action Requested at Channel Resource");
        ChannelDAO u = db.getChannelById(id).stream().findFirst().get();
        if(u != null) {
            if(u.isPublic()){
                return Response.status(Response.Status.OK).entity(u.toChannel()).build();
            }else{
                //TODO fazer verificação login
                return Response.status(Response.Status.OK).entity(u.toChannel()).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
        }
        //throw new ServiceUnavailableException();
    }

    /**
     * Updates the channel with the given id
     * Return the channel with the id.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response  UpdateById(@PathParam("id") String id,Channel newChannel) {
        log.info("updateById Action Requested at Channel Resource");
        ChannelDAO c = db.getChannelById(id).stream().findFirst().get();
        if (newChannel.getId()!=null || !newChannel.getId().equals("")){
            c.setId(newChannel.getId());
        }
        if (newChannel.getName()!=null || !newChannel.getName().equals("")){
            c.setName(newChannel.getName());
        }
        c.setIsPublic(newChannel.isPublic());

        if (newChannel.getMemberIds()!=null ){
            c.setMemberIds(newChannel.getMemberIds());
        }
        db.updateChannel(id,c);
        if(c != null )return Response.status(Response.Status.OK).entity(c.toChannel()).build();
            else  return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
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

        for (ChannelDAO c: db.getChannels()){
            ids.add(c.getId());
        }
        if(!ids.isEmpty())return Response.status(Response.Status.OK).entity(ids).build();
            else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.CHANNEL_NOT_FOUND).build();
    }


}
