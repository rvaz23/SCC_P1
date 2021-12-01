package scc.serverless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import com.microsoft.azure.functions.*;
import scc.data.Channel.ChannelDAO;
import scc.data.CosmosDBLayer;
import scc.data.Garbage.Garbage;
import scc.data.Message.MessageDAO;
import scc.data.User.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Azure Functions with Timer Trigger.
 */
public class CosmosDBFunction {

    CosmosDBLayer db = CosmosDBLayer.getInstance();
    RedisCache cache = RedisCache.getCachePool();

    @FunctionName("cosmosDBGarbageComputation")
    public void updateMostRecentUsers(@CosmosDBTrigger(name = "cosmosTest",
            databaseName = "scc2122db52656",
            collectionName = "garbage",
            createLeaseCollectionIfNotExists = true,
            connectionStringSetting = "AzureCosmosDBConnection")
                                              String[] garbage,
                                      final ExecutionContext context) {
        for (String gId : garbage) {
            ObjectMapper mapper = new ObjectMapper();
            //mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            Garbage g = null;
            try {
                g = mapper.readValue(gId, Garbage.class);
            } catch (JsonProcessingException e) {
            }
            if (g != null) {
                if (g.getType().equals("USER"))
                    usermethod(g.getInternal_id());
                if (g.getType().equals("MESSAGE"))
                    messagemethod(g.getInternal_id());
                if (g.getType().equals("CHANNEL"))
                    channelmethod(g.getInternal_id());
                db.removeGarbage(g.getId());

            }
        }

    }

    private void channelmethod(String channelId) {
        List<MessageDAO> messages = db.getMessagesWithChannel(channelId).stream().collect(Collectors.toList());
        for (MessageDAO message : messages) {
            String previousKey = message.getChannelId();
            message.setChannelId("DELETED CHANNEL");
            MessageDAO msg = db.updateMessageChannel(message.getId(), previousKey, message).getItem();
            try{
                cache.setMessage(msg.toMessage());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
        List<UserDAO> users = db.getUsersWithChannel(channelId).stream().collect(Collectors.toList());
        for (UserDAO user : users) {
            ArrayList<String> channels = user.getChannelIds();
            channels.remove(channelId);
            user.setChannelIds(channels);
            UserDAO userDAO =db.updateUser(user.getId(), user).getItem();
            cache.setUser(userDAO.toUser());
        }

    }

    private void messagemethod(String messageId) {
        List<MessageDAO> messages = db.getMessagesRespondingTo(messageId).stream().collect(Collectors.toList());
        for (MessageDAO message : messages) {
            message.setReplyTo("DELETED MESSAGE");
            MessageDAO messageDAO=db.updateMessage(message.getId(), message).getItem();
            try {
                cache.setMessage(message.toMessage());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }


    private void usermethod(String userId) {
        List<ChannelDAO> channels = CosmosDBLayer.getInstance().getChannelsWithUser(userId).stream().collect(Collectors.toList());
        for (ChannelDAO channel : channels) {
            ArrayList<String> members = channel.getMembers();
            members.remove(userId);
            channel.setMembers(members);
            if (channel.getOwner().equals(userId) || channel.getMembers().isEmpty()){
                db.delChannelById(channel.getId());
                cache.deleteChannel(channel.getId());
                db.putGarbage(new Garbage("CHANNEL",channel.getId()));
            }else {
                ChannelDAO channelDAO =db.updateChannel(channel.getId(), channel).getItem();
                cache.setChannel(channelDAO.toChannel());
            }
        }
        List<MessageDAO> messages = CosmosDBLayer.getInstance().getMessagesWithUser(userId).stream().collect(Collectors.toList());
        for (MessageDAO message : messages) {
            message.setUser("DELETED USER");
            MessageDAO messageDAO=db.updateMessage(message.getId(), message).getItem();
            try {
                cache.setMessage(messageDAO.toMessage());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

}
