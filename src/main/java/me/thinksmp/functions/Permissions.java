package me.thinksmp.functions;

import lombok.Getter;

@Getter
public enum Permissions {
    STAFF_CHAT("thinksmp.staffchat"),
    STAFF_SCOREBOARD("thinksmp.staffscoreboard"),
    VANISH("thinksmp.vanish"),
    VANISH_SEE("thinksmp.vanish.see"),
    GAMEMODE("thinksmp.gm"),
    VIP("thinksmp.vip"),
    GAMEMODE_OTHERS("thinksmp.gm.others"),
    ADMIN("thinksmp.admin"); // use this for all permissions

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

}