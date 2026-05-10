package me.thinksmp.managers;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import me.thinksmp.utility.UiUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class GuiManager {

    public static final String HOME_GUI_TITLE = GeneralUtility.translate("&7Your Homes");

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

        UiUtility.fillWithSpacers(gui);
        player1.openInventory(gui);
    }

    public static void openHomeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, HOME_GUI_TITLE);
        PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

        if (playerData == null) {
            player.sendMessage(GeneralUtility.translate("&cYour player data is not loaded yet."));
            return;
        }

        ItemStack glass = UiUtility.generateItem(
                new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1),
                GeneralUtility.translate("&7"),
                Collections.emptyList(),
                true
        );

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, glass);
        }

        int maxHomes = getMaxHomes(player);
        int[] slots = {11, 12, 13, 14, 15};

        for (int i = 1; i <= 5; i++) {
            int slot = slots[i - 1];

            if (i > maxHomes) {
                gui.setItem(slot, createLockedHomeItem(i));
                continue;
            }

            PlayerData.HomeData homeData = playerData.getHome(i);

            if (homeData == null) {
                gui.setItem(slot, createUnsetHomeItem(i));
                continue;
            }

            Location location = homeData.toLocation();

            if (location == null || location.getWorld() == null) {
                gui.setItem(slot, createBrokenHomeItem(i));
                continue;
            }

            gui.setItem(slot, createSetHomeItem(i, location));
        }

        player.openInventory(gui);
    }

    private static ItemStack createSetHomeItem(int homeId, Location location) {
        World world = location.getWorld();
        String dimension = getDimensionName(world);

        return UiUtility.generateItem(
                new ItemStack(Material.LIME_BED, 1),
                GeneralUtility.translate("&aHome " + homeId),
                Arrays.asList(
                        GeneralUtility.translate("&7Status: &aSet"),
                        GeneralUtility.translate("&7Coordinates: &e" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()),
                        GeneralUtility.translate("&7Dimension: &e" + dimension),
                        GeneralUtility.translate("&7World: &e" + world.getName()),
                        "",
                        GeneralUtility.translate("&eClick to teleport.")
                ),
                true
        );
    }

    private static ItemStack createUnsetHomeItem(int homeId) {
        return UiUtility.generateItem(
                new ItemStack(Material.RED_BED, 1),
                GeneralUtility.translate("&cHome " + homeId),
                Arrays.asList(
                        GeneralUtility.translate("&7Status: &cNot Set"),
                        "",
                        GeneralUtility.translate("&7Use &e/sethome " + homeId + " &7to set this home.")
                ),
                true
        );
    }

    private static ItemStack createBrokenHomeItem(int homeId) {
        return UiUtility.generateItem(
                new ItemStack(Material.RED_BED, 1),
                GeneralUtility.translate("&cHome " + homeId),
                Arrays.asList(
                        GeneralUtility.translate("&7Status: &cInvalid"),
                        GeneralUtility.translate("&7The world for this home no longer exists.")
                ),
                true
        );
    }

    private static ItemStack createLockedHomeItem(int homeId) {
        String required = homeId == 2 ? "Media" : "VIP";

        return UiUtility.generateItem(
                new ItemStack(Material.BARRIER, 1),
                GeneralUtility.translate("&cHome " + homeId + " Locked"),
                Arrays.asList(
                        GeneralUtility.translate("&7You don't have any more homes."),
                        GeneralUtility.translate("&7Required Rank: &e" + required)
                ),
                true
        );
    }

    private static int getMaxHomes(Player player) {
        if (player.hasPermission(Permissions.VIP.getPermission())) {
            return 5;
        }

        if (player.hasPermission(Permissions.MEDIA.getPermission())) {
            return 2;
        }

        return 1;
    }

    private static String getDimensionName(World world) {
        if (world == null) {
            return "Unknown";
        }

        if (world.getEnvironment() == World.Environment.NETHER) {
            return "Nether";
        }

        if (world.getEnvironment() == World.Environment.THE_END) {
            return "The End";
        }

        return "Overworld";
    }
}