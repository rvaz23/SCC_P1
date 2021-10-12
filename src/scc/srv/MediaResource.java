package scc.srv;

import scc.utils.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.core.util.BinaryData;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	Map<String,byte[]> map = new HashMap<String,byte[]>();
	String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=lab52656;AccountKey=aJeoanKvZB+ou9nWsIFWdvdVoq8CGliJVDduXBPJicbehuqMHvqrWzMGo4rOTJTlGF5dCcsDhHXYcdBZ6BKpkQ==;EndpointSuffix=core.windows.net";

	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		BlobContainerClient containerClient = new BlobContainerClientBuilder()
														.connectionString(storageConnectionString)
														.containerName("images")
														.buildClient();
														
	// Get client to blob
	String key = Hash.of(contents);
		BlobClient blob = containerClient.getBlobClient(key);

	// Upload contents from BinaryData (check documentation for other alternatives)
	BinaryData binaryData = BinaryData.fromBytes(contents);
			blob.upload(binaryData);	
		
		return "filename";												
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		byte[] content = map.get(id);
		if(content!=null){
			return content;
		}
		//TODO: complete !
		throw new ServiceUnavailableException();
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		return new ArrayList<String>( map.keySet());
	}
}
