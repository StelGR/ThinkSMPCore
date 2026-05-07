package me.thinksmp.listeners;

import me.thinksmp.Core;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import static me.thinksmp.utility.GeneralUtility.translate;

public class MOTDListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerPing(ServerListPingEvent event) {
        event.setMotd(translate("&cThink SMP &7| &aCome join us!\n&aTop Player: &c"+ Core.getLeaderboardManager().getTopMotd(1)).replace("[", "").replace("]", ""));
    }
}
