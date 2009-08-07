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
 * This class implements a module which can display bilancial information concerning a {@link Journal}. Each bilancial view is associated to a 
 * {@link StackFilter} and the module participates in the {@link FilterPool}. The preferences are stored in the following way: Within the node passed
 * in {@link #insertPreferences(Preferences, Journal)}, a subnode "sub" is added recursively, which contains the preferences node of the corresponding 
 * {@link BilancialPane} (named "pane"), a key-value pair for "name" which contains the name of the tab and, potentially, another "sub" node. The root node
 * itself will also contain a key-value pair named "selected", indicating which tab was selected 
 * @author Simon Hampe
 *
 */
public class BilancialModule extends JPanel implements JournalModule, ResourceDependent {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 3424614246815918836L;

	// DATA ************************************
	// *****************************************
	
	//A map of modules associated to a journal
	private static HashMap<Journal, BilancialModule> moduleMap = new HashMap<Journal, BilancialModule>();
	
	//The associated journal
	private Journal associatedJournal;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	// COMPONENTS ******************************
	// *****************************************
	
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
				FilterPool.getPool(associatedJournal).setFilterName(((BilancialPane)tabbedPane.getComponentAt(index)).getFilter(), evt.getNewValue().toString());
			}
		}
	};
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	public BilancialModule() {}
	
	/**
	 * Constructs a new module. Since modules should only be obtained via the {@link #getComponent(Preferences, Journal)} method, 
	 * this is protected
	 * @param j The associated journal. If a module has already been created for this journal, it is returned, regardless of the remaining parameters
	 * @param n The preferences for this module. If null, default values are used 
	 */
	protected BilancialModule(Journal j, Preferences n) {
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
		
		if(n != null) {
			Preferences node = n;
			try {
				int sel = -1;
				sel = Integer.parseInt(node.get("selected", "-1"));
				
				while(node.nodeExists("sub")) {
					node = node.node("sub");
					addPanel(node, null);
				}
				
				if(sel >= 0 && sel < tabbedPane.getTabCount()) tabbedPane.setSelectedIndex(sel);
			} catch (BackingStoreException e) {
				//abort
				logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.preferror", e.getLocalizedMessage()));
			}
		}		
		
		//Add listeners
		addButton.addActionListener(addListener);
		copyBox.addActionListener(itemListener);
	}
	
	// MODULE **********************************
	// *****************************************
	
	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		BilancialModule mod = moduleMap.get(j);
		if(mod == null) {
			mod = new BilancialModule(j,node);
			moduleMap.put(j, mod);
		}
		return mod;
	}

	@Override
	public String getID() {
		return "ff2module_bilancial";
	}

	@Override
	public Icon getTabViewIcon() {
		return new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/BilancialModule/tab.png"));
	}

	@Override
	public String getTabViewName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.name");
	}

	@Override
	public String getTabViewTooltip() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.tooltip");
	}

	@Override
	public void insertPreferences(Preferences node, Journal j) {
		if(moduleMap.containsKey(j)) {
			moduleMap.get(j).insertPreferences(node);
		}
	}
	
	protected void insertPreferences(Preferences node) {
		try {
			//Clear preferences
			if(node.nodeExists("sub")) {
				node.node("sub").removeNode();
			}
			
			Preferences subNode = node;
			if(tabbedPane.getTabCount() > 1) node.put("selected", Integer.toString(tabbedPane.getSelectedIndex()));
			for(int i = 1; i < tabbedPane.getTabCount(); i++) {
				subNode = subNode.node("sub");
				BilancialPane pane = (BilancialPane)tabbedPane.getComponentAt(i);
				subNode.put("name", ((EditCloseTabComponent)tabbedPane.getTabComponentAt(i)).getTextLabel().getText());
				pane.insertMyPreferences(subNode.node("pane"));
			}
			
		} catch (BackingStoreException e) {
			logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.prefsaveerror", e.getLocalizedMessage()));
		}
	}
	
	// CONTROL METHODS **********************************
	// **************************************************
	/**
	 * Adds a panel using the given preferences. If node == null, adds a new panel with default values and the given filter.
	 * If node != null, the filter is ignored. 
	 * The new tab is automatically selected and if node == null, the tab component is set to edit mode
	 */
	protected void addPanel(Preferences node, StackFilter f) {
		final BilancialPane pane = new BilancialPane(associatedJournal,f,node == null? null : node.node("pane"));
		tabbedPane.add(pane);
		
		String title = node == null? Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.defaulttitle") :
				node.get("name", Fsfibu2StringTableMgr.getString("fs.fibu2.module.BilancialModule.defaulttitle"));
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
	// RESOURCEDEPENDENT ********************************
	// **************************************************

	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("graphics/BilancialModule/tab.png");
		return tree;
	}

}
