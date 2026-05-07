package me.thinksmp.managers;

import lombok.Getter;
import me.thinksmp.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TpaManager {

    private static final long REQUEST_EXPIRE_MS = 60_000L;
    private static final long TELEPORT_DELAY_TICKS = 20L * 5L;

    private final Map<UUID, Deque<TpaRequest>> requests = new HashMap<>();
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> movementTasks = new HashMap<>();

    private final Core plugin;

    public TpaManager(Core plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage("§cYou cannot send a TPA request to yourself.");
            return;
        }

        cleanupExpiredRequests(target.getUniqueId(), true);

        Deque<TpaRequest> queue = requests.computeIfAbsent(target.getUniqueId(), uuid -> new ArrayDeque<>());

        boolean alreadyRequested = queue.stream()
                .anyMatch(request -> request.getRequester().equals(sender.getUniqueId()));

        if (alreadyRequested) {
            sender.sendMessage("§cYou already sent a TPA request to this player.");
            return;
        }

        queue.addLast(new TpaRequest(
                sender.getUniqueId(),
                target.getUniqueId(),
                System.currentTimeMillis()
        ));

        sender.sendMessage("§aTPA request sent to " + target.getName() + ".");

        target.sendMessage("§e" + sender.getName() + " has requested to teleport to you.");
        target.sendMessage("§7Use §a/tpaccept §7to accept or §c/tpdeny §7to deny.");
        target.sendMessage("§7Pending TPA requests: §e" + queue.size());
    }

    public void acceptRequest(Player target) {
        cleanupExpiredRequests(target.getUniqueId(), true);

        Deque<TpaRequest> queue = requests.get(target.getUniqueId());

        if (queue == null || queue.isEmpty()) {
            target.sendMessage("§cNo TPA requests to accept.");
            return;
        }

        if (!isSafeToTeleport(target)) {
            target.sendMessage("§cYou must be standing on safe solid ground to accept.");
            return;
        }

        TpaRequest request = null;
        Player requester = null;

        while (!queue.isEmpty()) {
            TpaRequest next = queue.pollFirst(); // oldest request first
            Player possibleRequester = Bukkit.getPlayer(next.getRequester());

            if (possibleRequester != null && possibleRequester.isOnline()) {
                request = next;
                requester = possibleRequester;
                break;
            }

            target.sendMessage("§cSkipped an offline TPA requester.");
        }

        if (queue.isEmpty()) {
            requests.remove(target.getUniqueId());
        }

        if (request == null || requester == null) {
            target.sendMessage("§cNo online TPA requesters found.");
            return;
        }

        if (teleportTasks.containsKey(requester.getUniqueId())) {
            requester.sendMessage("§cYou are already teleporting.");
            return;
        }

        Location targetLoc = target.getLocation().clone().add(0, 0.5, 0);
        Location startLoc = requester.getLocation().clone();

        requester.sendMessage("§aYour TPA request was accepted. Teleporting in 5 seconds...");
        requester.sendMessage("§7Do not move.");
        target.sendMessage("§aYou accepted " + requester.getName() + "'s TPA request.");

        Player finalRequester = requester;
        BukkitTask teleportTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            clearTeleportTasks(finalRequester.getUniqueId());

            if (!finalRequester.isOnline()) {
                return;
            }

            finalRequester.teleport(targetLoc);
            finalRequester.sendMessage("§aTeleported to " + target.getName() + "!");
        }, TELEPORT_DELAY_TICKS);

        Player finalRequester1 = requester;
        BukkitTask movementTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!finalRequester1.isOnline()) {
                clearTeleportTasks(finalRequester1.getUniqueId());
                return;
            }

            if (hasMoved(startLoc, finalRequester1.getLocation())) {
                cancelTeleport(finalRequester1, "§cTeleport cancelled because you moved.");
            }
        }, 0L, 5L);

        teleportTasks.put(requester.getUniqueId(), teleportTask);
        movementTasks.put(requester.getUniqueId(), movementTask);

        if (queue != null && !queue.isEmpty()) {
            target.sendMessage("§7Remaining TPA requests: §e" + queue.size());
        }
    }

    public void denyRequest(Player target) {
        cleanupExpiredRequests(target.getUniqueId(), true);

        Deque<TpaRequest> queue = requests.get(target.getUniqueId());

        if (queue == null || queue.isEmpty()) {
            target.sendMessage("§cNo TPA requests to deny.");
            return;
        }

        TpaRequest request = queue.pollFirst(); // deny oldest request first

        if (queue.isEmpty()) {
            requests.remove(target.getUniqueId());
        }

        Player requester = Bukkit.getPlayer(request.getRequester());

        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§cYour TPA request to " + target.getName() + " was denied.");
            target.sendMessage("§aDenied " + requester.getName() + "'s TPA request.");
        } else {
            target.sendMessage("§aDenied an offline player's TPA request.");
        }

        if (queue != null && !queue.isEmpty()) {
            target.sendMessage("§7Remaining TPA requests: §e" + queue.size());
        }
    }

    public void cancelOwnRequest(Player sender) {
        boolean removed = false;

        Iterator<Map.Entry<UUID, Deque<TpaRequest>>> mapIterator = requests.entrySet().iterator();

        while (mapIterator.hasNext()) {
            Map.Entry<UUID, Deque<TpaRequest>> entry = mapIterator.next();
            UUID targetUuid = entry.getKey();
            Deque<TpaRequest> queue = entry.getValue();

            Iterator<TpaRequest> requestIterator = queue.iterator();

            while (requestIterator.hasNext()) {
                TpaRequest request = requestIterator.next();

                if (!request.getRequester().equals(sender.getUniqueId())) {
                    continue;
                }

                requestIterator.remove();
                removed = true;

                Player target = Bukkit.getPlayer(targetUuid);

                if (target != null && target.isOnline()) {
                    target.sendMessage("§c" + sender.getName() + " cancelled their TPA request to you.");
                    target.sendMessage("§7Pending TPA requests: §e" + queue.size());
                }

                sender.sendMessage("§aCancelled your TPA request to "
                        + (target != null ? target.getName() : "that player") + ".");

                break;
            }

            if (queue.isEmpty()) {
                mapIterator.remove();
            }

            if (removed) {
                break;
            }
        }

        if (!removed) {
            sender.sendMessage("§cYou have no pending TPA requests to cancel.");
        }
    }

    public void cancelIncomingRequest(Player target) {
        cleanupExpiredRequests(target.getUniqueId(), true);

        Deque<TpaRequest> queue = requests.get(target.getUniqueId());

        if (queue == null || queue.isEmpty()) {
            target.sendMessage("§cYou have no incoming TPA requests to cancel.");
            return;
        }

        TpaRequest request = queue.pollFirst();

        if (queue.isEmpty()) {
            requests.remove(target.getUniqueId());
        }

        Player requester = Bukkit.getPlayer(request.getRequester());

        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§c" + target.getName() + " cancelled your TPA request.");
            target.sendMessage("§aCancelled " + requester.getName() + "'s TPA request.");
        } else {
            target.sendMessage("§aCancelled an offline player's TPA request.");
        }

        if (queue != null && !queue.isEmpty()) {
            target.sendMessage("§7Remaining TPA requests: §e" + queue.size());
        }
    }

    public void cancelTeleport(Player requester, String reason) {
        clearTeleportTasks(requester.getUniqueId());
        requester.sendMessage(reason);
    }

    public int getPendingRequestCount(Player target) {
        cleanupExpiredRequests(target.getUniqueId(), false);

        Deque<TpaRequest> queue = requests.get(target.getUniqueId());
        return queue == null ? 0 : queue.size();
    }

    private void cleanupExpiredRequests(UUID targetUuid, boolean notifyPlayers) {
        Deque<TpaRequest> queue = requests.get(targetUuid);

        if (queue == null || queue.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        Iterator<TpaRequest> iterator = queue.iterator();

        while (iterator.hasNext()) {
            TpaRequest request = iterator.next();

            if (now - request.getTime() < REQUEST_EXPIRE_MS) {
                continue;
            }

            iterator.remove();

            if (notifyPlayers) {
                Player requester = Bukkit.getPlayer(request.getRequester());
                Player target = Bukkit.getPlayer(request.getTarget());

                if (requester != null && requester.isOnline()) {
                    requester.sendMessage("§cYour TPA request to "
                            + (target != null ? target.getName() : "that player")
                            + " expired.");
                }
            }
        }

        if (queue.isEmpty()) {
            requests.remove(targetUuid);
        }
    }

    private void clearTeleportTasks(UUID requesterUuid) {
        BukkitTask teleportTask = teleportTasks.remove(requesterUuid);
        if (teleportTask != null) {
            teleportTask.cancel();
        }

        BukkitTask movementTask = movementTasks.remove(requesterUuid);
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

    private boolean isSafeToTeleport(Player target) {
        Location loc = target.getLocation();

        Material ground = loc.clone().subtract(0, 1, 0).getBlock().getType();
        Material feet = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();

        return ground.isSolid()
                && !isDangerous(ground)
                && isAir(feet)
                && isAir(head);
    }

    private boolean isAir(Material material) {
        return material == Material.AIR || material.name().endsWith("_AIR");
    }

    private boolean isDangerous(Material material) {
        String name = material.name();

        return material == Material.LAVA
                || name.equals("MAGMA_BLOCK")
                || name.equals("FIRE")
                || name.equals("SOUL_FIRE")
                || name.equals("CAMPFIRE")
                || name.equals("SOUL_CAMPFIRE")
                || name.equals("CACTUS");
    }

    @Getter
    private static class TpaRequest {
        private final UUID requester;
        private final UUID target;
        private final long time;

        public TpaRequest(UUID requester, UUID target, long time) {
            this.requester = requester;
            this.target = target;
            this.time = time;
        }
    }
}