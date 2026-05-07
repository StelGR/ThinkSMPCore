package me.thinksmp.utility;

import me.thinksmp.files.LocationsFile;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class ZoneUtil {

    private ZoneUtil() {
    }

    public static boolean isInsideZone(Player player, String zone) {
        if (player == null) {
            return false;
        }

        return isInsideZone(player.getLocation(), zone);
    }

    public static boolean isInsideZone(Location location, String zone) {
        if (location == null || location.getWorld() == null || zone == null || zone.trim().isEmpty()) {
            return false;
        }

        zone = zone.toLowerCase(Locale.ROOT);

        String base = "zones." + zone;

        if (!LocationsFile.get().contains(base + ".corner1")
                || !LocationsFile.get().contains(base + ".corner2")) {
            return false;
        }

        String world1 = LocationsFile.get().getString(base + ".corner1.world");
        String world2 = LocationsFile.get().getString(base + ".corner2.world");

        if (world1 == null || world2 == null) {
            return false;
        }

        if (!world1.equalsIgnoreCase(world2)) {
            return false;
        }

        if (!location.getWorld().getName().equalsIgnoreCase(world1)) {
            return false;
        }

        int x1 = LocationsFile.get().getInt(base + ".corner1.x");
        int y1 = LocationsFile.get().getInt(base + ".corner1.y");
        int z1 = LocationsFile.get().getInt(base + ".corner1.z");

        int x2 = LocationsFile.get().getInt(base + ".corner2.x");
        int y2 = LocationsFile.get().getInt(base + ".corner2.y");
        int z2 = LocationsFile.get().getInt(base + ".corner2.z");

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);

        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public static boolean zoneExists(String zone) {
        if (zone == null || zone.trim().isEmpty()) {
            return false;
        }

        zone = zone.toLowerCase(Locale.ROOT);
        return LocationsFile.get().contains("zones." + zone + ".corner1")
                && LocationsFile.get().contains("zones." + zone + ".corner2");
    }
}