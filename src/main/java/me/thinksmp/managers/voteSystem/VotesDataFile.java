package me.thinksmp.managers.voteSystem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static me.thinksmp.Core.getPlugin;

public class VotesDataFile {
    private static File file;
    private static FileConfiguration info;

    public static void setup() {
        file = new File(getPlugin().getDataFolder(), "votes.yml");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException ee) { ee.printStackTrace(); }
        info = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return info;
    }

    public static void save() {
        try {
            info.save(file);
        } catch (IOException e) {
            System.out.println("Could not save file.");
        }
    }

    public static void reload() {
        info = YamlConfiguration.loadConfiguration(file);
    }
}

