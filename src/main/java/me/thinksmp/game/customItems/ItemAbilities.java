package me.thinksmp.game.customItems;


import me.thinksmp.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ItemAbilities implements Listener {

    private final Map<Player, EnumMap<CustomItem, Long>> cooldowns = new HashMap<>();
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private final Map<Player, Long> frozen = new HashMap<>();
    private final Map<Projectile, CustomItem> projectileItems = new HashMap<>();

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateBossBar(player);
                    applyHeldEffects(player);
                }
            }
        }.runTaskTimer(Core.getPlugin(), 1L, 1L);
    }

    @EventHandler
    public void onSwordHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        CustomItem item = Core.getItemManager().getType(player.getInventory().getItemInMainHand());
        if (item != CustomItem.SWORD) return;

        if (isOnCooldown(player, item)) return;

        double damage = event.getFinalDamage();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double newHealth = Math.min(maxHealth, currentHealth + damage);
        double overflow = Math.max(0.0, currentHealth + damage - maxHealth);

        player.setHealth(newHealth);

        if (overflow > 0.0) {
            player.setAbsorptionAmount(Math.min(4.0, player.getAbsorptionAmount() + overflow));
        }

        setCooldown(player, item, 10);
    }

    @EventHandler
    public void onScytheHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        CustomItem item = Core.getItemManager().getType(player.getInventory().getItemInMainHand());
        if (item != CustomItem.ABYSSAL_SCYTHE) return;

        if (isOnCooldown(player, item)) return;

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 1));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 140, 1));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 0));

        setCooldown(player, item, 35);
    }

    @EventHandler
    public void onHammerUse(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        CustomItem item = Core.getItemManager().getType(player.getInventory().getItemInMainHand());
        if (item != CustomItem.HAMMER) return;

        event.setCancelled(true);

        if (isOnCooldown(player, item)) return;

        Vector direction = player.getLocation().getDirection().normalize().multiply(1.8);
        direction.setY(1.2);

        player.setVelocity(direction);
        setCooldown(player, item, 30);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        if (event.getEntity() instanceof Trident) {
            CustomItem item = Core.getItemManager().getType(player.getInventory().getItemInMainHand());

            if (item == CustomItem.TRIDENT) {
                projectileItems.put(event.getEntity(), item);
            }
        }
    }

    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;

        CustomItem item = projectileItems.remove(trident);
        if (item != CustomItem.TRIDENT) return;

        Entity hit = event.getHitEntity();
        if (!(hit instanceof LivingEntity victim)) return;

        if (isOnCooldown(player, item)) return;

        frozen.put(playerFromEntity(victim), System.currentTimeMillis() + 5000L);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 128));

        setCooldown(player, item, 30);
    }

    @EventHandler
    public void onFrozenMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Long until = frozen.get(player);

        if (until == null) return;

        if (System.currentTimeMillis() > until) {
            frozen.remove(player);
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from);
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();
        CustomItem item = Core.getItemManager().getType(bow);

        if (item != CustomItem.BOW) return;

        if (event.getForce() < 1.0F) return;

        if (isOnCooldown(player, item)) return;

        setCooldown(player, item, 45);

        double damage = 2.0D;
        int knockback = 0;
        boolean critical = true;
        int fireTicks = 0;

        if (event.getProjectile() instanceof AbstractArrow originalArrow) {
            damage = originalArrow.getDamage();
            knockback = originalArrow.getKnockbackStrength();
            critical = originalArrow.isCritical();
            fireTicks = originalArrow.getFireTicks();
        } else if (bow != null) {
            int power = bow.getEnchantmentLevel(Enchantment.POWER);
            int punch = bow.getEnchantmentLevel(Enchantment.PUNCH);
            boolean flame = bow.containsEnchantment(Enchantment.FLAME);

            damage = 2.0D;

            if (power > 0) {
                damage += (power * 0.5D) + 0.5D;
            }

            knockback = punch;
            fireTicks = flame ? 100 : 0;
        }

        double finalDamage = damage;
        int finalKnockback = knockback;
        boolean finalCritical = critical;
        int finalFireTicks = fireTicks;

        new BukkitRunnable() {
            int shots = 0;

            @Override
            public void run() {
                if (!player.isOnline() || shots >= 8) {
                    cancel();
                    return;
                }

                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(3.2D));
                arrow.setShooter(player);
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

                arrow.setDamage(finalDamage);
                arrow.setKnockbackStrength(finalKnockback);
                arrow.setCritical(finalCritical);

                if (finalFireTicks > 0) {
                    arrow.setFireTicks(finalFireTicks);
                }

                shots++;
            }
        }.runTaskTimer(Core.getPlugin(), 10L, 10L);
    }

    @EventHandler
    public void onTreeChop(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        CustomItem item = Core.getItemManager().getType(tool);
        if (item != CustomItem.AXE) return;

        Block start = event.getBlock();
        if (!Tag.LOGS.isTagged(start.getType())) return;

        event.setCancelled(true);

        List<Block> logs = getConnectedLogs(start, 256);

        for (Block block : logs) {
            for (ItemStack drop : block.getDrops(tool)) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }

            block.setType(Material.AIR);
        }
    }

    @EventHandler
    public void onMineOre(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        CustomItem item = Core.getItemManager().getType(tool);
        if (item != CustomItem.PICKAXE) return;

        Material mined = event.getBlock().getType();
        if (!isOre(mined)) return;

        if (Math.random() > 0.15) return;

        Material bonus = randomBonusOreDrop(mined);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(bonus));
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (Core.getItemManager().isCustomItem(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDroppedItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;

        if (Core.getItemManager().isCustomItem(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        cooldowns.remove(player);
        frozen.remove(player);

        BossBar bossBar = bossBars.remove(player);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    private void applyHeldEffects(Player player) {
        CustomItem item = Core.getItemManager().getType(player.getInventory().getItemInMainHand());

        if (item == CustomItem.PICKAXE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false, false));
        }
    }

    private List<Block> getConnectedLogs(Block start, int limit) {
        List<Block> result = new ArrayList<>();
        ArrayDeque<Block> queue = new ArrayDeque<>();
        HashSet<String> visited = new HashSet<>();

        queue.add(start);

        while (!queue.isEmpty() && result.size() < limit) {
            Block block = queue.poll();
            String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();

            if (!visited.add(key)) continue;
            if (!Tag.LOGS.isTagged(block.getType())) continue;

            result.add(block);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        queue.add(block.getRelative(x, y, z));
                    }
                }
            }
        }

        return result;
    }

    private boolean isOre(Material material) {
        return material == Material.COAL_ORE
                || material == Material.DEEPSLATE_COAL_ORE
                || material == Material.IRON_ORE
                || material == Material.DEEPSLATE_IRON_ORE
                || material == Material.COPPER_ORE
                || material == Material.DEEPSLATE_COPPER_ORE
                || material == Material.GOLD_ORE
                || material == Material.DEEPSLATE_GOLD_ORE
                || material == Material.REDSTONE_ORE
                || material == Material.DEEPSLATE_REDSTONE_ORE
                || material == Material.EMERALD_ORE
                || material == Material.DEEPSLATE_EMERALD_ORE
                || material == Material.LAPIS_ORE
                || material == Material.DEEPSLATE_LAPIS_ORE
                || material == Material.DIAMOND_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE
                || material == Material.ANCIENT_DEBRIS;
    }

    private Material randomBonusOreDrop(Material mined) {
        Material[] drops = {
                Material.COAL,
                Material.RAW_IRON,
                Material.RAW_COPPER,
                Material.RAW_GOLD,
                Material.REDSTONE,
                Material.EMERALD,
                Material.LAPIS_LAZULI,
                Material.DIAMOND,
                Material.NETHERITE_INGOT
        };

        return drops[(int) (Math.random() * drops.length)];
    }

    private Player playerFromEntity(LivingEntity entity) {
        return entity instanceof Player player ? player : null;
    }

    private boolean isOnCooldown(Player player, CustomItem item) {
        EnumMap<CustomItem, Long> map = cooldowns.get(player);
        if (map == null) return false;

        Long until = map.get(item);
        return until != null && System.currentTimeMillis() < until;
    }

    private void setCooldown(Player player, CustomItem item, int seconds) {
        cooldowns.computeIfAbsent(player, ignored -> new EnumMap<>(CustomItem.class))
                .put(item, System.currentTimeMillis() + seconds * 1000L);
    }

    private long getCooldownLeft(Player player, CustomItem item) {
        EnumMap<CustomItem, Long> map = cooldowns.get(player);
        if (map == null) return 0L;

        Long until = map.get(item);
        if (until == null) return 0L;

        return Math.max(0L, until - System.currentTimeMillis());
    }

    private void updateBossBar(Player player) {
        CustomItem held = Core.getItemManager().getType(player.getInventory().getItemInMainHand());

        if (held == null) {
            hideBossBar(player);
            return;
        }

        long left = getCooldownLeft(player, held);

        if (left <= 0L) {
            hideBossBar(player);
            return;
        }

        BossBar bossBar = bossBars.computeIfAbsent(player, ignored -> {
            BossBar created = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
            created.addPlayer(player);
            return created;
        });

        double total = cooldownLength(held) * 1000.0;
        double progress = Math.max(0.0, Math.min(1.0, left / total));

        bossBar.setTitle("§c" + held.getId() + " cooldown §7- §f" + String.format("%.1f", left / 1000.0) + "s");
        bossBar.setProgress(progress);

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    private void hideBossBar(Player player) {
        BossBar bossBar = bossBars.get(player);

        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    private int cooldownLength(CustomItem item) {
        return switch (item) {
            case SWORD -> 10;
            case HAMMER -> 30;
            case TRIDENT -> 30;
            case ABYSSAL_SCYTHE -> 35;
            case BOW -> 45;
            default -> 1;
        };
    }
}