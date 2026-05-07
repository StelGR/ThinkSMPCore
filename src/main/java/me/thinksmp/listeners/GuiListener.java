package me.thinksmp.listeners;

import me.thinksmp.Core;
import me.thinksmp.managers.GuiManager;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

public class GuiListener implements Listener {
    private final Map<String, Consumer<InventoryClickEvent>> guiHandlers = Map.of(
            GeneralUtility.translate("&7Viewing Your Settings"), this::handleSettingsGUI,
            GeneralUtility.translate("Viewing the information of"), this::handleInfoGUI
    );

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        guiHandlers.forEach((key, handler) -> {
            if (title.contains(key)) {
                handler.accept(e);
            }
        });
    }

    private void handleSettingsGUI(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        PlayerData data = Core.playerDataManager.getPlayerData(p);
        String name = item.getItemMeta().getDisplayName();

        Map<String, Runnable> actions = Map.of(
                "Chat Alerts", () -> data.setCanBePinged(!data.isCanBePinged()),
                "Private Messages", () -> data.setCanBeMessaged(!data.isCanBeMessaged()),
                "Point Sounds", () -> data.setHasPointSound(!data.isHasPointSound())
        );

        actions.forEach((key, action) -> {
            if (name.contains(key)) {
                action.run();
                GuiManager.openSettingsGUI(p);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
            }
        });
    }

    private void handleInfoGUI(InventoryClickEvent e) {
        e.setCancelled(true);
    }
}
