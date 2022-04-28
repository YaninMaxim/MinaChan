package PluginGroovySupport;


import PluginSystem.Plugin;
import PluginSystem.PluginLoader;
import PluginSystem.PluginManager;
import PluginSystem.PluginProxy;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;

public class PluginClass implements Plugin, PluginLoader {

    @Override
    public boolean initialize(PluginProxy pluginProxy) {
        PluginManager.getInstance().registerPluginLoader(this);
        return true;
    }

    @Override
    public void unload() {
        PluginManager.getInstance().unregisterPluginLoader(this);
    }

    @Override
    public boolean matchPath(Path path) {
        if (Files.isDirectory(path)) {
            path = path.resolve("plugin.groovy");
            if (Files.isReadable(path)) {
                return true;
            }
        }
        return path.getFileName().toString().endsWith(".groovy");
    }

    @Override
    public void loadByPath(Path path) throws Throwable {
        String id = path.getFileName().toString();
        if (Files.isDirectory(path)) {
            path = path.resolve("plugin.groovy");
        }
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("PluginGroovySupport.GroovyPlugin");
        compilerConfiguration.setClasspath(path.getParent().toString());
        GroovyShell groovyShell = new GroovyShell(compilerConfiguration);
        Script script = groovyShell.parse(path.toFile());
        GroovyPlugin plugin = (GroovyPlugin) script;
        PluginManager.getInstance().initializePlugin(id, plugin);
    }

}
