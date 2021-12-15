package scc.srv.Resources;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobItem;
import scc.utils.Hash;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	Map<String,byte[]> map = new HashMap<String,byte[]>();
	String storageConnectionString = System.getenv("BlobStoreConnection");
	//String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=rvscc;AccountKey=uxX/JvXipvoolorUtuHCcxrBsEIOg3UhWDHBRJrO1ltPwjd4nOfe2/HZ5P8EygYoxXfqPA9VrVIzSynFs+cpQw==;EndpointSuffix=core.windows.net";
	BlobContainerClient containerClient = new BlobContainerClientBuilder()
			.connectionString(storageConnectionString)
			.containerName("images")
			.buildClient();
	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {


		/*
		String key = Hash.of(contents);
		BlobClient blob = containerClient.getBlobClient(key);


		// Upload contents from BinaryData (check documentation for other alternatives)
		if(!blob.exists()){
			InputStream stream = new ByteArrayInputStream(contents);
			//BinaryData binaryData = BinaryData.fromBytes(contents);
			blob.upload(stream,contents.length);
		}
			return key;



			public byte[] read(File file) throws IOException, FileTooBigException {
    if (file.length() > MAX_FILE_SIZE) {
        throw new FileTooBigException(file);
    }
    ByteArrayOutputStream ous = null;
    InputStream ios = null;
    try {
        byte[] buffer = new byte[4096];
        ous = new ByteArrayOutputStream();
        ios = new FileInputStream(file);
        int read = 0;
        while ((read = ios.read(buffer)) != -1) {
            ous.write(buffer, 0, read);
        }
    }finally {
        try {
            if (ous != null)
                ous.close();
        } catch (IOException e) {
        }

        try {
            if (ios != null)
                ios.close();
        } catch (IOException e) {
        }
    }
    return ous.toByteArray();
}


			*/
		String key = Hash.of(contents);

		try {
			File file = new File("media/"+key);
			if (file.createNewFile()) {
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(contents);
				return key;
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return "key";
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		/*BlobClient blob = containerClient.getBlobClient(id);
		if (blob.exists()){
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			blob.download(outputStream);

			//converting it to the inputStream to return
			final byte[] bytes = outputStream.toByteArray();
			//BinaryData data = blob.downloadContent();
			//return data.toBytes();
			return bytes;
		}*/

		byte[] file= null;
		try {
			file= Files.readAllBytes(java.nio.file.Path.of("media/"+id));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//TODO: complete !
		throw new ServiceUnavailableException();
	}
	
	@DELETE
	@Path("/{id}")
	public boolean delete(@PathParam("id") String id) {
		BlobClient blob = containerClient.getBlobClient(id);
		if (blob.exists()){
		blob.delete();
		return true;
		}else {
			return false;
		}
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		List<String> results= new ArrayList<String>();
		for (BlobItem blobItem : containerClient.listBlobs()) {
			results.add(blobItem.getName());
		}
		return results;
	}
}
