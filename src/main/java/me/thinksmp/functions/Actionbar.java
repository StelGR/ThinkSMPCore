package me.thinksmp.functions;

import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Actionbar {
    public static void actionBar(Player player) {
        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            try {
                if (player == null || !player.isOnline()) return;

                StringBuilder message = new StringBuilder();

                boolean vanished = Core.getRamManager().getVanishedPlayers().contains(player);
                boolean inCombat = Core.getCombat().isInCombat(player);

                if (vanished) {
                    message.append("You are &cVanished&7");
                }

                if (inCombat) {
                    message.append(vanished ? "," : "&7,")
                            .append(" In Combat for &c")
                            .append(Core.getCombat().timeRemaining(player))
                            .append("&7.");
                } else if (vanished) {
                    message.append(".");
                }

                GeneralUtility.sendActionBar(player, GeneralUtility.translate(message.toString()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, 20L, 20L);
    }
}
