package test;

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Test1 {

	public static void main(String[] args) throws JSONException, IOException {
		
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "192.168.78.19", 6379, 60, "spinku12345");
		
		try (Jedis jedis = pool.getResource()) {
				String data = jedis.get("docid");
				
				if(data != null)
				{
					//dokumen lagi ada yang proses
					return;
				}
				else
				{
					//dokumen bisa diproses, set ke redis, jedis.setex("docid", "waktu key docid dihapus otomatis dari redis", "true");
					jedis.setex("docid", 180, "true");
					
				}
			}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			pool.close();
		}
	}
}
