package me.thinksmp.commands.normalCommands;

import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Messaging implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        if (label.equalsIgnoreCase("message") || label.equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                return false;
            }

            Player target = Bukkit.getPlayer(args[0]);

            // if you want a permission check, add it here.

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Could not find player " + ChatColor.RED + ".");
                return false;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "You cannot message yourself.");
                return false;
            }

            if (!Core.getPlayerDataManager().getPlayerData(target).isCanBeMessaged()) {
                player.sendMessage(ChatColor.RED + "You cannot message this player, they have their messages off.");
                return false;
            }

            String message = StringUtils.join(args, ' ', 1, args.length);

            sendMessage(player, target, message);

            Core.getRamManager().getPrivateMessages().put(player.getUniqueId(), target.getUniqueId());
            Core.getRamManager().getPrivateMessages().put(target.getUniqueId(), player.getUniqueId());
        } else if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("r")) {
            UUID lastMessaged = Core.getRamManager().getPrivateMessages().get(player.getUniqueId());

            if (lastMessaged == null) {
                player.sendMessage(ChatColor.RED + "You have nobody to reply to.");
                return true;
            }

            Player target = Bukkit.getPlayer(lastMessaged);

            // if you want a permission check, add it here.

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Could not find player " + ChatColor.RED + ".");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage(GeneralUtility.translate("&cUsage: /reply <message>"));
                return true;
            }

            if (!Core.getPlayerDataManager().getPlayerData(target).isCanBeMessaged()) {
                player.sendMessage(ChatColor.RED + "You cannot reply to this player, they seem to have their messages turned off.");
                return false;
            }

            String message = StringUtils.join(args, ' ');

            sendMessage(player, target, message);

            Core.getRamManager().getPrivateMessages().put(player.getUniqueId(), target.getUniqueId());
            Core.getRamManager().getPrivateMessages().put(target.getUniqueId(), player.getUniqueId());
        }
        return true;
    }

    private void sendMessage(Player sender, Player target, String message) {
        target.sendMessage(GeneralUtility.translate(sender.getDisplayName() + " &7to &eMe: &f" + sender.getDisplayName()));
        sender.sendMessage(GeneralUtility.translate("&eMe &7to " + target.getDisplayName() + "&7: &f" + message));
    }

//    this command does not require permission.
//    if you want to add permissions do something like
//    if (!player.hasPermission("core.gm")) {
//        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
//        return true;
//    }
}
