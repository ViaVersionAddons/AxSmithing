package com.artillexstudios.axsmithing.listener;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Version;
import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.viaversion.viaversion.api.Via;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(@NotNull final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SMITHING_TABLE) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (!AxSmithingPlugin.getConfiguration().getBoolean("listen-to-interact-event")) return;

        if (AxSmithingPlugin.is1_20()) {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) >= 762 && !AxSmithingPlugin.getConfiguration().getBoolean("menu.1_20.force-for-1_20-clients")) {
                return;
            }
        } else {
            if (!AxSmithingPlugin.getConfiguration().getBoolean("menu.1_16.force")) {
                return;
            }
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.getPlayer().closeInventory();
        AxSmithingPlugin.getSmithingTableImpl().open(event.getPlayer());
    }

    @EventHandler
    public void onInventoryOpenEvent(@NotNull final InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.SMITHING) return;

        if (AxSmithingPlugin.is1_20()) {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) >= 762 && !AxSmithingPlugin.getConfiguration().getBoolean("menu.1_20.force-for-1_20-clients")) {
                return;
            }
        } else {
            if (!AxSmithingPlugin.getConfiguration().getBoolean("menu.1_16.force")) {
                return;
            }
        }

        event.getPlayer().closeInventory();
        Scheduler.get().runLater(t -> {
            AxSmithingPlugin.getSmithingTableImpl().open((Player) event.getPlayer());
        }, 1);
    }
}
