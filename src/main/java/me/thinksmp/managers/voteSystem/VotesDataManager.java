package me.thinksmp.managers.voteSystem;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VotesDataManager {
    private final Map<String, Votes> votesMap = new HashMap<>();


    public Votes getVoteData(Player player) {
        return getVoteData(player.getName());
    }

    public Votes getVoteData(String playerName) {
        return this.votesMap.computeIfAbsent(playerName, Votes::new);
    }

    public void removeVoteData(String playerName) {
        this.votesMap.remove(playerName);
    }

    public Collection<Votes> getAllVotes() {
        return votesMap.values();
    }
}
