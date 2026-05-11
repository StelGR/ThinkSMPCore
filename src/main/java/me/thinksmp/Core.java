package me.thinksmp;

import dev.xf3d3.ultimateteams.api.UltimateTeamsAPI;
import lombok.Getter;
import lombok.Setter;
import me.thinksmp.commands.adminCommands.*;
import me.thinksmp.commands.normalCommands.*;
import me.thinksmp.commands.normalCommands.teleportCommands.*;
import me.thinksmp.files.Clocks;
import me.thinksmp.files.GuildsFile;
import me.thinksmp.files.LocationsFile;
import me.thinksmp.functions.GroupManagerFunction;
import me.thinksmp.functions.PlaceholderAPI;
import me.thinksmp.game.Combat;
import me.thinksmp.game.Events;
import me.thinksmp.game.customEnchants.listener.CustomEnchantAnvilListener;
import me.thinksmp.game.customEnchants.listener.CustomEnchantEffectListener;
import me.thinksmp.game.customEnchants.listener.CustomEnchantTableListener;
import me.thinksmp.game.customEnchants.listener.CustomEnchantTradeListener;
import me.thinksmp.game.customEnchants.manager.CustomEnchantManager;
import me.thinksmp.game.customItems.*;
import me.thinksmp.game.homeSystem.HomeCommand;
import me.thinksmp.game.homeSystem.SetHomeCommand;
import me.thinksmp.listeners.*;
import me.thinksmp.listeners.pvpProtection.PvPProtectionListener;
import me.thinksmp.managers.*;
import me.thinksmp.managers.voteSystem.VotesDataFile;
import me.thinksmp.managers.voteSystem.VotesDataFileManager;
import me.thinksmp.managers.voteSystem.VotesDataManager;
import me.thinksmp.playerData.PlayerDataFile;
import me.thinksmp.playerData.PlayerDataFileManager;
import me.thinksmp.playerData.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;

import static me.thinksmp.utility.GeneralUtility.log;
import static me.thinksmp.utility.GeneralUtility.translate;

public class Core extends JavaPlugin {
    @Getter
    @Setter
    private static Core plugin;
    @Getter
    @Setter
    public static PlayerDataManager playerDataManager;
    @Getter
    @Setter
    public static PlayerDataFileManager playerDataFileManager;
    @Getter
    @Setter
    public static RamManager ramManager;
    @Getter
    @Setter
    public static GroupManagerFunction groupManagerFunction;
    @Getter
    @Setter
    public static ScoreboardManager scoreboardManager;
    @Getter
    @Setter
    public static LeaderboardManager leaderboardManager;
    @Getter
    @Setter
    public static LocationsManager locationsManager;
    @Getter
    @Setter
    public static Combat combat;
    @Getter
    @Setter
    public static TpaManager tpaManager;
    @Getter
    @Setter
    public static Events events;

    @Getter
    @Setter
    public static ItemAbilities itemAbilities;

    @Getter
    @Setter
    public static VotesDataManager votesDataManager;
    @Getter
    @Setter
    public static VotesDataFileManager votesDataFileManager;

    @Getter
    @Setter
    public static ItemManager itemManager;

    @Getter
    @Setter
    public static Recipes recipes;

    @Getter
    @Setter
    public static CustomEnchantManager enchantManager;

    @Getter
    @Setter
    private PluginTimers pluginTimers;

    @Getter
    @Setter
    UltimateTeamsAPI ultimateTeamsAPI;

    @Getter
    @Setter
    SpearElytraBlocker spearElytraBlocker;

    @Getter
    @Setter
    static PlacedOreManager placedOreManager;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        try {
            plugin = this;
            setupInstances();
            setupFiles();
            setupListeners();
            setupCommands();
            new me.thinksmp.functions.PlaceholderAPI().register();
            setupScoreboard();
            setupOthers();


            long endTime = System.currentTimeMillis();
            log(translate("&b=====&e===========================================&b====="));
            log(translate("&aThinkSMP Core &ahas been successfully enabled."));
            //you could also have something like this in case you need a dependency to check whether that dependency was loaded or not
            //you may need to use Postworld load to see this properly
            //log("");
            //log((Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) ? translate("&ePlaceholderAPI&7: &aEnabled") : translate("&ePlaceholderAPI&7: &cDisabled"));
            log("");
            log(translate("&eVersion&7: &3v" + getDescription().getVersion()));
            log("");
            log(translate("&aThinkSMP Core &floaded in &a" + (endTime - startTime) + "ms"));
            log(translate("&b=====&e===========================================&b====="));
        } catch (Exception exception) {
            log(translate("&cThinkSMP Core had an error while loading: \n"));
            exception.printStackTrace();
        }
    }

    //the functions below are used to have a cleaner code layout, optional basically.

    public void setupInstances(){
        playerDataManager = new PlayerDataManager();
        playerDataFileManager = new PlayerDataFileManager();
        locationsManager = new LocationsManager();
        votesDataManager = new VotesDataManager();
        votesDataFileManager = new VotesDataFileManager();
        groupManagerFunction = new GroupManagerFunction();
        ramManager = new RamManager();
        scoreboardManager = new ScoreboardManager();
        leaderboardManager = new LeaderboardManager();
        combat = new Combat();
        tpaManager = new TpaManager(getPlugin());
        events = new Events();
        itemAbilities = new ItemAbilities();
        itemManager = new ItemManager();
        recipes = new Recipes();
        pluginTimers = new PluginTimers(this);
        enchantManager = new CustomEnchantManager(this);
        spearElytraBlocker = new SpearElytraBlocker(this);
        placedOreManager = new PlacedOreManager();


        ultimateTeamsAPI = UltimateTeamsAPI.getInstance();
        //for reassurance
        getCombat().startTimerTask(getPlugin());
        pluginTimers.startTask();
    }

    //i am not sure if loading files should be first, that should depend on your project

    public void setupFiles() {
        PlayerDataFile.setup();
        PlayerDataFile.save();
        GuildsFile.setup();
        GuildsFile.save();
        LocationsFile.setup();
        LocationsFile.save();
        Clocks.setup();
        VotesDataFile.setup();
        VotesDataFile.save();
        getVotesDataFileManager().loadAllDataFromFile();
        getLocationsManager().loadLocationFromFile();
        placedOreManager.loadPlacedOres();
    }

    public void setupOthers() {
        NamespacedKey cheaperGapKey = new NamespacedKey(this, "cheaper_gap");
        ItemStack gap = new ItemStack(Material.GOLDEN_APPLE);
        ShapedRecipe gapple = new ShapedRecipe(cheaperGapKey, gap);
        gapple.shape(" G ", "GPG", " G ");
        gapple.setIngredient('P', Material.APPLE);
        gapple.setIngredient('G', Material.GOLD_INGOT);

        NamespacedKey opGapKey = new NamespacedKey(this, "op_gap");
        ItemStack opgap = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ShapedRecipe opgapple = new ShapedRecipe(opGapKey, opgap);
        opgapple.shape("   ", " C ", "   ");
        opgapple.setIngredient('C', Material.HEAVY_CORE);

        Bukkit.removeRecipe(cheaperGapKey);
        Bukkit.removeRecipe(opGapKey);
        Bukkit.addRecipe(gapple);
        Bukkit.addRecipe(opgapple);

        getRecipes().oneTimeCustomRecipes.clear();

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.AXE,
                new String[]{"AGG", "DDS", "BBB"},
                getRecipes().ingredients(
                        'A', getRecipes().exact(Material.NETHERITE_AXE, 1),
                        'G', getRecipes().exact(Material.GOLDEN_APPLE, 32),
                        'D', getRecipes().exact(Material.ANCIENT_DEBRIS, 32),
                        'S', getRecipes().exact(Material.WITHER_SKELETON_SKULL, 1),
                        'B', getRecipes().exact(Material.BLAZE_ROD, 32)
                )
        );

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.BOW,
                new String[]{"BIL", "SWR", "KQR"},
                getRecipes().ingredients(
                        'B', getRecipes().exact(Material.BOW, 1),
                        'I', getRecipes().infinityBook(),
                        'L', getRecipes().exact(Material.LAVA_BUCKET, 1),
                        'S', getRecipes().exact(Material.SPECTRAL_ARROW, 32),
                        'W', getRecipes().exact(Material.WITHER_ROSE, 4),
                        'R', getRecipes().exact(Material.ARROW, 64),
                        'K', getRecipes().exact(Material.SKELETON_SKULL, 1),
                        'Q', getRecipes().exact(Material.SCULK_SHRIEKER, 16)
                )
        );

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.TRIDENT,
                new String[]{"THE", "NNN", "NNN"},
                getRecipes().ingredients(
                        'T', getRecipes().exact(Material.TRIDENT, 1),
                        'H', getRecipes().exact(Material.HEART_OF_THE_SEA, 1),
                        'E', getRecipes().exact(Material.ENCHANTED_GOLDEN_APPLE, 1),
                        'N', getRecipes().exact(Material.NAUTILUS_SHELL, 1)
                )
        );

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.SWORD,
                new String[]{" D ", " S ", " H "},
                getRecipes().ingredients(
                        'D', getRecipes().exact(Material.DRAGON_EGG, 1),
                        'S', getRecipes().exact(Material.NETHERITE_SWORD, 1),
                        'H', getRecipes().exact(Material.DRAGON_HEAD, 1)
                )
        );

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.HAMMER,
                new String[]{"MAB", "WGG", "SSE"},
                getRecipes().ingredients(
                        'M', getRecipes().exact(Material.MACE, 1),
                        'A', getRecipes().exact(Material.NETHERITE_AXE, 1),
                        'B', getRecipes().exact(Material.BREEZE_ROD, 32),
                        'W', getRecipes().exact(Material.WIND_CHARGE, 32),
                        'G', getRecipes().exact(Material.GOLDEN_APPLE, 5),
                        'S', getRecipes().exact(Material.STONE, 32),
                        'E', getRecipes().exact(Material.ENCHANTED_GOLDEN_APPLE, 1)
                )
        );

        getRecipes().registerOneTimeCustomRecipe(
                CustomItem.PICKAXE,
                new String[]{"PBG", "GDR", "RRR"},
                getRecipes().ingredients(
                        'P', getRecipes().exact(Material.NETHERITE_PICKAXE, 1),
                        'B', getRecipes().exact(Material.BEACON, 1),
                        'G', getRecipes().exact(Material.GOLDEN_APPLE, 16),
                        'D', getRecipes().exact(Material.DEEPSLATE_EMERALD_ORE, 1),
                        'R', getRecipes().exact(Material.REDSTONE_BLOCK, 10)
                )
        );

        Clocks.save();
    }

    public void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlaceholderAPI(), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new PointEventListener(), this);
        getServer().getPluginManager().registerEvents(new MOTDListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(getCombat(), this);
        getServer().getPluginManager().registerEvents(getEvents(), this);
        getServer().getPluginManager().registerEvents(getItemAbilities(), this);
        getServer().getPluginManager().registerEvents(getRecipes(), this);
        getServer().getPluginManager().registerEvents(getSpearElytraBlocker(), this);
        getServer().getPluginManager().registerEvents(new PvPProtectionListener(pluginTimers), this);

        getServer().getPluginManager().registerEvents(new CustomEnchantTableListener(getEnchantManager()), this);
        getServer().getPluginManager().registerEvents(new CustomEnchantAnvilListener(getEnchantManager()), this);
        getServer().getPluginManager().registerEvents(new CustomEnchantEffectListener(getEnchantManager()), this);
        getServer().getPluginManager().registerEvents(new CustomEnchantTradeListener(getEnchantManager()), this);
        getItemAbilities().start();

        getServer().getScheduler().runTaskTimer(this, getSpearElytraBlocker(), 1L, 1L);

    }

    public void setupCommands() {
        Objects.requireNonNull(getCommand("reply")).setExecutor(new Messaging());
        Objects.requireNonNull(getCommand("message")).setExecutor(new Messaging());
        // i am unsure if this is the best way to register the aliases (must be along side the plugin.yml).
        // and i don't think this is the best way to make a single item list, but oh well
        // and no i am not gonna use chat gpt for this lol
        Objects.requireNonNull(getCommand("message")).setAliases(Collections.singletonList("msg"));
        Objects.requireNonNull(getCommand("reply")).setAliases(Collections.singletonList("r"));

        Objects.requireNonNull(getCommand("thinkitem")).setExecutor(new ItemCommand());

        Objects.requireNonNull(getCommand("setcorner")).setExecutor(new SetCorner());

        Objects.requireNonNull(getCommand("gamemode")).setExecutor(new Gamemode());
        Objects.requireNonNull(getCommand("vanish")).setExecutor(new Vanish());
        Objects.requireNonNull(getCommand("staffchat")).setExecutor(new StaffChat());
        Objects.requireNonNull(getCommand("staffscoreboard")).setExecutor(new StaffScoreboard());
        Objects.requireNonNull(getCommand("leaderboard")).setExecutor(new Leaderboard());
        Objects.requireNonNull(getCommand("points")).setExecutor(new Points());
        Objects.requireNonNull(getCommand("information")).setExecutor(new Information());
        Objects.requireNonNull(getCommand("settings")).setExecutor(new Settings());
        Objects.requireNonNull(getCommand("tpa")).setExecutor(new TpaCommand());
        Objects.requireNonNull(getCommand("tpaccept")).setExecutor(new TpAccept());
        Objects.requireNonNull(getCommand("tpdeny")).setExecutor(new TpDeny());
        Objects.requireNonNull(getCommand("tpcancel")).setExecutor(new TpCancel());
        Objects.requireNonNull(getCommand("wild")).setExecutor(new WildCommand(this));
        Objects.requireNonNull(getCommand("wild")).setAliases(Collections.singletonList("rtp"));

        Objects.requireNonNull(getCommand("enderchest")).setExecutor(new EnderChest());
        Objects.requireNonNull(getCommand("smithingtable")).setExecutor(new SmithingTable());
        Objects.requireNonNull(getCommand("anvil")).setExecutor(new Anvil());

        Objects.requireNonNull(getCommand("crateeventtest")).setExecutor(new CrateEventTest());

        Objects.requireNonNull(getCommand("grace")).setExecutor(new GracePeriod(pluginTimers));
        Objects.requireNonNull(getCommand("grace")).setTabCompleter(new GracePeriod(pluginTimers));

        Objects.requireNonNull(getCommand("sethome")).setExecutor(new SetHomeCommand());
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommand());


        // lazy way to add this for now
        CommandsGUI commandsGUICommand = new CommandsGUI();

        Objects.requireNonNull(getCommand("commands")).setExecutor(commandsGUICommand);
        Bukkit.getPluginManager().registerEvents(commandsGUICommand, this);
    }

    public void setupScoreboard() {
        getScoreboardManager().init();
    }

    @Override
    public void onDisable() {
        long startTime = System.currentTimeMillis();
        try {
            saveData();

            long endTime = System.currentTimeMillis();
            log(translate("&b=====&e===========================================&b====="));
            log(translate("&cThinkSMP Core &ahas been successfully disabled."));
            //log("");
            //log((Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) ? translate("&ePlaceholderAPI&7: &aEnabled") : translate("&ePlaceholderAPI&7: &cDisabled"));
            log("");
            log(translate("&eVersion&7: &3v" + getDescription().getVersion()));
            log("");
            log(translate("&aThinkSMP Core &fshutdown in &a" + (endTime - startTime) + "ms"));
            log(translate("&b=====&e===========================================&b====="));
        } catch (Exception exception) {
            log(translate("&cThinkSMP Core had an error while shutting down: \n"));
            exception.printStackTrace();
        }
    }

    public void saveData() {
        try {
            getPlayerDataFileManager().saveAllPlayerDataToFile();
            getLocationsManager().saveLocationToFile();
            placedOreManager.savePlacedOres();
            getVotesDataFileManager().saveAllVoteDataToFile();

            VotesDataFile.save();
            Clocks.save();
            GuildsFile.save();
            LocationsFile.save();

            log("[" + getPlugin().getName() + "] Saved player data");
        }
        catch (Exception exception) {
            log("[" + getPlugin().getName() + "] Error while saving player data...");
            exception.printStackTrace();
        }
    }

}
