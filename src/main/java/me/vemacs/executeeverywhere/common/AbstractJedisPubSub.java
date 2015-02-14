package me.vemacs.executeeverywhere.common;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

@RequiredArgsConstructor
public abstract class AbstractJedisPubSub extends JedisPubSub implements Runnable {
    private final JedisPool pool;
    private final EEConfiguration configuration;

    @Override
    public void run() {
        try (Jedis jedis = pool.getResource()) {
            jedis.subscribe(this, configuration.getRedisChannels());
        }
    }

    @Override
    public void onPMessage(String s, String s1, String s2) {

    }

    @Override
    public void onSubscribe(String s, int i) {

    }

    @Override
    public void onUnsubscribe(String s, int i) {

    }

    @Override
    public void onPUnsubscribe(String s, int i) {

    }

    @Override
    public void onPSubscribe(String s, int i) {

    }
}
