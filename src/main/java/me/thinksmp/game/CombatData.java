package me.thinksmp.game;

import java.util.UUID;

public  class CombatData {
    UUID enemy;
    int timeLeft;
    CombatData(UUID enemy, int timeLeft) {
        this.enemy = enemy;
        this.timeLeft = timeLeft;
    }
}
