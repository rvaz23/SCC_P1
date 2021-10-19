package scc.srv.Resources;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import scc.utils.Hash;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserResource {

    @Path("/users")
    public class MediaResource
    {
        /**
         * Post a new image.The id of the image is its hash.
         */
        @POST
        @Path("/create")
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        @Produces(MediaType.APPLICATION_JSON)
        public String create() {
            //TODO
            return null;
        }

        /**
         * Return the contents of an image. Throw an appropriate error message if
         * id does not exist.
         */
        @GET
        @Path("/{id}")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        public byte[] getById(@PathParam("id") String id) {
            //TODO
            throw new ServiceUnavailableException();
        }

        /**
         * Lists the ids of images stored.
         */
        @GET
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public List<String> getAll() {
            //TODO
            return null;
        }
    }

}
