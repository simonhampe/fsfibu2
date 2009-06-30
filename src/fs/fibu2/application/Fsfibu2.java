package fs.fibu2.application;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.filter.FilterLoader;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.JournalModuleLoader;

/**
 * This class is responsible for initializing and starting fsfibu2. The expected initialization files (which are  all optional) are: <br>
 * - frameworkConfigurator.xml: This file configurates fsframework, mainly the path to fsframework. If the file does not exist, the user is prompted to
 * give the appropriate directory. The application will not start, if the user does not give a valid directory.<br>
 * - loggerConfigurator: This file configurates logging. It is supposed to be in a format as specified by {@link PropertyConfigurator}. If it is not present, 
 * a basic configuration is used <br>
 * If there are any class files in the directories accounts/, filters/, modules/, the corresponding classes must be declared in the packages fs.fibu2.account, fs.fibu2.filter,
 * fs.fibu2.module, so that the class loader may <br> 
 * All files should be located in the working directory.<br>
 * There is one single instance of this class for each VM.
 * @author Simon Hampe
 *
 */
public class Fsfibu2 {

	private static Logger logger = Logger.getLogger("fs.fibu2");
	
	private static Fsfibu2 global_instance;
	
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
		logger.addAppender(SplashScreenManager.getSplashScreenAppender());
		
		//Now configure fsframework
		try {
			FrameworkLoader.loadFramework();
		}
		catch(UnsupportedOperationException e) {
			JOptionPane.showMessageDialog(null, "Could not find fsframework. fsfibu2 will not be started", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		//Now load all user classes
		try {
			URL[] accountURL =  {new URL("file://accounts/")};
				URLClassLoader accountLoader = new URLClassLoader(accountURL);
			URL[] filterURL = {new URL("file://filters/")};
				URLClassLoader filterLoader = new URLClassLoader(filterURL);
			URL[] moduleURL = {new URL("file://modules/")};
				URLClassLoader moduleLoader = new URLClassLoader(moduleURL);
			//Accounts
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.loadingaccounts"));
			File accountDir = new File("accounts/");
			if(accountDir.exists()) {
				for(File a : accountDir.listFiles()) {
					String name = a.getName();
					if(name.endsWith(".class")) {
						String subname = name.substring(0, name.length()-6);
						try {
							Class<?> c = accountLoader.loadClass("fs.fibu2.account." + subname);
							AccountLoader.loadAccount(c);
						} catch (ClassNotFoundException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name));
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.accountnotloaded",subname,e.getMessage()));
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
							Class<?> c = filterLoader.loadClass("fs.fibu2.filter." + subname);
							FilterLoader.loadFilter(c);
						} catch (ClassNotFoundException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name));
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.filternotloaded",subname,e.getMessage()));
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
							Class<?> c = moduleLoader.loadClass("fs.fibu2.module." + subname);
							JournalModuleLoader.loadModule(c);
						} catch (ClassNotFoundException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.classnotfound",name));
						}
						catch(UnsupportedOperationException e) {
							logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.modulenotloaded",subname,e.getMessage()));
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			//Will not happen
		}
	}
	
	public Fsfibu2() {
		
	}

}
