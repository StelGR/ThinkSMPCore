package me.thinksmp.managers;

import lombok.Getter;
import lombok.Setter;
import me.thinksmp.files.LocationsFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

@Setter
@Getter
public class LocationsManager {
    Location spawnLocation;

    public void saveLocationToFile() {
        if (this.spawnLocation != null) {
            LocationsFile.get().set("spawn", this.spawnLocation.serialize());
            LocationsFile.save();
        }
    }

    public void loadLocationFromFile() {
        ConfigurationSection spawnSection = LocationsFile.get().getConfigurationSection("spawn");
        if (spawnSection != null)
            this.spawnLocation = createLocationFromSection(spawnSection);
    }

    private Location createLocationFromSection(ConfigurationSection section) {
        World world = Bukkit.getWorld(section.getString("world"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float pitch = (float)section.getDouble("pitch");
        float yaw = (float)section.getDouble("yaw");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
