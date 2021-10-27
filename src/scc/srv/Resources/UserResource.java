package scc.srv.Resources;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Put;
import com.azure.core.util.BinaryData;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;

import io.netty.handler.codec.http.HttpResponseStatus;
import scc.data.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
import scc.utils.Hash;
import scc.utils.Quotes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Stream;


@Path("/user")
public class UserResource {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        /**
         * Post a new user.The id of the user is its hash.
         */
        @POST
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public Response create(User user) {
            UserDAO userDAO = new UserDAO(user);

            //adiciona user ao canal
            for(String id: user.getChannelIds()){
                Optional<ChannelDAO> csmItr =db.getChannelById(id).stream().findFirst();
                if(!csmItr.isEmpty()){
                    ChannelDAO c = csmItr.get();
                    c.addUserToChannel(user.getId());
                    db.updateChannel(c.getId(),c);
                }
            }

            db.putUser(userDAO);
            return Response.status(Response.Status.OK).entity(user).build();
        }

        /**
         * Add user with Id to channel with Id
         */
        @PUT
        @Path("/{idUser}/{idChannel}")
        @Produces(MediaType.APPLICATION_JSON)
        public void  addUserToChannel(@PathParam("idUser") String idUser,@PathParam("idChannel") String idChannel) {
            Optional<UserDAO> csmItrU =db.getUserById(idUser).stream().findFirst();
            Optional<ChannelDAO> csmItrC =db.getChannelById(idChannel).stream().findFirst();

            if(!csmItrU.isEmpty() ||!csmItrC.isEmpty() ){
                ChannelDAO c = csmItrC.get();
                c.addUserToChannel(idUser);
                db.updateChannel(c.getId(),c);

                UserDAO u = csmItrU.get();
                u.addChannelToUser(idChannel);
                db.updateUser(u.getId(),u);
            }


            //throw new ServiceUnavailableException();
        }


        /**
         * Return the user with the id.
         */
        @GET
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getById(@PathParam("id") String id) {
        	 Optional<UserDAO> op =  db.getUserById(id).stream().findFirst();
        	if(op.isPresent()) {
        		UserDAO u = op.get();
                return Response.status(Response.Status.OK).entity(u.toUser()).build();
        	}else {
        		return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
        	}
            //throw new ServiceUnavailableException();
        }

        /**
         * Updates and returns the user if id is valid.
         */
        @PUT
        @Path("/{id}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response  updateById(@PathParam("id") String id,User user) {
        	Optional<UserDAO> op =  db.getUserById(id).stream().findFirst();
            if(op.isPresent()){
                UserDAO u =op.get();
                if (user.getName()!=null || !user.getName().equals("")){
                    u.setName(user.getName());
                }
                if(user.getPhotoId()!=null || !user.getPhotoId().equals("")){
                    u.setPhotoId(user.getPhotoId());
                }
                db.updateUser(id,u);
                return Response.status(Response.Status.OK).entity(u.toUser()).build();
            }
            else return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();

            //throw new ServiceUnavailableException();
        }

        @DELETE
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response  deleteById(@PathParam("id") String id) {
        	Optional<UserDAO> op =  db.getUserById(id).stream().findFirst();
            if(op.isPresent()){
                UserDAO u =op.get();
               db.delUser(u);
               return Response.status(Response.Status.OK).entity(u.toUser()).build();
            }else {
                return Response.status(Response.Status.NOT_FOUND).entity(Quotes.USER_NOT_FOUND).build();
	
            }
        }

        /**
         * Lists the ids of all users.
         */
        @GET
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getAll() {
            List<String> ids = new ArrayList<>();

            for (UserDAO u : db.getUsers()){
                ids.add(u.getId());
           }
            return Response.status(Response.Status.OK).entity(ids).build();
        }
}

