package me.thinksmp.managers;

import lombok.Getter;
import me.thinksmp.Core;
import me.thinksmp.game.customItems.CustomItem;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private final Map<UUID, Integer> pointsMap = new HashMap<>();
    @Getter
    private List<UUID> sortedPlayers = new ArrayList<>();

    private UUID currentFirstPlace = null;

    private final File saveFile;

    public LeaderboardManager() {
        this.saveFile = new File(Core.getPlugin().getDataFolder(), "leaderboard.dat");
        loadFromFile();
        startUpdater();
    }

    private void startUpdater() {
        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            boolean changed = false;

            // Update online players
            for (Player online : Bukkit.getOnlinePlayers()) {
                PlayerData data = Core.getPlayerDataManager().getPlayerData(online);
                int currentPoints = data.getPoints();
                if (!pointsMap.containsKey(online.getUniqueId()) || pointsMap.get(online.getUniqueId()) != currentPoints) {
                    pointsMap.put(online.getUniqueId(), currentPoints);
                    changed = true;
                }

                if (currentPoints >= 50000000 && !Core.getRecipes().isCustomItemCrafted(CustomItem.ABYSSAL_SCYTHE)) {
                    if (online.getInventory().firstEmpty() == -1) {
                        online.sendMessage("§cYour inventory is full. Clear one slot to receive the Abyssal Scythe.");
                        return;
                    }

                    Core.getRecipes().markCustomItemCrafted(CustomItem.ABYSSAL_SCYTHE, online);
                    Core.getItemManager().giveItem(online, CustomItem.ABYSSAL_SCYTHE);

                    online.sendMessage("§aCongratulations, you are the first and last person to receive the Abyssal Scythe");
                }
            }

            if (changed) {
                sortLeaderboard();
                checkFirstPlaceChange();
                saveToFile();
            }

        }, 0L, 100L); // 5 seconds
    }

    private void sortLeaderboard() {
        sortedPlayers = pointsMap.entrySet()
                .stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<UUID, Integer> e) -> e.getValue())
                        .reversed()
                        .thenComparing(e -> e.getKey().toString()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void checkFirstPlaceChange() {
        if (sortedPlayers.isEmpty()) {
            currentFirstPlace = null;
            return;
        }

        UUID newFirstPlace = sortedPlayers.get(0);

        if (currentFirstPlace == null) {
            currentFirstPlace = newFirstPlace;
            return;
        }

        if (Objects.equals(currentFirstPlace, newFirstPlace)) {
            return;
        }

        int oldPoints = pointsMap.getOrDefault(currentFirstPlace, 0);
        int newPoints = pointsMap.getOrDefault(newFirstPlace, 0);
        int diff = newPoints - oldPoints;

        if (diff >= 500) {
            String oldName = getName(currentFirstPlace);
            String newName = getName(newFirstPlace);

            Bukkit.broadcastMessage("§8§m                                          ");
            Bukkit.broadcastMessage("§e" + newName + " §ahas taken the #1 spot from §c" + oldName + " §aby §e" + GeneralUtility.formatNumberWithDots(diff) + " points!");
            Bukkit.broadcastMessage("§8§m                                          ");
        }

        currentFirstPlace = newFirstPlace;
    }

    public int getPosition(UUID uuid) {
        return sortedPlayers.indexOf(uuid) + 1;
    }

    public List<String> getTop(int count) {
        return sortedPlayers.stream()
                .limit(count)
                .map(uuid -> {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    int points = pointsMap.getOrDefault(uuid, 0);
                    return "&7"+name + " &f- &c" + GeneralUtility.formatNumberWithDots(points) + " &fpoints";
                })
                .collect(Collectors.toList());
    }

    public List<String> getTopMotd(int count) {
        return sortedPlayers.stream()
                .limit(count)
                .map(uuid -> {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    int points = pointsMap.getOrDefault(uuid, 0);
                    return "&7"+name + " &f- &c" + GeneralUtility.formatNumber(points) + " &fpoints";
                })
                .collect(Collectors.toList());
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(pointsMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        if (!saveFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            Map<UUID, Integer> loaded = (Map<UUID, Integer>) ois.readObject();
            pointsMap.clear();
            pointsMap.putAll(loaded);
            sortLeaderboard();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getName(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }
}
