package PluginGUI;


import PluginSystem.PluginManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.file.Path;

class CharacterWidget extends JPanel implements MouseListener, MouseMotionListener {
	
	private final MainWindow mainWindow;
	private Image characterImage;
	private Point clickPos;
	private boolean dragging;
	private boolean flip;
	private Skin currentSkin;
	private String currentImageName = "normal";
	
	CharacterWidget(MainWindow mainWindow) {
		super();
		this.mainWindow = mainWindow;
		setBackground(new Color(0, 0, 0, 0));
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (characterImage != null) {
			if (g instanceof Graphics2D) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			}
			g.drawImage(characterImage, 0, 0, this);
		} else {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
			g.setColor(Color.GRAY);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			g.drawLine(0, 0, getWidth() - 1, getHeight() - 1);
			g.drawLine(0, getHeight() - 1, getWidth() - 1, 0);
			g.setColor(Color.BLACK);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 24));
			FontMetrics metrics = g.getFontMetrics();
			String text = "No image";
			int x = (getWidth() - metrics.stringWidth(text)) / 2;
			int y = (getHeight() - metrics.getHeight()) / 2;
			g.drawString(text, x, y);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		String event = "gui-events:character-left-click";
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
			event = "gui-events:character-double-click";
		} else if (SwingUtilities.isRightMouseButton(e)) {
			event = "gui-events:character-right-click";
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			event = "gui-events:character-middle-click";
		}
		mainWindow.getPluginProxy().sendMessage(event, null);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		clickPos = e.getLocationOnScreen();
		if (SwingUtilities.isLeftMouseButton(e)) {
			dragging = true;
		} else if (SwingUtilities.isRightMouseButton(e)) {
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.add(mainWindow.optionsAction);
			popupMenu.addSeparator();
			if (!mainWindow.extraActions.isEmpty()) {
				for (Action action : mainWindow.extraActions) {
					popupMenu.add(action);
				}
				popupMenu.addSeparator();
			}
			popupMenu.add(mainWindow.quitAction);
			popupMenu.setLightWeightPopupEnabled(false);
			popupMenu.show(this, e.getX(), e.getY());
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			dragging = false;
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		int dx = e.getXOnScreen() - clickPos.x;
		int dy = e.getYOnScreen() - clickPos.y;
		if (dragging) {
			mainWindow.setPosition(new Point(
					mainWindow.getX() + dx,
					mainWindow.getY() + dy
			));
		}
		clickPos = e.getLocationOnScreen();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	void setImage(String name) {
		currentImageName = name;
		characterImage = (currentSkin != null) ? currentSkin.getImage(name) : null;
		if (characterImage != null) {
			setPreferredSize(new Dimension(characterImage.getWidth(this),
					characterImage.getHeight(this)));
		} else {
			setPreferredSize(new Dimension(300, 300));
		}
		repaint();
		mainWindow.updateSizes();
	}
	
	void setSkin(Skin skin) {
		currentSkin = skin;
		setImage(currentImageName);
		if (skin.isBuiltin()) {
			MainWindow.properties.setProperty("skin.builtin", "true");
			MainWindow.properties.setProperty("skin.name", skin.getName());
		} else {
			MainWindow.properties.setProperty("skin.builtin", "false");
			MainWindow.properties.setProperty("skin.name", skin.getBasePath().toString());
		}
	}
	
	void loadSkin(Path path) {
		setSkin(new Skin(path));
	}
	
	void loadBuiltinSkin(String name) {
		setSkin(new Skin(PluginManager.getInstance().assetDir.resolve("skins/"+name), true));
	}
	
	Skin getCurrentSkin() {
		return currentSkin;
	}
	
}
