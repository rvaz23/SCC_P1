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
        InsertOneResult result=messages.insertOne(message);
        if (result.wasAcknowledged())
            return message;
        else
            return null;
	}

	public MessageDAO getMessageById( String id) {
        init();
        return (MessageDAO) messages.find(eq("id", id)).first();
	}

	public List<MessageDAO> getAllMessages(int offset,int limit) {
		init();
		int lim =20;
		int off=0;
		if (offset!=0){
			off=offset;
		}
		if (limit!=0){
			lim = limit;
		}
		List<MessageDAO> list = new ArrayList<>();
		messages.find().skip(off).limit(lim).into(list);
		return list;
	}


	public List<MessageDAO> getMessages(int offset,int limit, String idChannel) {
		init();
		int lim =20;
		int off=0;
		if (offset!=0){
			off=offset;
		}
		if (limit!=0){
			lim = limit;
		}
		List<MessageDAO> list = new ArrayList<>();
		messages.find(eq("channel", idChannel)).skip(off).limit(lim).into(list);
		return list;//messages.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), MessageDAO.class);
	}
	//------------------------------Channels------------------------------


	public ChannelDAO addUserToChannel(String idChannel, String idUser) {
		init();
		ChannelDAO channel = (ChannelDAO) channels.find(eq("id", idChannel)).first();
		ArrayList<String> l =channel.getMembers();
		l.add(idUser);
		channel.setMembers(l);
		Bson query = eq("id",idChannel);
		channels.replaceOne(query, channel);
		return channel;
	}

    public ChannelDAO removeUserFromChannel(String idChannel, String idUser) {
		init();
		ChannelDAO channel = (ChannelDAO) channels.find(eq("id", idChannel)).first();
		if(channel!=null){
			ArrayList<String> l =channel.getMembers();
			l.remove(idUser);
			channel.setMembers(l);
			Bson query = eq("id",idChannel);
			channels.replaceOne(query, channel);
			return channel;
		}

		return null;
    }

	public ChannelDAO putChannel(ChannelDAO channel) {
		init();
		InsertOneResult result=channels.insertOne(channel);
		if (result.wasAcknowledged())
			return channel;
		else
			return null;
	}


	public List<ChannelDAO> getChannels(int offset, int limit) {
		init();
		int lim =20;
		int off=0;
		if (offset!=0){
			off=offset;
		}
		if (limit!=0){
			lim = limit;
		}
		List<ChannelDAO> list = new ArrayList<>();
		channels.find().skip(off).limit(lim).into(list);
		return list;
	}

	public ChannelDAO getChannelById( String id) {
		init();
		return (ChannelDAO) channels.find(eq("id", id)).first();	}

	public Boolean delChannelById(String id) {
		init();
		Bson query = eq("id", id);
		DeleteResult res = channels.deleteOne(query);
		return res.wasAcknowledged() ;
	}

	public boolean updateChannel(String id,ChannelDAO channel) {
		Bson query = eq("id",id);
		UpdateResult updateResult = channels.replaceOne(query, channel);
		return updateResult.wasAcknowledged();
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
		int lim =20;
		int off=0;
		if (offset!=0){
			off=offset;
		}
		if (limit!=0){
			lim = limit;
		}
		List<UserDAO> list = new ArrayList<>();
		 users.find().skip(off).limit(lim).into(list);

		//SELECT * FROM Users ORDER BY Users.id OFFSET 20 LIMIT 10
		return list;//users.queryItems(query+offString+limString, new CosmosQueryRequestOptions(), UserDAO.class);
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
