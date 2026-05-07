package me.thinksmp.game.customItems;


import me.thinksmp.Core;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemManager {

    private final NamespacedKey ITEM_KEY = new NamespacedKey(Core.getPlugin(), "think_item");

    public ItemStack createItem(CustomItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();

        if (meta == null) return stack;

        meta.setDisplayName(item.getDisplayName());
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, item.getId());

        String[] split = item.getModel().split(":");
        if (split.length == 2) {
            meta.setItemModel(new NamespacedKey(split[0], split[1]));
        }

        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE
        );

        stack.setItemMeta(meta);
        applyEnchantments(stack, item);
        return stack;
    }

    public void giveItem(org.bukkit.entity.Player player, CustomItem item) {
        player.getInventory().addItem(createItem(item));
    }

    public void giveAll(org.bukkit.entity.Player player) {
        for (CustomItem item : CustomItem.values()) {
            giveItem(player, item);
        }
    }

    public CustomItem getType(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;

        String id = meta.getPersistentDataContainer().get(ITEM_KEY, PersistentDataType.STRING);
        if (id == null) return null;

        return CustomItem.fromId(id);
    }

    public boolean isCustomItem(ItemStack stack) {
        return getType(stack) != null;
    }

    private void applyEnchantments(ItemStack stack, CustomItem item) {
        switch (item) {
            case SWORD -> {
                stack.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                stack.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 3);
                stack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
            }
            case HAMMER -> {
                stack.addUnsafeEnchantment(Enchantment.DENSITY, 5);
                stack.addUnsafeEnchantment(Enchantment.WIND_BURST, 4);
                stack.addUnsafeEnchantment(Enchantment.BREACH, 5);
            }
            case TRIDENT -> {
                stack.addUnsafeEnchantment(Enchantment.IMPALING, 5);
                stack.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
                stack.addUnsafeEnchantment(Enchantment.LOYALTY, 4);
            }
            case AXE -> {
                stack.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, 6);
                stack.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
            }
            case PICKAXE -> {
                stack.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
                stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, 6);
            }
            case ABYSSAL_SCYTHE -> {
                stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
                stack.addUnsafeEnchantment(Enchantment.SHARPNESS, 10);
                stack.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
            }
            case BOW -> {
                stack.addUnsafeEnchantment(Enchantment.POWER, 5);
                stack.addUnsafeEnchantment(Enchantment.PUNCH, 3);
                stack.addUnsafeEnchantment(Enchantment.INFINITY, 1);
                stack.addUnsafeEnchantment(Enchantment.FLAME, 1);
            }
        }
    }
}