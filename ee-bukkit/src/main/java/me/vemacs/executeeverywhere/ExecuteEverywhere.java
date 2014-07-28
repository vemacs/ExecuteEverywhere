package me.vemacs.executeeverywhere;

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class ExecuteEverywhere extends JavaPlugin implements Listener {
    private JedisPool pool;
    private static final Joiner joiner = Joiner.on(" ");
    private final String CHANNEL = "ee";
    private static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");
        String password = getConfig().getString("password");
        if (password == null || password.equals(""))
            pool = new JedisPool(new JedisPoolConfig(), ip, port, 0);
        else
            pool = new JedisPool(new JedisPoolConfig(), ip, port, 0, password);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Jedis jedis = pool.getResource();
                try {
                    jedis.subscribe(new EESubscriber(), CHANNEL);
                } catch (Exception e) {
                    pool.returnBrokenResource(jedis);
                }
                pool.returnResource(jedis);
            }
        }, "ExecuteEverywhere Subscriber").start();
    }

    @Override
    public boolean onCommand(CommandSender sender, final Command cmd, String label, String[] args) {
        if (args.length == 0) return false;
        String cmdString = joiner.join(args);
        if (cmdString.startsWith("/"))
            cmdString = cmdString.substring(1);
        final String finalCmdString = cmdString;
        getServer().getScheduler().runTaskAsynchronously(this, new BukkitRunnable() {
            @Override
            public void run() {
                Jedis jedis = pool.getResource();
                try {
                    jedis.publish(CHANNEL, finalCmdString);
                } catch (Exception e) {
                    pool.returnBrokenResource(jedis);
                }
                pool.returnResource(jedis);
            }
        });
        sender.sendMessage(ChatColor.GREEN + "Sent /" + cmdString + " for execution.");
        return true;
    }

    public class EESubscriber extends JedisPubSub {
        @Override
        public void onMessage(String channel, final String msg) {
            // Needs to be done in the server thread
            getServer().getScheduler().runTask(ExecuteEverywhere.instance, new BukkitRunnable() {
                @Override
                public void run() {
                    ExecuteEverywhere.instance.getLogger().info("Dispatching /" + msg);
                    getServer().dispatchCommand(getServer().getConsoleSender(), msg);
                }
            });
        }

        @Override
        public void onPMessage(String s, String s2, String s3) {
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
}

