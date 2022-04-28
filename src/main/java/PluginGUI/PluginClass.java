package PluginGUI;

import PluginSystem.Plugin;
import PluginSystem.PluginProxy;

public class PluginClass implements Plugin {
	
	private MainWindow mainWindow = null;
	
	@Override
	public boolean initialize(PluginProxy pluginProxy) {
		try {
			javax.swing.SwingUtilities.invokeAndWait(() -> {
				/* try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				} */
				mainWindow = new MainWindow();
				mainWindow.initialize(pluginProxy);
				mainWindow.setVisible(true);
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void unload() {
		try {
			mainWindow.dispose();
			mainWindow = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
