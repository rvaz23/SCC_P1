package scc.srv.Resources;

import com.azure.core.annotation.BodyParam;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
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

