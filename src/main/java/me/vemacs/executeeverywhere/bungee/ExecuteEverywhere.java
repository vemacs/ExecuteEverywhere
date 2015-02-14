package me.vemacs.executeeverywhere.bungee;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.vemacs.executeeverywhere.common.AbstractJedisPubSub;
import me.vemacs.executeeverywhere.common.EEConfiguration;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
import java.util.List;

public class ExecuteEverywhere extends Plugin {
    @Getter
    private JedisPool pool;
    @Getter
    private EEConfiguration configuration;
    private EESubscriber eeSubscriber;

    @Override
    public void onEnable() {
        final Configuration config;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(this, "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config.yml", e);
        }

        String ip = config.getString("ip");
        int port = config.getInt("port");
        String password = config.getString("password");
        List<String> serverGroups = config.getStringList("groups");
        configuration = new EEConfiguration(serverGroups, ip, port, password);

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {
                pool = configuration.getJedisPool();
                getProxy().getScheduler().runAsync(ExecuteEverywhere.this, eeSubscriber = new EESubscriber());
            }
        });

        getProxy().getPluginManager().registerCommand(this, new EECommand(this, "ee", EECommandKind.BUKKIT));
        getProxy().getPluginManager().registerCommand(this, new EECommand(this, "eb", EECommandKind.BUNGEECORD));
        getProxy().getPluginManager().registerCommand(this, new EECommand(this, "eeg", EECommandKind.GROUP));
    }

    @Override
    public void onDisable() {
        eeSubscriber.unsubscribe();
        pool.destroy();
    }

    public class EESubscriber extends AbstractJedisPubSub {
        public EESubscriber() {
            super(pool, configuration);
        }

        @Override
        public void onMessage(String channel, final String msg) {
            ExecuteEverywhere.this.getLogger().info("Dispatching /" + msg);
            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), msg);
        }
    }

    public static File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }
}

