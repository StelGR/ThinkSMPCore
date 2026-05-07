package me.thinksmp.managers;

import me.thinksmp.Core;
import me.thinksmp.playerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;


public class PluginTimers {

    public static final long GLOBAL_GRACE_PERIOD_MILLIS = 60L * 60L * 1000L;
    public static final long NEW_PLAYER_PVP_PROTECTION_MILLIS = 15L * 60L * 1000L;

    private final JavaPlugin plugin;

    private boolean graceWasActive;
    private boolean graceActive;
    private long graceEndsAt;

    private long lastProtectionTick;

    public PluginTimers(JavaPlugin plugin) {
        this.plugin = plugin;
        loadGracePeriodFromConfig();
        this.graceWasActive = isGracePeriodActive();
        this.lastProtectionTick = System.currentTimeMillis();
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long elapsed = Math.max(0L, now - lastProtectionTick);
            lastProtectionTick = now;

            boolean graceCurrentlyActive = isGracePeriodActive();

            if (graceWasActive && !graceCurrentlyActive) {
                Bukkit.broadcastMessage(color("&cGrace period has ended. PvP is now enabled."));
            }

            graceWasActive = graceCurrentlyActive;

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

                if (!playerData.hasPvpProtection()) {
                    continue;
                }

                playerData.tickPvpProtection(elapsed);

                if (!playerData.hasPvpProtection()) {
                    player.sendMessage(color("&cYour new-player PvP protection has expired."));
                }
            }
        }, 20L, 20L);
    }

    private void loadGracePeriodFromConfig() {
        FileConfiguration config = plugin.getConfig();

        this.graceActive = config.getBoolean("grace-period.active", false);
        this.graceEndsAt = config.getLong("grace-period.ends-at", 0L);

        if (graceActive && graceEndsAt <= System.currentTimeMillis()) {
            this.graceActive = false;
            this.graceEndsAt = 0L;

            config.set("grace-period.active", false);
            config.set("grace-period.ends-at", 0L);
            plugin.saveConfig();
        }
    }

    private void saveGracePeriodToConfig() {
        FileConfiguration config = plugin.getConfig();

        config.set("grace-period.active", graceActive);
        config.set("grace-period.ends-at", graceEndsAt);

        plugin.saveConfig();
    }

    public void startGracePeriod() {
        startGracePeriod(GLOBAL_GRACE_PERIOD_MILLIS);
    }

    public void startGracePeriod(long millis) {
        this.graceActive = true;
        this.graceEndsAt = System.currentTimeMillis() + Math.max(0L, millis);
        this.graceWasActive = true;

        saveGracePeriodToConfig();
    }

    public void stopGracePeriod() {
        this.graceActive = false;
        this.graceEndsAt = 0L;
        this.graceWasActive = false;

        saveGracePeriodToConfig();
    }

    public boolean isGracePeriodActive() {
        if (!graceActive || graceEndsAt <= 0L) {
            return false;
        }

        if (System.currentTimeMillis() >= graceEndsAt) {
            graceActive = false;
            graceEndsAt = 0L;
            saveGracePeriodToConfig();
            return false;
        }

        return true;
    }

    public long getGracePeriodTimerMillis() {
        if (!isGracePeriodActive()) {
            return 0L;
        }

        return Math.max(0L, graceEndsAt - System.currentTimeMillis());
    }

    public String getGracePeriodTimer() {
        return formatTime(getGracePeriodTimerMillis());
    }

    public void startPVPProtection(Player player) {
        startPVPProtection(player.getUniqueId(), NEW_PLAYER_PVP_PROTECTION_MILLIS);
    }

    public void startPVPProtection(UUID uuid, long millis) {
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(uuid);
        playerData.startPvpProtection(millis);
    }

    public void stopPVPProtection(UUID uuid) {
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(uuid);
        playerData.stopPvpProtection();
    }

    public boolean isPVPProtected(UUID uuid) {
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(uuid);
        return playerData.hasPvpProtection();
    }

    public boolean isPVPProtected(Player player) {
        return isPVPProtected(player.getUniqueId());
    }

    public long getPVPProtectionTimerMillis(UUID uuid) {
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(uuid);
        return playerData.hasPvpProtection() ? playerData.getPvpProtectionRemainingMillis() : 0L;
    }

    public String getPVPProtectionTimer(UUID uuid) {
        return formatTime(getPVPProtectionTimerMillis(uuid));
    }

    public String getPVPProtectionTimer(Player player) {
        return getPVPProtectionTimer(player.getUniqueId());
    }

    public String getPVPProtectionTimer(OfflinePlayer player) {
        return getPVPProtectionTimer(player.getUniqueId());
    }

    public String formatTime(long millis) {
        if (millis <= 0L) {
            return "0s";
        }

        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;

        if (hours > 0L) {
            return hours + "h " + minutes + "m " + secs + "s";
        }

        if (minutes > 0L) {
            return minutes + "m " + secs + "s";
        }

        return secs + "s";
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}