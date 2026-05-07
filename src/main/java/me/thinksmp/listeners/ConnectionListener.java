package me.thinksmp.listeners;

import fr.mrmicky.fastboard.FastBoard;
import me.thinksmp.Core;
import me.thinksmp.functions.Actionbar;
import me.thinksmp.functions.Permissions;
import me.thinksmp.managers.VanishManager;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.playerData.PlayerDataFile;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.*;

import static me.thinksmp.Core.*;
import static me.thinksmp.utility.GeneralUtility.*;



public class ConnectionListener implements Listener {
    private final Map<UUID, BukkitTask> delayedTasks = new HashMap<>();
    private final Map<UUID, BukkitRunnable> playerTimers = new HashMap<>();


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin (PlayerJoinEvent event){
        Player player = event.getPlayer();
        // Set display name with prefix
        player.setDisplayName(translate(getGroupManagerFunction().getPrefix(player) + player.getName() + getGroupManagerFunction().getSuffix(player)));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (getRamManager().getVanishedPlayers().contains(onlinePlayer)) {
                if (!player.hasPermission(Permissions.VANISH_SEE.getPermission())) {
                    player.hidePlayer(getPlugin(), onlinePlayer);
                }
            }
        }

        if (getPlayerDataManager().getPlayerData(player).isVanish() && player.hasPermission(Permissions.VANISH.getPermission())) {
            VanishManager.vanish(player);
            event.setJoinMessage(null);
        } else {
            VanishManager.unVanish(player);
            event.setJoinMessage(translate(player.getDisplayName() + "&8 has joined the server."));

        }

        PlayerData playerData;
        // Load or initialize first join date asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (PlayerDataFile.get().getString(player.getUniqueId() + ".playername") == null) {
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    player.setGameMode(GameMode.SURVIVAL);
                    Bukkit.broadcastMessage(translate("&7Welcome " + player.getDisplayName() + " &7to the server!"));
                    player.sendMessage(translate("&aWelcome! Check out our discord server: &ehttps://discord.gg/ThywRjFXPh"));

                    Core.getPlugin().getPluginTimers().startPVPProtection(player);
                    player.sendMessage(translate("&aYou have 15 minutes of new-player PvP protection."));
                });
            }
        });
        playerData = Core.getPlayerDataManager().getPlayerData(player);

        // Initialize first join date
        playerData.setFirstJoinDate((playerData.getFirstJoinDate() == null) ? getFirstJoinedFormat() : playerData.getFirstJoinDate());
        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerData data = Core.getPlayerDataManager().getPlayerData(onlinePlayer);

                if (data == null) {
                    continue;
                }

                UUID uuid = onlinePlayer.getUniqueId();

                int position = Core.getLeaderboardManager().getPosition(uuid);

                String positionColor =
                        position == 1 ? "&6" :
                                position == 2 ? "&3" :
                                        position == 3 ? "&c" : "&7";

                String teamTag = Core.getPlugin()
                        .getUltimateTeamsAPI()
                        .findTeamByMember(uuid)
                        .map(team -> " &8[&f" + team.getName() + "&8]")
                        .orElse("");

                String tabName = positionColor + position + "# &f- &c"
                        + GeneralUtility.formatNumber(data.getPoints()) + " "
                        + Core.getGroupManagerFunction().getPrefix(onlinePlayer)
                        + onlinePlayer.getName()
                        + Core.getGroupManagerFunction().getSuffix(onlinePlayer)
                        + teamTag;

                onlinePlayer.setPlayerListName(translate(tabName));
            }
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.setDisplayName(translate(
                        Core.getGroupManagerFunction().getPrefix(onlinePlayer) +
                                onlinePlayer.getName() +
                                Core.getGroupManagerFunction().getSuffix(onlinePlayer)
                ));
            }
        }, 0L, 40L);

        Actionbar.actionBar(player);

        player.setCustomNameVisible(true);
        playerData.setName(player.getName());
        playerData.setDisplayName(player.getDisplayName());

        FastBoard board = new FastBoard(player);
        getScoreboardManager().getBoards().put(player.getUniqueId(), board);
        getScoreboardManager().updateBoard(board);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            if (player.isOnline()) startPlayerPointsTimer(player);
            delayedTasks.remove(player.getUniqueId()); // cleanup
        }, 60 * 60 * 20L);

        delayedTasks.put(player.getUniqueId(), task);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoinAsync(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            String playerName = PlayerDataFile.get().getString(uuid + ".playername");
            if (playerName == null) {
                getPlayerDataFileManager().savePlayerData(getPlayerDataManager().getPlayerData(uuid));
            } else {
                getPlayerDataFileManager().loadDataFromFile(uuid);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit (PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (getRamManager().getVanishedPlayers().contains(player)) {
            getPlayerDataManager().getPlayerData(player).setVanish(true);
            getRamManager().getVanishedPlayers().remove(player);
            event.setQuitMessage(null);
        } else {
            getPlayerDataManager().getPlayerData(player).setVanish(false);
            event.setQuitMessage(translate( player.getDisplayName() + "&8 has left the server."));
        }
        PlayerData playerData = getPlayerDataManager().getPlayerData(player);
        playerData.setLastOnline(getLastOnlineFormat());

        FastBoard board = getScoreboardManager().getBoards().remove(player.getUniqueId());
        if (board != null) board.delete();

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> getPlayerDataFileManager().savePlayerData(playerData));

        BukkitTask delayed = delayedTasks.remove(player.getUniqueId());
        if (delayed != null) delayed.cancel();

        // cancel repeating timer
        BukkitRunnable repeating = playerTimers.remove(player.getUniqueId());
        if (repeating != null) repeating.cancel();

        //must remove player data afterwards, since it takes ram and is useless to stay since we save it in storage
        //getPlayerDataManager().removePlayerData(player.getUniqueId());
    }

    public String getLastOnlineFormat() {
        Date dateLast = new Date();
        SimpleDateFormat formatterLast = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatterLast.format(dateLast);
    }

    public String getFirstJoinedFormat() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }


    public void startPlayerPointsTimer(Player player) {
        UUID uuid = player.getUniqueId();
        Random random = new Random();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Player onlinePlayer = Bukkit.getPlayer(uuid);
                if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                    cancel(); // cancel this task if player disconnected
                    playerTimers.remove(uuid);
                    return;
                }

                int points = random.nextInt(8001) + 2000; // 2000-10000 points
                PlayerData playerData = Core.getPlayerDataManager().getPlayerData(uuid);
                playerData.setPoints(playerData.getPoints() + points);

                onlinePlayer.sendMessage(ChatColor.GREEN + "You received " + points + " points for being online!");
                onlinePlayer.sendMessage(ChatColor.GREEN + "Thank you for your 60 minutes play time!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        };

        runnable.runTaskTimer(Core.getPlugin(), 0L, 60 * 60 * 20L); // every 30 minutes
        playerTimers.put(uuid, runnable);
    }


}
