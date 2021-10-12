package scc.utils;

import java.nio.file.Path;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;


public class UploadToStorage {

	public static void main(String[] args) {
		if( args.length != 1) {
			System.out.println( "Use: java scc.utils.UploadToStorage filename");
		}
		String filename = args[0];
		

		// Get connection string in the storage access keys page
		String storageConnectionString = "ZVZ2knY7OT+3hSmDnmx2B5aeN1dmPlUh8AYtIwx5tj7oLD7LvIbFgxvomCppmropkOhZl539nwsMk3POMRW4jg==";

		try {
			BinaryData data = BinaryData.fromFile(Path.of(filename));

			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
														.connectionString(storageConnectionString)
														.containerName("images")
														.buildClient();

			// Get client to blob
			BlobClient blob = containerClient.getBlobClient( filename);

            for (BlobItem blobItem : containerClient.listBlobs()) {
                System.out.println("\t" + blobItem.getName());
            }

			// Upload contents from BinaryData (check documentation for other alternatives)
			blob.upload(data);
			
			System.out.println( "File updloaded : " + filename);


		} catch( Exception e) {
			e.printStackTrace();
		}
	}


    public void list(String filename){
        // Get connection string in the storage access keys page
        String storageConnectionString = "ZVZ2knY7OT+3hSmDnmx2B5aeN1dmPlUh8AYtIwx5tj7oLD7LvIbFgxvomCppmropkOhZl539nwsMk3POMRW4jg==";

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName("images")
                .buildClient();

        BlobClient blob = containerClient.getBlobClient( filename);

        for (BlobItem blobItem : containerClient.listBlobs()) {
            System.out.println("\t" + blobItem.getName());
        }

    }
}
