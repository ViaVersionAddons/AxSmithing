package com.artillexstudios.axsmithing.command;

import com.artillexstudios.axsmithing.AxSmithingPlugin;
import com.artillexstudios.axsmithing.utils.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AxSmithingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("axsmithing.reload")) {
            sender.sendMessage(StringUtils.format(AxSmithingPlugin.getConfiguration().getString("messages.prefix") + AxSmithingPlugin.getConfiguration().getString("messages.no-permission")));
            return true;
        }

        String took = AxSmithingPlugin.getInstance().reload();
        sender.sendMessage(StringUtils.format(AxSmithingPlugin.getConfiguration().getString("messages.prefix") + AxSmithingPlugin.getConfiguration().getString("messages.reload").replace("%time%", took)));
        return true;
    }
}
