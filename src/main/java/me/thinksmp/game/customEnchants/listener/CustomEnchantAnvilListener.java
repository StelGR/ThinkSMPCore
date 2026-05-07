package me.thinksmp.game.customEnchants.listener;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;

public final class CustomEnchantAnvilListener implements Listener {

    private final CustomEnchantManager manager;

    public CustomEnchantAnvilListener(CustomEnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if (first == null || first.getType() == Material.AIR || second == null || second.getType() == Material.AIR) {
            return;
        }

        Map<CustomEnchantType, Integer> firstCustom = manager.getEnchantments(first);
        Map<CustomEnchantType, Integer> secondCustom = manager.getEnchantments(second);

        boolean firstHasCustom = !firstCustom.isEmpty();
        boolean secondHasCustom = !secondCustom.isEmpty();

        if (!firstHasCustom && !secondHasCustom) {
            return;
        }

        ItemStack vanillaResult = event.getResult();
        ItemStack result;

        if (vanillaResult != null && vanillaResult.getType() != Material.AIR) {
            result = vanillaResult.clone();
        } else if (secondHasCustom) {
            result = first.clone();
        } else {
            return;
        }

        for (CustomEnchantType type : firstCustom.keySet()) {
            if (manager.hasVanillaConflict(result, type, null)) {
                event.setResult(null);
                return;
            }
        }

        for (Map.Entry<CustomEnchantType, Integer> entry : firstCustom.entrySet()) {
            CustomEnchantType type = entry.getKey();
            int level = entry.getValue();

            if (manager.getLevel(result, type) <= 0) {
                manager.forceApply(result, type, level);
            }
        }

        boolean customChanged = false;

        for (Map.Entry<CustomEnchantType, Integer> entry : secondCustom.entrySet()) {
            CustomEnchantType type = entry.getKey();
            int secondLevel = entry.getValue();

            if (!manager.canApply(result, type)) {
                continue;
            }

            int currentLevel = manager.getLevel(result, type);
            int newLevel;

            if (currentLevel == secondLevel && currentLevel < type.maxLevel()) {
                newLevel = currentLevel + 1;
            } else {
                newLevel = Math.max(currentLevel, secondLevel);
            }

            if (newLevel > currentLevel) {
                manager.forceApply(result, type, newLevel);
                customChanged = true;
            }
        }

        if (secondHasCustom && !customChanged) {
            event.setResult(null);
            return;
        }

        manager.updateLore(result);
        event.setResult(result);

        if (customChanged) {
            int currentCost = getRepairCost(event);
            int customCost = Math.max(1, secondCustom.size() * 4);
            setRepairCost(event, Math.max(1, currentCost) + customCost);
        }
    }

    private int getRepairCost(PrepareAnvilEvent event) {
        try {
            Method method = event.getInventory().getClass().getMethod("getRepairCost");
            Object value = method.invoke(event.getInventory());

            if (value instanceof Integer) {
                return (Integer) value;
            }
        } catch (Throwable ignored) {
        }

        return 1;
    }

    private void setRepairCost(PrepareAnvilEvent event, int cost) {
        try {
            Method method = event.getInventory().getClass().getMethod("setRepairCost", int.class);
            method.invoke(event.getInventory(), cost);
        } catch (Throwable ignored) {
        }
    }
}