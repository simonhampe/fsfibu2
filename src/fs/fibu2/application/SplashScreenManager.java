package fs.fibu2.application;

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
	 * Inserts a text in the logging bar of the fsfibu2 splash screen (if it is visible)
	 * @param data
	 */
	public static void setText(String data) {
		//TODO: Write
	}
	
}
