package me.thinksmp.managers;

import me.thinksmp.files.LocationsFile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlacedOreManager {

    private static final String ROOT = "placed-ores";

    private final Map<Material, Set<BlockKey>> placedOres = new ConcurrentHashMap<>();
    private boolean dirty;

    private final Set<Material> ores = EnumSet.of(
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE
    );

    public void loadPlacedOres() {
        placedOres.clear();

        ConfigurationSection root = LocationsFile.get().getConfigurationSection(ROOT);

        if (root == null) {
            return;
        }

        for (String materialName : root.getKeys(false)) {
            Material material = Material.matchMaterial(materialName);

            if (material == null || !isOre(material)) {
                continue;
            }

            for (String raw : root.getStringList(materialName + ".locations")) {
                BlockKey key = BlockKey.fromString(raw);

                if (key == null) {
                    continue;
                }

                placedOres.computeIfAbsent(material, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            }
        }

        dirty = false;
    }

    public void savePlacedOres() {
        if (!dirty) {
            return;
        }

        LocationsFile.get().set(ROOT, null);

        for (Map.Entry<Material, Set<BlockKey>> entry : placedOres.entrySet()) {
            Material material = entry.getKey();
            Set<BlockKey> locations = entry.getValue();

            if (locations == null || locations.isEmpty()) {
                continue;
            }

            ArrayList<String> serialized = new ArrayList<>();

            for (BlockKey key : locations) {
                serialized.add(key.toString());
            }

            Collections.sort(serialized);
            LocationsFile.get().set(ROOT + "." + material.name() + ".locations", serialized);
        }

        LocationsFile.save();
        dirty = false;
    }

    public void markPlacedOre(Location location, Material material) {
        if (location == null || material == null || !isOre(material)) {
            return;
        }

        BlockKey key = BlockKey.fromLocation(location);

        if (key == null) {
            return;
        }

        placedOres.computeIfAbsent(material, ignored -> ConcurrentHashMap.newKeySet()).add(key);
        dirty = true;
    }

    public boolean isPlacedOre(Location location, Material material) {
        if (location == null || material == null || !isOre(material)) {
            return false;
        }

        Set<BlockKey> locations = placedOres.get(material);

        if (locations == null || locations.isEmpty()) {
            return false;
        }

        BlockKey key = BlockKey.fromLocation(location);

        return key != null && locations.contains(key);
    }

    public boolean removePlacedOre(Location location, Material material) {
        if (location == null || material == null || !isOre(material)) {
            return false;
        }

        Set<BlockKey> locations = placedOres.get(material);

        if (locations == null || locations.isEmpty()) {
            return false;
        }

        BlockKey key = BlockKey.fromLocation(location);

        if (key == null) {
            return false;
        }

        boolean removed = locations.remove(key);

        if (locations.isEmpty()) {
            placedOres.remove(material);
        }

        if (removed) {
            dirty = true;
        }

        return removed;
    }

    public boolean isOre(Material material) {
        return material != null && ores.contains(material);
    }

    private static final class BlockKey {

        private final String world;
        private final int x;
        private final int y;
        private final int z;

        private BlockKey(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static BlockKey fromLocation(Location location) {
            if (location == null) {
                return null;
            }

            World world = location.getWorld();

            if (world == null) {
                return null;
            }

            return new BlockKey(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        private static BlockKey fromString(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }

            String[] split = input.split(";");

            if (split.length != 4) {
                return null;
            }

            try {
                return new BlockKey(
                        split[0],
                        Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]),
                        Integer.parseInt(split[3])
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        @Override
        public String toString() {
            return world + ";" + x + ";" + y + ";" + z;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (!(object instanceof BlockKey other)) {
                return false;
            }

            return x == other.x
                    && y == other.y
                    && z == other.z
                    && world.equals(other.world);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}
