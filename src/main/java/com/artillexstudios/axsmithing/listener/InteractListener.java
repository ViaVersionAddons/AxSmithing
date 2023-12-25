package com.artillexstudios.axsmithing.listener;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Version;
import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.viaversion.viaversion.api.Via;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class InteractListener implements Listener {

    @EventHandler
    public void onInventoryOpenEvent(@NotNull final InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.SMITHING) return;

        if (AxSmithingPlugin.is1_20()) {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) >= Version.v1_20_1.protocolId && !AxSmithingPlugin.getConfiguration().getBoolean("menu.1_20.force-for-1_20-clients")) {
                return;
            }
        } else {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) != Version.v1_20_1.protocolId && Via.getAPI().getPlayerVersion(event.getPlayer()) != Version.v1_20_2.protocolId) {
                return;
            }
        }

        event.getPlayer().closeInventory();
        Scheduler.get().runLater(t -> {
            AxSmithingPlugin.getSmithingTableImpl().open((Player) event.getPlayer());
        }, 1);
    }
}
