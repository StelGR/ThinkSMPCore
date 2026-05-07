package me.thinksmp.game.customEnchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CustomEnchantType {

    ORB_BOOSTER("orb_booster", "Orb Booster", 5, 18),
    FIRE_WALKER("fire_walker", "Fire Walker", 1, 9),
    STRAIGHT_SHOT("straight_shot", "Straight Shot", 3, 12),
    EXCAVATOR("excavator", "Excavator", 1, 10),
    SMELTER("smelter", "Smelter", 1, 12),
    EXPLOSION("explosion", "Explosion", 1, 7),
    GRAPPLING("grappling", "Grappling", 1, 7),
    BLOODLUST("bloodlust", "Bloodlust", 5, 12),
    BLUNT_PROTECTION("blunt_protection", "Blunt Protection", 5, 15);

    private final String id;
    private final String display;
    private final int maxLevel;
    private final int tableWeight;

    CustomEnchantType(String id, String display, int maxLevel, int tableWeight) {
        this.id = id;
        this.display = display;
        this.maxLevel = maxLevel;
        this.tableWeight = tableWeight;
    }

    public String id() {
        return id;
    }

    public String display() {
        return display;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int tableWeight() {
        return tableWeight;
    }

    public boolean appliesTo(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        String name = item.getType().name();

        switch (this) {
            case ORB_BOOSTER:
                return isPickaxe(name) || isSword(name);
            case FIRE_WALKER:
                return isBoots(name);
            case STRAIGHT_SHOT:
                return name.equals("BOW") || name.equals("CROSSBOW");
            case EXCAVATOR:
                return isPickaxe(name) || isShovel(name) || isHoe(name);
            case SMELTER:
                return isPickaxe(name);
            case EXPLOSION:
            case GRAPPLING:
                return name.equals("BOW");
            case BLOODLUST:
                return isSword(name);
            case BLUNT_PROTECTION:
                return isArmor(name);
            default:
                return false;
        }
    }

    public boolean conflictsWith(CustomEnchantType other) {
        if (other == null || other == this) {
            return false;
        }

        return (this == EXPLOSION && other == GRAPPLING)
                || (this == GRAPPLING && other == EXPLOSION);
    }

    public static boolean isPickaxe(String name) {
        return name.endsWith("_PICKAXE");
    }

    public static boolean isShovel(String name) {
        return name.endsWith("_SHOVEL") || name.endsWith("_SPADE");
    }

    public static boolean isHoe(String name) {
        return name.endsWith("_HOE");
    }

    public static boolean isSword(String name) {
        return name.endsWith("_SWORD");
    }

    public static boolean isBoots(String name) {
        return name.endsWith("_BOOTS");
    }

    public static boolean isArmor(String name) {
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }
}
