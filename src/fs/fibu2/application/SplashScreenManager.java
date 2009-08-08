package fs.fibu2.application;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class manages the splash screen. It can be used as an {@link Appender} for logging while initializing fsfibu2.  
 * @author Simon Hampe
 *
 */
public final class SplashScreenManager {

	private static Appender appender = new AppenderSkeleton() {

		@Override
		protected void append(LoggingEvent arg0) {
			if(SplashScreen.getSplashScreen() != null && SplashScreen.getSplashScreen().isVisible()) {
				setText(arg0.getMessage().toString());
			}
		}

		@Override
		public void close() {
			//Ignore
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}
		
	};
	
	private SplashScreenManager() {}
	
	public static Appender getSplashScreenAppender() {
		return appender;
	}
	
	/**
	 * Inserts a text in the logging bar of the fsfibu2 splash screen (if it is visible) and also prints the current version in
	 * the upper left corner
	 */
	public static void setText(String data) {
		SplashScreen splash = SplashScreen.getSplashScreen();
		if(splash != null) {
			//Clear splash image
			Graphics2D g = splash.createGraphics();
			Dimension size = splash.getSize();
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, size.width, size.height);
			//Draw new text
			g.setPaintMode();
			g.setColor(Color.BLACK);
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			//If the string is too long, replace text by '...'
			if(g.getFontMetrics().getStringBounds(data, g).getWidth() > 589) {
				while(g.getFontMetrics().getStringBounds(data, g).getWidth() > 589) {
					data = data.substring(0, data.length()-1);
				}
				data = data.substring(0, data.length()-3);
				data = data.concat("...");
			}
			//Display it
			g.drawString(data, 10, 265);
			//Draw version
			g.setFont(g.getFont().deriveFont(Font.PLAIN));
			g.setColor(Color.WHITE);
			g.drawString(Fsfibu2.version, 5, g.getFont().getSize() + 5);
			splash.update();
		}

		
	}
	
}
