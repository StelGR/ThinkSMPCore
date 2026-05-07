package me.thinksmp.managers.voteSystem;

import com.bencodez.votingplugin.events.PlayerVoteEvent;
import me.thinksmp.Core;
import me.thinksmp.playerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VotesManager implements Listener {

    public VotesManager() {
        Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
            long now = System.currentTimeMillis();

            for (Votes votes : Core.getVotesDataManager().getAllVotes()) {
                if (votes.getVotedTime() != null) {
                    long votedAt = Long.parseLong(votes.getVotedTime());
                    if (now - votedAt >= 24 * 60 * 60 * 1000L) {
                        votes.setHasVotedToday(false);
                        votes.setHasReceivedReward(false);
                        votes.setVotedTime(null);
                    }
                }
            }
        }, 20L, 20L); // runs every second
    }

    @EventHandler
    public void onPlayerVote(PlayerVoteEvent event) {
        String playerName = event.getPlayer(); // string, works for offline players
        long timestamp = event.getTime(); // epoch millis from VotingPlugin

        // get or create vote data
        Votes votes = Core.getVotesDataManager().getVoteData(playerName);

        // update
        votes.setPlayerName(playerName);
        votes.setVotedTime(String.valueOf(timestamp));
        votes.setHasVotedToday(true);
        votes.setHasReceivedReward(false);

        // optional: give if online
        if (Bukkit.getPlayerExact(playerName) != null) {
            PlayerData data = Core.getPlayerDataManager().getPlayerData(Bukkit.getPlayerExact(playerName));
            data.setPoints(data.getPoints() + 1000);

            votes.setHasReceivedReward(true);
            Bukkit.getPlayerExact(playerName).sendMessage("§aYou received 1000 points for voting!");

            Core.getVotesDataFileManager().saveVoteData(votes);
        }

        // save to file immediately (optional, can also wait until saveAllPlayerDataToFile())
        Core.getVotesDataFileManager().saveVoteData(votes);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        Votes votes = Core.getVotesDataManager().getVoteData(name);

        if (votes.isHasVotedToday() && !votes.isHasReceivedReward()) {
            // give reward
            PlayerData data = Core.getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId());
            data.setPoints(data.getPoints() + 1000);

            votes.setHasReceivedReward(true);
            event.getPlayer().sendMessage("§aYou received 1000 points for voting!");

            Core.getVotesDataFileManager().saveVoteData(votes);
        }
    }

}
