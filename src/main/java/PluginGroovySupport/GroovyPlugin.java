package PluginGroovySupport;

import PluginSystem.MessageListener;
import PluginSystem.Plugin;
import PluginSystem.PluginProxy;
import PluginSystem.ResponseListener;
import groovy.lang.Script;
import java.util.ArrayList;
import java.util.List;

public abstract class GroovyPlugin extends Script implements Plugin {
    private PluginProxy pluginProxy = null;
    private List<Runnable> cleanupHandlers = new ArrayList<>();
    @Override
    public boolean initialize(PluginProxy pluginProxy) {
        this.pluginProxy = pluginProxy;
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public void unload() {
        for (Runnable runnable : cleanupHandlers) {
            runnable.run();
        }
    }
    protected void sendMessage(String tag, Object data) {
        pluginProxy.sendMessage(tag, data);
    }
    protected void sendMessage(String tag, Object data, ResponseListener responseListener) {
        pluginProxy.sendMessage(tag, data, responseListener);
    }
    protected void addMessageListener(String tag, MessageListener listener) {
        pluginProxy.addMessageListener(tag, listener);
    }
    protected void removeMessageListener(String tag, MessageListener listener) {
        pluginProxy.removeMessageListener(tag, listener);
    }
    protected void addCleanupHandler(Runnable handler) {
        cleanupHandlers.add(handler);
    }
}
