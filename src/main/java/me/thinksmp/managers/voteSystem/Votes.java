package me.thinksmp.managers.voteSystem;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Votes {
    private String playerName;
    private String votedTime;
    private boolean hasVotedToday;
    private boolean hasReceivedReward;

    public Votes(String playerName) {
        this.playerName = playerName;
        this.votedTime = null;
        this.hasVotedToday = false;
        this.hasReceivedReward = false;
    }
}
