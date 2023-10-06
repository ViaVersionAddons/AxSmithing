package com.artillexstudios.axsmithing.gui.impl;

import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.artillexstudios.axsmithing.gui.SmithingTable;
import com.artillexstudios.axsmithing.utils.ItemBuilder;
import com.artillexstudios.axsmithing.utils.StringUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmithingTable_V1_16 implements SmithingTable, InventoryHolder {
    private final int outputSlot;
    private final int upgradeSlot;
    private final int itemSlot;
    private final boolean dontConvertWithModelData;

    public SmithingTable_V1_16() {
        YamlDocument config = AxSmithingPlugin.getConfiguration();
        outputSlot = config.getInt("menu.1_16.output-slot");
        upgradeSlot = config.getInt("menu.1_16.upgrade-slot");
        itemSlot = config.getInt("menu.1_16.item-slot");
        dontConvertWithModelData = config.getBoolean("menu.1_16.dont-convert-with-modeldata");
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, AxSmithingPlugin.getConfiguration().getInt("menu.1_16.rows") * 9, StringUtils.format(AxSmithingPlugin.getConfiguration().getString("menu.1_16.title")));
        Set<Object> items = AxSmithingPlugin.getConfiguration().getSection("menu.1_16.items").getKeys();

        for (Object item : items) {
            Section section = AxSmithingPlugin.getConfiguration().getSection("menu.1_16.items").getSection(item.toString());
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
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_16)) return;

        if ((event.getSlot() == upgradeSlot || event.getSlot() == itemSlot) && event.getInventory().getItem(outputSlot) != null) {
            event.getInventory().setItem(outputSlot, null);
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
        }

        if (event.getSlot() == itemSlot || event.getSlot() == upgradeSlot) {
            event.setCancelled(false);
        }

        if (event.getSlot() == outputSlot && event.getCurrentItem() != null && (event.getCursor() == null || (event.getCursor() != null && event.getCursor().getType().equals(Material.AIR)))) {
            event.setCancelled(false);
        }

        if (event.getSlot() == outputSlot && event.getView().getTopInventory().getItem(outputSlot) != null && event.getView().getTopInventory().getItem(outputSlot).getType() != Material.AIR) {
            int amount;
            if (event.getInventory().getItem(itemSlot) != null) {
                amount = event.getInventory().getItem(itemSlot).getAmount() - 1;
                ItemStack item = event.getInventory().getItem(itemSlot);
                item.setAmount(amount);
                event.getInventory().setItem(itemSlot, item);
                ((Player) event.getWhoClicked()).updateInventory();
            }

            if (event.getInventory().getItem(upgradeSlot) != null) {
                amount = event.getInventory().getItem(upgradeSlot).getAmount() - 1;
                ItemStack item = event.getInventory().getItem(upgradeSlot);
                item.setAmount(amount);
                event.getInventory().setItem(upgradeSlot, item);
                ((Player) event.getWhoClicked()).updateInventory();
            }

            return;
        }

        updateGui(event.getInventory());
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_16)) return;

        if (event.getInventory().getItem(itemSlot) != null && event.getInventory().getItem(itemSlot).getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(event.getInventory().getItem(itemSlot));
        if (event.getInventory().getItem(upgradeSlot) != null && event.getInventory().getItem(upgradeSlot).getType() != Material.AIR)
            event.getPlayer().getInventory().addItem(event.getInventory().getItem(upgradeSlot));
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof SmithingTable_V1_16)) return;

        if (event.getInventory().getItem(outputSlot) != null && event.getInventory().getItem(outputSlot).getType() != Material.AIR) {
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
        Bukkit.getScheduler().runTaskLater(AxSmithingPlugin.getInstance(), t -> {
            ItemStack base = inv.getItem(itemSlot);
            ItemStack addition = inv.getItem(upgradeSlot);
            if (addition == null || addition.getType().isAir()) {
                addition = new ItemStack(Material.AIR);
            }

            if (base == null || addition.getType().isAir()) {
                base = new ItemStack(Material.AIR);
            }

            ItemStack finalAddition = addition;
            ItemStack finalBase = base;

            boolean successful = checkRecipe(inv, finalBase, finalAddition);

            if (!successful) {
                checkRecipe(inv, finalAddition, finalBase);
            }
        }, 0L);
    }

    private boolean checkRecipe(Inventory inventory, ItemStack finalBase, ItemStack finalAddition) {
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            inventory.setItem(outputSlot, new ItemStack(Material.AIR));
            Recipe recipe = recipeIterator.next();

            if (recipe instanceof SmithingRecipe smithingRecipe) {
                boolean test1 = smithingRecipe.getBase().test(finalBase);
                ItemMeta baseItemMeta = finalBase.getItemMeta();
                boolean test2 = smithingRecipe.getAddition().test(finalAddition);

                if (dontConvertWithModelData && baseItemMeta.hasCustomModelData()) {
                    return false;
                }

                if (test1 && test2) {
                    ItemStack item = smithingRecipe.getResult();
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
}
