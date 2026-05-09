package me.thinksmp.functions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PlaceholderAPI extends PlaceholderExpansion implements Listener {

    @Override
    public @NotNull String getIdentifier() {
        return "thinksmp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Stel";
    }

    @Override
    public @NotNull String getVersion() {
        return Core.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String params) {
        String placeholder = params.toLowerCase(Locale.ROOT);

        return switch (placeholder) {
            case "online" -> String.valueOf(Bukkit.getOnlinePlayers().size()
                    - Core.getRamManager().getVanishedPlayers().size());
            case "rank" -> {
                if (p == null) yield "";
                yield Core.getGroupManagerFunction().getGroup(p);
            }
            case "team" -> {
                if (p == null) yield "";
                yield Core.getPlugin()
                        .getUltimateTeamsAPI()
                        .findTeamByMember(p.getUniqueId())
                        .map(team -> " &8[&f" + team.getName() + "&8]")
                        .orElse("");
            }
            case "points" -> {
                if (p == null) yield "";
                yield GeneralUtility.formatNumberWithDots(Core.getPlayerDataManager().getPlayerData(p).getPoints());
            }
            case "position" -> {
                if (p == null) yield "";
                yield String.valueOf(Core.getLeaderboardManager().getPosition(p.getUniqueId()));
            }
            case "rankcolor" -> {
                if (p == null) yield "";
                yield "&" + GeneralUtility.getLastColorCode(Core.getGroupManagerFunction().getPrefix(p));
            }
            case "ping" -> {
                if (p == null || !p.isOnline()) yield "";
                yield String.valueOf(p.getPing());
            }
            case "gmmode" -> {
                if (p == null) yield "";
                yield p.getGameMode().toString();
            }
            case "staffchat" -> {
                if (p == null) yield "";
                yield Core.getPlayerDataManager().getPlayerData(p).isStaffChat()
                        ? "&aEnabled"
                        : "&cDisabled";
            }
            case "vanished" -> {
                if (p == null) yield "";
                yield Core.getRamManager().getVanishedPlayers().contains(p)
                        ? "&aEnabled"
                        : "&cDisabled";
            }
            default -> null;
        };
    }
}