package me.thinksmp.game.customEnchants.listener;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class CustomEnchantTableListener implements Listener {

    private final CustomEnchantManager manager;

    public CustomEnchantTableListener(CustomEnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        sanitizeForBedrockEnchantTable(event.getItem());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.ENCHANTING) {
            return;
        }

        sanitizeForBedrockEnchantTable(event.getCurrentItem());
        sanitizeForBedrockEnchantTable(event.getCursor());

        Bukkit.getScheduler().runTask(manager.plugin(), () -> {
            sanitizeForBedrockEnchantTable(event.getView().getTopInventory().getItem(0));

            if (event.getWhoClicked() instanceof Player player) {
                try {
                    player.updateInventory();
                } catch (Throwable ignored) {
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.ENCHANTING) {
            return;
        }

        for (ItemStack item : event.getNewItems().values()) {
            sanitizeForBedrockEnchantTable(item);
        }

        Bukkit.getScheduler().runTask(manager.plugin(), () -> {
            sanitizeForBedrockEnchantTable(event.getView().getTopInventory().getItem(0));

            if (event.getWhoClicked() instanceof Player player) {
                try {
                    player.updateInventory();
                } catch (Throwable ignored) {
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        sanitizeForBedrockEnchantTable(item);

        if (!manager.rollEnchantTableChance(event.getExpLevelCost())) {
            return;
        }

        List<CustomEnchantType> eligible = manager.getEligibleTableEnchantments(
                item,
                event.getEnchantsToAdd().keySet()
        );

        if (eligible.isEmpty()) {
            return;
        }

        CustomEnchantType selected = manager.randomWeighted(eligible);

        if (selected == null) {
            return;
        }

        int level = manager.randomLevel(selected, event.getExpLevelCost());
        Player player = event.getEnchanter();

        Bukkit.getScheduler().runTask(manager.plugin(), () -> {
            if (item.getType() == Material.AIR) {
                return;
            }

            sanitizeForBedrockEnchantTable(item);
            manager.forceApply(item, selected, level);
            sanitizeForBedrockEnchantTable(item);
            manager.updateLore(item);

            try {
                player.updateInventory();
            } catch (Throwable ignored) {
            }
        });
    }

    private void sanitizeForBedrockEnchantTable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        boolean changed = false;

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (enchantment == null) {
                continue;
            }

            boolean canEnchant;

            try {
                canEnchant = enchantment.canEnchantItem(item);
            } catch (Throwable ignored) {
                canEnchant = true;
            }

            if (!canEnchant) {
                meta.removeEnchant(enchantment);
                changed = true;
            }
        }

        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            changed = true;
        }

        if (changed) {
            item.setItemMeta(meta);
        }
    }
}