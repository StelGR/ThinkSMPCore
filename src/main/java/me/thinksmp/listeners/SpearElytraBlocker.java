package me.thinksmp.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpearElytraBlocker implements Listener, Runnable {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> lastElytraTick = new HashMap<>();
    private final Map<UUID, Long> messageCooldown = new HashMap<>();

    private int tick;

    public SpearElytraBlocker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        tick++;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refresh(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastElytraTick.remove(event.getPlayer().getUniqueId());
        messageCooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(player), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(player), 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> refresh(player), 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isSpear(item)) {
            return;
        }

        if (cannotUseSpear(player)) {
            event.setCancelled(true);
            send(player, "&cYou cannot use a spear while using an elytra.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;

        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                attacker = player;
            }
        }

        if (attacker == null) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInHand();

        if (!isSpear(item) && !(event.getDamager() instanceof Projectile && event.getDamager().getType().name().equalsIgnoreCase("TRIDENT"))) {
            return;
        }

        if (cannotUseSpear(attacker)) {
            event.setCancelled(true);
            send(attacker, "&cYou cannot use a spear while using an elytra.");
        }
    }

    private boolean cannotUseSpear(Player player) {
        refresh(player);

        Integer last = lastElytraTick.get(player.getUniqueId());

        if (last == null) {
            return false;
        }

        return tick - last <= 20;
    }

    private void refresh(Player player) {
        if (isWearingElytra(player)) {
            lastElytraTick.put(player.getUniqueId(), tick);
        }
    }

    private boolean isWearingElytra(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();

        if (chestplate == null || chestplate.getType() == Material.AIR) {
            return false;
        }

        return chestplate.getType().equals(Material.ELYTRA);
    }

    private boolean isSpear(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        String materialName = item.getType().name();

        return materialName.equals("SPEAR")
                || materialName.endsWith("_SPEAR")
                || materialName.equals("WOODEN_SPEAR")
                || materialName.equals("STONE_SPEAR")
                || materialName.equals("COPPER_SPEAR")
                || materialName.equals("IRON_SPEAR")
                || materialName.equals("GOLDEN_SPEAR")
                || materialName.equals("DIAMOND_SPEAR")
                || materialName.equals("NETHERITE_SPEAR");
    }

    private void send(Player player, String message) {
        long now = System.currentTimeMillis();
        long last = messageCooldown.getOrDefault(player.getUniqueId(), 0L);

        if (now - last < 1200L) {
            return;
        }

        messageCooldown.put(player.getUniqueId(), now);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}