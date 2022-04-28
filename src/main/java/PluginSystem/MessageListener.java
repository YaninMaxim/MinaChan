package PluginSystem;

public interface MessageListener {
	
	void handleMessage(String sender, String tag, Object data);
	
}
