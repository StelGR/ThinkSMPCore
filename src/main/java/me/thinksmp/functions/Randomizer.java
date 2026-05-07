package me.thinksmp.functions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Randomizer {

    private static final Random random = new Random();

    public static CompletableFuture<Location> getRandomLocation(World world, int min, int max) {
        CompletableFuture<Location> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(
                org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(Randomizer.class),
                () -> {
                    Location safeLocation = null;

                    boolean nether = world.getEnvironment() == World.Environment.NETHER;

                    for (int attempt = 0; attempt < 50; attempt++) {
                        int x = random.nextInt(max - min + 1) + min;
                        int z = random.nextInt(max - min + 1) + min;

                        try {
                            Location loc = Bukkit.getScheduler().callSyncMethod(
                                    org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(Randomizer.class),
                                    () -> findSafeLocation(world, x, z, nether)
                            ).get();

                            if (loc != null) {
                                safeLocation = loc;
                                break;
                            }

                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    if (safeLocation == null) {
                        safeLocation = world.getSpawnLocation();
                    }

                    future.complete(safeLocation);
                }
        );

        return future;
    }

    private static Location findSafeLocation(World world, int x, int z, boolean nether) {
        int minY = nether ? 20 : world.getMinHeight();
        int maxY = nether ? 125 : world.getMaxHeight();

        minY = Math.max(minY, world.getMinHeight());
        maxY = Math.min(maxY, world.getMaxHeight() - 2);

        /*
         * Nether:
         * Scan from low to high so we find real terrain/caves first,
         * not spots near the upper ceiling.
         *
         * Overworld:
         * Scan top-down like your old logic.
         */
        if (nether) {
            for (int y = minY; y <= maxY; y++) {
                if (isSafeSpawn(world, x, y, z, true)) {
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            }
        } else {
            for (int y = maxY; y > minY; y--) {
                if (isSafeSpawn(world, x, y, z, false)) {
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            }
        }

        return null;
    }

    private static boolean isSafeSpawn(World world, int x, int y, int z, boolean nether) {
        Block ground = world.getBlockAt(x, y - 1, z);
        Block below1 = world.getBlockAt(x, y - 2, z);
        Block below2 = world.getBlockAt(x, y - 3, z);

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);

        Material groundType = ground.getType();
        Material below1Type = below1.getType();
        Material below2Type = below2.getType();

        if (!groundType.isSolid()) {
            return false;
        }

        if (isDangerous(groundType)) {
            return false;
        }

        if (!isAir(feet.getType()) || !isAir(head.getType())) {
            return false;
        }

        /*
         * Extra Nether safety:
         * Make sure the block you spawn on is not just a thin floating ledge
         * with empty/lava space underneath.
         */
        if (nether) {
            if (!below1Type.isSolid() || !below2Type.isSolid()) {
                return false;
            }

            if (isDangerous(below1Type) || isDangerous(below2Type)) {
                return false;
            }

            /*
             * Avoid spawning too close to the Nether ceiling.
             */
            return y < 126;
        }

        return true;
    }

    private static boolean isAir(Material material) {
        return material == Material.AIR || material.name().endsWith("_AIR");
    }

    private static boolean isDangerous(Material material) {
        String name = material.name();

        return material == Material.LAVA
                || name.equals("MAGMA_BLOCK")
                || name.equals("FIRE")
                || name.equals("SOUL_FIRE")
                || name.equals("CACTUS")
                || name.equals("CAMPFIRE")
                || name.equals("SOUL_CAMPFIRE");
    }
}