package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.data.Channel.Channel;
import scc.data.Message.Message;
import scc.data.User.User;

public class RedisCache {

	private static final String RedisHostname = System.getenv("REDIS_URL");
	private static final String RedisKey = System.getenv("REDIS_KEY");
	private static final boolean cacheUse = Boolean.parseBoolean(System.getenv("USE_CACHE"));
	//private static final String RedisHostname = "lab152656.redis.cache.windows.net";
	//private static final String RedisKey = "3cTDiucqBEL39BHdJZyyW44mqDtP6IUZFAzCaMWf8PM=";
	
	private static RedisCache instance;
	private static JedisPool redis;
	
	public synchronized static RedisCache getCachePool() {
		if( instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		redis = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);
		instance = new RedisCache(redis);
		return instance;
		
	}

	ObjectMapper mapper;
	
	public RedisCache(JedisPool redis) {
		this.redis=redis;
		mapper = new ObjectMapper();
	}


	private synchronized Jedis init() {
		try (Jedis jedis = getCachePool().redis.getResource()) {
			return jedis;
		}
	}
	
	


	public String setChannel(Channel channel) throws  JsonProcessingException{
		if (!cacheUse)
			return null;
		Jedis client =init();
		String result= client.set("channel:"+ channel.getId(), mapper.writeValueAsString(channel));
		client.close();
		return result;
	}

	public Channel getChannel(String id){
		if (!cacheUse)
			return null;
		Jedis client =init();
		String res= client.get("channel:"+id);
		Channel channel;
		try{
			channel= mapper.readValue(res,Channel.class);
		}catch (Exception e){
			return null;
		}
		client.close();
		return channel;
	}

	public boolean deleteChannel(String id){
		if (!cacheUse)
			return false;
		Jedis client =init();
		long deleted = client.del("channel:"+id);
		if(deleted>0){
			client.close();
			return true;
		}else{
			client.close();
			return false;
		}
	}

	//------------------------------ User --------------------------------
	public User getUser(String id){
		if(!cacheUse)
			return null;
		Jedis client =init();
		String res = client.get("user:"+id);
		User user;
		try {
			user = mapper.readValue(res, User.class);
		} catch (Exception e) {
			client.close();
			return null;
		}
		client.close();
		return user;
	}

	public String setUser(User user) throws JsonProcessingException {
		if (!cacheUse)
			return null;
		Jedis client =init();
		String result= client.set("user:"+user.getId(), mapper.writeValueAsString(user));
		client.close();
		return result;
	}
	
	public boolean deleteUser(String id) {
		if (!cacheUse)
			return false;
		Jedis client =init();
		long deleted =client.del("user:"+id);
		client.close();
		if (deleted>0) {
			return true;
		}else {
			return false;
		}
	}


	//------------------------------ Message --------------------------------
	public String setMessage(Message message) throws JsonProcessingException {
		if (!cacheUse)
			return null;
		Jedis client =init();
		String result= client.set("message:"+message.getId(), mapper.writeValueAsString(message));
		client.close();
		return result;
	}

	public Message getMessage(String id){
		if (!cacheUse)
			return null;
		Jedis client =init();
		String res = client.get("message:"+id);
		Message message;
		try {
			message = mapper.readValue(res, Message.class);
		} catch (Exception e) {
			client.close();
			return null;
		}
		client.close();
		return message;

	}




	//------------------------------- Auth -----------------------------------
	
	public String putSession(Session session) {
		Jedis client =init();
		String cookie = client.setex("cookie:"+session.getUid(),3600 ,session.getUser());
		client.close();
		return cookie;
	}
	
	public boolean verifySessionCookie(String cookie,String user) {
		Jedis client =init();
		if(!cookie.equals("") || !user.equals("")){
			String userCookie = client.get("cookie:"+cookie);
			if(userCookie!=null) {
				if(userCookie.equals(user)) {
					client.close();
					return true;
				}
			}
		}
		client.close();
		return false;
	}
}
