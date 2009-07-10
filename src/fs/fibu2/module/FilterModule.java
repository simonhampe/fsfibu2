package fs.fibu2.module;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalModule;
import fs.gui.EditCloseTabComponent;
import fs.xml.FsfwDefaultReference;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * The filter module provides an overview over a journal together with the possibility to set an advanced filter.
 * @author Simon Hampe
 *
 */
public class FilterModule extends JPanel implements JournalModule, ResourceDependent {

	// DATA **************************
	// *******************************
	
	private static HashMap<Journal, FilterModule> modules = new HashMap<Journal, FilterModule>();
	
	private Journal associatedJournal;
	
	// COMPONENTS ********************
	// *******************************
	
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private JButton addButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.add"));
	
	// LISTENERS *********************
	// *******************************
	
	private ActionListener addListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			addPanel(null);
		}
	};
	
	// CONSTRUCTORS ******************
	// *******************************
	
	public FilterModule() {}
	
	protected FilterModule(Journal j, Preferences node) {
		super(new BorderLayout());
		associatedJournal = j;
		
		//Create dummy tab for addition
		JPanel dummyPanel = new JPanel();
		tabbedPane.add(dummyPanel);
		tabbedPane.setTabComponentAt(0, addButton);
		tabbedPane.setEnabledAt(0, false);
		
		add(tabbedPane, BorderLayout.CENTER);
		
		//Add listeners
		addButton.addActionListener(addListener);
	}
	
	// CONTROL METHODS ***************
	// *******************************
	
	/**
	 * Adds a panel using the given preferences. If node == null, adds a new panel with default values. 
	 * The new tab is automatically selected and if node == null, the tab component is set to edit mode
	 */
	protected void addPanel(Preferences node) {
		FilterPane pane = new FilterPane(node,associatedJournal);
		tabbedPane.add(pane);
		EditCloseTabComponent tabComponent = new EditCloseTabComponent(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.defaulttitle"),
				tabbedPane,true,true,FsfwDefaultReference.getDefaultReference());
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, tabComponent);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
		if(node == null) {
			tabComponent.setToEditMode();
		}
	}
	
	// JOURNALMODULE *****************
	// *******************************
	
	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		FilterModule module = modules.get(j);
		if(module == null) {
			module = new FilterModule(j,node);
			modules.put(j, module);
		}
		return module;
	}

	@Override
	public String getID() {
		return "ff2module_filter";
	}

	@Override
	public Icon getTabViewIcon() {
		return new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/FilterModule/tab.png"));
	}

	@Override
	public String getTabViewName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.name");
	}

	@Override
	public String getTabViewTooltip() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.tooltip");
	}

	@Override
	public void insertPreferences(Preferences node, Journal j) {
		if(modules.containsKey(j)) {
			modules.get(j).insertPreferences(node);
		}
	}
	
	protected void insertPreferences(Preferences node) {
		//TODO: Write
	}

	// RESOURCEDEPENDENT *************************
	// *******************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("gaphics/FilterModule/tab.png");
		tree.addPath("graphics/FilterModule/filter.png");
		return tree;
	}

}
