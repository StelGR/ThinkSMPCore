package me.thinksmp.utility;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UiUtility {
    public static ItemStack generateItem(ItemStack itemStack, String itemName, List<String> meta) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(meta);
        itemMeta.setDisplayName(itemName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateItem(ItemStack itemStack, String itemName, List<String> meta, Boolean hideFlag) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(meta);
        itemMeta.setDisplayName(itemName);
        if (hideFlag.booleanValue()) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateItem(ItemStack itemStack, String itemName, List<String> lore3, List<String> meta2, Boolean hideFlag) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> meta = new ArrayList<>();
        meta.addAll(lore3);
        meta.addAll(meta2);
        itemMeta.setLore(meta);
        itemMeta.setDisplayName(itemName);
        if (hideFlag.booleanValue()) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateSkull(ItemStack itemStack, String itemName, List<String> meta, UUID uuid) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        SkullMeta im = (SkullMeta)itemMeta;
        im.setLore(meta);
        im.setDisplayName(itemName);
        im.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public static ItemStack generateSkull(ItemStack itemStack, String itemName, List<String> meta) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        SkullMeta im = (SkullMeta)itemMeta;
        im.setLore(meta);
        im.setDisplayName(itemName);
        itemStack.setItemMeta(im);
        return itemStack;
    }

    public static ItemStack createSpacer() {
        ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(" ");
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack createSpacerLegacy() {
        ItemStack i = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, (short)15);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(" ");
        i.setItemMeta(im);
        return i;
    }
    public static ItemStack createSpacerLegacy(byte color) {
        ItemStack i = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, color);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(" ");
        i.setItemMeta(im);
        return i;
    }

    public static void fillWithSpacers(Inventory gui) {
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, createSpacer());
            }
        }
    }
}
