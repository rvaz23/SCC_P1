package scc.data;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = "https://scc52656db.documents.azure.com:443/";
	private static final String DB_KEY = "ERc01dQzrml0sg2xw6VETFPOpATzAq6QcexlLOF6PXPRkbHFLu0dHWy57yyEBfPVwDLJ3Auiv0AQRLU4gb2RyQ==";
	private static final String DB_NAME = "scc52656db";
	
	private static CosmosDBLayer instance;

	public static synchronized CosmosDBLayer getInstance() {
		if( instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder()
		         .endpoint(CONNECTION_URL)
		         .key(DB_KEY)
		         .gatewayMode()		// replace by .directMode() for better performance
		         .consistencyLevel(ConsistencyLevel.SESSION)
		         .connectionSharingAcrossClientsEnabled(true)
		         .contentResponseOnWriteEnabled(true)
		         .buildClient();
		instance = new CosmosDBLayer( client);
		return instance;
		
	}
	
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer messages;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		messages= db.getContainer("messages");
		
	}
	//------------------------------Messages------------------------------

	public CosmosItemResponse<Object> delMessageById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return messages.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<Object> delMessage(MessageDAO message) {
		init();
		return messages.deleteItem(message, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<MessageDAO> putMessage(MessageDAO message) {
		init();
		return messages.createItem(message);
	}

	public CosmosPagedIterable<MessageDAO> getMessageById( String id) {
		init();
		return messages.queryItems("SELECT * FROM messages WHERE messages.id=\"" + id + "\"", new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessages() {
		init();
		return messages.queryItems("SELECT * FROM messages ", new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	//------------------------------Users------------------------------

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<Object> delUser(MessageDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}

    public CosmosItemResponse<UserDAO> updateUser(String id,UserDAO user) {
        init();
		PartitionKey key = new PartitionKey( id);
        return users.replaceItem(user,id,key,new CosmosItemRequestOptions());
    }

	public CosmosPagedIterable<UserDAO> getUserById( String id) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public void close() {
		client.close();
	}
	
	
}
