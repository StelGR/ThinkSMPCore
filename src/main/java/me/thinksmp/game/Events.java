package me.thinksmp.game;

import lombok.Getter;
import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Events implements Listener {
    private final Random random = new Random();

    public Events() {
        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            if (Bukkit.getOnlinePlayers().size() >= 10) {
                Random random = new Random();

                int x = random.nextInt(8_001) - 4_000;
                int z = random.nextInt(8_001) - 4_000;

                World world = Bukkit.getWorld("world");

                if (world != null) {
                    int y = world.getMaxHeight() - 2;

                    Location randomLocation = new Location(world, x, y, z);

                    if (random.nextInt(100) < 70) {
                        startMeteor(randomLocation);
                    }
                }
            }
        }, 0L, 2 * 60 * 60 * 20L);
    }

    public void startMeteor(Location spawnLoc) {
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ An unknown crate is falling out of the sky!");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3f, 1f);

            p.sendTitle(
                    ChatColor.DARK_RED + "⚠ WARNING ⚠",
                    ChatColor.LIGHT_PURPLE + "A mysterious crate is falling from the sky!",
                    10, 60, 20
            );
        }

        Location loc = spawnLoc.clone();
        loc.setY(loc.getWorld().getMaxHeight());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (loc.getBlock().getType() == Material.CHEST) {
                    loc.getBlock().setType(Material.AIR);
                }
                loc.subtract(0, 1, 0);

                if (loc.getBlock().getType().isSolid() || loc.getY() <= loc.getWorld().getMinHeight() + 1) {
                    impact(loc.add(0, 1, 0)); // go back up 1 because last subtract stepped inside the ground
                    cancel();
                    return;
                }

                loc.getBlock().setType(Material.CHEST);

                loc.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.5, 0.3, 0.05);

                loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f,
                        (float) (0.5 + random.nextDouble() * 1.5));
            }
        }.runTaskTimer(Core.getPlugin(), 0L, 2L);
    }


    // impact: replace chest filling logic
    private void impact(Location loc) {
        World world = loc.getWorld();

        // Explosion effect
        world.spawnParticle(Particle.EXPLOSION, loc, 3, 2, 2, 2, 0.1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.6f);

        // Real crater (charged creeper ~ radius 6)
        world.createExplosion(loc, 10f, false, true);

        // Knockback
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(world) && p.getLocation().distance(loc) < 8) {
                Vector knockback = p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5);
                knockback.setY(0.5);
                p.setVelocity(knockback);
            }
        }

        // Place real loot chest
        loc.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) loc.getBlock().getState();

        Rarity rarity = rollRarity();
        String name = rarity.getColoredName() + ChatColor.GRAY + " Crate";
        chest.setCustomName(name);
        chest.update();

        fillChest(chest.getBlockInventory(), rarity);

        // Announce coords
        Bukkit.broadcastMessage(GeneralUtility.translate(ChatColor.GOLD + "⚡ &cA "
                + rarity.getColoredName() + " &ccrate has landed at "
                + ChatColor.YELLOW + loc.getBlockX() + ", "
                + loc.getBlockY() + ", " + loc.getBlockZ()));

        // Special sound
        if (rarity == Rarity.LEGENDARY || rarity == Rarity.MYTHIC) {
            world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 5f, 1f);
        }
    }


    private Rarity rollRarity() {
        double roll = random.nextDouble() * 100;
        if (roll <= 0.5) return Rarity.MYTHIC;
        if (roll <= 5.5) return Rarity.LEGENDARY;
        if (roll <= 15.5) return Rarity.RARE;
        return Rarity.COMMON;
    }

    private void fillChest(Inventory inv, Rarity rarity) {
        List<ItemStack> pool = switch (rarity) {
            case COMMON -> Arrays.asList(
                    prepareItem(Material.BREAD, rarity),
                    prepareItem(Material.COOKED_BEEF, rarity),
                    prepareItem(Material.APPLE, rarity),
                    prepareItem(Material.IRON_HELMET, rarity),
                    prepareItem(Material.LEATHER_BOOTS, rarity),
                    prepareItem(Material.IRON_PICKAXE, rarity),
                    prepareItem(Material.CARROT, rarity),
                    prepareItem(Material.POTATO, rarity),
                    prepareItem(Material.COOKED_MUTTON, rarity),
                    prepareItem(Material.COOKED_CHICKEN, rarity),
                    prepareItem(Material.MELON_SLICE, rarity),
                    prepareItem(Material.PUMPKIN_PIE, rarity),
                    prepareItem(Material.STONE_SWORD, rarity),
                    prepareItem(Material.STONE_PICKAXE, rarity),
                    prepareItem(Material.STONE_AXE, rarity),
                    prepareItem(Material.CHAINMAIL_HELMET, rarity),
                    prepareItem(Material.CHAINMAIL_BOOTS, rarity),
                    prepareItem(Material.SHIELD,  rarity),
                    prepareItem(Material.BOW, rarity),
                    prepareItem(Material.ARROW, rarity)
            );
            case RARE -> Arrays.asList(
                    // Some common items
                    prepareItem(Material.IRON_SWORD, rarity),
                    prepareItem(Material.IRON_PICKAXE, rarity),
                    prepareItem(Material.IRON_HELMET, rarity),
                    prepareItem(Material.COOKED_BEEF, rarity),
                    prepareItem(Material.GOLDEN_APPLE, rarity),

                    // Mid-tier items
                    prepareItem(Material.IRON_CHESTPLATE, rarity),
                    prepareItem(Material.IRON_LEGGINGS, rarity),
                    prepareItem(Material.IRON_BOOTS, rarity),
                    prepareItem(Material.EXPERIENCE_BOTTLE, rarity),
                    prepareItem(Material.CROSSBOW, rarity),

                    prepareItem(Material.GOLDEN_CARROT, rarity),
                    prepareItem(Material.TOTEM_OF_UNDYING, rarity),
                    prepareItem(Material.SPYGLASS, rarity),
                    prepareItem(Material.COOKED_SALMON, rarity),
                    prepareItem(Material.COOKED_COD, rarity),

                    prepareItem(Material.LANTERN, rarity),
                    prepareItem(Material.ENDER_PEARL, rarity),
                    prepareItem(Material.SHIELD, rarity),
                    prepareItem(Material.BOW, rarity),
                    prepareItem(Material.FIRE_CHARGE, rarity)
            );

            case LEGENDARY -> Arrays.asList(
                    // Some mid-tier items
                    prepareItem(Material.IRON_SWORD, rarity),
                    prepareItem(Material.GOLDEN_APPLE, rarity),
                    prepareItem(Material.IRON_PICKAXE, rarity),

                    // Mostly high-tier items
                    prepareItem(Material.DIAMOND_SWORD, rarity),
                    prepareItem(Material.DIAMOND_PICKAXE, rarity),
                    prepareItem(Material.DIAMOND_AXE, rarity),
                    prepareItem(Material.DIAMOND_HELMET, rarity),
                    prepareItem(Material.DIAMOND_CHESTPLATE, rarity),
                    prepareItem(Material.DIAMOND_LEGGINGS, rarity),
                    prepareItem(Material.DIAMOND_BOOTS, rarity),

                    prepareItem(Material.ENCHANTED_GOLDEN_APPLE, rarity),
                    prepareItem(Material.NETHERITE_INGOT, rarity),
                    prepareItem(Material.BEACON, rarity),
                    prepareItem(Material.SHULKER_SHELL, rarity),
                    prepareItem(Material.ELYTRA, rarity),

                    prepareItem(Material.CROSSBOW, rarity),
                    prepareItem(Material.TOTEM_OF_UNDYING, rarity),
                    prepareItem(Material.FIREWORK_ROCKET, rarity),
                    prepareItem(Material.ENCHANTED_BOOK, rarity),
                    prepareItem(Material.SPYGLASS, rarity)
            );

            case MYTHIC -> Arrays.asList(
                    // Some high-tier items
                    prepareItem(Material.DIAMOND_SWORD, rarity),
                    prepareItem(Material.ELYTRA, rarity),
                    prepareItem(Material.NETHERITE_SWORD, rarity),

                    // Mostly ultra-rare/endgame items
                    prepareItem(Material.NETHERITE_CHESTPLATE, rarity),
                    prepareItem(Material.NETHERITE_HELMET, rarity),
                    prepareItem(Material.NETHERITE_LEGGINGS, rarity),
                    prepareItem(Material.NETHERITE_BOOTS, rarity),
                    prepareItem(Material.NETHERITE_AXE, rarity),
                    prepareItem(Material.NETHERITE_PICKAXE, rarity),
                    prepareItem(Material.TRIDENT, rarity),
                    prepareItem(Material.DRAGON_HEAD, rarity),
                    prepareItem(Material.DRAGON_EGG, rarity),
                    prepareItem(Material.HEART_OF_THE_SEA, rarity),
                    prepareItem(Material.NAUTILUS_SHELL, rarity),
                    prepareItem(Material.BEACON, rarity),
                    prepareItem(Material.TOTEM_OF_UNDYING, rarity),
                    prepareItem(Material.ENCHANTED_GOLDEN_APPLE, rarity),
                    prepareItem(Material.FIREWORK_ROCKET, rarity),
                    prepareItem(Material.ENCHANTED_BOOK, rarity),
                    prepareItem(Material.SHULKER_BOX, rarity),
                    prepareItem(Material.SPYGLASS, rarity)
            );
        };

        // Shuffle the pool
        Collections.shuffle(pool, random);

        // Pick up to 10 items (or less if pool smaller)
        List<ItemStack> chosen = pool.subList(0, Math.min(10, pool.size()));

        // Place them in random slots
        for (ItemStack item : chosen) {
            int slot;
            do {
                slot = random.nextInt(inv.getSize());
            } while (inv.getItem(slot) != null); // ensure unique slots
            inv.setItem(slot, item);
        }
    }


    private ItemStack prepareItem(Material material, Rarity rarity) {
        ItemStack item = new ItemStack(material);

        // Handle stackable items
        if (material.getMaxStackSize() > 1) {
            int amount = 8 + random.nextInt(41); // 8–48
            item.setAmount(amount);
        }

        // Handle durability items
        int maxDurability = material.getMaxDurability();
        if (maxDurability > 0) {
            int minDamage = (int) (maxDurability * 0.10);
            int maxDamage = (int) (maxDurability * 0.60);
            int damage = random.nextInt(minDamage, maxDamage + 1);
            item.setDurability((short) Math.max(0, Math.min(maxDurability, damage)));
        }

        // Apply enchant chance based on rarity
        maybeEnchant(item, rarity);

        return item;
    }

    private void maybeEnchant(ItemStack item, Rarity rarity) {
        double chance = switch (rarity) {
            case COMMON -> 0.05;
            case RARE -> 0.20;
            case LEGENDARY -> 0.50;
            case MYTHIC -> 0.90;
        };

        if (random.nextDouble() > chance) return; // no enchants

        Material type = item.getType();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Weapons
        if (type.name().endsWith("_SWORD") || type == Material.BOW) {
            if (type.name().endsWith("_SWORD")) if (random.nextBoolean()) meta.addEnchant(Enchantment.SHARPNESS, 1 + random.nextInt(3), true);
            if (type.name().endsWith("_SWORD")) if (random.nextInt(100) < 30) meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
            if (random.nextInt(100) < 50) meta.addEnchant(Enchantment.UNBREAKING, 1 + random.nextInt(2), true);
            if (type == Material.BOW) {
                if (random.nextInt(100) < 40) meta.addEnchant(Enchantment.POWER, 1 + random.nextInt(3), true);
                if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.FLAME, 1, true);
            }
        }

        // Armor
        else if (type.name().endsWith("_HELMET") || type.name().endsWith("_CHESTPLATE")
                || type.name().endsWith("_LEGGINGS") || type.name().endsWith("_BOOTS")) {
            if (random.nextBoolean()) meta.addEnchant(Enchantment.PROTECTION, 1 + random.nextInt(3), true);
            if (random.nextInt(100) < 50) meta.addEnchant(Enchantment.UNBREAKING, 1 + random.nextInt(2), true);
            if (type.name().endsWith("_BOOTS") && random.nextInt(100) < 30) {
                meta.addEnchant(Enchantment.FEATHER_FALLING, 1 + random.nextInt(2), true);
            }
        }

        // Tools
        else if (type.name().endsWith("_PICKAXE") || type.name().endsWith("_AXE") || type.name().endsWith("_SHOVEL")) {
            if (random.nextInt(100) < 50) meta.addEnchant(Enchantment.EFFICIENCY, 1 + random.nextInt(4), true);
            if (random.nextInt(100) < 40) meta.addEnchant(Enchantment.UNBREAKING, 1 + random.nextInt(2), true);
            if (type.name().endsWith("_PICKAXE")) if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.FORTUNE, 1 + random.nextInt(2), true);
        }

        else if (type == Material.CROSSBOW) {
            if (random.nextInt(100) < 40) meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.PIERCING, 1 + random.nextInt(3), true);
            if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.QUICK_CHARGE, 1 + random.nextInt(2), true);
        }

        else if (type == Material.ENCHANTED_BOOK) {
            if (random.nextInt(100) < 30) meta.addEnchant(Enchantment.EFFICIENCY, 1 + random.nextInt(4), true);
            if (random.nextInt(100) < 70) meta.addEnchant(Enchantment.UNBREAKING, 1 + random.nextInt(2), true);
            if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.PIERCING, 1 + random.nextInt(3), true);
            if (random.nextInt(100) < 20) meta.addEnchant(Enchantment.QUICK_CHARGE, 1 + random.nextInt(2), true);
            if (random.nextInt(100) < 5) meta.addEnchant(Enchantment.FORTUNE, 1 + random.nextInt(2), true);
            if (random.nextInt(100) < 10) meta.addEnchant(Enchantment.FEATHER_FALLING, 1 + random.nextInt(2), true);
        }

        item.setItemMeta(meta);
    }


    private enum Rarity {
        COMMON("&a", "Common"),
        RARE("&9", "Rare"),
        LEGENDARY("&e", "Legendary"),
        MYTHIC("&6", "Mythic");

        private final String colorCode;
        @Getter
        private final String name;

        Rarity(String colorCode, String name) {
            this.colorCode = colorCode;
            this.name = name;
        }

        public String getColoredName() {
            return GeneralUtility.translate(colorCode + name);
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) e.getClickedBlock().getState();
            if (chest.getCustomName() != null && chest.getCustomName().contains("Falling Crate")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) e.getBlock().getState();
            if (chest.getCustomName() != null && chest.getCustomName().contains("Crate")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(org.bukkit.event.entity.EntityExplodeEvent e) {
        e.blockList().removeIf(b -> b.getType() == Material.CHEST &&
                ((Chest) b.getState()).getCustomName() != null &&
                ((Chest) b.getState()).getCustomName().contains("Crate"));
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof Chest chest) {
            if (chest.getCustomName() != null && chest.getCustomName().contains("Crate")) {
                boolean empty = true;
                for (ItemStack item : e.getInventory().getContents()) {
                    if (item != null) { empty = false; break; }
                }
                if (empty) {
                    chest.getBlock().setType(Material.AIR);
                    Bukkit.broadcastMessage(ChatColor.GREEN + e.getPlayer().getName()
                            + " has claimed the " + chest.getCustomName() + ChatColor.GREEN + "!");
                }
            }
        }
    }
}