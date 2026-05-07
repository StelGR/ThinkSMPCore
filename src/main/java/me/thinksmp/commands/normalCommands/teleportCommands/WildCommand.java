package me.thinksmp.commands.normalCommands.teleportCommands;

import me.thinksmp.Core;
import me.thinksmp.functions.Randomizer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WildCommand implements CommandExecutor {

    private static final long COOLDOWN_MS = 60_000L;
    private static final long WARMUP_TICKS = 20L * 5L;

    private final Core plugin;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> movementTasks = new HashMap<>();

    public WildCommand(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (teleportTasks.containsKey(uuid)) {
            player.sendMessage("§cYou are already preparing to RTP.");
            return true;
        }

        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(uuid, 0L);
        long remaining = COOLDOWN_MS - (now - lastUse);

        if (remaining > 0) {
            long seconds = (remaining + 999) / 1000;
            player.sendMessage("§cYou must wait " + seconds + "s before using /rtp again.");
            return true;
        }

        Location startLoc = player.getLocation().clone();

        player.sendMessage("§aRandom teleport starting in 5 seconds...");
        player.sendMessage("§7Do not move.");

        BukkitTask teleportTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            clearTeleportTasks(uuid);

            if (!player.isOnline()) {
                return;
            }

            player.sendMessage("§eFinding a safe location...");

            World world = player.getWorld();

            /*
             * Change these values if you want a bigger/smaller RTP range.
             * Example: -5000 to 5000
             */
            int min = -5000;
            int max = 5000;

            Randomizer.getRandomLocation(world, min, max).thenAccept(location -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                player.teleport(location);
                cooldowns.put(uuid, System.currentTimeMillis());

                player.sendMessage("§aYou have been randomly teleported!");
            }));
        }, WARMUP_TICKS);

        BukkitTask movementTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                clearTeleportTasks(uuid);
                return;
            }

            if (hasMoved(startLoc, player.getLocation())) {
                clearTeleportTasks(uuid);
                player.sendMessage("§cRTP cancelled because you moved.");
            }
        }, 0L, 5L);

        teleportTasks.put(uuid, teleportTask);
        movementTasks.put(uuid, movementTask);

        return true;
    }

    private void clearTeleportTasks(UUID uuid) {
        BukkitTask teleportTask = teleportTasks.remove(uuid);

        if (teleportTask != null) {
            teleportTask.cancel();
        }

        BukkitTask movementTask = movementTasks.remove(uuid);

        if (movementTask != null) {
            movementTask.cancel();
        }
    }

    private boolean hasMoved(Location from, Location to) {
        if (from == null || to == null) {
            return true;
        }

        if (from.getWorld() == null || to.getWorld() == null) {
            return true;
        }

        if (!from.getWorld().equals(to.getWorld())) {
            return true;
        }

        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }
}
