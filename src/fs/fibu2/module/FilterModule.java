package fs.fibu2.module;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.DefaultFilters;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.FilterPool;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.filter.FilterPool.StackFilterTripel;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.FilterPoolModel;
import fs.fibu2.view.model.JournalModule;
import fs.fibu2.view.render.FilterPoolRenderer;
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
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 3851970671264968499L;

	private static HashMap<Journal, FilterModule> modules = new HashMap<Journal, FilterModule>();
	
	private Journal associatedJournal;
	
	private Logger logger = Logger.getLogger(FilterModule.class);
	
	// COMPONENTS ********************
	// *******************************
	
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private JButton addButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.add"));
	private JComboBox copyBox = new JComboBox();
	
	// LISTENERS *********************
	// *******************************
	
	private ActionListener addListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			addPanel(null,null);
		}
	};
	
	private ActionListener itemListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object item = copyBox.getSelectedItem();
			if(item == null) return;
			if(item instanceof Integer) {
				addPanel(null, new StackFilter(new Vector<EntryFilter>(Arrays.asList(DefaultFilters.getYearFilter((Integer)item))),
						(HashSet<EntryFilter>)null,
						(HashSet<EntryFilter>) null));
			}
			if(item instanceof StackFilterTripel) {
				addPanel(null, ((StackFilterTripel)item).filter.clone());
			}
		}
	};
	
	//Listens to the tab titles
	private PropertyChangeListener titleListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if("text".equals(evt.getPropertyName())) {
				int index = tabbedPane.indexOfTabComponent((Component)evt.getSource());
				FilterPool.getPool(associatedJournal).setFilterName(((FilterPane)tabbedPane.getComponentAt(index)).getFilter(), evt.getNewValue().toString());
			}
		}
	};
	
	// CONSTRUCTORS ******************
	// *******************************
	
	public FilterModule() {}
	
	protected FilterModule(Journal j, Preferences node) {
		super(new BorderLayout());
		associatedJournal = j;
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		//Create dummy tab for addition
		JPanel dummyPanel = new JPanel();
		tabbedPane.add(dummyPanel);
		JPanel tabPanel = new JPanel();
			tabPanel.add(addButton);
			tabPanel.add(new JLabel(Fsfibu2StringTableMgr.getString("fs.fibu2.module.copyfilter")));
			tabPanel.add(copyBox);
			copyBox.setModel(new FilterPoolModel(associatedJournal,this,true));
			copyBox.setRenderer(new FilterPoolRenderer());
		tabbedPane.setTabComponentAt(0, tabPanel);
		tabbedPane.setEnabledAt(0, false);
		
		add(tabbedPane, BorderLayout.CENTER);
		
		//Extract preferences
		
		if(node != null) {
			Integer selected = 0;
			try {
				selected = Integer.parseInt(node.get("selected", "0"));
			}
			catch(NumberFormatException e) {
				//Ignore
			}
			try {
				while(node.nodeExists("paneNode")) {
					node = node.node("paneNode");
					if(node.nodeExists("config")) {
						addPanel(node.node("config"),null);
					}
				}
			} catch (BackingStoreException e) {
				logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.preferror",e.getMessage()));
			}
			if(selected > 0 && selected < tabbedPane.getTabCount()) tabbedPane.setSelectedIndex(selected);
		}		
		
		//Add listeners
		addButton.addActionListener(addListener);
		copyBox.addActionListener(itemListener);
	}
	
	// CONTROL METHODS ***************
	// *******************************
	
	/**
	 * Adds a panel using the given preferences. If node == null, adds a new panel with default values and the given filter.
	 * If node != null, the filter is ignored. 
	 * The new tab is automatically selected and if node == null, the tab component is set to edit mode
	 */
	protected void addPanel(Preferences node, StackFilter f) {
		final FilterPane pane = new FilterPane(node,f,associatedJournal);
		tabbedPane.add(pane);
		
		String title = node == null? Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.defaulttitle") :
				node.get("name", Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.defaulttitle"));
		final EditCloseTabComponent tabComponent = new EditCloseTabComponent(title,
				tabbedPane,true,true,FsfwDefaultReference.getDefaultReference());
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, tabComponent);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
		tabComponent.addPropertyChangeListener(titleListener);
		if(node == null) {
			tabComponent.setToEditMode();
		}
		tabComponent.getCloseButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterPool.getPool(associatedJournal).removeFilter(pane.getFilter());
			}
		});
		
		String poolName = node == null? Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.defaulttitle") : tabComponent.getTextLabel().getText();
		FilterPool.getPool(associatedJournal).addFilter(pane.getFilter(), this, poolName);
		
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
		if(node != null) {
			try {
				//Clear old prefs
				Preferences paneNode = node;
				if(paneNode.nodeExists("paneNode")) {
					paneNode.node("paneNode").removeNode(); 
				}
				
				//Write new prefs
				if(tabbedPane.getTabCount() > 1) node.put("selected", Integer.toString(tabbedPane.getSelectedIndex()));
				for(int i = 1; i < tabbedPane.getTabCount(); i++) {
					paneNode = paneNode.node("paneNode");
					FilterPane pane = (FilterPane)tabbedPane.getComponentAt(i);
					paneNode.node("config").put("name", ((EditCloseTabComponent)tabbedPane.getTabComponentAt(i)).getTextLabel().getText());
					pane.insertPreferences(paneNode.node("config"));
				}
			} catch (BackingStoreException e) {
				logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.prefsaveerror", e.getMessage()));;
			}
		}
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
