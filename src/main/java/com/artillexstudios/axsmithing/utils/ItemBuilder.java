package com.artillexstudios.axsmithing.utils;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ItemBuilder {
    private final Section section;
    private final Map<String, String> replacements;
    private @NotNull ItemStack item = new ItemStack(Material.RED_BANNER);

    public ItemBuilder(Section section, Map<String, String> replacements) {
        this.section = section;
        this.replacements = replacements;

        createItem();
    }

    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }

    private void createItem() {
        setMaterial();
        setName();
        setLore();
        setAmount();
        setColor();
        setGlow();
        setCustomModelData();
        setEnchantments();
    }

    private void setName() {
        if (!section.isString("name")) return;

        AtomicReference<String> message = new AtomicReference<>(section.getString("name"));
        replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(StringUtils.format(message.get()));

        item.setItemMeta(meta);
    }

    private void setLore() {
        if (section.getStringList("lore") == null) return;
        if (!item.hasItemMeta()) return;

        ArrayList<String> lore = new ArrayList<>();

        for (String txt : section.getStringList("lore")) {
            AtomicReference<String> message = new AtomicReference<>(txt);
            replacements.forEach((key, value) -> message.set(message.get().replace(key, value)));
            lore.add(StringUtils.format(String.valueOf(message)));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    private void setMaterial() {
        if (!section.isString("material")) return;

        Material material = Material.matchMaterial(section.getString("material"));
        if (material == null) {
            Bukkit.getLogger().warning("Error while creating item: " + section.getNameAsString() + ". Invalid material. Defaulting to RED_BANNER");
            return;
        }

        item = new ItemStack(material);
    }

    private void setAmount() {
        if (!section.isInt("amount")) return;

        int amount = section.getInt("amount");

        item.setAmount(amount);
    }

    private void setColor() {
        if (!item.getType().toString().startsWith("LEATHER_")) return;
        if (!section.isString("color")) return;

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
        String[] rgb = section.getString("color").replace(" ", "").split(",");
        leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

        item.setItemMeta(leatherArmorMeta);
    }

    private void setGlow() {
        if (!section.isBoolean("glow")) return;

        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
    }

    private void setCustomModelData() {
        if (!section.isInt("custommodeldata")) return;

        int data = section.getInt("custommodeldata");
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(data);

        item.setItemMeta(meta);
    }

    private void setEnchantments() {
        if (!section.isList("enchantments")) return;

        ItemMeta meta = item.getItemMeta();

        for (String ench : section.getStringList("enchantments")) {
            String[] ench2 = ench.split(":");
            Enchantment ench3 = null;
            for (Enchantment str : Enchantment.values()) {
                if (("minecraft:" + ench2[0]).equalsIgnoreCase(str.getKey().toString())) {
                    ench3 = str;
                    meta.addEnchant(str, Integer.parseInt(ench2[1]), true);
                    break;
                }
            }
            if (ench3 == null) {
                Bukkit.getLogger().warning("Invalid enchantment: " + ench2[0]);
            }
        }

        item.setItemMeta(meta);
    }
}
