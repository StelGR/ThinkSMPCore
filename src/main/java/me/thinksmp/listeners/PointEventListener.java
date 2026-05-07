package me.thinksmp.listeners;

import me.thinksmp.Core;
import me.thinksmp.files.LocationsFile;
import me.thinksmp.functions.Points;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PointEventListener implements Listener {

    private final NamespacedKey potKey = new NamespacedKey(Core.getPlugin(), "points_pot");

    private final Map<String, Integer> ADVANCEMENT_POINTS = new HashMap<>();

    public PointEventListener() {
        // Initialize known advancements with their points
        ADVANCEMENT_POINTS.put("minecraft:story/shiny_gear", 10_000);
        ADVANCEMENT_POINTS.put("minecraft:nether/create_full_beacon", 10_000);
        ADVANCEMENT_POINTS.put("minecraft:nether/create_beacon", 5_000);
        ADVANCEMENT_POINTS.put("minecraft:adventure/adventuring_time", 1_000_000);
        ADVANCEMENT_POINTS.put("minecraft:adventure/hero_of_the_village", 500);
        ADVANCEMENT_POINTS.put("minecraft:husbandry/balanced_diet", 300);
        ADVANCEMENT_POINTS.put("minecraft:nether/all_effects", 500_000);
        ADVANCEMENT_POINTS.put("minecraft:nether/get_wither_skull", 3_000);
        ADVANCEMENT_POINTS.put("minecraft:story/enter_the_nether", 1_000);
        ADVANCEMENT_POINTS.put("minecraft:story/enter_the_end", 5_000);
        ADVANCEMENT_POINTS.put("minecraft:nether/netherite_armor", 50_000);
        ADVANCEMENT_POINTS.put("minecraft:nether/summon_wither", 25_000);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
//        Randomizer.getRandomLocation(world, -30000, 30000).thenAccept(loc -> Bukkit.getScheduler().runTask(Core.getPlugin(), () -> player.teleport(loc)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);
        Material type = event.getBlock().getType();

        if (type == Material.AIR || type == Material.FIRE || type == Material.SOUL_FIRE) return;

        if (Core.getPlacedOreManager().removePlacedOre(event.getBlock().getLocation(), type)) {
            return;
        }

        int points = switch (type) {
            case SUGAR_CANE -> 10;
            case BOOKSHELF -> 15;
            case PUMPKIN, BAMBOO, MELON, POTATOES -> 20;
            case CARROTS, CACTUS -> 5;
            case WHEAT -> 30;
            case NETHER_WART, BEETROOT -> 50;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> Points.COAL_ORE.getPoints();
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Points.COPPER_ORE.getPoints();
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Points.IRON_ORE.getPoints();
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Points.GOLD_ORE.getPoints();
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Points.REDSTONE_ORE.getPoints();
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Points.LAPIS_ORE.getPoints();
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Points.EMERALD_ORE.getPoints();
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Points.DIAMOND_ORE.getPoints();
            case ANCIENT_DEBRIS -> Points.ANCIENT_DEBRI.getPoints();
            case NETHER_QUARTZ_ORE -> Points.QUARTZ_ORE.getPoints();
            default -> 1;
        };

        playerData.setPoints(playerData.getPoints() + points);
        if (points > 1) {
            player.sendMessage(ChatColor.GREEN + "+" + points + " point(s) for breaking " + type.toString().toLowerCase().replace("_", " ") + "!");
            if (playerData.isHasPointSound()) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(killer);

        switch (event.getEntity().getType()) {
            // Passive animals
            case COW:
            case PIG:
            case SHEEP:
            case CHICKEN:
            case RABBIT:
            case HORSE:
            case DONKEY:
            case BAT:
            case FOX:
            case BEE:
            case COD:
            case FROG:
            case MULE:
            case WOLF:
            case GOAT:
            case LLAMA:
            case PANDA:
            case CAMEL:
            case SQUID:
            case ARMADILLO:
            case PUFFERFISH:
            case AXOLOTL:
            case PARROT:
            case SNIFFER:
            case HAPPY_GHAST:
            case POLAR_BEAR:
            case CAT:
            case ALLAY:
            case OCELOT:
            case SALMON:
            case MOOSHROOM:
                playerData.setPoints(playerData.getPoints() + Points.ANIMAL.getPoints());
                killer.sendMessage(ChatColor.GREEN + "+" + Points.ANIMAL.getPoints() + " points for killing an animal!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                break;

            // Hostile mobs
            case ZOMBIE:
            case SKELETON:
            case CREEPER:
            case SPIDER:
            case ENDERMAN:
            case BLAZE:
            case WITCH:
            case SLIME:
            case MAGMA_CUBE:
            case GUARDIAN:
            case PIGLIN:
            case PIGLIN_BRUTE:
            case ENDERMITE:
            case SHULKER:
            case VINDICATOR:
            case EVOKER:
            case GHAST:
            case ZOGLIN:
            case DROWNED:
            case CAVE_SPIDER:
            case CREAKING:
            case ZOMBIE_VILLAGER:
            case HUSK:
            case BOGGED:
            case PILLAGER:
            case ZOMBIFIED_PIGLIN:
            case BREEZE:
            case VEX:
            case ILLUSIONER:
            case RAVAGER:
            case PHANTOM:
                playerData.setPoints(playerData.getPoints() + Points.MONSTER.getPoints());
                killer.sendMessage(ChatColor.GREEN + "+" + Points.MONSTER.getPoints() + " points for killing a monster!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                break;
            // Special bosses
            case ELDER_GUARDIAN:
                playerData.setPoints(playerData.getPoints() + (Points.MONSTER.getPoints() * 5));
                killer.sendMessage(ChatColor.GREEN + "+" + Points.WARDEN.getPoints() + " points for killing the Elder Guardian!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;

            case WARDEN:
                playerData.setPoints(playerData.getPoints() + Points.WARDEN.getPoints());
                killer.sendMessage(ChatColor.GREEN + "+" + Points.WARDEN.getPoints() + " points for killing the Warden!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;
            case WITHER:
                playerData.setPoints(playerData.getPoints() + Points.WITHER.getPoints());
                killer.sendMessage(ChatColor.GREEN + "+" + Points.WARDEN.getPoints() + " points for killing the Wither!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;
            case ENDER_DRAGON:
                playerData.setPoints(playerData.getPoints() + Points.ENDER_DRAGON.getPoints());
                killer.sendMessage(ChatColor.GREEN + "+" + Points.WARDEN.getPoints() + " points for killing the Ender Dragon!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;
            case IRON_GOLEM:
                playerData.setPoints(playerData.getPoints() + 100);
                killer.sendMessage(ChatColor.GREEN + "+" + 100 + " points for killing an Iron Golem!");
                if (playerData.isHasPointSound()) killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;

            default:
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnyPotBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.DECORATED_POT) return;

        Location loc = event.getBlock().getLocation();
        String potId = getPotId(loc);
        if (potId == null) return; // not one of our point pots, allow normal breaking

        // Cancel *any* attempt to break point pots manually
        event.setCancelled(true);
    }


    @EventHandler
    public void onPotExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getType() == Material.DECORATED_POT && getPotId(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPotProjectileHit(ProjectileHitEvent event) {
        Block block = event.getHitBlock();

        if (block == null || block.getType() != Material.DECORATED_POT) {
            return;
        }

        Location loc = block.getLocation();
        String potId = getPotId(loc);

        if (potId == null) {
            return;
        }

        event.setCancelled(true);
        event.getEntity().remove();

        ProjectileSource shooter = event.getEntity().getShooter();

        if (shooter instanceof Player player) {
            claimPot(player, loc, potId);
            return;
        }

        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            if (loc.getBlock().getType() != Material.DECORATED_POT) {
                loc.getBlock().setType(Material.DECORATED_POT);
            }

            Block skullBlock = loc.clone().add(0, 1, 0).getBlock();
            if (skullBlock.getType() == Material.AIR) {
                skullBlock.setType(Material.SKELETON_SKULL);
            }
        });
    }

    @EventHandler
    public void onPotPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.DECORATED_POT && getPotId(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotBurn(BlockBurnEvent event) {
        if (event.getBlock().getType() == Material.DECORATED_POT && getPotId(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();
        String key = advancement.getKey().toString();

        // Skip recipe-based advancements
        if (key.startsWith("minecraft:recipes/")) {
            return;
        }

        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

        // Get points from map, default to 100 if not found
        int points = ADVANCEMENT_POINTS.getOrDefault(key, 100);

        // Debug message to console
        GeneralUtility.log("Advancement: " + key + " | Points: " + points);

        playerData.setPoints(playerData.getPoints() + points);
        player.sendMessage(ChatColor.GREEN + "+" + GeneralUtility.formatNumberWithDots( points) + " points for advancement!");
        if (playerData.isHasPointSound()) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }



    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);
        Material type = event.getBlockPlaced().getType();

        if (Core.getPlacedOreManager().isOre(type)) {
            Core.getPlacedOreManager().markPlacedOre(event.getBlockPlaced().getLocation(), type);
        }

        int points = 0;

        if (type == Material.BEACON) {
            points = 200;
        }

        if (points > 0) {
            playerData.setPoints(playerData.getPoints() + points);
            player.sendMessage(ChatColor.GREEN + "+" + GeneralUtility.formatNumberWithDots(points) + " points for placing " + type.toString().toLowerCase().replace("_", " ") + "!");
            if (playerData.isHasPointSound()) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

        if (playerData == null || playerData.getPoints() <= 0) return;

        int lostPoints = playerData.getPoints() / 2;
        playerData.setPoints(lostPoints);


        Location loc = player.getLocation();
        player.sendMessage(GeneralUtility.translate("&cYou died at "+Math.round(loc.getX())+", "+Math.round(loc.getY())+", "+Math.round(loc.getZ())));
        // Spawn decorated pot
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            loc.getBlock().setType(Material.DECORATED_POT);
            loc.clone().add(0, 1, 0).getBlock().setType(Material.SKELETON_SKULL);
        }, 2L);
        // Spawn armor stand above it
        Location armorstandLoc = loc.getBlock().getLocation().clone();

        double roundedX = Math.round(armorstandLoc.getX() * 2) / 2.0;
        double roundedZ = Math.round(armorstandLoc.getZ() * 2) / 2.0;
        armorstandLoc.setX(roundedX);
        armorstandLoc.setZ(roundedZ);

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(armorstandLoc.clone().add(0.5, 1.5, 0.5), EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName("§a" + GeneralUtility.formatNumberWithDots(lostPoints));
        stand.getPersistentDataContainer().set(potKey, PersistentDataType.INTEGER, lostPoints);
        ArmorStand stand2 = (ArmorStand) player.getWorld().spawnEntity(armorstandLoc.clone().add(0.5, 1.25, 0.5), EntityType.ARMOR_STAND);
        stand2.setInvisible(true);
        stand2.setMarker(true);
        stand2.setCustomNameVisible(true);
        stand2.setCustomName("§cPoints");
        stand2.getPersistentDataContainer().set(potKey, PersistentDataType.INTEGER, lostPoints);

        // Store location in Locations.yml
        String key = UUID.randomUUID().toString();
        LocationsFile.get().set("pots." + key + ".world", loc.getWorld().getName());
        LocationsFile.get().set("pots." + key + ".x", loc.getBlockX());
        LocationsFile.get().set("pots." + key + ".y", loc.getBlockY());
        LocationsFile.get().set("pots." + key + ".z", loc.getBlockZ());
        LocationsFile.get().set("pots." + key + ".points", lostPoints);
        LocationsFile.save();
    }


    @EventHandler(ignoreCancelled = true)
    public void onPotBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.DECORATED_POT) {
            return;
        }

        Location loc = event.getBlock().getLocation();
        String potId = getPotId(loc);

        if (potId == null) {
            return;
        }

        event.setCancelled(true);
        claimPot(event.getPlayer(), loc, potId);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.DECORATED_POT) {
            return;
        }

        Location loc = event.getClickedBlock().getLocation();
        String potId = getPotId(loc);

        if (potId == null) {
            return;
        }

        event.setCancelled(true);
        claimPot(event.getPlayer(), loc, potId);
    }

    private String getPotId(Location loc) {
        if (!LocationsFile.get().isConfigurationSection("pots")) return null;
        for (String key : LocationsFile.get().getConfigurationSection("pots").getKeys(false)) {
            String world = LocationsFile.get().getString("pots." + key + ".world");
            int x = LocationsFile.get().getInt("pots." + key + ".x");
            int y = LocationsFile.get().getInt("pots." + key + ".y");
            int z = LocationsFile.get().getInt("pots." + key + ".z");

            if (world.equals(loc.getWorld().getName()) && x == loc.getBlockX() && y == loc.getBlockY() && z == loc.getBlockZ()) {
                return key;
            }
        }
        return null;
    }

    private void removePotEntities(Location loc) {
        loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 1.0, 0.5), 0.5, 1, 0.5).stream()
                .filter(e -> e instanceof ArmorStand)
                .forEach(Entity::remove);
        loc.getBlock().setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonExplodeDamage(EntityDamageEvent e) {
        if (!isDragonOrPart(e.getEntity())) return;
        EntityDamageEvent.DamageCause c = e.getCause();
        if (c == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || c == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            e.setCancelled(true);
            e.setDamage(0.0);
        }
    }

    private boolean isDragonOrPart(Entity entity) {
        if (entity instanceof EnderDragon) return true;
        if (entity instanceof EnderDragonPart) {
            ((EnderDragonPart) entity).getParent();
            return true;
        }
        return false;
    }

    @EventHandler
    public void onDragonExplosionDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    private void claimPot(Player player, Location loc, String potId) {
        int points = LocationsFile.get().getInt("pots." + potId + ".points");

        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);
        playerData.setPoints(playerData.getPoints() + points);

        removePotEntities(loc);
        loc.getBlock().setType(Material.AIR);
        loc.clone().add(0, 1, 0).getBlock().setType(Material.AIR);

        LocationsFile.get().set("pots." + potId, null);
        LocationsFile.save();

        player.sendMessage("§aYou claimed " + GeneralUtility.formatNumberWithDots(points) + " points!");

        if (playerData.isHasPointSound()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

}
