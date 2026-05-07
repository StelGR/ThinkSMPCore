package me.thinksmp.commands.normalCommands;

import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Leaderboard implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        sender.sendMessage("§6--- Leaderboard ---");
        int pos = 1;
        for (String line : Core.getLeaderboardManager().getTop(10)) {
            sender.sendMessage(GeneralUtility.translate("§e" + pos + ". §f" + line));
            pos++;
        }
        return true;
    }
}
