package fs.fibu2.module;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.BilancialPanel;
import fs.fibu2.view.render.JournalTable;
import fs.fibu2.view.render.JournalTableBar;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Contains a journal table view with table bar and a filter editor which can be toggled on/off
 * @author Simon Hampe
 *
 */
public class FilterPane extends JPanel implements ResourceDependent {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -3491854264546994038L;
	
	// COMPONENTS ********************
	// *******************************
	
	private JournalTable table;
	private JournalTableBar bar;
	private JToggleButton filterButton = new JToggleButton(new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/FilterModule/filter.png")));
	private BilancialPanel bilancialPanel;
	
	private EntryFilter filter;
	private EntryFilterEditor filterComponent;
	
	// LISTENERS *********************
	// *******************************
	
	private ActionListener visbilityListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			filterComponent.setVisible(filterButton.isSelected());
		}
	};
	
	// CONSTRUCTOR *******************
	// *******************************
	
	/**
	 * Construct a filter pane for the given journal, using the given preferences. If node == null, default values are used
	 */
	public FilterPane(Preferences node, Journal j) {
		super();
		
		//Init components
		filter = new StackFilter();
		
		table = new JournalTable(new JournalTableModel(j,null,true,true));
		//Read out preferences
		if(node != null) {
			String displayyear = node.get("displayyear", null);
			if(displayyear != null) table.getJournalTableModel().setYearSeparatorsVisible(Boolean.parseBoolean(displayyear));
			String displayreading = node.get("displayreading", null);
			if(displayreading != null) table.getJournalTableModel().setReadingPointsVisible(Boolean.parseBoolean(displayreading));
			try {
				if(node.nodeExists("filter")) {
					filter = filter.createMeFromPreferences(node.node("filter"));
				}
			} catch (BackingStoreException e) {
				//Ignore
			}
		}
		table.getJournalTableModel().setFilter(filter);
		
		filterButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.module.FilterPane.filter"));
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
	
	// GETTERS ***********************
	// *******************************
	
	/**
	 * Inserts the preferences of this pane into the given node
	 */
	public void insertPreferences(Preferences node) {
		if(node != null) {
			node.put("displayyear", Boolean.toString(table.getJournalTableModel().areYearSeparatorsVisible()));
			node.put("displayreading", Boolean.toString(table.getJournalTableModel().areReadingPointsVisible()));
			filter.insertMyPreferences(node);
		}
	}
	
	// RESOURCEDEPENDENT *************
	// *******************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/FilterModule/filter.png");
		return tree;
	}

}
