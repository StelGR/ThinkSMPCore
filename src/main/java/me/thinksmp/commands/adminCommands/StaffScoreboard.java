package me.thinksmp.commands.adminCommands;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.thinksmp.utility.GeneralUtility.*;

public class StaffScoreboard implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            log(translate("&cOnly players may use this command."));
            return false;
        }
        
        if (!player.hasPermission(Permissions.ADMIN.getPermission()) || !player.hasPermission(Permissions.STAFF_SCOREBOARD.getPermission())) {
            player.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            return false;
        }
        
        boolean staffScoreboardStatus = !Core.getPlayerDataManager().getPlayerData(player).isStaffScoreboard();
        Core.getPlayerDataManager().getPlayerData(player).setStaffScoreboard(staffScoreboardStatus);
        player.sendMessage(translate("&8[&cStaff&8-&cScoreboard&8] " + (staffScoreboardStatus ? "&aYou have enabled" : "&cYou have disabled") + " staff scoreboard."));
        return false;
    }
}
