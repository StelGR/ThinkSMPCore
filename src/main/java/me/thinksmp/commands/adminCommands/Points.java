package me.thinksmp.commands.adminCommands;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.playerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Points implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission(Permissions.ADMIN.getPermission())) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /points <check|add|remove|set> <player> [amount]");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        PlayerData data = Core.getPlayerDataManager().getPlayerData(target);

        switch (action) {
            case "check":
                sender.sendMessage("§e" + target.getName() + " has §6" + data.getPoints() + " §epoints.");
                break;

            case "add":
                if (args.length < 3) {
                    sender.sendMessage("§cPlease specify an amount.");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    data.setPoints(data.getPoints() + amount);
                    sender.sendMessage("§aAdded §6" + amount + " §apoints to §e" + target.getName() + "§a.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount must be a number.");
                }
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§cPlease specify an amount.");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    data.setPoints(Math.max(0, data.getPoints() - amount));
                    sender.sendMessage("§aRemoved §6" + amount + " §apoints from §e" + target.getName() + "§a.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount must be a number.");
                }
                break;

            case "set":
                if (args.length < 3) {
                    sender.sendMessage("§cPlease specify an amount.");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    data.setPoints(amount);
                    sender.sendMessage("§aSet §e" + target.getName() + "§a's points to §6" + amount + "§a.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cAmount must be a number.");
                }
                break;

            default:
                sender.sendMessage("§eUsage: /points <check|add|remove|set> <player> [amount]");
                break;
        }
        return true;
    }
}
