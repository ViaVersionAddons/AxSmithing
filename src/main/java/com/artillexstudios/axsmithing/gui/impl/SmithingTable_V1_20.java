package com.artillexstudios.axsmithing.gui.impl;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.YamlDocument;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.artillexstudios.axsmithing.gui.SmithingTable;
import com.artillexstudios.axsmithing.utils.ItemBuilder;
import com.artillexstudios.axsmithing.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmithingTable_V1_20 implements SmithingTable, InventoryHolder {
    private final int outputSlot;
    private final int upgradeSlot;
    private final int itemSlot;
    private final int templateSlot;
    private final boolean needNetheriteTemplate;
    private final boolean dontConvertWithModelData;
    private final Field templateField;
    private final Field baseField;
    private final Field additionField;
    private final Field resultField;

    public SmithingTable_V1_20() {
        YamlDocument config = AxSmithingPlugin.getConfiguration();
        outputSlot = config.getInt("menu.1_20.output-slot");
        upgradeSlot = config.getInt("menu.1_20.upgrade-slot");
        templateSlot = config.getInt("menu.1_20.template-slot");
        itemSlot = config.getInt("menu.1_20.item-slot");
        needNetheriteTemplate = config.getBoolean("menu.1_20.need-netherite-template");
        dontConvertWithModelData = config.getBoolean("menu.1_20.dont-convert-with-modeldata");
        try {
            templateField = SmithingTransformRecipe.class.getDeclaredField("template");
            templateField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            baseField = SmithingRecipe.class.getDeclaredField("base");
            baseField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            additionField = SmithingRecipe.class.getDeclaredField("addition");
            additionField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            resultField = SmithingRecipe.class.getDeclaredField("result");
            resultField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, AxSmithingPlugin.getConfiguration().getInt("menu.1_20.rows") * 9, StringUtils.format(AxSmithingPlugin.getConfiguration().getString("menu.1_20.title")));
        Set<Object> items = AxSmithingPlugin.getConfiguration().getSection("menu.1_20.items").getKeys();

        for (Object item : items) {
            Section section = AxSmithingPlugin.getConfiguration().getSection("menu.1_20.items").getSection(item.toString());
            List<String> slots = section.getStringList("slots");
            ItemStack itemStack = new ItemBuilder(section, Map.of()).getItem();

            for (Integer slot : slots(slots)) {
                inventory.setItem(slot, itemStack);
            }
        }

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_20)) return;

        if ((event.getSlot() == upgradeSlot || event.getSlot() == itemSlot || event.getSlot() == templateSlot) && event.getInventory().getItem(outputSlot) != null) {
            event.getInventory().setItem(outputSlot, null);
            if (checkRecipes(event.getInventory(), event.getInventory().getItem(templateSlot), event.getInventory().getItem(itemSlot), event.getInventory().getItem(upgradeSlot))) {
                event.getInventory().setItem(outputSlot, null);
            }
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
        }

        if (event.getSlot() == itemSlot || event.getSlot() == upgradeSlot || event.getSlot() == templateSlot) {
            event.setCancelled(false);
        }

        if (event.getSlot() == outputSlot && event.getCurrentItem() != null && (event.getCursor() == null || (event.getCursor() != null && event.getCursor().getType().equals(Material.AIR)))) {
            event.setCancelled(false);
        }

        if (event.getSlot() == outputSlot && event.getView().getTopInventory().getItem(outputSlot) != null && event.getView().getTopInventory().getItem(outputSlot).getType() != Material.AIR) {
            int amount;
            if (event.getInventory().getItem(templateSlot) != null) {
                amount = event.getInventory().getItem(templateSlot).getAmount() - 1;
                if (amount == 0) {
                    event.getInventory().setItem(templateSlot, null);
                } else {
                    event.getInventory().getItem(templateSlot).setAmount(amount);
                }

                ((Player) event.getWhoClicked()).updateInventory();
            }

            if (event.getInventory().getItem(itemSlot) != null) {
                amount = event.getInventory().getItem(itemSlot).getAmount() - 1;
                if (amount == 0) {
                    event.getInventory().setItem(itemSlot, null);
                } else {
                    event.getInventory().getItem(itemSlot).setAmount(amount);
                }

                ((Player) event.getWhoClicked()).updateInventory();
            }

            if (event.getInventory().getItem(upgradeSlot) != null) {
                amount = event.getInventory().getItem(upgradeSlot).getAmount() - 1;
                if (amount == 0) {
                    event.getInventory().setItem(upgradeSlot, null);
                } else {
                    event.getInventory().getItem(upgradeSlot).setAmount(amount);
                }

                ((Player) event.getWhoClicked()).updateInventory();
            }

            return;
        }

        updateGui(event.getInventory());
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_20)) return;

        if (event.getInventory().getItem(templateSlot) != null && event.getInventory().getItem(templateSlot).getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(event.getInventory().getItem(templateSlot));
        if (event.getInventory().getItem(itemSlot) != null && event.getInventory().getItem(itemSlot).getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(event.getInventory().getItem(itemSlot));
        if (event.getInventory().getItem(upgradeSlot) != null && event.getInventory().getItem(upgradeSlot).getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(event.getInventory().getItem(upgradeSlot));
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_20)) return;

        ItemStack outputItem = event.getInventory().getItem(outputSlot);
        if (event.getRawSlots().contains(outputSlot) && (outputItem == null || outputItem.getType().isAir()) && !event.getOldCursor().getType().isAir()) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getItem(outputSlot) != null) {
            event.getInventory().getItem(outputSlot).setAmount(0);
        }

        updateGui(event.getInventory());
    }

    public List<Integer> slots(@NotNull List<String> strings) {
        List<Integer> returnedSlots = new ArrayList<>();

        for (String s : strings) {
            if (s.contains("-")) {
                String[] splitS = s.split("-");
                returnedSlots.addAll(getSlots(Integer.parseInt(splitS[0]), Integer.parseInt(splitS[1])));
            } else {
                returnedSlots.add(Integer.parseInt(s));
            }
        }

        return returnedSlots;
    }

    @NotNull
    private List<Integer> getSlots(int small, int max) {
        List<Integer> slots = new ArrayList<>();

        for (int i = small; i <= max; i++) slots.add(i);
        return slots;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }

    private void updateGui(Inventory inv) {
        Scheduler.get().runLater(t -> {
            ItemStack template = inv.getItem(templateSlot);
            ItemStack base = inv.getItem(itemSlot);
            ItemStack addition = inv.getItem(upgradeSlot);
            if (addition == null || addition.getType().isAir()) {
                addition = new ItemStack(Material.AIR);
            }

            if (base == null || addition.getType().isAir()) {
                base = new ItemStack(Material.AIR);
            }

            if (template == null || addition.getType().isAir()) {
                if (!needNetheriteTemplate) {
                    template = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
                } else {
                    template = new ItemStack(Material.AIR);
                }
            }

            ItemStack finalTemplate = template;
            ItemStack finalAddition = addition;
            ItemStack finalBase = base;

            // Very very very ugly solution, but I guess, it works!
            checkRecipes(inv, finalTemplate, finalBase, finalAddition);
        }, 1L);
    }

    private boolean checkRecipes(Inventory inv, ItemStack finalTemplate, ItemStack finalBase, ItemStack finalAddition) {
        boolean successful = checkRecipe(inv, finalBase, finalAddition, finalTemplate);
        if (!successful) {
            successful = checkRecipe(inv, finalBase, finalTemplate, finalAddition);
        }
        if (!successful) {
            successful = checkRecipe(inv, finalTemplate, finalBase, finalAddition);
        }
        if (!successful) {
            successful = checkRecipe(inv, finalTemplate, finalAddition, finalBase);
        }
        if (!successful) {
            successful = checkRecipe(inv, finalAddition, finalBase, finalTemplate);
        }
        if (!successful) {
            checkRecipe(inv, finalAddition, finalTemplate, finalBase);
        }

        return successful;
    }

    private boolean checkRecipe(Inventory inventory, ItemStack finalTemplate, ItemStack finalBase, ItemStack finalAddition) {
        if (inventory.getItem(outputSlot) != null && !inventory.getItem(outputSlot).getType().isAir()) {
            return false;
        }
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            if (recipe instanceof SmithingTrimRecipe trimRecipe) {
                boolean test1 = trimRecipe.getTemplate().test(finalTemplate);
                boolean test2 = trimRecipe.getBase().test(finalBase);
                boolean test3 = trimRecipe.getAddition().test(finalAddition);

                if (test1 && test2 && test3) {
                    ItemStack clone = finalBase.clone();
                    ItemMeta meta = clone.getItemMeta();
                    if (meta instanceof ArmorMeta armorMeta) {
                        ArmorTrim trim = armorTrim(finalAddition, finalTemplate);

                        armorMeta.setTrim(trim);
                        clone.setItemMeta(armorMeta);
                    }

                    inventory.setItem(outputSlot, clone);
                    return true;
                } else {
                    inventory.setItem(outputSlot, new ItemStack(Material.AIR));
                }
            }

            if (recipe instanceof SmithingTransformRecipe transformRecipe) {
                RecipeChoice template = getTemplate(transformRecipe);
                RecipeChoice base = getBase(transformRecipe);
                RecipeChoice addition = getAddition(transformRecipe);
                if (template == null || base == null || addition == null) {
                    return false;
                }

                boolean test1 = template.test(finalTemplate);
                boolean test2 = base.test(finalBase);
                ItemMeta baseItemMeta = finalBase.getItemMeta();
                if (baseItemMeta == null) return false;
                boolean test3 = addition.test(finalAddition);

                if (dontConvertWithModelData && baseItemMeta.hasCustomModelData()) {
                    return false;
                }

                if (test1 && test2 && test3) {
                    ItemStack item = getResult(transformRecipe);
                    if (item == null) {
                        return false;
                    }
                    item.setItemMeta(baseItemMeta);
                    inventory.setItem(outputSlot, item);
                    return true;
                } else {
                    inventory.setItem(outputSlot, new ItemStack(Material.AIR));
                }
            }
        }

        return false;
    }

    public RecipeChoice getTemplate(SmithingTransformRecipe recipe) {
        RecipeChoice recipeChoice;
        try {
            recipeChoice = (RecipeChoice) templateField.get(recipe);
            if (recipeChoice == null) {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return recipeChoice.clone();
    }

    public RecipeChoice getBase(SmithingTransformRecipe recipe) {
        RecipeChoice recipeChoice;
        try {
            recipeChoice = (RecipeChoice) baseField.get(recipe);
            if (recipeChoice == null) {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return recipeChoice.clone();
    }

    public RecipeChoice getAddition(SmithingTransformRecipe recipe) {
        RecipeChoice recipeChoice;
        try {
            recipeChoice = (RecipeChoice) additionField.get(recipe);
            if (recipeChoice == null) {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return recipeChoice.clone();
    }

    public ItemStack getResult(SmithingTransformRecipe recipe) {
        ItemStack result;
        try {
            result = (ItemStack) resultField.get(recipe);
            if (result == null) {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return result.clone();
    }

    @Nullable
    private ArmorTrim armorTrim(@NotNull ItemStack finalAddition, @NotNull ItemStack finalTemplate) {
        TrimMaterial material;
        TrimPattern pattern;

        switch (finalAddition.getType()) {
            case DIAMOND -> material = TrimMaterial.DIAMOND;
            case GOLD_INGOT -> material = TrimMaterial.GOLD;
            case EMERALD -> material = TrimMaterial.EMERALD;
            case IRON_INGOT -> material = TrimMaterial.IRON;
            case QUARTZ -> material = TrimMaterial.QUARTZ;
            case AMETHYST_SHARD -> material = TrimMaterial.AMETHYST;
            case LAPIS_LAZULI -> material = TrimMaterial.LAPIS;
            case REDSTONE -> material = TrimMaterial.REDSTONE;
            case COPPER_INGOT -> material = TrimMaterial.COPPER;
            case NETHERITE_INGOT -> material = TrimMaterial.NETHERITE;
            default -> material = null;
        }

        switch (finalTemplate.getType()) {
            case COAST_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.COAST;
            case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.DUNE;
            case EYE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.EYE;
            case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SENTRY;
            case HOST_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.HOST;
            case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SHAPER;
            case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SILENCE;
            case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.RAISER;
            case RIB_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.RIB;
            case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SNOUT;
            case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SPIRE;
            case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.TIDE;
            case VEX_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.VEX;
            case WARD_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.WARD;
            case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.WAYFINDER;
            default -> pattern = null;
        }

        if (pattern == null || material == null) {
            return null;
        }

        return new ArmorTrim(material, pattern);
    }
}
