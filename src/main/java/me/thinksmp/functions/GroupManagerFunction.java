package me.thinksmp.functions;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.List;

public class GroupManagerFunction {

    private GroupManager groupManager;

    public boolean hasGroupManager() {
        if (this.groupManager != null) return true;

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        Plugin GMplugin = pluginManager.getPlugin("GroupManager");

        if (GMplugin != null && GMplugin.isEnabled()) {
            this.groupManager = (GroupManager) GMplugin;
            return true;
        }
        return false;
    }

    public String getGroup(Player player) {
        if (!hasGroupManager()) return null;

        AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        if (handler == null) return null;

        return handler.getGroup(player.getName());
    }

    public boolean setGroup(Player player, String group) {
        if (!hasGroupManager()) return false;

        OverloadedWorldHolder handler = this.groupManager.getWorldsHolder().getWorldData(player);
        if (handler == null) return false;

        handler.getUser(player.getName()).setGroup(handler.getGroup(group));
        return true;
    }

    public List<String> getGroups(Player player) {
        if (!hasGroupManager()) return null;

        AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        if (handler == null) return null;
        return Arrays.asList(handler.getGroups(player.getName()));
    }

    public String getPrefix(Player player) {
        if (!hasGroupManager()) return null;

        AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        if (handler == null) return null;
        return handler.getUserPrefix(player.getName());
    }

    public String getSuffix(Player player) {
        if (!hasGroupManager()) return null;
        AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        if (handler == null) return null;

        return handler.getUserSuffix(player.getName());
    }

    @Deprecated
    public boolean hasPermission(Player player, String node) {
        if (!hasGroupManager()) return false;

        AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);
        if (handler == null) return false;

        return handler.has(player, node);
    }
}
