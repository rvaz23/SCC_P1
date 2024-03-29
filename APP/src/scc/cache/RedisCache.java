package scc.cache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.data.Channel.Channel;
import scc.data.Message.Message;
import scc.data.User.User;

import java.util.*;

public class RedisCache {

    private static final Logger logger = LogManager.getLogger(RedisCache.class);

    private static final String RedisHostname = System.getenv("REDIS_URL");
    private static final String RedisKey = System.getenv("REDIS_KEY");
    private static final boolean cacheUse = Boolean.parseBoolean(System.getenv("USE_CACHE"));
    //private static final String RedisHostname = "lab152656.redis.cache.windows.net";
    //private static final String RedisKey = "3cTDiucqBEL39BHdJZyyW44mqDtP6IUZFAzCaMWf8PM=";

    private static RedisCache instance;
    private static JedisPool redis;

    public synchronized static RedisCache getCachePool() {
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
        redis = new JedisPool(poolConfig,RedisHostname,6379,5000,false);
        //redis = new JedisPool(poolConfig, RedisHostname, 6379, 5000, RedisKey, false);
        instance = new RedisCache(redis);
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
            String result = client.setex("channel:" + channel.getId(),300 ,mapper.writeValueAsString(channel));
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
            result = client.setex("user:" + user.getId(),300 ,mapper.writeValueAsString(user));
            client.close();
            return result;
        } catch (Exception e) {
            return null;
        }

    }

    public String increment(Channel channel) {
        try (Jedis client = getCachePool().redis.getResource()) {
            if(client.get("toptrending:" + channel.getId()) ==null){
                client.setex("toptrending:" + channel.getId(),3600 ,mapper.writeValueAsString(0));
            }

            Long result= client.incr("toptrending:" + channel.getId());
            client.close();
            return result.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getTop5() {
        try (Jedis client = getCachePool().redis.getResource()) {
            Set<String> res = client.keys("toptrending:*");//verificar se é necessario tirar o "toptrending" ou n vem
            SortedMap<String, Set<String>> map = new TreeMap<>();

            for(String channelName: res){
                String counter = client.get(channelName);

                Set<String> a= map.get(counter);
                if(a==null || a.isEmpty()){
                    a= new HashSet<>();
                    a.add(channelName);
                    map.put(counter,a);
                }else{
                    a.add(channelName);
                    map.put(counter,a);
                }
            }

            List<String> result= new ArrayList<>(5);
            int i=0;
            while(i<5 && result.size()<=5 && !map.isEmpty()){
                String lkey = map.lastKey();
                Set<String> listChannels = map.get(lkey);
                for(String channel: listChannels){
                    String ch = channel.split(":")[1];
                    result.add(ch);
                    if(i==4){
                        return result;
                    }
                    if(i<5 && result.size()<=5){
                        i++;
                    }
                }
                map.remove(lkey);
            }

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
            String result = client.setex("message:" + message.getId(),300, mapper.writeValueAsString(message));
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


    //------------------------------- Auth -----------------------------------

    public String putSession(Session session) {
        try (Jedis client = getCachePool().redis.getResource()) {
            String cookie = client.setex("cookie:" + session.getUid(), 3600, session.getUser());
            client.close();
            return cookie;
        }catch (Exception e){
            return null;
        }
    }

    public boolean verifySessionCookie(String cookie, String user) {
        try (Jedis client = getCachePool().redis.getResource()) {
            if (!cookie.equals("") || !user.equals("")) {
                String userCookie = client.get("cookie:" + cookie);
                if (userCookie != null) {
                    if (userCookie.equals(user)) {
                        client.close();
                        return true;
                    }
                }
            }
            client.close();
            return false;
        }catch (Exception e){
            logger.info(e.getMessage());
            return false;
        }
    }
}
