package me.thinksmp.commands.normalCommands.teleportCommands;

import me.thinksmp.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage("§cUsage: /tpa <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cThat player is not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot send a TPA to yourself.");
            return true;
        }

        if (Core.getCombat().isInCombat(player)) {
            player.sendMessage("§cYou cannot send a TPA while in combat.");
            return true;
        }

        Core.getTpaManager().sendRequest(player, target);
        return true;
    }
}
