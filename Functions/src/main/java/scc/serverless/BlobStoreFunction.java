package scc.serverless;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.BlobStorage.Blob;
import scc.cache.RedisCache;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Blob Trigger.
 */
public class BlobStoreFunction {

    @FunctionName("ReplicateContent")
    public void ReplicateBlob(@BlobTrigger(name = "ReplicateContent",
            dataType = "binary",
            path = "images/{name}",
            connection = "BlobStoreConnection")
                                      byte[] content,
                              @BindingName("name") String imName,
                              final ExecutionContext context) {
        //Verificar se conteudo existe nos outros containers

        for (String conn : Blob.SetConnections()) {
            BlobContainerClient containerClient = new BlobContainerClientBuilder()
                    .connectionString(conn)
                    .containerName("images")
                    .buildClient();
            BlobClient blob = containerClient.getBlobClient(imName);
            if(!blob.exists()){
                BinaryData binaryData = BinaryData.fromBytes(content);
                blob.upload(binaryData);
            }
        }
    }

}
