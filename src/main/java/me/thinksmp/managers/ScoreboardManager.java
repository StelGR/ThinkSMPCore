package me.thinksmp.managers;

import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.entity.Player;

import java.util.*;

import static me.thinksmp.Core.*;
import static org.bukkit.Bukkit.getServer;

@Setter
@Getter
public class ScoreboardManager {

    public Map<UUID, FastBoard> boards = new HashMap<>();

    public String getTitle(Player player) {
        return getPlayerDataManager().getPlayerData(player).isStaffScoreboard()
                ? "§b§lThinkSMP §7- §cSTAFF"
                : "§b§lThinkSMP";
    }

    public List<String> getLines(Player player) {
        List<String> text = new ArrayList<>();

        text.add("§8§m                                ");
        text.add("§fOnline: §b%thinksmp_online%§7/§b" + getServer().getMaxPlayers());
        text.add("§fRank: §b%thinksmp_rank%");
        text.add("§fPing: §b%thinksmp_ping%");
        text.add("§3");
        text.add("§fYour Points: §b%thinksmp_points%");
        text.add("§fYour Position: §b%thinksmp_position%");

        if (getPlayerDataManager().getPlayerData(player).isStaffScoreboard()) {
            text.add("§2");
            text.add("§fStaff Chat: §b%thinksmp_staffchat%");
            text.add("§fVanished: §b%thinksmp_vanished%");
            text.add("§fGamemode: §b%thinksmp_gmmode%");
        }

        if (getPlayerDataManager().getPlayerData(player).hasPvpProtection())
        {
            text.add("§3");
            text.add("§fPVP Protection: §b" + Core.getPlugin().getPluginTimers().getPVPProtectionTimer(player));
        }

        if (Core.getPlugin().getPluginTimers().isGracePeriodActive())
        {
            text.add("§fGrace period: §b" + Core.getPlugin().getPluginTimers().getGracePeriodTimer());
        }

        text.add("§7§l");
        text.add("§bplay.thinksmp.net");
        text.add("§8§m                                ");

        List<String> replaced = new ArrayList<>();

        for (String line : text) {
            String parsed = PlaceholderAPI.setPlaceholders(player, line);
            replaced.add(GeneralUtility.translate(parsed));
        }

        return replaced;
    }

    public void init() {
        getServer().getScheduler().runTaskTimer(getPlugin(), () -> {
            for (FastBoard board : getBoards().values()) {
                updateBoard(board);
            }
        }, 2L, 2L);
    }

    public void updateBoard(FastBoard board) {
        Player player = board.getPlayer();

        if (player == null || !player.isOnline()) {
            return;
        }

        board.updateTitle(getTitle(player));
        board.updateLines(getLines(player));
    }
}