package scc.srv.Resources;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.data.Channel;
import scc.data.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/channel")
public class ChannelResource {
    CosmosDBLayer db = CosmosDBLayer.getInstance();

    /**
     * Post a new channel.The id of the channel is its hash.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Channel create(Channel channel) {
        ChannelDAO channelDAO = new ChannelDAO(channel);
        db.putChannel(channelDAO);
        return channel;
    }

    /**
     * Delete the channel with the given id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteById(@PathParam("id") String id) {

        if(db.getUserById(id)!=null){
            db.delChannelById(id);
            return id;
        }
        return null;
    }

    /**
     * Updates the channel with the given id
     * Return the channel with the id.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Channel  UpdateById(@PathParam("id") String id,Channel newChannel) {
        ChannelDAO c = db.getChannelById(id).stream().findFirst().get();
        if (newChannel.getId()!=null || !newChannel.getId().equals("")){
            c.setId(newChannel.getId());
        }
        if (newChannel.getName()!=null || !newChannel.getName().equals("")){
            c.setName(newChannel.getName());
        }
        c.setStatus(newChannel.getStatus());

        if (newChannel.getMemberIds()!=null ){
            c.setMemberIds(newChannel.getMemberIds());
        }
        db.updateChannel(id,c);
        return c.toChannel();
        //throw new ServiceUnavailableException();
    }



    /**
     * Lists the ids of all channels.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAll() {
        List<String> ids = new ArrayList<>();

        for (ChannelDAO c: db.getChannels()){
            ids.add(c.getId());
        }
        return ids;
    }


}
