package fs.fibu2.view.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fs.fibu2.module.BilancialModule;
import fs.fibu2.module.ChartModule;
import fs.fibu2.module.ExportModule;
import fs.fibu2.module.FilterModule;
import fs.fibu2.module.OverviewModule;

/**
 * This class handles loading of {@link JournalModule} objects by ID. It also supports dynamic loading of JournalModule classes.
 * @author Simon Hampe
 *
 */
public class JournalModuleLoader {

	//The map of all class objects associated to their IDs
	private static HashMap<String, Class<? extends JournalModule>> moduleMap = new HashMap<String, Class<? extends JournalModule>>();
	//The set of all listeners
	private static HashSet<ChangeListener> listenerList = new HashSet<ChangeListener>();
	//An ordered list of all module IDs which were available from beginning (i.e. were added in the static code below and not dynamically)
	private static Vector<String> defaultModules = new Vector<String>();
	
	//Init all default modules 
	static {	
		setModule("ff2module_overview", OverviewModule.class);
			defaultModules.add("ff2module_overview");
		setModule("ff2module_filter", FilterModule.class);
			defaultModules.add("ff2module_filter");
		setModule("ff2module_bilancial",BilancialModule.class);
			defaultModules.add("ff2module_bilancial");
		setModule("ff2module_chart", ChartModule.class);
			defaultModules.add("ff2module_chart");
		setModule("ff2module_export", ExportModule.class);
			defaultModules.add("ff2module_export");
	}
	
	/**
	 * Creates an instance of the module associated to this id and returns it
	 * @throws IllegalArgumentException - If there is no module class for this id
	 * @throws InstantiationException - If the nullary constructor or the class can not be accessed 
	 */
	public static JournalModule getModule(String id) throws IllegalArgumentException, InstantiationException{
		if(id == null) throw new NullPointerException("Null id invalid for module creation");
		try {
			return (moduleMap.get(id).newInstance());
		}
		catch(NullPointerException ne)  {
			throw new IllegalArgumentException("No module class found for id '" + id + "'");
		} catch (IllegalAccessException e) {
			throw new InstantiationException(e.getMessage());
		} 
	}
	
	/**
	 * Sets the module class for the given id. If there already is a class for this id, it is overwritten. If module == null, the corresponding class
	 * is removed. If id == null, this call is ignored
	 */
	public static void setModule(String id, Class<? extends JournalModule> module) {
		if(id == null) return;
		if(module == null) moduleMap.remove(id);
		else moduleMap.put(id, module);
		fireStateChanged(new ChangeEvent(id));
	}
	
	/**
	 * This tries to create a {@link JournalModule} instance from the given class and will then create a mapping for its id. If there is already a mapping for this
	 * id, this call is ignored
	 * @throws UnsupportedOperationException - If any error occurs during instantiation
	 */
	public static void loadModule(Class<?> moduleClass) {
		try {
			JournalModule f = (JournalModule) moduleClass.newInstance();
			if(!moduleMap.containsKey(f.getID())) {
				moduleMap.put(f.getID(), f.getClass());
				fireStateChanged(new ChangeEvent(f.getID()));
			}
		}
		catch(Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * @return A list of all ids for which this loader has a mapping
	 */
	public static HashSet<String> getModuleIDs() {
		return new HashSet<String>(moduleMap.keySet());
	}
	
	public static Vector<String> getDefaultModules() {
		return new Vector<String>(defaultModules);
	}

	// LISTENER METHODS ********************************
	// *************************************************
	
	/**
	 * Adds a listener to this class. Listeners are notified, whenever a filter is added to or removed from the loader. 
	 * The change event contains the corresponding filter id as source 
	 */
	public static void addChangeListener(ChangeListener l) {
		if(l != null) listenerList.add(l);
	}
	
	/**
	 * Removes l as a listener from this class
	 */
	public static void removeChangeListener(ChangeListener l) {
		listenerList.remove(l);
	}
	
	protected static void fireStateChanged(ChangeEvent e) {
		for(ChangeListener l : listenerList) l.stateChanged(e);
	}
	
}
