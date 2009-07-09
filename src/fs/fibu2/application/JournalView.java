package fs.fibu2.application;

import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.JournalModule;
import fs.fibu2.view.model.JournalModuleLoader;

/**
 * This class implements the view associated to a journal. It consists of several tabs (located at the left side), one for each registered JournalModule.
 * @author Simon Hampe
 *
 */
public class JournalView extends JTabbedPane {
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -2512994917270410618L;

	//The associated Journal
	private Journal associatedJournal;
	
	//The class logger
	private Logger logger = Logger.getLogger(this.getClass());
	
	//A list of open modules
	private Vector<JournalModule> modules = new Vector<JournalModule>();
	
	/**
	 * Constructs a journal view
	 * @param j The associated journal. If null, a dummy journal is created
	 * @param prefNode The node in which the view should locate the initialization data for its modules. For each module the 
	 * constructor looks for a node which bears as name the id of the module and if it exists, passes it on.
	 */
	public JournalView(Journal j, Preferences prefNode) {
		associatedJournal = j == null? new Journal() : j;
		modules = makeOrderedList();
		int index = 0;
		for(JournalModule module : modules) {
			//Init component
			add(module.getComponent(prefNode == null? null : prefNode.node(module.getID()),j));
			if(module.getTabViewIcon() != null) setIconAt(index, module.getTabViewIcon());
			setTitleAt(index, module.getTabViewName());
			setToolTipTextAt(index, module.getTabViewTooltip());
			index++;
		}
		setSelectedIndex(0);
		setTabPlacement(JTabbedPane.LEFT);
	}
	
	/**
	 * Inserts the preferences of each module into this node
	 */
	public void insertPreferences(Preferences node) {
		for(JournalModule module : modules) {
			Preferences mnode = node.node(module.getID());
			module.insertPreferences(mnode, associatedJournal);
		}
	}
	
	/**
	 * @return A vector of JournalModules associated to the IDs in the {@link JournalModuleLoader}. It logs a warning for each ID for which it cannot retrieve a module.
	 * The order of the JournalModules in the vector is such that the default modules come first
	 */
	private Vector<JournalModule> makeOrderedList() {
		Vector<String> moduleIDs = JournalModuleLoader.getDefaultModules();
		for(String id : JournalModuleLoader.getModuleIDs()) if(!moduleIDs.contains(id)) moduleIDs.add(id);
		Vector<JournalModule> modules = new Vector<JournalModule>();
		for(String id : moduleIDs) {
			try {
				modules.add(JournalModuleLoader.getModule(id));
			} catch (Exception e) {
				logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.JournalView.moduleerror",id,e.getMessage()));
			}
		}
		return modules;
	}
	
}
