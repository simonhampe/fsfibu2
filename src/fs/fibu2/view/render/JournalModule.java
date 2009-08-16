package fs.fibu2.view.render;

import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JPanel;

import fs.fibu2.data.model.Journal;

/**
 * A journal module is a generic component for editing / displaying an fsfibu2 journal. A journal module might be for
 * example a tabbed pane for filter views of a journal or just a panel with several diagrams. When fsfibu2 is started, for each
 * module registered with the {@link JournalModuleLoader} or corresponding class file in the modules/ folder, one tab is added to the main tabbed view.
 * Each class implementing this interface should have a nullary constructor. 
 * @author Simon Hampe
 *
 */
public interface JournalModule {
	
	/**
	 * @return The icon for the tab in the tabbed view. Can be null.
	 */
	public Icon getTabViewIcon();
	
	/**
	 * @return The string to display as name of the tab. Can be null.
	 */
	public String getTabViewName();
	
	/**
	 * @return The tooltip for the tab. Can be null.
	 */
	public String getTabViewTooltip();
	
	/**
	 * @param node The {@link Preferences} node in which the configuration of this module is placed. If the configuration is not a
	 * valid configuration, this method should nevertheless return normally, possibly with a component initialized to standard values. The preference
	 * node might in particular be null.
	 * @param j The journal on which this component should work.
	 * @return The actual visual component of the module which is displayed in the tab. It should be the same component for all calls of this method with
	 * the same journal
	 */
	public JPanel getComponent(Preferences node, Journal j);
	
	/**
	 * Saves the configuration of the module for the given journal in the given node
	 * @param j TODO
	 */
	public void insertPreferences(Preferences node, Journal j);
	
	/**
	 * @return The (unique) ID of this module. The convention is 'ff2module_(descriptivestring)'
	 */
	public String getID();
	
}
