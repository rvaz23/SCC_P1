package scc.srv.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import scc.cache.RedisCache;
import scc.data.*;
import scc.data.Channel.Channel;
import scc.data.Channel.ChannelDAO;
import scc.data.Message.Message;
import scc.data.Message.MessageDAO;
import scc.data.User.User;
import scc.data.User.UserDAO;

import javax.ws.rs.core.Cookie;
import java.util.Optional;

public class GetObjects {

    private static MongoDB db = MongoDB.getInstance();
    private static RedisCache cache = RedisCache.getCachePool();


    public static String getCookie(Cookie session) {
        if (session == null) {
            return "";
        } else {
            return session.getValue();
        }
    }

    public static User getUserIfExists(String idUser) throws JsonProcessingException {
        User user = cache.getUser(idUser);
        if(user==null){
            UserDAO u = db.getUserById(idUser);
            if (u!=null) {
                user = u.toUser();
                cache.setUser(user);
            }
        }
        return user;
    }

    public static User getUserIfExistsByName(String name) throws JsonProcessingException {
        //User user = cache.getUser(idUser);
        User user = null;
        if(user==null){
           UserDAO u = db.getUserByUsername(name);
            if (u!=null) {
                user = u.toUser();
                cache.setUser(user);
            }
        }
        return user;
    }

    public static Message getMessageIfExists(String idMessage) throws JsonProcessingException {
        Message message = cache.getMessage(idMessage);
        if(message==null){
            MessageDAO m = db.getMessageById(idMessage);
            if (m!=null) {
                message = m.toMessage();
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
