package com.artillexstudios.axsmithing.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface SmithingTable {

    void open(Player player);

    void handleClick(InventoryClickEvent event);

    void handleClose(InventoryCloseEvent event);

    void handleDrag(InventoryDragEvent event);
}
