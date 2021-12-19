package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import scc.data.Channel.ChannelDAO;
import scc.data.Garbage.Garbage;
import scc.data.Message.MessageDAO;
import scc.data.User.UserDAO;


import java.net.UnknownHostException;

import static com.mongodb.client.model.Filters.eq;

public class MongoDB {
	private static final String CONNECTION_URL = System.getenv("MONGO_URL");//;
	private static final String DB_KEY = System.getenv("COSMOSDB_KEY");//;
	private static final String DB_NAME = System.getenv("MONGO_DATABASE");//

	private static MongoDB instance;

	public static synchronized MongoDB getInstance() {
		if( instance != null)
			return instance;

		MongoClient mongoClient = MongoClients.create(CONNECTION_URL);

		instance = new MongoDB( mongoClient);
		return instance;

	}

	MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection users;
	private MongoCollection messages;
	private MongoCollection channels;
	private MongoCollection garbage;

	public MongoDB(MongoClient client) {
		this.mongoClient = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = mongoClient.getDatabase(DB_NAME);
		users = db.getCollection("users",UserDAO.class);
		messages= db.getCollection("messages",MessageDAO.class);
		channels = db.getCollection("channels",ChannelDAO.class);
        garbage = db.getCollection("garbage",Garbage.class);
		
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
        return messages.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}
	//------------------------------Channels------------------------------


	public CosmosItemResponse<ChannelDAO> addUserToChannel(String idChannel, String idUser) {
		init();
		PartitionKey key = new PartitionKey( idChannel);
		return channels.patchItem(idChannel, key, CosmosPatchOperations.create().add("/members/0",idUser), ChannelDAO.class);
	}

    public CosmosItemResponse<ChannelDAO> removeUserFromChannel(String idChannel, String idUser) {
        init();
        PartitionKey key = new PartitionKey( idChannel);
        return channels.patchItem(idChannel, key, CosmosPatchOperations.create().remove("/members/" + idUser), ChannelDAO.class);
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

	public boolean delUserById(String id) {
		init();
		Bson query = eq("id", id);
		DeleteResult res = users.deleteOne(query);
		return res.wasAcknowledged() ;
	}
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	public UserDAO putUser(UserDAO user) {
		init();
		InsertOneResult result=users.insertOne(UserDAO.toDBObject(user));
		if (result.wasAcknowledged())
			return user;
		else
			return null;

	}

	public CosmosItemResponse<UserDAO> addChannelToUser(String idUser,String idChannel) {
		init();
		PartitionKey key = new PartitionKey( idUser);
		return users.patchItem(idUser, key, CosmosPatchOperations.create().add("/channelIds/0",idChannel), UserDAO.class);
	}

    public CosmosItemResponse<UserDAO> removeChannelFromUser(String idUser,String idChannel) {
        init();
        PartitionKey key = new PartitionKey(idUser);
        return users.patchItem(idUser, key, CosmosPatchOperations.create().remove("/channelIds/" + idChannel), UserDAO.class);
    }

    public CosmosItemResponse<UserDAO> updateUser(String id,UserDAO user) {
        init();
		PartitionKey key = new PartitionKey( id);
        return users.replaceItem(user,id,key,new CosmosItemRequestOptions());
    }

	public CosmosPagedIterable<UserDAO> getUserById( String id) {
		init();
		UserDAO somebody = (UserDAO) users.find(eq("id", id)).first();
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

    public CosmosItemResponse<MessageDAO> updateMessage(String id, MessageDAO message) {
        init();
        PartitionKey key = new PartitionKey( id);
        return messages.replaceItem(message,id,key,new CosmosItemRequestOptions());
    }

    public CosmosItemResponse<Garbage> putGarbage(Garbage gb) {
        init();
        return garbage.createItem(gb);
    }

	public void close() {
		mongoClient.close();
	}
	
	
}
