package me.thinksmp.game.customItems;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum CustomItem {
    SWORD("sword", Material.NETHERITE_SWORD, "thinksmp:sword", "§cLifesteal Sword"),
    HAMMER("hammer", Material.MACE, "thinksmp:hammer", "§6War Hammer"),
    TRIDENT("trident", Material.TRIDENT, "thinksmp:trident", "§bFrozen Trident"),
    AXE("axe", Material.NETHERITE_AXE, "thinksmp:axe", "§2Treechopper Axe"),
    PICKAXE("pickaxe", Material.NETHERITE_PICKAXE, "thinksmp:pickaxe", "§9Miner Pickaxe"),
    ABYSSAL_SCYTHE("abyssal_scythe", Material.NETHERITE_HOE, "thinksmp:abyssal_scythe", "§5Abyssal Scythe"),
    BOW("bow", Material.BOW, "thinksmp:bow", "§dRapidfire Bow");

    private final String id;
    private final Material material;
    private final String model;
    private final String displayName;

    CustomItem(String id, Material material, String model, String displayName) {
        this.id = id;
        this.material = material;
        this.model = model;
        this.displayName = displayName;
    }

    public static CustomItem fromId(String id) {
        for (CustomItem item : values()) {
            if (item.id.equalsIgnoreCase(id)) return item;
        }
        return null;
    }
}
