package fs.fibu2.application;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class is responsible for initializing and starting fsfibu2. The expected initialization files (which are  all optional) are: <br>
 * - frameworkConfigurator.xml: This file configurates fsframework, mainly the path to fsframework. If the file does not exist, the user is prompted to
 * give the appropriate directory. The application will not start, if the user does not give a valid directory.<br>
 * - loggerConfigurator: This file configurates logging. It is supposed to be in a format as specified by {@link PropertyConfigurator}. If it is not present, 
 * a basic configuration is used <br>
 * All files should be located in the working directory.
 * @author Simon Hampe
 *
 */
public class Fsfibu2 {

	private static Logger logger = Logger.getLogger("fs.fibu2");
	
	/**
	 * Initializes and starts fsfibu2. Command line arguments are ignored.
	 */
	public static void main(String[] args) {
		//First configure logging
		File f = new File("loggerConfigurator");
		if(f.exists()) {
			PropertyConfigurator.configure(f.getAbsolutePath());
			logger.info("Logging configured via " + f.getName());
		}
		else {
			BasicConfigurator.configure();
			logger.info("No logging configuration file found. Basic configuration used.");
		}
		
		//Now configure fsframework
		try {
			FrameworkLoader.loadFramework();
		}
		catch(UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(null, "Could not find fsframework. fsfibu2 will not be started", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		//Now load all user classes
		
		
	}

}
