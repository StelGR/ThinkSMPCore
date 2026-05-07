package me.thinksmp.game.customEnchants.listener;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomEnchantEffectListener implements Listener {

    private static final String META_EXPLOSION = "custom_enchant_explosion";
    private static final String META_GRAPPLING = "custom_enchant_grappling";

    private static final long BLOODLUST_DAMAGE_MEMORY_MS = 5000L;

    private final CustomEnchantManager manager;
    private final Random random = new Random();
    private final Map<UUID, BloodlustHit> bloodlustHits = new ConcurrentHashMap<>();

    public CustomEnchantEffectListener(CustomEnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool == null || tool.getType() == Material.AIR) {
            return;
        }

        int orbBooster = manager.getLevel(tool, CustomEnchantType.ORB_BOOSTER);
        int smelter = manager.getLevel(tool, CustomEnchantType.SMELTER);
        int excavator = manager.getLevel(tool, CustomEnchantType.EXCAVATOR);

        boolean hasExcavator = excavator > 0 && !player.isSneaking();
        boolean hasSmelter = smelter > 0 && CustomEnchantType.isPickaxe(tool.getType().name()) && isSmeltableOre(event.getBlock().getType());

        if (!hasExcavator && !hasSmelter) {
            if (orbBooster > 0 && event.getExpToDrop() > 0) {
                event.setExpToDrop(applyOrbBoost(event.getExpToDrop(), orbBooster));
            }

            return;
        }

        event.setCancelled(true);

        List<Block> blocks = hasExcavator ? getExcavatorBlocks(player, event.getBlock()) : singleBlock(event.getBlock());
        int broken = 0;
        int totalExp = 0;

        for (Block block : blocks) {
            if (!canBreak(player, tool, block)) {
                continue;
            }

            int blockExp = block.equals(event.getBlock()) ? event.getExpToDrop() : getEstimatedOreExp(block.getType(), tool);

            if (orbBooster > 0 && blockExp > 0) {
                blockExp = applyOrbBoost(blockExp, orbBooster);
            }

            totalExp += blockExp;

            breakBlock(player, tool, block, smelter > 0);
            broken++;
        }

        if (totalExp > 0) {
            player.giveExp(totalExp);
        }

        if (broken > 1) {
            damageItem(player, tool, broken - 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHoeTill(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = event.getItem();

        if (tool == null || tool.getType() == Material.AIR) {
            return;
        }

        if (!CustomEnchantType.isHoe(tool.getType().name())) {
            return;
        }

        if (manager.getLevel(tool, CustomEnchantType.EXCAVATOR) <= 0 || player.isSneaking()) {
            return;
        }

        Block center = event.getClickedBlock();
        int tilled = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = center.getRelative(x, 0, z);

                if (canTill(block)) {
                    block.setType(getFarmlandMaterial());
                    tilled++;
                }
            }
        }

        if (tilled > 0) {
            event.setCancelled(true);
            damageItem(player, tool, tilled);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack boots = equipment.getBoots();

        if (boots == null || boots.getType() == Material.AIR) {
            return;
        }

        if (manager.getLevel(boots, CustomEnchantType.FIRE_WALKER) <= 0) {
            return;
        }

        Material magma = Material.matchMaterial("MAGMA_BLOCK");

        if (magma == null) {
            return;
        }

        Location center = player.getLocation();
        int converted = 0;

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 0; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);

                    if (!isLava(block.getType())) {
                        continue;
                    }

                    Material original = block.getType();
                    block.setType(magma);
                    converted++;

                    Bukkit.getScheduler().runTaskLater(manager.plugin(), () -> {
                        if (block.getType() == magma) {
                            block.setType(original);
                        }
                    }, 60L);
                }
            }
        }

        if (converted > 0) {
            damageItem(player, boots, converted);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getProjectile() instanceof AbstractArrow)) {
            return;
        }

        ItemStack bow = event.getBow();

        if (bow == null || bow.getType() == Material.AIR) {
            return;
        }

        int straightShot = manager.getLevel(bow, CustomEnchantType.STRAIGHT_SHOT);

        if (straightShot > 0) {
            double factor;

            if (straightShot >= 3) {
                factor = 1.0D;
            } else if (straightShot == 2) {
                factor = 0.80D;
            } else {
                factor = 0.60D;
            }

            Vector current = event.getProjectile().getVelocity();
            double speed = Math.max(0.1D, current.length());
            Vector straight = player.getEyeLocation().getDirection().normalize();

            Vector result = current.lengthSquared() > 0.0D
                    ? current.normalize().multiply(1.0D - factor).add(straight.multiply(factor)).normalize().multiply(speed)
                    : straight.multiply(speed);

            event.getProjectile().setVelocity(result);
        }

        if (bow.getType() == Material.BOW) {
            if (manager.getLevel(bow, CustomEnchantType.EXPLOSION) > 0) {
                event.getProjectile().setMetadata(META_EXPLOSION, new FixedMetadataValue(manager.plugin(), true));
            }

            if (manager.getLevel(bow, CustomEnchantType.GRAPPLING) > 0) {
                event.getProjectile().setMetadata(META_GRAPPLING, new FixedMetadataValue(manager.plugin(), true));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();

        if (projectile.hasMetadata(META_EXPLOSION)) {
            projectile.getWorld().createExplosion(projectile.getLocation(), 2.0F, false, true);
            projectile.remove();
            return;
        }

        if (projectile.hasMetadata(META_GRAPPLING)) {
            ProjectileSource source = event.getEntity().getShooter();

            if (source instanceof Player player && event.getHitBlock() != null) {
                Vector pull = projectile.getLocation().toVector().subtract(player.getLocation().toVector());

                if (pull.lengthSquared() > 0.0D) {
                    pull.normalize().multiply(1.65D);
                    pull.setY(Math.min(1.25D, pull.getY() + 0.45D));
                    player.setVelocity(pull);
                }
            }

            projectile.remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata(META_GRAPPLING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBloodlustDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity instanceof Player || entity.getType().name().equals("ARMOR_STAND")) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();

        int level = manager.getLevel(weapon, CustomEnchantType.BLOODLUST);

        if (level <= 0) {
            return;
        }

        if (!CustomEnchantType.isSword(weapon.getType().name())) {
            return;
        }

        bloodlustHits.put(entity.getUniqueId(), new BloodlustHit(player.getUniqueId(), level, System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player || entity.getType().name().equals("ARMOR_STAND")) {
            bloodlustHits.remove(entity.getUniqueId());
            return;
        }

        Player killer = entity.getKiller();

        int orbBooster = 0;
        int bloodlust = 0;

        if (killer != null && killer.isOnline()) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();

            if (weapon != null && weapon.getType() != Material.AIR) {
                orbBooster = manager.getLevel(weapon, CustomEnchantType.ORB_BOOSTER);
                bloodlust = manager.getLevel(weapon, CustomEnchantType.BLOODLUST);
            }
        }

        BloodlustHit hit = bloodlustHits.remove(entity.getUniqueId());

        if ((killer == null || bloodlust <= 0) && hit != null && System.currentTimeMillis() - hit.timestamp <= BLOODLUST_DAMAGE_MEMORY_MS) {
            Player stored = Bukkit.getPlayer(hit.playerUuid);

            if (stored != null && stored.isOnline()) {
                killer = stored;
                bloodlust = hit.level;

                ItemStack weapon = stored.getInventory().getItemInMainHand();

                if (weapon != null && weapon.getType() != Material.AIR) {
                    orbBooster = manager.getLevel(weapon, CustomEnchantType.ORB_BOOSTER);
                }
            }
        }

        if (killer == null || !killer.isOnline()) {
            return;
        }

        if (orbBooster > 0 && event.getDroppedExp() > 0) {
            event.setDroppedExp(applyOrbBoost(event.getDroppedExp(), orbBooster));
        }

        if (bloodlust > 0) {
            applyBloodlust(killer, bloodlust);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMaceDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        if (weapon == null || weapon.getType() == Material.AIR || !weapon.getType().name().equals("MACE")) {
            return;
        }

        EntityEquipment equipment = victim.getEquipment();

        if (equipment == null) {
            return;
        }

        int totalLevel = 0;

        for (ItemStack armor : equipment.getArmorContents()) {
            totalLevel += manager.getLevel(armor, CustomEnchantType.BLUNT_PROTECTION);
        }

        if (totalLevel <= 0) {
            return;
        }

        double reduction = Math.min(0.75D, totalLevel * 0.15D);
        event.setDamage(event.getDamage() * (1.0D - reduction));
    }

    private void applyBloodlust(Player player, int level) {
        int seconds = Math.min(15, Math.max(1, level) * 3);
        int duration = seconds * 20;

        PotionEffectType strength = getPotionType("STRENGTH", "INCREASE_DAMAGE");
        PotionEffectType speed = getPotionType("SPEED");

        if (strength != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0), true);
        }

        if (speed != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1), true);
        }
    }

    private PotionEffectType getPotionType(String... names) {
        for (String name : names) {
            try {
                PotionEffectType type = PotionEffectType.getByName(name);

                if (type != null) {
                    return type;
                }
            } catch (Throwable ignored) {
            }

            try {
                Field field = PotionEffectType.class.getField(name);
                Object value = field.get(null);

                if (value instanceof PotionEffectType) {
                    return (PotionEffectType) value;
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private int applyOrbBoost(int base, int level) {
        double multiplier = 1.0D + (level * 0.50D);
        return Math.max(base, (int) Math.round(base * multiplier));
    }

    private List<Block> singleBlock(Block block) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(block);
        return blocks;
    }

    private List<Block> getExcavatorBlocks(Player player, Block center) {
        List<Block> blocks = new ArrayList<>();
        Vector direction = player.getEyeLocation().getDirection();

        double ax = Math.abs(direction.getX());
        double ay = Math.abs(direction.getY());
        double az = Math.abs(direction.getZ());

        if (ay > ax && ay > az) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    blocks.add(center.getRelative(x, 0, z));
                }
            }
        } else if (ax > az) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    blocks.add(center.getRelative(0, y, z));
                }
            }
        } else {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    blocks.add(center.getRelative(x, y, 0));
                }
            }
        }

        return blocks;
    }

    private boolean canBreak(Player player, ItemStack tool, Block block) {
        if (block == null || block.getType() == Material.AIR || block.getType() == Material.BEDROCK) {
            return false;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        Collection<ItemStack> drops = block.getDrops(tool);
        return !drops.isEmpty();
    }

    private void breakBlock(Player player, ItemStack tool, Block block, boolean smelt) {
        Collection<ItemStack> drops = block.getDrops(tool);

        for (ItemStack drop : drops) {
            ItemStack finalDrop = smelt ? smeltDrop(drop, block.getType()) : drop;

            if (finalDrop != null && finalDrop.getType() != Material.AIR && finalDrop.getAmount() > 0) {
                block.getWorld().dropItemNaturally(block.getLocation(), finalDrop);
            }
        }

        block.setType(Material.AIR);
    }

    private ItemStack smeltDrop(ItemStack drop, Material originalBlock) {
        if (drop == null || drop.getType() == Material.AIR) {
            return drop;
        }

        Material result = null;
        String dropName = drop.getType().name();
        String blockName = originalBlock.name();

        if (dropName.equals("RAW_IRON") || blockName.endsWith("IRON_ORE")) {
            result = Material.IRON_INGOT;
        } else if (dropName.equals("RAW_GOLD") || blockName.endsWith("GOLD_ORE")) {
            result = Material.GOLD_INGOT;
        } else if (dropName.equals("RAW_COPPER") || blockName.endsWith("COPPER_ORE")) {
            result = Material.COPPER_INGOT;
        } else if (blockName.equals("ANCIENT_DEBRIS")) {
            result = Material.matchMaterial("NETHERITE_SCRAP");
        }

        if (result == null) {
            return drop;
        }

        return new ItemStack(result, drop.getAmount());
    }

    private boolean isSmeltableOre(Material material) {
        String name = material.name();

        return name.endsWith("IRON_ORE")
                || name.endsWith("GOLD_ORE")
                || name.endsWith("COPPER_ORE")
                || name.equals("ANCIENT_DEBRIS");
    }

    private boolean canTill(Block block) {
        if (block == null) {
            return false;
        }

        Block above = block.getRelative(0, 1, 0);

        if (above.getType() != Material.AIR) {
            return false;
        }

        String name = block.getType().name();

        return name.equals("DIRT")
                || name.equals("GRASS_BLOCK")
                || name.equals("COARSE_DIRT")
                || name.equals("ROOTED_DIRT")
                || name.equals("DIRT_PATH");
    }

    private Material getFarmlandMaterial() {
        Material farmland = Material.matchMaterial("FARMLAND");

        if (farmland != null) {
            return farmland;
        }

        Material soil = Material.matchMaterial("SOIL");
        return soil != null ? soil : Material.DIRT;
    }

    private boolean isLava(Material material) {
        if (material == null) {
            return false;
        }

        String name = material.name();
        return name.equals("LAVA") || name.equals("STATIONARY_LAVA");
    }

    private int getEstimatedOreExp(Material material, ItemStack tool) {
        if (material == null || hasSilkTouch(tool)) {
            return 0;
        }

        String name = material.name();

        if (name.endsWith("COAL_ORE")) {
            return random.nextInt(3);
        }

        if (name.endsWith("DIAMOND_ORE") || name.endsWith("EMERALD_ORE")) {
            return 3 + random.nextInt(5);
        }

        if (name.endsWith("LAPIS_ORE") || name.endsWith("NETHER_QUARTZ_ORE")) {
            return 2 + random.nextInt(4);
        }

        if (name.endsWith("REDSTONE_ORE")) {
            return 1 + random.nextInt(5);
        }

        return 0;
    }

    private boolean hasSilkTouch(ItemStack item) {
        if (item == null) {
            return false;
        }

        try {
            Enchantment enchantment = Enchantment.getByName("SILK_TOUCH");

            if (enchantment != null && item.containsEnchantment(enchantment)) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        try {
            Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("silk_touch"));

            return enchantment != null && item.containsEnchantment(enchantment);
        } catch (Throwable ignored) {
        }

        return false;
    }

    private void damageItem(Player player, ItemStack item, int amount) {
        if (item == null || item.getType() == Material.AIR || amount <= 0) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (!(meta instanceof Damageable damageable)) {
            return;
        }

        int damage = damageable.getDamage() + amount;
        int max = item.getType().getMaxDurability();

        if (max > 0 && damage >= max) {
            item.setAmount(0);
            return;
        }

        damageable.setDamage(damage);
        item.setItemMeta(meta);
    }

    private static final class BloodlustHit {

        private final UUID playerUuid;
        private final int level;
        private final long timestamp;

        private BloodlustHit(UUID playerUuid, int level, long timestamp) {
            this.playerUuid = playerUuid;
            this.level = level;
            this.timestamp = timestamp;
        }
    }
}