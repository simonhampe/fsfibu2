package fs.fibu2.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.birosoft.liquid.LiquidLookAndFeel;

import fs.fibu2.data.format.JournalExportLoader;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.filter.FilterLoader;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.render.JournalModuleLoader;
import fs.xml.PolyglotStringTable;

/**
 * This class is responsible for initializing and starting fsfibu2. The expected initialization files (which are  all optional) are: <br>
 * - frameworkConfigurator.xml: This file configurates fsframework, mainly the path to fsframework. If the file does not exist, the user is prompted to
 * give the appropriate directory. The application will not start, if the user does not give a valid directory.<br>
 * - loggerConfigurator: This file configurates logging. It is supposed to be in a format as specified by {@link PropertyConfigurator}. If it is not present, 
 * a basic configuration is used <br>
 * If there are any class files in the directories accounts/, filters/, modules/, exports/, the corresponding classes must be declared in the packages fs.fibu2.account, fs.fibu2.filter,
 * fs.fibu2.module, fs.fibu2.export so that the class loader can load them<br> 
 * All files should be located in the working directory.<br>
 * There is one single instance of this class for each VM.
 * @author Simon Hampe
 *
 */
public class Fsfibu2 {

	private static Logger logger = Logger.getLogger("fs.fibu2");
	
	private static Fsfibu2 global_instance;
	
	public final static String version = "2.0 Beta";
	
	private static MainFrame frame;
	
	/**
	 * Initializes and starts fsfibu2. Command line arguments are ignored.
	 */
	public static void main(String[] args) {
		Locale.setDefault(Locale.GERMANY);
		try {
			UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
			LiquidLookAndFeel.setStipples(false);
			LiquidLookAndFeel.setShowTableGrids(true);
			LiquidLookAndFeel.setToolbarFlattedButtons(false);
			LiquidLookAndFeel.setDefaultRowBackgroundMode(false);
		} catch (Exception e) {
			logger.error("Could not set look and feel. Resetting to default");
		}
		if(global_instance == null) {
			global_instance = new Fsfibu2();
			global_instance.init();
		}
	}
	
	//A private constructor to maintain singleton status
	private Fsfibu2() {}
	
	/**
	 * Initializes and actually starts fsfibu2
	 */
	private void init() {
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
		logger.addAppender(SplashScreenManager.getSplashScreenAppender());
		
		//Now configure fsframework
		try {
			FrameworkLoader.loadFramework();
		}
		catch(UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(null, "Could not find fsframework. fsfibu2 will not be started", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		//Load language
		logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadinglanguage"));
		String languageID = Preferences.userRoot().node("fsfibu2").node("lang").get("lang", "en");
		PolyglotStringTable.setGlobalLanguageID(languageID);
		
		//Now load all user classes
		try {
			URL[] accountURL =  {new URL("file://accounts/")};
				CustomLoader accountLoader = new CustomLoader(accountURL);
			URL[] filterURL = {new URL("file://filters/")};
				CustomLoader filterLoader = new CustomLoader(filterURL);
			URL[] moduleURL = {new URL("file://modules/") };
				CustomLoader moduleLoader = new CustomLoader(moduleURL);
			URL[] exportURL = {new URL("file://exports/")};
				CustomLoader exportLoader = new CustomLoader(exportURL);
			//Accounts
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadingaccounts"));
			File accountDir = new File("accounts/");
			if(accountDir.exists()) {
				for(File a : accountDir.listFiles()) {
					String name = a.getName();
					if(name.endsWith(".class")) {
						String subname = name.substring(0, name.length()-6);
						try {
							Class<?> c = accountLoader.getClassFromFile("fs.fibu2.account." + subname,a);
							AccountLoader.loadAccount(c);
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.accountnotloaded",subname,e.getMessage()));
						}
						catch (Throwable e) 
						{
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name) + ": " + e.getLocalizedMessage());
						}
					}
				}
			}
			//Filters
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadingfilters"));
			File filterDir = new File("filters/");
			if(filterDir.exists()) {
				for(File a : filterDir.listFiles()) {
					String name = a.getName();
					if(name.endsWith(".class")) {
						String subname = name.substring(0, name.length()-6);
						try {
							Class<?> c = filterLoader.getClassFromFile("fs.fibu2.filter." + subname,a);
							FilterLoader.loadFilter(c);
						} catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.filternotloaded",subname,e.getMessage()));
						}
						catch (Throwable e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name) + ": " + e.getLocalizedMessage());
						}
					}
				}
			}
			//Modules
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadingmodules"));
			File moduleDir = new File("modules/");
			if(moduleDir.exists()) {
				for(File a : moduleDir.listFiles()) {
					String name = a.getName();
					if(name.endsWith(".class")) {
						String subname = name.substring(0, name.length()-6);
						try {
							Class<?> c = moduleLoader.getClassFromFile("fs.fibu2.module." + subname,a);
							JournalModuleLoader.loadModule(c);
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.modulenotloaded",subname,e.getMessage()));
						}
						catch (Throwable e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name) + ": " + e.getLocalizedMessage());
						}
					}
				}
			}
			//Exports
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadingexports"));
			File exportDir = new File("exports/");
			if(exportDir.exists()) {
				for(File a : exportDir.listFiles()) {
					String name = a.getName();
					if(name.endsWith(".class")) {
						String subname = name.substring(0, name.length()-6);
						try {
							Class<?> c = exportLoader.getClassFromFile("fs.fibu2.export." + subname,a);
							JournalExportLoader.loadExport(c);
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.exportnotloaded",subname,e.getMessage()));
						}
						catch (Throwable e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name));
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			//Will not happen
		}
		
		//Create MainFrame
		frame = new MainFrame();
		frame.pack();
		frame.setVisible(true);
		JFrame.setDefaultLookAndFeelDecorated(false);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
	
	/**
	 * @return The fsfibu2 application main frame. This will be null as long as application initialization is not finished, so you should not call
	 * this method in code which concerns graphical initialization
	 */
	public static MainFrame getFrame() {
		return frame;
	}

	/**
	 * A custom class loader for loading plugins from .class files
	 * @author Simon Hampe
	 *
	 */
	private class CustomLoader extends URLClassLoader {

		public CustomLoader(URL[] urls) {
			//Add all jar files in pluginlibs/
			super(urls);
			File directory = new File("pluginlibs/");
			File[] jarfiles = directory.listFiles();
			for(File f : jarfiles) {
				if(f.getName().endsWith(".jar")) {
					try {
						addURL(f.toURI().toURL());
					} catch (MalformedURLException e) {
						//Will not happen
					}
				}
			}
		}
		
		/**
		 * Loads a class from a given file
		 * @param name The fully qualified name of the class
		 * @param f The .class file
		 * @return The class object
		 * @throws IOException - If anything goes wrong 
		 */
		public Class<?> getClassFromFile(String name, File f) throws IOException {
			byte[] b = new byte[(int)f.length()];
			FileInputStream stream = new FileInputStream(f);
			stream.read(b);
			return defineClass(name, b, 0, b.length);
		}
		
	}
	
}
