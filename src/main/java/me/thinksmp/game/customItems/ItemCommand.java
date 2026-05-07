package me.thinksmp.game.customItems;


import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission(Permissions.ADMIN.getPermission())) {
            player.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /thinkitem <all/sword/hammer/trident/axe/pickaxe/abyssal_scythe/bow>");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            Core.getItemManager().giveAll(player);
            player.sendMessage("§aYou received all custom items.");
            return true;
        }

        CustomItem item = CustomItem.fromId(args[0]);

        if (item == null) {
            player.sendMessage("§cUnknown item.");
            return true;
        }

        Core.getItemManager().giveItem(player, item);
        player.sendMessage("§aYou received §f" + item.getId() + "§a.");
        return true;
    }
}