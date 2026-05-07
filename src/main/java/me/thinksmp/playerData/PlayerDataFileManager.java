package me.thinksmp.playerData;

import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

import static me.thinksmp.Core.*;

public class PlayerDataFileManager {

    public void loadDataFromFile(UUID playerUUID) {
        String playerName = PlayerDataFile.get().getString(playerUUID + ".playername");
        PlayerData playerData = getPlayerDataManager().getPlayerData(playerUUID);

        if (playerName == null) {
            GeneralUtility.log("Player name is null for UUID: " + playerUUID);
            return;
        }

        FileConfiguration playerDataFile = PlayerDataFile.get();

        playerData.setName(playerName);
        playerData.setDisplayName(playerDataFile.getString(playerUUID + ".displayname"));
        playerData.setLastOnline(playerDataFile.getString(playerUUID + ".lastOnline"));
        playerData.setFirstJoinDate(playerDataFile.getString(playerUUID + ".firstJoinDate"));
        playerData.setCanBeMessaged(playerDataFile.getBoolean(playerUUID + ".canBeMessaged"));
        playerData.setStaffChat(playerDataFile.getBoolean(playerUUID + ".staffChat"));
        playerData.setVanish(playerDataFile.getBoolean(playerUUID + ".vanish"));
        playerData.setStaffScoreboard(playerDataFile.getBoolean(playerUUID + ".staffScoreboard"));
        playerData.setCanBePinged(playerDataFile.getBoolean(playerUUID + ".canBePinged"));
        playerData.setHasPointSound(playerDataFile.getBoolean(playerUUID + ".hasPointSound"));
        playerData.setHasUsedWildCommand(playerDataFile.getBoolean(playerUUID + ".hasUsedWildCommand"));
        playerData.setPoints(playerDataFile.getInt(playerUUID + ".points"));

        long remainingMillis = playerDataFile.getLong(playerUUID + ".pvpProtectionRemainingMillis", -1L);

        if (remainingMillis < 0L) {
            long oldEndAt = playerDataFile.getLong(playerUUID + ".pvp-protection-end", 0L);

            if (oldEndAt > 0L) {
                remainingMillis = Math.max(0L, oldEndAt - System.currentTimeMillis());
            } else {
                remainingMillis = 0L;
            }
        }

        boolean pvpProtected = playerDataFile.getBoolean(playerUUID + ".pvpProtected", remainingMillis > 0L);

        playerData.setPvpProtectionRemainingMillis(Math.max(0L, remainingMillis));
        playerData.setPvpProtected(pvpProtected && remainingMillis > 0L);

        loadHomes(playerData, playerUUID, playerDataFile);
    }

    public void saveAllPlayerDataToFile() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(getPlayerDataManager().getPlayerData(player));
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            try {
                Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());

                if (player == null) {
                    continue;
                }

                savePlayerData(getPlayerDataManager().getPlayerData(player));
            } catch (Exception ignored) {
            }
        }
    }

    public void savePlayerData(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        UUID playerUUID = playerData.getPlayerUUID();

        PlayerDataFile.get().set(playerUUID + ".playername", playerData.getName());
        PlayerDataFile.get().set(playerUUID + ".displayname", playerData.getDisplayName());
        PlayerDataFile.get().set(playerUUID + ".lastOnline", playerData.getLastOnline());
        PlayerDataFile.get().set(playerUUID + ".firstJoinDate", playerData.getFirstJoinDate());
        PlayerDataFile.get().set(playerUUID + ".canBeMessaged", playerData.isCanBeMessaged());
        PlayerDataFile.get().set(playerUUID + ".staffChat", playerData.isStaffChat());
        PlayerDataFile.get().set(playerUUID + ".vanish", playerData.isVanish());
        PlayerDataFile.get().set(playerUUID + ".staffScoreboard", playerData.isStaffScoreboard());
        PlayerDataFile.get().set(playerUUID + ".canBePinged", playerData.isCanBePinged());
        PlayerDataFile.get().set(playerUUID + ".hasPointSound", playerData.isHasPointSound());
        PlayerDataFile.get().set(playerUUID + ".points", playerData.getPoints());
        PlayerDataFile.get().set(playerUUID + ".hasUsedWildCommand", playerData.isHasUsedWildCommand());

        PlayerDataFile.get().set(playerUUID + ".pvpProtected", playerData.hasPvpProtection());
        PlayerDataFile.get().set(playerUUID + ".pvpProtectionRemainingMillis", playerData.getPvpProtectionRemainingMillis());
        PlayerDataFile.get().set(playerUUID + ".pvp-protection-end", null);

        saveHomes(playerData, playerUUID);

        PlayerDataFile.save();
    }

    private void loadHomes(PlayerData playerData, UUID playerUUID, FileConfiguration file) {
        playerData.getHomes().clear();

        ConfigurationSection section = file.getConfigurationSection(playerUUID + ".homes");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            int id;

            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                continue;
            }

            if (id < 1 || id > 5) {
                continue;
            }

            String path = playerUUID + ".homes." + id;
            String world = file.getString(path + ".world");

            if (world == null || world.isEmpty()) {
                continue;
            }

            double x = file.getDouble(path + ".x");
            double y = file.getDouble(path + ".y");
            double z = file.getDouble(path + ".z");
            float yaw = (float) file.getDouble(path + ".yaw");
            float pitch = (float) file.getDouble(path + ".pitch");

            playerData.getHomes().put(id, new PlayerData.HomeData(world, x, y, z, yaw, pitch));
        }
    }

    private void saveHomes(PlayerData playerData, UUID playerUUID) {
        PlayerDataFile.get().set(playerUUID + ".homes", null);

        for (Map.Entry<Integer, PlayerData.HomeData> entry : playerData.getHomes().entrySet()) {
            int id = entry.getKey();
            PlayerData.HomeData home = entry.getValue();

            if (id < 1 || id > 5 || home == null || home.getWorld() == null) {
                continue;
            }

            String path = playerUUID + ".homes." + id;

            PlayerDataFile.get().set(path + ".world", home.getWorld());
            PlayerDataFile.get().set(path + ".x", home.getX());
            PlayerDataFile.get().set(path + ".y", home.getY());
            PlayerDataFile.get().set(path + ".z", home.getZ());
            PlayerDataFile.get().set(path + ".yaw", home.getYaw());
            PlayerDataFile.get().set(path + ".pitch", home.getPitch());
        }
    }
}