package me.thinksmp.commands.normalCommands.teleportCommands;

import me.thinksmp.Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpAccept implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (Core.getCombat().isInCombat(player)) {
            player.sendMessage("§cYou cannot accept a TPA while in combat.");
            return true;
        }

        Core.getTpaManager().acceptRequest(player);
        return true;
    }
}
