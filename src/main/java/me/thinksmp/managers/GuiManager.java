package me.thinksmp.managers;

import me.thinksmp.Core;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import me.thinksmp.utility.UiUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class GuiManager {

    public static void openPlayerInfo(Player player, Player player1) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.GRAY + "Viewing the information of " + ChatColor.RED + player1.getName());
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player1);

        if (playerData != null) {
            gui.setItem(13, UiUtility.generateSkull(new ItemStack(Material.SKELETON_SKULL, 1), GeneralUtility.translate("&e" + playerData.getDisplayName() + "&7's information."),
                    Arrays.asList(GeneralUtility.translate("&7Player Name: &e" + playerData.getName()),
                            GeneralUtility.translate("&7Last Online: &e" + playerData.getLastOnline()),
                            GeneralUtility.translate("&7First Join: &e" + playerData.getFirstJoinDate()),
                            GeneralUtility.translate("&7UUID: &e" + playerData.getPlayerUUID()))));
        }
        gui.setItem(11, UiUtility.generateItem(new ItemStack(Material.EMERALD, 1), GeneralUtility.translate("&ePoints"),
                Arrays.asList(
                        GeneralUtility.translate("&7Points: &e" + playerData.getPoints()),
                        GeneralUtility.translate("&7Leaderboard Position: &e" + Core.getLeaderboardManager().getPosition(playerData.getPlayerUUID()))
                ), true));
        gui.setItem(15, UiUtility.generateItem(new ItemStack(Material.REDSTONE_TORCH, 1), GeneralUtility.translate("&eSettings"),
                Arrays.asList(
                        GeneralUtility.translate("&7Can be Messaged: &e" + playerData.isCanBeMessaged()),
                        GeneralUtility.translate("&7Can be Pinged: &e" + playerData.isCanBePinged()),
                        GeneralUtility.translate("&7Has Point Sounds: &e" + playerData.isHasPointSound())
                ), true));
        UiUtility.fillWithSpacers(gui);

        player.openInventory(gui);
    }

    public static void openSettingsGUI(Player player1) {
        Inventory gui = Bukkit.createInventory(player1, 27, ChatColor.GRAY + "Viewing Your Settings");
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player1);
        if (playerData.isCanBePinged()) {
            gui.setItem(11, UiUtility.generateItem(new ItemStack(Material.JUKEBOX, 1), GeneralUtility.translate("&aChat Alerts"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Get notified when someone types your name in chat.")),
                    true));
        } else {
            gui.setItem(11, UiUtility.generateItem(new ItemStack(Material.JUKEBOX, 1), GeneralUtility.translate("&cChat Alerts"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Get notified when someone types your name in chat.")),
                    true));
        }
        if (playerData.isCanBeMessaged()) {
            gui.setItem(13, UiUtility.generateItem(new ItemStack(Material.OAK_SIGN, 1), GeneralUtility.translate("&aPrivate Messages"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Allows players to send you private messages.")),
                    true));
        } else {
            gui.setItem(13, UiUtility.generateItem(new ItemStack(Material.OAK_SIGN, 1), GeneralUtility.translate("&cPrivate Messages"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Allows players to send you private messages.")),
                    true));
        }

        if (playerData.isHasPointSound()) {
            gui.setItem(15, UiUtility.generateItem(new ItemStack(Material.BELL, 1), GeneralUtility.translate("&aPoint Sounds"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Hear a sound when you gain points.")),
                    true));
        } else {
            gui.setItem(15, UiUtility.generateItem(new ItemStack(Material.BELL, 1), GeneralUtility.translate("&cPoint Sounds"),
                    Collections.singletonList(
                            GeneralUtility.translate("&7Hear a sound when you gain points.")),
                    true));
        }
//        if (playerData.isRemindForPhoto()) {
//            gui.setItem(15, UiUtility.generateItem(new ItemStack(Material.PAINTING, 1, (short) 2), GeneralUtility.translate("&aPhoto Reminders"),
//                    Collections.singletonList(
//                            GeneralUtility.translate("&7Reminds you to take a photo of your country.")),
//                    true));
//        } else {
//            gui.setItem(15, UiUtility.generateItem(new ItemStack(Material.PAINTING, 1, (short) 2), GeneralUtility.translate("&cPhoto Reminders"),
//                    Collections.singletonList(
//                            GeneralUtility.translate("&7Reminds you to take a photo of your country.")),
//                    true));
//        }
        UiUtility.fillWithSpacers(gui);
        player1.openInventory(gui);
    }

}