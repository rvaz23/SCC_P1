package scc.srv.Resources;

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

public class UserResource {


    CosmosDBLayer db = CosmosDBLayer.getInstance();
    @Path("/user")
    public class MediaResource
    {
        /**
         * Post a new image.The id of the image is its hash.
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
         * Return the contents of an image. Throw an appropriate error message if
         * id does not exist.
         */
        @GET
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public User  getById(@PathParam("id") String id) {
            UserDAO u = db.getUserById(id).stream().findFirst().get();

            return u.toUser();
            //throw new ServiceUnavailableException();
        }

        /**
         * Lists the ids of users.
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

}
