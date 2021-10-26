package scc.srv.Resources;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Put;
import com.azure.core.util.BinaryData;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import scc.data.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
import scc.utils.Hash;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;



@Path("/user")
public class UserResource {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        /**
         * Post a new user.The id of the user is its hash.
         */
        @POST
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public User create(User user) {
            UserDAO userDAO = new UserDAO(user);

            //adiciona user ao canal
            for(String id: user.getChannelIds()){
                ChannelDAO c =db.getChannelById(id).stream().findFirst().get();;
                if(c!=null){
                     c.addUserToChannel(user.getId());
                     db.updateChannel(c.getId(),c);
                }
            }

            db.putUser(userDAO);
            return user;
        }

        /**
         * Return the user with the id.
         */
        @GET
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_JSON)
        public User  getById(@PathParam("id") String id) {
            UserDAO u = db.getUserById(id).stream().findFirst().get();
            return u.toUser();
            //throw new ServiceUnavailableException();
        }

        /**
         * Updates and returns the user if id is valid.
         */
        @PUT
        @Path("/{id}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public User  updateById(@PathParam("id") String id,User user) {
            CosmosPagedIterable<UserDAO> uList = db.getUserById(id);
            UserDAO u = null;
            if(uList!=null ){
                u = uList.stream().findFirst().get();
                if (user.getId()!=null || !user.getId().equals("")){
                    u.setId(user.getId());
                }
                if (user.getName()!=null || !user.getName().equals("")){
                    u.setName(user.getName());
                }
                if(user.getPhotoId()!=null || !user.getPhotoId().equals("")){
                    u.setPhotoId(user.getPhotoId());
                }
                db.updateUser(id,u);
                return u.toUser();
            }
            else return null;

            //throw new ServiceUnavailableException();
        }

        @DELETE
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_JSON)
        public User  deleteById(@PathParam("id") String id) {
            CosmosPagedIterable<UserDAO> uList = db.getUserById(id);
            UserDAO u = null;
            if(uList!=null ){
                u = uList.stream().findFirst().get();
                UserDAO response = (UserDAO) db.delUser(u).getItem();
                if(response!=null){
                    return u.toUser();
                }
                return u.toUser();
            }
             return null;
        }

        /**
         * Lists the ids of all users.
         */
        @GET
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public List<String> getAll() {
            List<String> ids = new ArrayList<>();

            for (UserDAO u : db.getUsers()){
                ids.add(u.getId());
           }
            return ids;
        }
}

