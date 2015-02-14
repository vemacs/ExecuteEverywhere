package me.vemacs.executeeverywhere.bungee;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;

public class EECommand extends Command {
    private final ExecuteEverywhere plugin;
    private final EECommandKind kind;

    public EECommand(ExecuteEverywhere plugin, String name, EECommandKind kind) {
        super(name, "executeeverywhere.use");
        this.kind = kind;
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender commandSender, String[] args) {
        if (args.length < 1 || (kind == EECommandKind.GROUP && args.length < 2)) {
            commandSender.sendMessage(ChatColor.RED + "Invalid usage.");
            if (kind == EECommandKind.GROUP) {
                commandSender.sendMessage(ChatColor.RED + "/" + getName() + " <group> <command>");
            } else {
                commandSender.sendMessage(ChatColor.RED + "/" + getName() + " <command>");
            }
            return;
        }

        String group = args[0];
        final String channel;
        final String run;

        switch (kind) {
            case BUNGEECORD:
                channel = "eb";
                run = Joiner.on(' ').join(args);
                commandSender.sendMessage(ChatColor.GRAY + "(Assuming you want to run this command over all BungeeCord servers.)");
                break;
            case BUKKIT:
                channel = "ee";
                run = Joiner.on(' ').join(args);
                commandSender.sendMessage(ChatColor.GRAY + "(Assuming you want to run this command over all Bukkit servers.)");
                break;
            case GROUP:
                channel = "ee-" + group;
                run = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                return;
        }

        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                try (Jedis jedis = plugin.getPool().getResource()) {
                    jedis.publish(channel, run);
                    commandSender.sendMessage(ChatColor.GREEN + "Command successfully queued for execution.");
                } catch (JedisConnectionException e) {
                    commandSender.sendMessage(ChatColor.RED + "Could not send the command! Please try again.");
                }
            }
        });
    }
}
