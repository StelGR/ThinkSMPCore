package me.thinksmp.game.customEnchants.listener;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CustomEnchantTradeListener implements Listener {

    private final CustomEnchantManager manager;

    public CustomEnchantTradeListener(CustomEnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (villager.getProfession() != Villager.Profession.LIBRARIAN) {
            return;
        }

        if (!manager.rollLibrarianChance()) {
            return;
        }

        List<CustomEnchantType> pool = new ArrayList<>(Arrays.asList(CustomEnchantType.values()));
        CustomEnchantType type = manager.randomWeighted(pool);

        if (type == null) {
            return;
        }

        int level = manager.randomLevel(type, 30);
        ItemStack result = manager.createBook(type, level);

        MerchantRecipe recipe = new MerchantRecipe(result, 6);
        recipe.addIngredient(new ItemStack(Material.EMERALD, manager.getBookPrice(type, level)));
        recipe.addIngredient(new ItemStack(Material.BOOK, 1));

        configureRecipe(recipe, manager.getVillagerExperience(type, level));

        event.setRecipe(recipe);
    }

    private void configureRecipe(MerchantRecipe recipe, int villagerExperience) {
        try {
            recipe.setVillagerExperience(villagerExperience);
        } catch (Throwable ignored) {
            invoke(recipe, "setVillagerExperience", int.class, villagerExperience);
        }

        try {
            recipe.setExperienceReward(true);
        } catch (Throwable ignored) {
            invoke(recipe, "setExperienceReward", boolean.class, true);
        }

        try {
            recipe.setPriceMultiplier(0.05F);
        } catch (Throwable ignored) {
            invoke(recipe, "setPriceMultiplier", float.class, 0.05F);
        }

        try {
            recipe.setDemand(0);
        } catch (Throwable ignored) {
            invoke(recipe, "setDemand", int.class, 0);
        }
    }

    private void invoke(Object object, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = object.getClass().getMethod(methodName, parameterType);
            method.invoke(object, value);
        } catch (Throwable ignored) {
        }
    }
}