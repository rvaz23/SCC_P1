package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.data.Channel.Channel;
import scc.data.Message.Message;
import scc.data.User.User;

import java.util.Set;


public class RedisCache {

		private static final String RedisHostname = System.getenv("REDIS_URL");
		private static final String RedisKey = System.getenv("REDIS_KEY");
		private static final boolean cacheUse = true;// Boolean.parseBoolean(System.getenv("USE_CACHE"));
		//private static final String RedisHostname = "lab152656.redis.cache.windows.net";
		//private static final String RedisKey = "3cTDiucqBEL39BHdJZyyW44mqDtP6IUZFAzCaMWf8PM=";

		private static scc.cache.RedisCache instance;
		private static JedisPool redis;

		public synchronized static scc.cache.RedisCache getCachePool() {
			if (instance != null)
				return instance;
			final JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(256);
			poolConfig.setMaxIdle(128);
			poolConfig.setMinIdle(16);
			poolConfig.setTestOnBorrow(true);
			poolConfig.setTestOnReturn(true);
			poolConfig.setTestWhileIdle(true);
			poolConfig.setNumTestsPerEvictionRun(3);
			poolConfig.setBlockWhenExhausted(true);
			redis = new JedisPool(poolConfig, RedisHostname, 6380, 5000, RedisKey, true);
			instance = new scc.cache.RedisCache(redis);
			return instance;

		}

		ObjectMapper mapper;

		public RedisCache(JedisPool redis) {
			this.redis = redis;
			mapper = new ObjectMapper();
		}


		public String setChannel(Channel channel) {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String result = client.setex("channel:" + channel.getId(),300, mapper.writeValueAsString(channel));
				client.close();
				return result;
			} catch (Exception e) {
				return null;
			}
		}

		public Channel getChannel(String id) {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String res = client.get("channel:" + id);
				Channel channel;
				channel = mapper.readValue(res, Channel.class);
				client.close();
				return channel;
			} catch (Exception e) {
				return null;
			}
		}

		public boolean deleteChannel(String id) {
			if (!cacheUse)
				return false;
			try (Jedis client = getCachePool().redis.getResource()) {
				long deleted = client.del("channel:" + id);
				client.close();
				if (deleted > 0) {
					return true;
				} else {
					return false;
				}

			} catch (Exception e) {
				return false;
			}
		}

		//------------------------------ User --------------------------------
		public User getUser(String id) {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String res = client.get("user:" + id);
				User user;
				user = mapper.readValue(res, User.class);
				return user;
			} catch (Exception e) {
				return null;
			}

		}

		public String setUser(User user) {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String result;
				result = client.setex("user:" + user.getId(), 300,mapper.writeValueAsString(user));
				client.close();
				return result;
			} catch (Exception e) {
				return null;
			}

		}

		public boolean deleteUser(String id) {
			if (!cacheUse)
				return false;
			try (Jedis client = getCachePool().redis.getResource()) {
				long deleted = client.del("user:" + id);
				client.close();
				if (deleted > 0) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}


		//------------------------------ Message --------------------------------
		public String setMessage(Message message) throws JsonProcessingException {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String result = client.setex("message:" + message.getId(), 300,mapper.writeValueAsString(message));
				client.close();
				return result;
			} catch (Exception e) {
				return null;
			}
		}

		public Message getMessage(String id) {
			if (!cacheUse)
				return null;
			try (Jedis client = getCachePool().redis.getResource()) {
				String res = client.get("message:" + id);
				Message message;
				message = mapper.readValue(res, Message.class);
				client.close();
				return message;
			} catch (Exception e) {
				return null;
			}
		}

		public boolean deleteMessage(String id) {
			if (!cacheUse)
				return false;
			try (Jedis client = getCachePool().redis.getResource()) {
				long deleted = client.del("message:" + id);
				client.close();
				if (deleted > 0) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

		public boolean deleteTrending() {
			try (Jedis client = getCachePool().redis.getResource()) {
				Set<String> res = client.keys("toptrending:*");
				long deleted=0;
				for (String key :res){
					deleted = client.del(key);
				}
				client.close();
				if (deleted > 0) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

}
