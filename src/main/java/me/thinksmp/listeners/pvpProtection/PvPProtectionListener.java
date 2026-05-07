package me.thinksmp.listeners.pvpProtection;

import me.thinksmp.managers.PluginTimers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvPProtectionListener implements Listener {

    private final PluginTimers timers;
    private final Map<UUID, Long> messageCooldown = new HashMap<>();

    public PvPProtectionListener(PluginTimers timers) {
        this.timers = timers;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = getAttacker(event.getDamager());

        if (attacker == null) {
            return;
        }

        if (timers.isGracePeriodActive()) {
            event.setCancelled(true);
            sendCooldown(attacker, "&cPvP is disabled during grace period. Time left: &e" + timers.getGracePeriodTimer());
            return;
        }

        if (timers.isPVPProtected(attacker.getUniqueId())) {
            event.setCancelled(true);
            sendCooldown(attacker, "&cYou cannot attack players while your PvP protection is active. Time left: &e" + timers.getPVPProtectionTimer(attacker));
            return;
        }

        if (timers.isPVPProtected(victim.getUniqueId())) {
            event.setCancelled(true);
            sendCooldown(attacker, "&cThat player has new-player PvP protection. Time left: &e" + timers.getPVPProtectionTimer(victim));
        }
    }

    private Player getAttacker(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }

        if (entity instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private void sendCooldown(Player player, String message) {
        long now = System.currentTimeMillis();
        long last = messageCooldown.getOrDefault(player.getUniqueId(), 0L);

        if (now - last < 1200L) {
            return;
        }

        messageCooldown.put(player.getUniqueId(), now);
        player.sendMessage(color(message));
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
