package me.thinksmp.commands.adminCommands;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.thinksmp.utility.GeneralUtility.*;

public class StaffChat implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String Label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length == 0) {
                log(translate("&cUsage: /sc <message>"));
            } else {
                String argsCombined = String.join(" ", args);
                Bukkit.getServer().getOnlinePlayers().stream()
                        .filter(staff -> staff.hasPermission(Permissions.STAFF_CHAT.getPermission()))
                        .forEach(staff -> {
                            staff.sendMessage(translate("&8[&cStaff&8-&cChat&8] &c&oConsole " + translate("&f: " + argsCombined)));
                            log(translate("&8[&cStaff&8-&cChat&8] &c&oConsole " + translate("&f: " + argsCombined)));
                        });
            }
            return false;
        }

        if (!player.hasPermission(Permissions.ADMIN.getPermission()) || !player.hasPermission(Permissions.STAFF_CHAT.getPermission()))  {
            player.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }

        boolean staffChatStatus = !Core.getPlayerDataManager().getPlayerData(player).isStaffChat();
        Core.getPlayerDataManager().getPlayerData(player).setStaffChat(staffChatStatus);
        player.sendMessage(translate("&8[&cStaff&8-&cChat&8] " + (staffChatStatus ? "&aYou have enabled" : "&cYou have disabled") + " staff-chat."));
        return false;
    }
}

