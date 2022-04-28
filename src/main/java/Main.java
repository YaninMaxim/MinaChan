import PluginSystem.PluginManager;

public class Main {
    public static void main(String[] args) {
        PluginManager pluginManager = PluginManager.getInstance();
        pluginManager.initialize();
        pluginManager.loadPluginByPackageName("PluginGroovySupport");
        pluginManager.loadPluginByPackageName("PluginGUI");
        pluginManager.tryLoadPluginByName("random_phrases");
    }
}
