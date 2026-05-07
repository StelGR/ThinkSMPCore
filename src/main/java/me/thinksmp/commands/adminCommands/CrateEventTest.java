package me.thinksmp.commands.adminCommands;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrateEventTest implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            GeneralUtility.log(GeneralUtility.translate("&cOnly players may use this command."));
            return false;
        }

        if (!player.hasPermission(Permissions.ADMIN.getPermission())) {
            player.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }

        Core.getEvents().startMeteor(player.getLocation());
        return false;
    }
}
