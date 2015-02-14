package me.vemacs.executeeverywhere.bukkit;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;

@RequiredArgsConstructor
public class EECommand implements CommandExecutor {
    private final ExecuteEverywhere plugin;

    @Override
    public boolean onCommand(final CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length < 1 || (command.getName().equalsIgnoreCase("eeg") && args.length < 2)) {
            commandSender.sendMessage(ChatColor.RED + "Invalid usage.");
            if (command.getName().equalsIgnoreCase("eec")) {
                commandSender.sendMessage(ChatColor.RED + "/" + s + " <group> <command>");
            } else {
                commandSender.sendMessage(ChatColor.RED + "/" + s + " <command>");
            }
            return true;
        }

        final String channel;
        final String run;

        if (command.getName().equals("eeg")) {
            channel = "ee-" + args[0];
            run = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
        } else if (command.getName().equals("ee")) {
            channel = "ee";
            run = Joiner.on(' ').join(args);
            commandSender.sendMessage(ChatColor.GRAY + "(Assuming you want to run this command over all Bukkit servers.)");
        } else if (command.getName().equals("eb")) {
            channel = "eb";
            run = Joiner.on(' ').join(args);
            commandSender.sendMessage(ChatColor.GRAY + "(Assuming you want to run this command over all BungeeCord servers.)");
        } else {
            // Shouldn't happen
            return false;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
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

        return true;
    }
}
