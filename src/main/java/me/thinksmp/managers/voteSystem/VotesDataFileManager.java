package me.thinksmp.managers.voteSystem;

import me.thinksmp.Core;
import me.thinksmp.utility.GeneralUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VotesDataFileManager {

    public void loadAllDataFromFile() {
        FileConfiguration file = VotesDataFile.get();

        if (file.getConfigurationSection("") == null) {
            return; // nothing stored
        }

        for (String playerName : file.getConfigurationSection("").getKeys(false)) {
            String storedName = file.getString(playerName + ".playername");

            if (storedName == null) {
                GeneralUtility.log("Skipping vote data for: " + playerName + " (no playername found)");
                continue;
            }

            Votes voteData = Core.getVotesDataManager().getVoteData(playerName);
            voteData.setPlayerName(storedName);
            voteData.setVotedTime(file.getString(playerName + ".votedTime"));
            voteData.setHasVotedToday(file.getBoolean(playerName + ".hasVotedToday", false));
            voteData.setHasReceivedReward(file.getBoolean(playerName + ".hasReceivedReward", false));
        }

        GeneralUtility.log("Loaded " + Core.getVotesDataManager().getAllVotes().size() + " votes into memory.");
    }


    public void saveAllVoteDataToFile() {
        // Online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            saveVoteData(Core.getVotesDataManager().getVoteData(player.getName()));
        }

        // Offline players (careful: this can be very large on big servers)
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            try {
                saveVoteData(Core.getVotesDataManager().getVoteData(offlinePlayer.getName()));
            } catch (Exception ignored) {
            }
        }
    }

    public void saveVoteData(Votes votes) {
        String playerName = votes.getPlayerName();
        FileConfiguration file = VotesDataFile.get();

        file.set(playerName + ".playername", votes.getPlayerName());
        file.set(playerName + ".votedTime", votes.getVotedTime());
        file.set(playerName + ".hasVotedToday", votes.isHasVotedToday());
        file.set(playerName + ".hasReceivedReward", votes.isHasReceivedReward());

        VotesDataFile.save();
    }
}
