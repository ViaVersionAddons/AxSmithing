package com.artillexstudios.axsmithing;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.YamlDocument;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.utils.Version;
import com.artillexstudios.axsmithing.command.AxSmithingCommand;
import com.artillexstudios.axsmithing.command.AxSmithingTabComplete;
import com.artillexstudios.axsmithing.gui.SmithingTable;
import com.artillexstudios.axsmithing.gui.impl.SmithingTable_V1_16;
import com.artillexstudios.axsmithing.gui.impl.SmithingTable_V1_20;
import com.artillexstudios.axsmithing.listener.InteractListener;
import com.artillexstudios.axsmithing.listener.InventoryListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class AxSmithingPlugin extends AxPlugin {
    private static YamlDocument config;
    private static SmithingTable smithingTableImpl;
    private static AxSmithingPlugin instance;
    private static boolean v1_20 = false;

    public static SmithingTable getSmithingTableImpl() {
        return smithingTableImpl;
    }

    @NotNull
    public static YamlDocument getConfiguration() {
        return config;
    }

    public static AxSmithingPlugin getInstance() {
        return instance;
    }

    public static boolean is1_20() {
        return v1_20;
    }

    // TODO: Fix dependencies
//    @Override
//    public void onLoad() {
//        DependencyManager dependencyManager = new DependencyManager();
//        dependencyManager.load(this, getResource("dependencies.json"));
//    }

    @Override
    public void enable() {
        instance = this;
        new Metrics(this, 19575);
        try {
            config = YamlDocument.create(new File(this.getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).setDetailedErrors(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initializeSmithingTableImpl();
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getPluginCommand("axsmithing").setExecutor(new AxSmithingCommand());
        Bukkit.getPluginCommand("axsmithing").setTabCompleter(new AxSmithingTabComplete());
    }

    public void reload() {
        try {
            config.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initializeSmithingTableImpl();
    }

    private void initializeSmithingTableImpl() {
        if (Bukkit.getBukkitVersion().startsWith("1.2")) {
            v1_20 = true;
            smithingTableImpl = new SmithingTable_V1_20();
        } else {
            smithingTableImpl = new SmithingTable_V1_16();
        }
    }
}