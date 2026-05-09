package me.thinksmp.listeners;

import me.thinksmp.Core;
import me.thinksmp.functions.Permissions;
import me.thinksmp.playerData.PlayerData;
import me.thinksmp.utility.GeneralUtility;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.thinksmp.utility.GeneralUtility.translate;

public class ChatListener extends ListenerAdapter implements Listener {
    public int maxCapsPercentage = 75;

    public int minLength = 5;

    public boolean globalMute = false;

    public HashMap<String, Long> chatCooldowns = new HashMap<>();
    public HashMap<String, Long> commandCooldowns = new HashMap<>();

    public static boolean isUppercase(char c) {
        return Character.isUpperCase(c);
    }

    public static double getUppercasePercentage(String string) {
        double upperCase = 0.0D;
        byte b;
        int i;
        char[] arrayOfChar;
        for (i = (arrayOfChar = string.toCharArray()).length, b = 0; b < i; ) {
            char c = arrayOfChar[b];
            if (isUppercase(c))
                upperCase++;
            b = (byte)(b + 1);
        }
        return upperCase / string.length() * 100.0D;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void checkGlobalMute(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        if (this.globalMute && !event.getPlayer().hasPermission("antispam.globalmute")) {
            event.getPlayer().sendMessage(ChatColor.RESET + String.valueOf(ChatColor.RESET));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkCaps(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        if (!event.getPlayer().hasPermission("antispam.caps") && event.getMessage().length() >= this.minLength && getUppercasePercentage(event.getMessage()) > this.maxCapsPercentage)
            event.setMessage(event.getMessage().toLowerCase());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkChatSpam(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (player.hasPermission("antispam.spam"))
            return;
        long time = System.currentTimeMillis();
        Long lastUse = this.chatCooldowns.get(player.getName());
        if (lastUse == null)
            lastUse = 0L;
        if (lastUse + 1200L > time) {
            player.sendMessage(ChatColor.RED + "Slow down your chat please.");
            event.setCancelled(true);
        }
        this.chatCooldowns.remove(player.getName());
        this.chatCooldowns.put(player.getName(), time);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void checkCommandSpam(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("antispam.spam"))
            return;
        long time = System.currentTimeMillis();
        Long lastUse = this.commandCooldowns.get(player.getName());
        if (lastUse == null)
            lastUse = 0L;
        if (lastUse + 1200L > time) {
            player.sendMessage(ChatColor.RED + "Slow down your chat please.");
            event.setCancelled(true);
        }
        this.commandCooldowns.remove(player.getName());
        this.commandCooldowns.put(player.getName(), time);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            if (event.isCancelled()) return;

            Player player = event.getPlayer();
            String originalMessage = event.getMessage();
            PlayerData playerData = Core.getPlayerDataManager().getPlayerData(player);

            // Replace emojis in the message
            Map<String, String> emojis = Map.of(
                    ":coffee:", "☕",
                    ":thumbs_up:", "👍",
                    ":smile:", "😀",
                    ":heart:", "❤️",
                    ":rofl:", "🤣"
            );
            String processedMessage = originalMessage;
            for (Map.Entry<String, String> entry : emojis.entrySet()) {
                processedMessage = processedMessage.replace(entry.getKey(), entry.getValue());
            }

            boolean isStaffChat = false;

            if (player.hasPermission(Permissions.STAFF_CHAT.getPermission()) || player.hasPermission(Permissions.ADMIN.getPermission())) {
                boolean staffChatActive = originalMessage.startsWith("#") || (playerData != null && playerData.isStaffChat());

                if (staffChatActive) {
                    String staffMessageContent = processedMessage.trim();

                    if (originalMessage.startsWith("#") && staffMessageContent.startsWith("#")) {
                        staffMessageContent = staffMessageContent.substring(1).trim();
                    }

                    if (!staffMessageContent.isEmpty()) {
                        isStaffChat = true;
                        event.setCancelled(true);

                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (online.hasPermission(Permissions.STAFF_CHAT.getPermission())) {
                                online.sendMessage(translate("&8[&cStaff&8-&cChat&8] &f" + player.getName() + "&f: " + staffMessageContent));
                            }
                        }
                    }
                }
            }

            if (!isStaffChat) {
                // Process player mentions
                List<Player> mentionedPlayers = new ArrayList<>();
                String mentionProcessedMessage = processedMessage;

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    PlayerData targetData = Core.getPlayerDataManager().getPlayerData(onlinePlayer);
                    String playerName = onlinePlayer.getName();
                    if (targetData != null && targetData.isCanBePinged() && mentionProcessedMessage.contains(playerName)) {
                        mentionProcessedMessage = mentionProcessedMessage.replace(playerName, "&e@" + playerName + "&f");
                        mentionedPlayers.add(onlinePlayer);
                    }
                }
                event.setMessage(translate(mentionProcessedMessage));
                int position = Core.getLeaderboardManager().getPosition(playerData.getPlayerUUID());
                // Set regular chat format
                event.setFormat(translate("&8[" + (position == 1 ? "&6" : position == 2 ? "&3" : position == 3 ? "&c" : "&7")
                        + position + "# &f- &c" +GeneralUtility.formatNumber( playerData.getPoints())+"&8] "
                        + (Core.getPlugin().getUltimateTeamsAPI().findTeamByMember(player.getUniqueId()).isPresent() ?
                        "&8[&f" + Core.getPlugin().getUltimateTeamsAPI().findTeamByMember(player.getUniqueId()).get().getName() + "&8] " : "") + "%1$s &8» &f%2$s"));

                // Send ping effects to mentioned players
                if (!mentionedPlayers.isEmpty()) {
                    Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                        for (Player mentioned : mentionedPlayers) {
                            mentioned.sendTitle("", translate("&eYou have been pinged by " + player.getDisplayName()), 10, 70, 20);
                            mentioned.playSound(mentioned.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!player.isOnline() || Core.getPlayerDataManager().getPlayerData(player).isVanish()) return;

        String deathMessage = event.getDeathMessage();
        if (deathMessage == null || deathMessage.isEmpty()) {
            deathMessage = GeneralUtility.translate(player.getDisplayName() + "&f died.");
        }
    }
}