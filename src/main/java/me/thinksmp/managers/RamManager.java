package me.thinksmp.managers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RamManager {

    public HashMap<UUID, UUID> privateMessages = new HashMap<>();
    public List<Player> vanishedPlayers = new ArrayList<>();

}
