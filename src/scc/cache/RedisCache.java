package scc.cache;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.data.User;

public class RedisCache {
	private static final String RedisHostname = "lab152656.redis.cache.windows.net";
	private static final String RedisKey = "3cTDiucqBEL39BHdJZyyW44mqDtP6IUZFAzCaMWf8PM=";
	
	private static RedisCache instance;
	
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
	
	private static JedisPool redis;
	private Jedis client;
	ObjectMapper mapper;
	
	public RedisCache(JedisPool redis) {
		this.redis = redis;
	}
	
	private synchronized void init() {
		if( client != null)
			return;
		client = redis.getResource();
		mapper = new ObjectMapper();
		
	}
	
	
	public String setUser(User user) throws JsonProcessingException {
		init();
		return client.set("user:"+user.getId(), mapper.writeValueAsString(user));	
	}
	
	public User getUser(String id){
		init();
		String res = client.get("user:"+id);
		User user;
		try {
			user = mapper.readValue(res, User.class);
		} catch (Exception e) {
			return null;
		}
		return user;
	}
	
	public boolean deleteUser(String id) {
		init();
		long deleted =client.del("user:"+id);
		if (deleted>0) {
			return true;
		}else {
			return false;
		}
	}
	
	public String putSession(Session session) {
		init();
		String cookie = client.setex("cookie:"+session.getUid(),3600 ,session.getUser());
		return cookie;
	}
	
	public boolean verifySessionCookie(String cookie,String user) {
		init();
		if(!cookie.equals("") || !user.equals("")){
			String userCookie = client.get("cookie:"+cookie);
			if(userCookie!=null) {
				if(userCookie.equals(user))
					return true;
			}
		}

		return false;
	}
}
