package me.thinksmp.managers;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishManager {
    public static void vanish(Player player) {
        Core.getRamManager().getVanishedPlayers().add(player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission(Permissions.VANISH_SEE.getPermission())) {
                onlinePlayer.hidePlayer(Core.getPlugin(), player);
            }
        }
    }

    public static void unVanish(Player player) {
        Core.getRamManager().getVanishedPlayers().remove(player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(Core.getPlugin(), player);
        }
    }
}
