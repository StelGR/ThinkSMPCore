package me.thinksmp.game.homeSystem;


import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        int homeId = parseHomeId(player, args);

        if (homeId == -1) {
            return true;
        }

        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);
        Location location = player.getLocation();

        playerData.setHome(homeId, location);
        Core.getPlayerDataFileManager().savePlayerData(playerData);

        player.sendMessage(GeneralUtility.translate("&aHome " + homeId + " has been set."));
        return true;
    }

    private int parseHomeId(Player player, String[] args) {
        boolean vip = player.hasPermission(Permissions.VIP.getPermission());

        if (args.length == 0) {
            return 1;
        }

        if (args.length > 1) {
            player.sendMessage(GeneralUtility.translate("&cUsage: /sethome " + (vip ? "<1-5>" : "")));
            return -1;
        }

        int homeId;

        try {
            homeId = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            player.sendMessage(GeneralUtility.translate("&cHome must be a number."));
            return -1;
        }

        if (homeId < 1 || homeId > 5) {
            player.sendMessage(GeneralUtility.translate("&cHome must be between 1 and 5."));
            return -1;
        }

        if (!vip && homeId != 1) {
            player.sendMessage(GeneralUtility.translate("&cOnly VIP players can use multiple homes."));
            return -1;
        }

        return homeId;
    }
}