package com.artillexstudios.axsmithing.dependency;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class DependencyManager {

    public void load(JavaPlugin plugin, InputStream stream) {
        BukkitLibraryManager manager = new BukkitLibraryManager(plugin, "libraries");
        manager.addMavenCentral();
        manager.addMavenLocal();
        manager.addJitPack();

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(stream));

        Map<?, ?> read = gson.fromJson(reader, Map.class);

        Object rep = read.get("repositories");
        Object dep = read.get("dependencies");

        if (rep instanceof ArrayList<?> repositories) {
            for (Object repository : repositories) {
                if (repository instanceof LinkedTreeMap<?,?> repos) {
                    String url = (String) repos.get("url");
                    manager.addRepository(url);
                }
            }
        }

        if (dep instanceof ArrayList<?> libraries) {
            for (Object o : libraries) {
                if (o instanceof LinkedTreeMap<?,?> map) {
                    String group = (String) map.get("group");
                    String artifact = (String) map.get("artifact");
                    String version = (String) map.get("version");

                    Library.Builder builder = Library.builder().groupId(group).artifactId(artifact).version(version);

                    if (map.containsKey("relocate")) {
                        LinkedTreeMap<?, ?> relocate = (LinkedTreeMap<?,?>) map.get("relocate");
                        builder.relocate((String) relocate.get("from"), (String) relocate.get("to"));
                    }

                    if (map.containsKey("isolated")) {
                        builder.isolatedLoad((Boolean) map.get("isolated"));
                    }

                    manager.loadLibrary(builder.build());
                }
            }
        }
    }
}
