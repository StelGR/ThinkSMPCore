package me.thinksmp.commands.adminCommands;

import me.thinksmp.files.LocationsFile;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SetCorner implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("thinksmp.setcorner")) {
            player.sendMessage(GeneralUtility.translate("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(GeneralUtility.translate("&cUsage: /setcorner <1|2> [zone]"));
            return true;
        }

        int corner;

        try {
            corner = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            player.sendMessage(GeneralUtility.translate("&cCorner must be 1 or 2."));
            return true;
        }

        if (corner != 1 && corner != 2) {
            player.sendMessage(GeneralUtility.translate("&cCorner must be 1 or 2."));
            return true;
        }

        String zone = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "spawn";
        Location loc = player.getLocation();

        String path = "zones." + zone + ".corner" + corner;

        LocationsFile.get().set(path + ".world", loc.getWorld().getName());
        LocationsFile.get().set(path + ".x", loc.getBlockX());
        LocationsFile.get().set(path + ".y", loc.getBlockY());
        LocationsFile.get().set(path + ".z", loc.getBlockZ());
        LocationsFile.save();

        player.sendMessage(GeneralUtility.translate("&aSet corner &f" + corner + " &afor zone &f" + zone + "&a."));
        player.sendMessage(GeneralUtility.translate("&7World: &f" + loc.getWorld().getName()));
        player.sendMessage(GeneralUtility.translate("&7X: &f" + loc.getBlockX() + " &7Y: &f" + loc.getBlockY() + " &7Z: &f" + loc.getBlockZ()));

        return true;
    }
}
