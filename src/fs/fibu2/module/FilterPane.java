package fs.fibu2.module;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.dom4j.Document;

import fs.fibu2.data.model.Journal;
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

/**
 * Contains a journal table view with table bar and a filter editor which can be toggled on/off
 * @author Simon Hampe
 *
 */
public class FilterPane extends JPanel implements ResourceDependent {

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
	
	// CONSTRUCTOR *******************
	// *******************************
	
	/**
	 * Construct a filter pane for the given journal, using the given preferences. If node == null, default values are used
	 */
	public FilterPane(Preferences node, Journal j) {
		super();
		
		//Init components
		filter = new StackFilter();
		//TODO: Read out preferences here
		table = new JournalTable(new JournalTableModel(j,filter,true,true));
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
		
	}
	
	// RESOURCEDEPENDENT *************
	// *******************************
	
	@Override
	public void assignReference(ResourceReference r) {
		// TODO Auto-generated method stub

	}

	@Override
	public Document getExpectedResourceStructure() {
		// TODO Auto-generated method stub
		return null;
	}

}
