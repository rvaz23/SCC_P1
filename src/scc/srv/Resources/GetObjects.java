package scc.srv.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import scc.cache.RedisCache;
import scc.data.*;

import java.util.Optional;

public class GetObjects {

    private static CosmosDBLayer db = CosmosDBLayer.getInstance();
    private static RedisCache cache = RedisCache.getCachePool();

    public static User getUserIfExists(String idUser) throws JsonProcessingException {
        User user = cache.getUser(idUser);
        if(user==null){
            Optional<UserDAO> op = db.getUserById(idUser).stream().findFirst();
            if (op.isPresent()) {
                user = op.get().toUser();
                cache.setUser(user);
            }
        }
        return user;
    }

    public static User getUserIfExistsByName(String name) throws JsonProcessingException {
        //User user = cache.getUser(idUser);
        User user = null;
        if(user==null){
            Optional<UserDAO> op = db.getUserByUsername(name).stream().findFirst();
            if (op.isPresent()) {
                user = op.get().toUser();
                cache.setUser(user);
            }
        }
        return user;
    }

    public static Message getMessageIfExists(String idMessage) throws JsonProcessingException {
        Message message = cache.getMessage(idMessage);
        if(message==null){
            Optional<MessageDAO> op = db.getMessageById(idMessage).stream().findFirst();
            if (op.isPresent()) {
                message = op.get().toMessage();
                cache.setMessage(message);
            }
        }
        return message;
    }

    public static Channel getChannelIfExists(String idChannel) throws JsonProcessingException {
        Channel channel = cache.getChannel(idChannel);
        if(channel==null){
            Optional<ChannelDAO> op = db.getChannelById(idChannel).stream().findFirst();
            if (op.isPresent()) {
                channel = op.get().toChannel();
                cache.setChannel(channel);
            }
        }
        return channel;
    }


}
