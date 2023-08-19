package com.artillexstudios.axsmithing.listener;

import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(@NotNull final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SMITHING_TABLE) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        if (AxSmithingPlugin.is1_20()) {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) == ProtocolVersion.v1_20.getVersion()) {
                return;
            }
        } else {
            if (Via.getAPI().getPlayerVersion(event.getPlayer()) != ProtocolVersion.v1_20.getVersion()) {
                return;
            }
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.getPlayer().closeInventory();
        AxSmithingPlugin.getSmithingTableImpl().open(event.getPlayer());
    }
}
