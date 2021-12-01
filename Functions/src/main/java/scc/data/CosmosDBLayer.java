package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.data.Channel.ChannelDAO;
import scc.data.Garbage.Garbage;
import scc.data.Message.MessageDAO;
import scc.data.User.UserDAO;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");//;
	private static final String DB_NAME = System.getenv("COSMOSDB_DATABASE");//;
	private static final String DB_KEY = System.getenv("COSMOSDB_KEY");//
	
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
	private CosmosContainer channels;
	private CosmosContainer garbage;

	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		messages= db.getContainer("messages");
		channels = db.getContainer("channels");
        garbage = db.getContainer("garbage");
		
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
	public CosmosItemResponse<MessageDAO> updateMessage(String id,MessageDAO message) {
		init();
		PartitionKey key = new PartitionKey(message.getChannelId());
		return messages.replaceItem(message,id,key,new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<MessageDAO> updateMessageChannel(String id,String pkey,MessageDAO message) {
		init();
		PartitionKey key = new PartitionKey(pkey);
		messages.deleteItem(id,key,new CosmosItemRequestOptions());
		return messages.createItem(message,new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<MessageDAO> getMessageById( String id) {
		init();
		return messages.queryItems("SELECT * FROM messages WHERE messages.id=\"" + id + "\"", new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessages(int offset, int limit) {
		init();
        String offString=" OFFSET 0";
        String limString=" LIMIT 20";
        if (offset!=0){
            offString=" OFFSET "+offset;
        }
        if (limit!=0){
            limString=" LIMIT "+limit;
        }
        String query = "SELECT * FROM messages";
        return channels.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}
	//------------------------------Channels------------------------------


	public CosmosItemResponse<ChannelDAO> addUserToChannel(String idChannel, String idUser) {
		init();
		PartitionKey key = new PartitionKey( idChannel);
		return channels.patchItem(idChannel, key, CosmosPatchOperations.create().add("/members/0",idUser), ChannelDAO.class);
	}

	public CosmosItemResponse<ChannelDAO> putChannel(ChannelDAO channel) {
		init();
		return channels.createItem(channel);
	}


	public CosmosPagedIterable<ChannelDAO> getChannels(int offset, int limit) {
		init();
        String offString=" OFFSET 0";
        String limString=" LIMIT 20";
        if (offset!=0){
            offString=" OFFSET "+offset;
        }
        if (limit!=0){
            limString=" LIMIT "+limit;
        }
        String query = "SELECT * FROM channels";
        return channels.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosPagedIterable<ChannelDAO> getChannelById( String id) {
		init();
		return channels.queryItems("SELECT * FROM channels WHERE channels.id=\"" + id + "\"", new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosItemResponse<Object> delChannelById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return channels.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<ChannelDAO> updateChannel(String id,ChannelDAO channel) {
		init();
		PartitionKey key = new PartitionKey( id);
		return channels.replaceItem(channel,id,key,new CosmosItemRequestOptions());
	}







	//------------------------------Users------------------------------

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}

	public CosmosItemResponse<UserDAO> addChannelToUser(String idUser,String idChannel) {
		init();
		PartitionKey key = new PartitionKey( idUser);
		return users.patchItem(idUser, key, CosmosPatchOperations.create().add("/channelIds/0",idChannel), UserDAO.class);
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
	
	public CosmosPagedIterable<UserDAO> getUserByUsername( String username) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.name=\"" + username + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers(int offset,int limit) {
		init();
		String offString=" OFFSET 0";
		String limString=" LIMIT 20";
		if (offset!=0){
			offString=" OFFSET "+offset;
		}
		if (limit!=0){
			limString=" LIMIT "+limit;
		}
		String query ="SELECT * FROM Users ORDER BY Users.id" ;//LIMIT 20";
		//SELECT * FROM Users ORDER BY Users.id OFFSET 20 LIMIT 10
		return users.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessages(int offset,int limit, String idChannel) {
		init();
		String offString=" OFFSET 0";
		String limString=" LIMIT 20";
		if (offset!=0){
			offString=" OFFSET "+offset;
		}
		if (limit!=0){
			limString=" LIMIT "+limit;
		}

		String query ="SELECT * FROM messages WHERE messages.channelId=\"" + idChannel +"\" ORDER BY messages.id "   ;
		//SELECT * FROM Users ORDER BY Users.id OFFSET 20 LIMIT 10
		return messages.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}



	//---------------------- GARBAGE ---------------------------//
    public CosmosItemResponse<Garbage> putGarbage(Garbage gb) {
        init();
        return garbage.createItem(gb);
    }

	public CosmosPagedIterable<Garbage> GetGarbage( String id) {
		init();
		return garbage.queryItems("SELECT * FROM garbage WHERE garbage.id=" + id , new CosmosQueryRequestOptions(), Garbage.class);
	}

	public CosmosItemResponse<Object> removeGarbage( String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return garbage.deleteItem(id,key, new CosmosItemRequestOptions());
	}



	public CosmosPagedIterable<ChannelDAO> getChannelsWithUser(String idUser) {
		init();
		String query = "SELECT * FROM channels where ARRAY_CONTAINS(channels.members, '"+idUser+"')";
		return channels.queryItems(query, new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessagesWithUser(String idUser) {
		init();
		String query = "SELECT * FROM messages where messages.user ='"+idUser+"'";
		return messages.queryItems(query, new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessagesRespondingTo(String messageId) {
		init();
		String query = "SELECT * FROM messages where messages.replyTo ='"+messageId+"'";
		return messages.queryItems(query, new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<MessageDAO> getMessagesWithChannel(String channelId) {
		init();
		String query = "SELECT * FROM messages where messages.channelId ='"+channelId+"'";
		return messages.queryItems(query, new CosmosQueryRequestOptions(), MessageDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsersWithChannel(String channelId) {
		init();
		String query = "SELECT * FROM users where ARRAY_CONTAINS(users.channelIds, '"+channelId+"')";
		return users.queryItems(query, new CosmosQueryRequestOptions(), UserDAO.class);
	}


	public void close() {
		client.close();
	}
	
	
}
