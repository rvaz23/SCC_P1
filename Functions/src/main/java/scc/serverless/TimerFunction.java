package scc.serverless;

import java.text.SimpleDateFormat;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */

//timer para limpar o garbage
//Timer para limpar os increments da cache para o trending channel
public class TimerFunction {
    @FunctionName("periodic-compute")
    public void cosmosFunction( @TimerTrigger(name = "periodicSetTime", 
    								schedule = "0 * */1 * * *")
    				String timerInfo,
    				ExecutionContext context) {
		RedisCache.getCachePool().deleteTrending();
    }
}
