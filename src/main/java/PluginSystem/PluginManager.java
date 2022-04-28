package PluginSystem;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PluginManager {
	
	private static final PluginManager instance = new PluginManager();
	private final Map<String, PluginProxy> plugins = new HashMap<>();
	private final Map<String, Set<MessageListener>> messageListeners = new HashMap<>();
	private final List<PluginLoader> loaders = new ArrayList<>();
	private static OutputStream logStream = null;
	
	private PluginManager() {
	}
	
	public static PluginManager getInstance() {
		return instance;
	}
	
	public void initialize() {
		loadPluginByClass(CorePlugin.class);
	}
	
	public boolean initializePlugin(String id, Plugin plugin) {
		if (!plugins.containsKey(id)) {
			PluginProxy pluginProxy = new PluginProxy(plugin);
			if (pluginProxy.initialize(id)) {
				plugins.put(id, pluginProxy);
				log("Registered plugin: " + id);
				sendMessage("core", "core-events:plugin-load", id);
				return true;
			}
		}
		return false;
	}
	
	void unregisterPlugin(PluginProxy pluginProxy) {
		plugins.remove(pluginProxy.getId());
		log("Unregistered plugin: " + pluginProxy.getId());
		sendMessage("core", "core-events:plugin-unload", pluginProxy.getId());
	}
	
	void sendMessage(String sender, String tag, Object data) {
		Set<MessageListener> listeners = messageListeners.getOrDefault(tag, null);
		if (listeners != null) {
			for (MessageListener listener : listeners) {
				listener.handleMessage(sender, tag, data);
			}
		}
	}
	
	void registerMessageListener(String tag, MessageListener listener) {
		Set<MessageListener> listeners = messageListeners.getOrDefault(tag, null);
		if (listeners == null) {
			listeners = new HashSet<>();
			messageListeners.put(tag, listeners);
		}
		if (tag.equals("core-events:plugin-load")) {
			for (String id : plugins.keySet()) {
				listener.handleMessage("core", "core-events:plugin-load", id);
			}
		}
		listeners.add(listener);
	}
	
	void unregisterMessageListener(String tag, MessageListener listener) {
		Set<MessageListener> listeners = messageListeners.getOrDefault(tag, null);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.size() == 0) {
				messageListeners.remove(tag);
			}
		}
	}
	
	public synchronized void registerPluginLoader(PluginLoader loader) {
		loaders.add(loader);
	}
	
	public synchronized void unregisterPluginLoader(PluginLoader loader) {
		loaders.remove(loader);
	}
	
	private PluginProxy getPlugin(String name) {
		return plugins.getOrDefault(name, null);
	}
	
	public boolean unloadPlugin(String name) {
		PluginProxy plugin = getPlugin(name);
		if (plugin != null) {
			plugin.unload();
			return true;
		}
		return false;
	}
	
	public boolean loadPluginByClass(Class cls) {
		try {
			Object plugin = cls.getDeclaredConstructor().newInstance();
			if (plugin instanceof Plugin) {
				String packageName = cls.getPackage().getName();
				return initializePlugin(packageName, (Plugin) plugin);
			}
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	
	public boolean loadPluginByClassName(String className) {
		try {
			Class cls = getClass().getClassLoader().loadClass(className);
			return loadPluginByClass(cls);
		} catch (ClassNotFoundException e) {
			log(e.getMessage());
			return false;
		}
	}
	
	public boolean loadPluginByPackageName(String packageName) {
		return loadPluginByClassName(packageName + ".PluginClass");
	}
	
	public synchronized void loadPluginByPath(Path path) throws Throwable {
		for (PluginLoader loader : loaders) {
			if (loader.matchPath(path)) {
				loader.loadByPath(path);
				return;
			}
		}
		throw new Exception("Could not match loader for plugin " + path.toString());
	}
	
	public boolean tryLoadPluginByPath(Path path) {
		try {
			loadPluginByPath(path);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void loadPluginByName(String name) throws Throwable {
		Path jarPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		Path pluginsDirPath;
		if (Files.isDirectory(jarPath)) {
			pluginsDirPath = jarPath.resolve("../../../../plugins");
		} else {
			pluginsDirPath = jarPath.getParent().resolve("../plugins");
		}
		loadPluginByPath(pluginsDirPath.resolve(name));
	}
	
	public boolean tryLoadPluginByName(String name) {
		try {
			loadPluginByName(name);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	void quit() {
		List<PluginProxy> pluginsToUnload = new ArrayList<>();
		for (Map.Entry<String, PluginProxy> entry : plugins.entrySet()) {
			if (!entry.getKey().equals("core")) {
				pluginsToUnload.add(entry.getValue());
			}
		}
		for (PluginProxy plugin : pluginsToUnload) {
			plugin.unload();
		}
		pluginsToUnload.clear();
		getPlugin("core").unload();
		System.exit(0);
	}
	/* Logging */

	/** Log info to file and console.
	 * You cannot call this method, use your plugin's proxy. **/

	static void log(String id, String message){
		log(id,message,LoggerLevel.INFO);
	}

	static void log(String id, String message,LoggerLevel level) {
		String text = id + ": " + message;

		if (level.equals(LoggerLevel.ERROR)){
			System.err.println(text);
			writeStringToLogStream(text);
		} else if (level.getValue() >= LoggerLevel.WARN.getValue() && level.getValue() <= LoggerLevel.TRACE.getValue()){
			System.out.println(text);
			writeStringToLogStream(text);
		}
	}

	static void writeStringToLogStream(String text){
		if (logStream != null) {
			try {
				logStream.write((text + "\n").getBytes("UTF-8"));
			} catch (IOException e) {
				logStream = null;
				log(e);
			}
		}
	}


	static void log(String id, String message, List<Object> stacktrace) {
		log(id, message,LoggerLevel.ERROR);
		for (Object line : stacktrace) {
			log(id, "   at " + line.toString(),LoggerLevel.ERROR);
		}
	}

	static void log(Throwable e) {
		log("core", e);
	}

	static void log(String message) {
		log("core", message);
	}

	static void log(String id, Throwable e) {
		while (e.getCause() != null) e = e.getCause();

		List<Object> stackTrace = new ArrayList<>(Arrays.asList(e.getStackTrace()));
		log(id, e.getClass().getSimpleName() + ":  " + e.getMessage(), stackTrace);

		Map error = new HashMap();
		error.put("class", e.getClass().getSimpleName());
		error.put("message", e.getMessage());
		error.put("stacktrace", stackTrace);

		getInstance().sendMessage(id, "core-events:error", error);
	}
}
