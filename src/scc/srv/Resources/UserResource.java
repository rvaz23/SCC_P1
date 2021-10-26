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
        public User create(User user) {
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
            return user;
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
        public User getById(@PathParam("id") String id) {
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
            Optional<UserDAO> csmItr =db.getUserById(id).stream().findFirst();
            if(!csmItr.isEmpty() ){
                UserDAO u =csmItr.get();

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

