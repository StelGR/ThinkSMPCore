package me.thinksmp.listeners;

import me.thinksmp.Core;
import me.thinksmp.managers.GuiManager;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
            GeneralUtility.translate("Viewing the information of"), this::handleInfoGUI,
            GuiManager.HOME_GUI_TITLE, this::handleHomeGUI
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

    private void handleHomeGUI(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        int homeId = getHomeIdFromSlot(e.getRawSlot());

        if (homeId == -1) {
            return;
        }

        ItemStack item = e.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (item.getType() == Material.BARRIER) {
            player.sendMessage(GeneralUtility.translate("&cYou don't have any more homes."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
            return;
        }

        if (item.getType() == Material.RED_BED) {
            player.sendMessage(GeneralUtility.translate("&cYou do not have home " + homeId + " set."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F);
            return;
        }

        if (item.getType() != Material.LIME_BED) {
            return;
        }

        player.closeInventory();
        Bukkit.dispatchCommand(player, "home " + homeId);
    }

    private int getHomeIdFromSlot(int slot) {
        return switch (slot) {
            case 11 -> 1;
            case 12 -> 2;
            case 13 -> 3;
            case 14 -> 4;
            case 15 -> 5;
            default -> -1;
        };
    }
}