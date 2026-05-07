package me.thinksmp.functions;

import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class PlaceholderOld {
    private static final Map<Player, Map<String, String>> cachedValues = new WeakHashMap<>();

    public static void init() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Map<String, String> values = new HashMap<>();
                    try {
                        values.put("nv_online", String.valueOf(Bukkit.getOnlinePlayers().size() - Core.getRamManager().getVanishedPlayers().size()));
                        values.put("nv_rank", Core.getGroupManagerFunction().getGroup(p));
                        values.put("nv_points", String.valueOf(Core.getPlayerDataManager().getPlayerData(p).getPoints()));
                        values.put("nv_position", String.valueOf(Core.getLeaderboardManager().getPosition(p.getUniqueId())));
                        values.put("nv_rankcolor", "&" + GeneralUtility.getLastColorCode(Core.getGroupManagerFunction().getPrefix(p)));
                        values.put("nv_ping", String.valueOf(p.getPing()));
                        values.put("nv_gmmode", p.getGameMode().toString());
                        values.put("nv_staffchat", Core.getPlayerDataManager().getPlayerData(p).isStaffChat() ? "&aEnabled" : "&cDisabled");
                        values.put("nv_vanished", Core.getRamManager().getVanishedPlayers().contains(p) ? "&aEnabled" : "&cDisabled");
                    } catch (Exception ignored) {}
                    cachedValues.put(p, values);
                }
            }
        }.runTaskTimerAsynchronously(Core.getPlugin(), 0L, 20L);
    }

    public static String apply(Player p, String line) {
        if (line == null || p == null) return "";
        Map<String, String> placeholders = cachedValues.get(p);
        if (placeholders == null) return line;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            line = line.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return line;
    }

    public static List<String> apply(Player p, List<String> lines) {
        return lines.stream().map(line -> apply(p, line)).collect(Collectors.toList());
    }
}
