package scc.BlobStorage;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class Blob {


    public  static List<String> SetConnections() {
        ArrayList<String> conn = new ArrayList<String>();
        String US_WEST =System.getenv("BLOB_WESTUS");
        if (US_WEST!=null){
            conn.add(US_WEST);
        }
        String N_EUROPE =System.getenv("BLOB_NEURO");
        if (N_EUROPE!=null){
            conn.add(N_EUROPE);
        }
        return conn;
    }

}
