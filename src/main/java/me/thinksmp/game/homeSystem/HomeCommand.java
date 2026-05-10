package me.thinksmp.game.homeSystem;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.managers.GuiManager;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomeCommand implements CommandExecutor {

    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            GuiManager.openHomeGUI(player);
            return true;
        }

        int homeId = parseHomeId(player, args);

        if (homeId == -1) {
            return true;
        }

        if (Core.getCombat().isInCombat(player)) {
            player.sendMessage("§cYou cannot teleport to your home while in combat.");
            return true;
        }

        if (pendingTeleports.containsKey(player.getUniqueId())) {
            player.sendMessage(GeneralUtility.translate("&cYou are already teleporting. Do not move."));
            return true;
        }

        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

        if (playerData == null) {
            player.sendMessage(GeneralUtility.translate("&cYour player data is not loaded yet."));
            return true;
        }

        PlayerData.HomeData homeData = playerData.getHome(homeId);

        if (homeData == null) {
            player.sendMessage(GeneralUtility.translate("&cYou do not have home " + homeId + " set."));
            return true;
        }

        Location homeLocation = homeData.toLocation();

        if (homeLocation == null || homeLocation.getWorld() == null) {
            player.sendMessage(GeneralUtility.translate("&cThat home world no longer exists."));
            return true;
        }

        startHomeTeleport(player, homeLocation, homeId);
        return true;
    }

    private void startHomeTeleport(Player player, Location homeLocation, int homeId) {
        UUID uuid = player.getUniqueId();
        Location startLocation = player.getLocation().clone();

        player.sendMessage(GeneralUtility.translate("&aTeleporting to home " + homeId + " in 5 seconds. Do not move."));

        BukkitTask task = new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                Player online = Bukkit.getPlayer(uuid);

                if (online == null || !online.isOnline()) {
                    cancelTeleport(uuid);
                    return;
                }

                if (Core.getCombat().isInCombat(online)) {
                    online.sendMessage("§cHome teleport cancelled because you entered combat.");
                    cancelTeleport(uuid);
                    return;
                }

                Location current = online.getLocation();

                if (current.getWorld() == null
                        || startLocation.getWorld() == null
                        || !current.getWorld().equals(startLocation.getWorld())
                        || current.distanceSquared(startLocation) > 1.0D) {
                    online.sendMessage(GeneralUtility.translate("&cHome teleport cancelled because you moved."));
                    cancelTeleport(uuid);
                    return;
                }

                ticks += 5;

                if (ticks >= 100) {
                    pendingTeleports.remove(uuid);
                    online.teleport(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                    online.sendMessage(GeneralUtility.translate("&aTeleported to home " + homeId + "."));
                    cancel();
                }
            }
        }.runTaskTimer(Core.getPlugin(), 0L, 5L);

        pendingTeleports.put(uuid, task);
    }

    private void cancelTeleport(UUID uuid) {
        BukkitTask task = pendingTeleports.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }

    private int parseHomeId(Player player, String[] args) {
        int maxHomes = getMaxHomes(player);

        if (args.length > 1) {
            player.sendMessage(GeneralUtility.translate("&cUsage: /home <1-" + maxHomes + ">"));
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

        if (homeId > maxHomes) {
            player.sendMessage(GeneralUtility.translate("&cYou do not have access to home " + homeId + "."));
            return -1;
        }

        return homeId;
    }

    private int getMaxHomes(Player player) {
        if (player.hasPermission(Permissions.VIP.getPermission())) {
            return 5;
        }

        if (player.hasPermission(Permissions.MEDIA.getPermission())) {
            return 2;
        }

        return 1;
    }
}