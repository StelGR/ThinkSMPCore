package me.thinksmp.game.customItems;

import me.thinksmp.Core;
import me.thinksmp.utility.ZoneUtil;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static me.thinksmp.Core.getPlugin;
import static me.thinksmp.Core.itemManager;

public class Recipes implements Listener {
    public final Map<NamespacedKey, OneTimeCustomRecipe> oneTimeCustomRecipes = new HashMap<>();

    public void registerOneTimeCustomRecipe(CustomItem item, String[] shape, Map<Character, SlotIngredient> ingredients) {
        NamespacedKey key = new NamespacedKey(getPlugin(), "custom_" + item.getId());
        OneTimeCustomRecipe data = new OneTimeCustomRecipe(item, key, shape, ingredients);
        oneTimeCustomRecipes.put(key, data);

        Bukkit.removeRecipe(key);

        if (isCustomItemCrafted(item)) {
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, itemManager.createItem(item));
        recipe.shape(shape);

        for (Map.Entry<Character, SlotIngredient> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue().material);
        }

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPrepareOneTimeCustomCraft(PrepareItemCraftEvent event) {
        OneTimeCustomRecipe recipe = getOneTimeRecipe(event.getRecipe());
        if (recipe == null) return;

        if (isCustomItemCrafted(recipe.item) || !matchesMatrix(event.getInventory().getMatrix(), recipe)) {
            event.getInventory().setResult(null);
            return;
        }

        event.getInventory().setResult(itemManager.createItem(recipe.item));
    }

    @EventHandler
    public void onCraftOneTimeCustomItem(CraftItemEvent event) {
        OneTimeCustomRecipe recipe = getOneTimeRecipe(event.getRecipe());
        if (recipe == null) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (isCustomItemCrafted(recipe.item)) {
            player.sendMessage("§cThis item has already been crafted once.");
            event.getInventory().setResult(null);
            player.updateInventory();
            return;
        }

        if (event.isShiftClick()) {
            player.sendMessage("§cShift-click crafting is disabled for this recipe.");
            player.updateInventory();
            return;
        }

        if (!ZoneUtil.isInsideZone(player, "forgery")) {
            player.sendMessage("§cYou cannot craft that here.");
            player.updateInventory();
            return;
        }

        ItemStack cursor = player.getItemOnCursor();
        if (!isAir(cursor)) {
            player.sendMessage("§cEmpty your cursor before crafting this item.");
            player.updateInventory();
            return;
        }

        CraftingInventory inventory = event.getInventory();

        if (!matchesMatrix(inventory.getMatrix(), recipe)) {
            inventory.setResult(null);
            player.updateInventory();
            return;
        }

        consumeMatrix(inventory, recipe);

        ItemStack result = itemManager.createItem(recipe.item);
        player.setItemOnCursor(result);

        markCustomItemCrafted(recipe.item, player);
        Bukkit.removeRecipe(recipe.key);
        inventory.setResult(null);

        Bukkit.broadcastMessage("§a" + player.getName() + " crafted " + recipe.item.getDisplayName() + "§a for the first and only time.");
        player.updateInventory();
    }

    private OneTimeCustomRecipe getOneTimeRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed keyed)) return null;
        return oneTimeCustomRecipes.get(keyed.getKey());
    }

    private boolean matchesMatrix(ItemStack[] matrix, OneTimeCustomRecipe recipe) {
        for (int row = 0; row < 3; row++) {
            String line = recipe.shape[row];

            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                char key = line.charAt(col);

                ItemStack actual = matrix[index];

                if (key == ' ') {
                    if (!isAir(actual)) return false;
                    continue;
                }

                SlotIngredient expected = recipe.ingredients.get(key);
                if (expected == null) return false;

                if (!expected.matches(actual)) return false;

                if (isCustomItemStack(actual)) return false;
            }
        }

        return true;
    }

    private void consumeMatrix(CraftingInventory inventory, OneTimeCustomRecipe recipe) {
        ItemStack[] matrix = inventory.getMatrix();

        for (int row = 0; row < 3; row++) {
            String line = recipe.shape[row];

            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                char key = line.charAt(col);

                if (key != ' ') {
                    matrix[index] = null;
                }
            }
        }

        inventory.setMatrix(matrix);
    }

    private boolean isCustomItemStack(ItemStack item) {
        if (isAir(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(customItemKey(), PersistentDataType.STRING);
    }

    private NamespacedKey customItemKey() {
        return new NamespacedKey(getPlugin(), "custom_item");
    }

    public boolean isCustomItemCrafted(CustomItem item) {
        return getPlugin().getConfig().getBoolean("one-time-custom-items." + item.getId() + ".crafted", false);
    }

    public void markCustomItemCrafted(CustomItem item, Player player) {
        String path = "one-time-custom-items." + item.getId();

        getPlugin().getConfig().set(path + ".crafted", true);
        getPlugin().getConfig().set(path + ".player", player.getName());
        getPlugin().getConfig().set(path + ".uuid", player.getUniqueId().toString());
        getPlugin().getConfig().set(path + ".time", System.currentTimeMillis());

        getPlugin().saveConfig();
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    public SlotIngredient exact(Material material, int amount) {
        return new SlotIngredient(material, amount, null);
    }

    private SlotIngredient exact(Material material, int amount, Predicate<ItemStack> matcher) {
        return new SlotIngredient(material, amount, matcher);
    }

    public SlotIngredient infinityBook() {
        return exact(Material.ENCHANTED_BOOK, 1, item -> {
            if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta)) return false;

            Enchantment infinity = Enchantment.getByKey(NamespacedKey.minecraft("infinity"));
            return infinity != null && meta.hasStoredEnchant(infinity);
        });
    }

    public Map<Character, SlotIngredient> ingredients(Object... values) {
        Map<Character, SlotIngredient> map = new HashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            map.put((Character) values[i], (SlotIngredient) values[i + 1]);
        }

        return map;
    }

    public final class OneTimeCustomRecipe {
        private final CustomItem item;
        private final NamespacedKey key;
        private final String[] shape;
        private final Map<Character, SlotIngredient> ingredients;

        private OneTimeCustomRecipe(CustomItem item, NamespacedKey key, String[] shape, Map<Character, SlotIngredient> ingredients) {
            this.item = item;
            this.key = key;
            this.shape = shape;
            this.ingredients = ingredients;
        }
    }

    public final class SlotIngredient {
        private final Material material;
        private final int amount;
        private final Predicate<ItemStack> matcher;

        private SlotIngredient(Material material, int amount, Predicate<ItemStack> matcher) {
            this.material = material;
            this.amount = amount;
            this.matcher = matcher;
        }

        private boolean matches(ItemStack item) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (item.getType() != material) return false;
            if (item.getAmount() != amount) return false;
            return matcher == null || matcher.test(item);
        }
    }
}
