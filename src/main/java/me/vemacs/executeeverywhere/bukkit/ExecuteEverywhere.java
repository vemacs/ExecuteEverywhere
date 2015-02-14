package me.vemacs.executeeverywhere.bukkit;

import lombok.Getter;
import me.vemacs.executeeverywhere.common.AbstractJedisPubSub;
import me.vemacs.executeeverywhere.common.EEConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPool;

import java.lang.Override;
import java.util.List;

public class ExecuteEverywhere extends JavaPlugin implements Listener {
    @Getter
    private JedisPool pool;
    @Getter
    private EEConfiguration configuration;
    private EESubscriber eeSubscriber;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");
        String password = getConfig().getString("password");
        List<String> serverGroups = getConfig().getStringList("groups");
        configuration = new EEConfiguration(serverGroups, ip, port, password);

        pool = configuration.getJedisPool();

        getServer().getScheduler().runTaskAsynchronously(this, eeSubscriber = new EESubscriber());

        EECommand command = new EECommand(this);
        getCommand("ee").setExecutor(command);
        getCommand("eb").setExecutor(command);
        getCommand("eeg").setExecutor(command);
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
            // Needs to be done in the server thread
             new BukkitRunnable() {
                @Override
                public void run() {
                    getLogger().info("Dispatching /" + msg);
                    getServer().dispatchCommand(getServer().getConsoleSender(), msg);
                }
            }.runTaskAsynchronously(ExecuteEverywhere.this);
        }
    }
}

