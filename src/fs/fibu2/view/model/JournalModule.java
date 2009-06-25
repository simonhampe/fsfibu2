package fs.fibu2.view.model;

import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A journal module is a generic component for editing / displaying an fsfibu2 journal. A journal module might be for
 * example a tabbed pane for filter views of a journal or just a panel with several diagrams. When fsfibu2 is started, for each
 * module registered with the {@link JournalModuleLoader} or corresponding class file in the modules/ folder, one tab is added to the main tabbed view. 
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
	 * valid configuration, this method should nevertheless return normally, possibly with a component initialized to standard values.
	 * @return The actual visual component of the module which is displayed in the tab. It should be the same component for all calls of this method
	 */
	public JPanel getComponent(Preferences node);
	
	/**
	 * Saves the configuration of the module in the given node
	 */
	public void insertPreferences(Preferences node);
	
	/**
	 * @return The (unique) ID of this module
	 */
	public String getID();
	
}
