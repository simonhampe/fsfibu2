package fs.fibu2.module;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalModule;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.BilancialPanel;
import fs.fibu2.view.render.JournalTable;
import fs.fibu2.view.render.JournalTableBar;
import fs.gui.GUIToolbox;
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
	
	private HashMap<Journal, FilterModule> modules = new HashMap<Journal, FilterModule>();
	
	// COMPONENTS ********************
	// *******************************
	
	private JournalTable table;
	private JournalTableBar bar;
	private JToggleButton filterButton = new JToggleButton(new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/FilterModule/filter.png")));
	private BilancialPanel bilancialPanel;
	
	private StackFilter filter;
	private EntryFilterEditor filterComponent;
	
	// LISTENERS *********************
	// *******************************
	
	private ActionListener visbilityListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			filterComponent.setVisible(filterButton.isSelected());
		}
	};
	
	// CONSTRUCTORS ******************
	// *******************************
	
	public FilterModule() {}
	
	protected FilterModule(Journal j, Preferences node) {
		super();
		
		//Init components
		filter = new StackFilter();
		//TODO: Read out preferences here
		table = new JournalTable(new JournalTableModel(j,filter,true,true));
		filterButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterModule.filter"));
		filterButton.setSelected(false);
		filterButton.addActionListener(visbilityListener);
		bar = new JournalTableBar(table);
			bar.setFloatable(false);
		bilancialPanel = new BilancialPanel(table);
		filterComponent = filter.getEditor(j);
		filterComponent.setVisible(false);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		//Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcBar = GUIToolbox.buildConstraints(0, 0, 1, 1); gcBar.weightx = 100;
		GridBagConstraints gcButton = GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gcTable = GUIToolbox.buildConstraints(0, 1, 2, 1); gcTable.weighty = 100;
		GridBagConstraints gcBilancial = GUIToolbox.buildConstraints(0, 2, 2, 1);
		GridBagConstraints gcFilter = GUIToolbox.buildConstraints(2, 0, 1, 3); gcFilter.weighty = 100;
		
		gbl.setConstraints(bar, gcBar);
		gbl.setConstraints(filterButton, gcButton);
		gbl.setConstraints(scrollPane, gcTable);
		gbl.setConstraints(bilancialPanel, gcBilancial);
		gbl.setConstraints(filterComponent, gcFilter);
		
		add(bar); add(filterButton); add(scrollPane); add(bilancialPanel); 
		add(filterComponent);
		
		
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
