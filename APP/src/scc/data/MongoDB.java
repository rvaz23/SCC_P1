package scc.data;

import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import scc.data.Channel.ChannelDAO;
import scc.data.Garbage.Garbage;
import scc.data.Message.MessageDAO;
import scc.data.User.User;
import scc.data.User.UserDAO;


import java.util.ArrayList;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDB {
	private static final String CONNECTION_URL = "mongodb://"+System.getenv("MONGO_URL");//;
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
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
		CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
		db = mongoClient.getDatabase(DB_NAME).withCodecRegistry(pojoCodecRegistry);
		users = db.getCollection("users", UserDAO.class);
		messages= db.getCollection("messages",MessageDAO.class);
		channels = db.getCollection("channels",ChannelDAO.class);
        garbage = db.getCollection("garbage",Garbage.class);
		
	}
	//------------------------------Messages------------------------------

	public String getDB(){
		return mongoClient.listDatabaseNames().first();
	}


    public Boolean delMessageById(String id) {
        init();
        Bson query = eq("id", id);
        DeleteResult res = messages.deleteOne(query);
        return res.wasAcknowledged() ;
	}
	public Boolean delMessage(MessageDAO message) {
        init();
        Bson filter = eq("id", message.getId());
        DeleteResult result = messages.deleteOne(filter);
        return result.wasAcknowledged() ;
	}
	public MessageDAO putMessage(MessageDAO message) {
		init();
        InsertOneResult result=messages.insertOne(MessageDAO.toDBObject(message));
        if (result.wasAcknowledged())
            return message;
        else
            return null;
	}

	public MessageDAO getMessageById( String id) {
        init();
        return (MessageDAO) messages.find(eq("id", id)).first();
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
        return null;//messages.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}
	//------------------------------Channels------------------------------


	public CosmosItemResponse<ChannelDAO> addUserToChannel(String idChannel, String idUser) {
		init();
		PartitionKey key = new PartitionKey( idChannel);
		return null;//channels.patchItem(idChannel, key, CosmosPatchOperations.create().add("/members/0",idUser), ChannelDAO.class);
	}

    public CosmosItemResponse<ChannelDAO> removeUserFromChannel(String idChannel, String idUser) {
        init();
        PartitionKey key = new PartitionKey( idChannel);
        return null;//channels.patchItem(idChannel, key, CosmosPatchOperations.create().remove("/members/" + idUser), ChannelDAO.class);
    }

	public CosmosItemResponse<ChannelDAO> putChannel(ChannelDAO channel) {
		init();
		return null;//channels.createItem(channel);
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
        return null;//channels.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosPagedIterable<ChannelDAO> getChannelById( String id) {
		init();
		return null;//channels.queryItems("SELECT * FROM channels WHERE channels.id=\"" + id + "\"", new CosmosQueryRequestOptions(), ChannelDAO.class);
	}

	public CosmosItemResponse<Object> delChannelById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return null;//channels.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<ChannelDAO> updateChannel(String id,ChannelDAO channel) {
		init();
		PartitionKey key = new PartitionKey( id);
		return null;//UserDAOchannels.replaceItem(channel,id,key,new CosmosItemRequestOptions());
	}





	//------------------------------Users------------------------------

	public UserDAO putUser(UserDAO user) {
		init();
		InsertOneResult result=users.insertOne(user);
		if (result.wasAcknowledged())
			return user;
		else
			return null;

	}

	public UserDAO getUserById( String id) {
		init();
		return (UserDAO) users.find(eq("id", id)).first();
	}

	public boolean delUser(UserDAO user) {
		init();
		Bson filter = eq("id", user.getId());
		DeleteResult result = users.deleteOne(filter);
		return result.wasAcknowledged() ;
	}

	public boolean updateUser(String id,UserDAO user) {
		init();
		Bson query = eq("id",id);
		UpdateResult updateResult = users.replaceOne(query, user);
		return updateResult.wasAcknowledged();
	}

	public UserDAO getUserByUsername( String username) {
		init();
		UserDAO user = (UserDAO) users.find(eq("name", username)).first();
		return user;
	}


	public boolean delUserById(String id) {
		init();
		Bson query = eq("id", id);
		DeleteResult res = users.deleteOne(query);
		return res.wasAcknowledged() ;
	}





	public UserDAO addChannelToUser(String idUser, String idChannel) {
		init();
		UserDAO somebody = (UserDAO) users.find(eq("id", idUser)).first();
		somebody.addChannel(idChannel);
		Bson query = eq("id",idUser);
		users.replaceOne(query, somebody);
		return somebody;
	}

    public UserDAO removeChannelFromUser(String idUser, String idChannel) {
        init();
		UserDAO somebody = (UserDAO) users.find(eq("id", idUser)).first();
		if(somebody!=null){
			somebody.removeChannel(idChannel);
			Bson query = eq("id",idUser);
			users.replaceOne(query, somebody);
			return somebody;
		}

		return null;
    }


	


	public List<UserDAO> getUsers(int offset, int limit) {
		init();
		String offString=" OFFSET 0";
		int lim =20;
		if (offset!=0){
		}
		if (limit!=0){
			lim = limit;
		}
		List<UserDAO> list = new ArrayList<>();
		 users.find().skip(offset).limit(lim).into(list);

		//SELECT * FROM Users ORDER BY Users.id OFFSET 20 LIMIT 10
		return list;//users.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), UserDAO.class);
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
		return null;//messages.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}


	////////////////////////////////////7

    public CosmosItemResponse<MessageDAO> updateMessage(String id, MessageDAO message) {
        init();
        PartitionKey key = new PartitionKey( id);
        return null;//messages.replaceItem(message,id,key,new CosmosItemRequestOptions());
    }

    public CosmosItemResponse<Garbage> putGarbage(Garbage gb) {
        init();
        return null;//garbage.createItem(gb);
    }

	public void close() {
		mongoClient.close();
	}
	
	
}
