package me.thinksmp.playerData;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class PlayerData {
    private final UUID playerUUID;

    private String name;
    private String displayName;
    private String lastOnline;
    private String firstJoinDate;

    private int points;

    private boolean staffChat;
    private boolean staffScoreboard;
    private boolean vanish;
    private boolean canBeMessaged;
    private boolean canBeInvited;
    private boolean canBePinged;
    private boolean hasPointSound;
    private boolean hasUsedWildCommand;

    private boolean pvpProtected;
    private long pvpProtectionRemainingMillis;

    private final Map<Integer, HomeData> homes = new ConcurrentHashMap<>();

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;

        this.canBeMessaged = true;
        this.canBePinged = true;
        this.canBeInvited = true;
        this.lastOnline = null;
        this.firstJoinDate = null;
        this.vanish = false;
        this.staffChat = false;
        this.staffScoreboard = false;
        this.hasPointSound = true;
        this.hasUsedWildCommand = false;

        this.pvpProtected = false;
        this.pvpProtectionRemainingMillis = 0L;

        this.points = 0;
    }

    public boolean hasPvpProtection() {
        return pvpProtected && pvpProtectionRemainingMillis > 0L;
    }

    public void startPvpProtection(long millis) {
        this.pvpProtected = true;
        this.pvpProtectionRemainingMillis = Math.max(0L, millis);
    }

    public void stopPvpProtection() {
        this.pvpProtected = false;
        this.pvpProtectionRemainingMillis = 0L;
    }

    public void tickPvpProtection(long millis) {
        if (!hasPvpProtection()) {
            stopPvpProtection();
            return;
        }

        this.pvpProtectionRemainingMillis = Math.max(0L, this.pvpProtectionRemainingMillis - Math.max(0L, millis));

        if (this.pvpProtectionRemainingMillis <= 0L) {
            stopPvpProtection();
        }
    }

    public void setHome(int id, Location location) {
        if (location == null || location.getWorld() == null || id < 1 || id > 5) {
            return;
        }

        this.homes.put(id, HomeData.fromLocation(location));
    }

    public boolean hasHome(int id) {
        return this.homes.containsKey(id);
    }

    public HomeData getHome(int id) {
        return this.homes.get(id);
    }

    public void removeHome(int id) {
        this.homes.remove(id);
    }

    @Setter
    @Getter
    public static class HomeData {
        private String world;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public HomeData(String world, double x, double y, double z, float yaw, float pitch) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public static HomeData fromLocation(Location location) {
            return new HomeData(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );
        }

        public Location toLocation() {
            World bukkitWorld = Bukkit.getWorld(world);

            if (bukkitWorld == null) {
                return null;
            }

            return new Location(bukkitWorld, x, y, z, yaw, pitch);
        }
    }
}