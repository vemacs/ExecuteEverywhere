package me.vemacs.executeeverywhere.common;

import com.google.common.collect.ObjectArrays;
import lombok.Data;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

@Data
public class EEConfiguration {
    private final List<String> groups;
    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;

    public String[] getRedisChannels() {
        String[] channels = groups.toArray(new String[groups.size()]);
        for (int i = 0; i < channels.length; i++) {
            channels[i] = "ee-" + channels[i];
        }

        // For legacy EE compatibility, we add the old "ee" and "eb" channels.
        if (EEPlatform.BUKKIT.current()) {
            channels = ObjectArrays.concat("ee", channels);
            channels = ObjectArrays.concat("ee-bukkit", channels);
        } else if (EEPlatform.BUNGEECORD.current()) {
            channels = ObjectArrays.concat("eb", channels);
            channels = ObjectArrays.concat("ee-bungeecord", channels);
        }

        return channels;
    }

    public JedisPool getJedisPool() {
        if (redisPassword == null || redisPassword.isEmpty())
            return new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 0);
        else
            return new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 0, redisPassword);
    }
}
