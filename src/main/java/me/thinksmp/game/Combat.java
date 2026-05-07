package me.thinksmp.game;

import me.thinksmp.Core;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Combat implements Listener {

    private final Map<UUID, CombatData> combatMap = new HashMap<>();
    private final int combatDuration = 30; // seconds

    public void startTimerTask(org.bukkit.plugin.Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, CombatData>> it = combatMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, CombatData> entry = it.next();
                    CombatData data = entry.getValue();
                    data.timeLeft--;
                    if (data.timeLeft <= 0) {
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        if (!(e.getEntity() instanceof Player victim)) return;
        Player attacker = null;


        if (e.getDamager() instanceof Player p) {
            attacker = p;
        } else if (e.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null || attacker == victim) return;

        if (Objects.equals(Core.getPlugin().getUltimateTeamsAPI().findTeamByMember(attacker.getUniqueId()), Core.getPlugin().getUltimateTeamsAPI().findTeamByMember(attacker.getUniqueId()))) return;

        if (!isInCombat(victim)) {
            victim.sendMessage("§cYou are now in combat. Do not log out.");
        }
        combatMap.put(victim.getUniqueId(), new CombatData(attacker.getUniqueId(), combatDuration));

        if (!isInCombat(attacker)) {
            attacker.sendMessage("§cYou are now in combat. Do not log out.");
        }
        combatMap.put(attacker.getUniqueId(), new CombatData(victim.getUniqueId(), combatDuration));
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (isInCombat(p)) {
            p.setHealth(0);
        }
        combatMap.remove(p.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        combatMap.remove(dead.getUniqueId());
    }

    public boolean isInCombat(Player player) {
        return timeRemaining(player) > 0;
    }

    public int timeRemaining(Player player) {
        CombatData data = combatMap.get(player.getUniqueId());
        return data != null ? data.timeLeft : 0;
    }

    public UUID getLastAttacker(Player player) {
        CombatData data = combatMap.get(player.getUniqueId());
        return data != null ? data.enemy : null;
    }
}
