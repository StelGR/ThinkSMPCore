package me.thinksmp.commands.normalCommands;


import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class SmithingTable implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (Core.getCombat().isInCombat(player)) {
            player.sendMessage("§cYou cannot open a smithing table while in combat.");
            return true;
        }

        if (!player.hasPermission(Permissions.VIP.getPermission())) {
            player.sendMessage("§cNo permission");
            return true;
        }

        player.openInventory(MenuType.SMITHING.create(player, "Smithing Table"));
        return true;
    }
}
