package me.thinksmp.game.customEnchants.manager;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class CustomEnchantManager {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public CustomEnchantManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public NamespacedKey key(CustomEnchantType type) {
        return new NamespacedKey(plugin, "custom_enchant_" + type.id());
    }

    public int getLevel(ItemStack item, CustomEnchantType type) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return 0;
        }

        Integer level = meta.getPersistentDataContainer().get(key(type), PersistentDataType.INTEGER);
        return level == null ? 0 : Math.max(0, level);
    }

    public boolean has(ItemStack item, CustomEnchantType type) {
        return getLevel(item, type) > 0;
    }

    public Map<CustomEnchantType, Integer> getEnchantments(ItemStack item) {
        Map<CustomEnchantType, Integer> map = new EnumMap<>(CustomEnchantType.class);

        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return map;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return map;
        }

        for (CustomEnchantType type : CustomEnchantType.values()) {
            Integer level = meta.getPersistentDataContainer().get(key(type), PersistentDataType.INTEGER);

            if (level != null && level > 0) {
                map.put(type, Math.min(level, type.maxLevel()));
            }
        }

        return map;
    }

    public boolean canApply(ItemStack item, CustomEnchantType type) {
        return canApply(item, type, null);
    }

    public boolean canApply(ItemStack item, CustomEnchantType type, Collection<Enchantment> vanillaToAdd) {
        if (item == null || item.getType() == Material.AIR || type == null) {
            return false;
        }

        if (item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK) {
            return true;
        }

        if (!type.appliesTo(item)) {
            return false;
        }

        Map<CustomEnchantType, Integer> existing = getEnchantments(item);

        for (CustomEnchantType other : existing.keySet()) {
            if (type.conflictsWith(other) || other.conflictsWith(type)) {
                return false;
            }
        }

        return !hasVanillaConflict(item, type, vanillaToAdd);
    }

    public boolean apply(ItemStack item, CustomEnchantType type, int level) {
        if (!canApply(item, type)) {
            return false;
        }

        return forceApply(item, type, level);
    }

    public boolean forceApply(ItemStack item, CustomEnchantType type, int level) {
        if (item == null || item.getType() == Material.AIR || type == null) {
            return false;
        }

        level = Math.max(1, Math.min(type.maxLevel(), level));

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        meta.getPersistentDataContainer().set(key(type), PersistentDataType.INTEGER, level);
        setGlint(meta, item, true);
        item.setItemMeta(meta);
        updateLore(item);
        return true;
    }

    public void updateLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        List<String> oldLore = meta.hasLore() && meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        List<String> cleanLore = new ArrayList<>();

        for (String line : oldLore) {
            String stripped = ChatColor.stripColor(line);
            boolean customLine = false;

            for (CustomEnchantType type : CustomEnchantType.values()) {
                if (stripped != null && stripped.startsWith(type.display() + " ")) {
                    customLine = true;
                    break;
                }
            }

            if (!customLine) {
                cleanLore.add(line);
            }
        }

        List<String> newLore = new ArrayList<>();
        Map<CustomEnchantType, Integer> enchantments = getEnchantments(item);

        for (CustomEnchantType type : CustomEnchantType.values()) {
            int level = enchantments.getOrDefault(type, 0);

            if (level > 0) {
                newLore.add(ChatColor.GRAY + type.display() + " " + toRoman(level));
            }
        }

        if (!newLore.isEmpty() && !cleanLore.isEmpty()) {
            newLore.add("");
        }

        newLore.addAll(cleanLore);
        meta.setLore(newLore.isEmpty() ? null : newLore);
        setGlint(meta, item, !enchantments.isEmpty());
        item.setItemMeta(meta);
    }

    public ItemStack createBook(CustomEnchantType type, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        forceApply(book, type, level);

        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            clearDisplayName(meta);
            setGlint(meta, book, true);
            book.setItemMeta(meta);
            updateLore(book);
        }

        return book;
    }

    public List<CustomEnchantType> getEligibleTableEnchantments(ItemStack item, Collection<Enchantment> vanillaToAdd) {
        List<CustomEnchantType> list = new ArrayList<>();

        for (CustomEnchantType type : CustomEnchantType.values()) {
            if (canApply(item, type, vanillaToAdd)) {
                list.add(type);
            }
        }

        return list;
    }

    public CustomEnchantType randomWeighted(List<CustomEnchantType> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }

        int total = 0;

        for (CustomEnchantType type : types) {
            total += Math.max(1, type.tableWeight());
        }

        int value = random.nextInt(total);

        for (CustomEnchantType type : types) {
            value -= Math.max(1, type.tableWeight());

            if (value < 0) {
                return type;
            }
        }

        return types.get(random.nextInt(types.size()));
    }

    public int randomLevel(CustomEnchantType type, int power) {
        if (type.maxLevel() <= 1) {
            return 1;
        }

        int cap;

        if (power >= 30) {
            cap = type.maxLevel();
        } else if (power >= 20) {
            cap = Math.min(type.maxLevel(), 4);
        } else if (power >= 10) {
            cap = Math.min(type.maxLevel(), 3);
        } else {
            cap = Math.min(type.maxLevel(), 2);
        }

        return 1 + random.nextInt(Math.max(1, cap));
    }

    public boolean rollEnchantTableChance(int expLevelCost) {
        double chance = 0.035D + Math.min(0.185D, expLevelCost / 160.0D);
        return random.nextDouble() <= chance;
    }

    public boolean rollLibrarianChance() {
        return random.nextDouble() <= 0.13D;
    }

    public int getBookPrice(CustomEnchantType type, int level) {
        int base = switch (type) {
            case EXPLOSION, GRAPPLING -> 28;
            case EXCAVATOR, FIRE_WALKER, SMELTER -> 24;
            case BLUNT_PROTECTION, BLOODLUST, ORB_BOOSTER -> 14;
            default -> 16;
        };

        return Math.min(64, base + (level * 7));
    }

    public int getVillagerExperience(CustomEnchantType type, int level) {
        int base = switch (type) {
            case EXPLOSION, GRAPPLING -> 12;
            case EXCAVATOR, FIRE_WALKER, SMELTER -> 10;
            case BLUNT_PROTECTION, BLOODLUST, ORB_BOOSTER -> 7;
            default -> 6;
        };

        return Math.max(2, base + (level * 2));
    }

    public String toRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }

    public boolean hasVanillaConflict(ItemStack item, CustomEnchantType type, Collection<Enchantment> vanillaToAdd) {
        List<Enchantment> enchants = new ArrayList<>();

        if (item != null) {
            enchants.addAll(item.getEnchantments().keySet());
        }

        if (vanillaToAdd != null) {
            enchants.addAll(vanillaToAdd);
        }

        if (type == CustomEnchantType.FIRE_WALKER) {
            return containsVanilla(enchants, "FROST_WALKER");
        }

        if (type == CustomEnchantType.STRAIGHT_SHOT) {
            return containsVanilla(enchants, "ARROW_INFINITE", "INFINITY");
        }

        if (type == CustomEnchantType.BLUNT_PROTECTION) {
            return containsVanilla(enchants,
                    "PROTECTION_ENVIRONMENTAL",
                    "PROTECTION",
                    "PROTECTION_FIRE",
                    "FIRE_PROTECTION",
                    "PROTECTION_EXPLOSIONS",
                    "BLAST_PROTECTION",
                    "PROTECTION_PROJECTILE",
                    "PROJECTILE_PROTECTION"
            );
        }

        return false;
    }

    private boolean containsVanilla(Collection<Enchantment> enchants, String... names) {
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }

        for (Enchantment enchantment : enchants) {
            if (enchantment == null) {
                continue;
            }

            String enchantName = enchantment.getName();
            String keyName = "";

            try {
                keyName = enchantment.getKey().getKey();
            } catch (Throwable ignored) {
            }

            for (String name : names) {
                if (enchantName != null && enchantName.equalsIgnoreCase(name)) {
                    return true;
                }

                if (keyName != null && keyName.equalsIgnoreCase(name)) {
                    return true;
                }

                if (keyName != null && keyName.equalsIgnoreCase(name.toLowerCase().replace("_", ""))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void setGlint(ItemMeta meta, ItemStack item, boolean enabled) {
        if (meta == null) {
            return;
        }

        try {
            Method method = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
            method.invoke(meta, enabled ? Boolean.TRUE : null);
            return;
        } catch (Throwable ignored) {
        }

        if (!enabled) {
            return;
        }

        if (!meta.getEnchants().isEmpty()) {
            return;
        }

        Enchantment dummy = getDummyGlintEnchant();

        if (dummy == null) {
            return;
        }

        try {
            meta.addEnchant(dummy, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Throwable ignored) {
        }
    }

    private Enchantment getDummyGlintEnchant() {
        String[] names = {
                "LURE",
                "LUCK",
                "ARROW_INFINITE",
                "DURABILITY",
                "UNBREAKING"
        };

        for (String name : names) {
            try {
                Enchantment enchantment = Enchantment.getByName(name);

                if (enchantment != null) {
                    return enchantment;
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private void clearDisplayName(ItemMeta meta) {
        if (meta == null) {
            return;
        }

        try {
            meta.setDisplayName(null);
        } catch (Throwable ignored) {
            try {
                Method method = meta.getClass().getMethod("displayName", Object.class);
                method.invoke(meta, new Object[]{null});
            } catch (Throwable ignoredToo) {
            }
        }
    }
}