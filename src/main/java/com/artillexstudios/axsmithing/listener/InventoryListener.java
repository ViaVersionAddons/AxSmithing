package com.artillexstudios.axsmithing.listener;

import com.artillexstudios.axsmithing.AxSmithingPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClickEvent(@NotNull final InventoryClickEvent event) {
        AxSmithingPlugin.getSmithingTableImpl().handleClick(event);
    }

    @EventHandler
    public void onInventoryDragEvent(@NotNull final InventoryDragEvent event) {
        AxSmithingPlugin.getSmithingTableImpl().handleDrag(event);
    }

    @EventHandler
    public void onInventoryCloseEvent(@NotNull final InventoryCloseEvent event) {
        AxSmithingPlugin.getSmithingTableImpl().handleClose(event);
    }
}
