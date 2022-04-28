import PluginSystem.PluginManager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.initialize();
        pluginManager.loadPluginByPackageName("PluginGroovySupport");
        pluginManager.loadPluginByPackageName("PluginGUI");
        try{
            Path pluginsDir = pluginManager.pluginsDir;
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(pluginsDir);
            for (Path path : directoryStream) {
                pluginManager.tryLoadPluginByPath(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
