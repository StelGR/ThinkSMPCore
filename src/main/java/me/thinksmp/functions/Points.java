package me.thinksmp.functions;

import lombok.Getter;

@Getter
public enum Points {
    //ores
    BASIC_BLOCKS(1),
    COAL_ORE(5),
    COPPER_ORE(10),
    IRON_ORE(20),
    GOLD_ORE(50),
    LAPIS_ORE(75),
    REDSTONE_ORE(75),
    QUARTZ_ORE(50),
    DIAMOND_ORE(500),
    EMERALD_ORE(250),
    ANCIENT_DEBRI(1500),
    ANIMAL(30),
    MONSTER(50),
    WARDEN(50000),
    WITHER(25000),
    ENDER_DRAGON(250000);

    private final int points;

    Points(int points) {
        this.points = points;
    }

}