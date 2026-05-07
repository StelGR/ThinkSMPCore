package me.thinksmp.game.customEnchants.listener;

import me.thinksmp.game.customEnchants.CustomEnchantType;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.List;

public final class CustomEnchantTableListener implements Listener {

    private final CustomEnchantManager manager;

    public CustomEnchantTableListener(CustomEnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (!manager.rollEnchantTableChance(event.getExpLevelCost())) {
            return;
        }

        List<CustomEnchantType> eligible = manager.getEligibleTableEnchantments(
                event.getItem(),
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
        manager.apply(event.getItem(), selected, level);
    }
}
