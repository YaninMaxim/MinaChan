package PluginSystem;

import java.nio.file.Path;

public interface PluginLoader {
	
	boolean matchPath(Path path);
	
	void loadByPath(Path path) throws Throwable;
	
}
