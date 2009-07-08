package fs.fibu2.module;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.dom4j.Document;

import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.DateFilter;
import fs.fibu2.filter.DefaultFilters;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalModule;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.YearListModel;
import fs.fibu2.view.render.BilancialPanel;
import fs.fibu2.view.render.CategoryListRenderer;
import fs.fibu2.view.render.JournalTable;
import fs.fibu2.view.render.JournalTableBar;
import fs.fibu2.view.render.YearListRenderer;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This module contains a journal table with an editing toolbar and combo boxes for basic year and category filters. By default, the current
 * year is automatically selected.
 * @author Simon Hampe
 *
 */
public class OverviewModule extends JPanel implements JournalModule, ResourceDependent {

	// DATA ******************************
	// ***********************************
	
	private Journal associatedJournal;
	
	private static HashMap<Journal, OverviewModule> modules = new HashMap<Journal, OverviewModule>();
	
	private final static String sgroup = "fs.fibu2.module.OverviewModule";
	
	// COMPONENTS ************************
	// ***********************************
	
	private ImageIcon tabIcon = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/OverviewModule/tab.png"));
	
	private JournalTableBar bar;
	private JournalTable table;
	private JToolBar comboBar = new JToolBar();
	private JComboBox yearBox = new JComboBox();
	private JComboBox categoryBox = new JComboBox();
	private BilancialPanel bilancialPanel;
	private JToggleButton nameButton = new JToggleButton(Fsfibu2StringTableMgr.getString(sgroup + ".namepanel"));
	private JPanel namePanel = new JPanel();
		private JTextField nameField = new JTextField();
		private JTextArea descriptionArea = new JTextArea();
		private JButton applyButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".apply"));
		private JButton restoreButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".restore"));
	
	// LISTENERS *************************
	// ***********************************
	
	private ItemListener yearItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			updateFilter();
		}
	};
	
	private ItemListener categoryItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			updateFilter();
		}
	};
	
	//Toggles visibility of the name pane
	private ActionListener toggleNamePanelListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			namePanel.setVisible(nameButton.isSelected());
		}
	};
	
	private ActionListener applyListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			table.getJournalTableModel().getAssociatedJournal().setNameAndDescriptionUndoable(nameField.getText(), descriptionArea.getText());
		}
	};
	
	private ActionListener restoreListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			nameField.setText(table.getJournalTableModel().getAssociatedJournal().getName());
			descriptionArea.setText(table.getJournalTableModel().getAssociatedJournal().getDescription());
		}
	};
	
	// CONSTRUCTOR ***********************
	// ***********************************
	
	/**
	 * Constructs the module which creates the actual view
	 */
	public OverviewModule() {}
	
	/**
	 * Constructs the actual component for a journal j and the given preferences
	 */
	protected OverviewModule(Preferences node, Journal j) {
		super();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		table = new JournalTable(new JournalTableModel(j,null,true,true));
		categoryBox.setModel(new CategoryListModel(j,true));
		categoryBox.setRenderer(new CategoryListRenderer(" > "));
		yearBox.setModel(new YearListModel(j));
		yearBox.setRenderer(new YearListRenderer());

		//Now extract preferences
//		if(node != null) {
//			String displayyear = node.get("displayyear", null);
//			if(displayyear != null) table.getJournalTableModel().setYearSeparatorsVisible(Boolean.parseBoolean(displayyear));
//			String displayreading = node.get("displayreading", null);
//			if(displayreading != null) table.getJournalTableModel().setReadingPointsVisible(Boolean.parseBoolean(displayreading));
//			String year = node.get("year", null);
//			if(year != null && !(year.equals("null"))) {
//				try {
//					Integer intyear = Integer.parseInt(year);
//					yearBox.setSelectedItem(intyear);
//				}
//				catch(NumberFormatException e) {
//					//Ignore
//				}
//			}
//			try {
//				if(node.nodeExists("filter")) {
//					CategoryFilter filter = (CategoryFilter)(new CategoryFilter()).createMeFromPreferences(node.node("filter"));
//					categoryBox.setSelectedItem(filter.getEqualityCategory());
//				}
//			} catch (BackingStoreException e) {
//				//Ignore
//			}
//		}
		
		bar = new JournalTableBar(table);
			bar.setFloatable(false);
			bar.setAlignmentX(LEFT_ALIGNMENT);
		bilancialPanel = new BilancialPanel(table);
		
		comboBar.setFloatable(false);
		comboBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		comboBar.add(new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".yearlabel")));
		comboBar.add(yearBox);
		comboBar.add(new JToolBar.Separator());
		comboBar.add(new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".categorylabel")));
		comboBar.add(categoryBox);
		comboBar.add(nameButton);
		
		nameButton.addActionListener(toggleNamePanelListener);
		applyButton.addActionListener(applyListener);
		restoreButton.addActionListener(restoreListener);
		
		nameField.setText(table.getJournalTableModel().getAssociatedJournal().getName());
		descriptionArea.setText(table.getJournalTableModel().getAssociatedJournal().getDescription());
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		namePanel.setVisible(false);
		
		JScrollPane pane = new JScrollPane(table);
		JPanel fillPanel = new JPanel(); 
		
		GridBagConstraints gcBar = GUIToolbox.buildConstraints(0, 0, 2, 1); gcBar.weightx = 100;
		GridBagConstraints gcCombo = GUIToolbox.buildConstraints(0, 1, 2, 1); gcCombo.weightx = 100;
		GridBagConstraints gcName = GUIToolbox.buildConstraints(0, 2, 1, 1);  
		GridBagConstraints gcFill = GUIToolbox.buildConstraints(1, 2, 1, 1); gcFill.weightx = 100;
		GridBagConstraints gcPane = GUIToolbox.buildConstraints(0, 3, 2, 1); gcPane.weightx = gcPane.weighty = 100;
		GridBagConstraints gcBilancial = GUIToolbox.buildConstraints(0, 4, 2, 1); gcBilancial.weightx = 100;
		
		gbl.setConstraints(bar, gcBar);
		gbl.setConstraints(comboBar, gcCombo);
		gbl.setConstraints(namePanel, gcName);
		gbl.setConstraints(fillPanel, gcFill);
		gbl.setConstraints(pane, gcPane);
		gbl.setConstraints(bilancialPanel, gcBilancial);
		
		add(bar); 
		add(comboBar);
		add(namePanel);
		add(fillPanel);
		add(pane);
		add(bilancialPanel);
		
		//Layout name pane
		JLabel lName = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".name"));
			lName.setAlignmentX(LEFT_ALIGNMENT);
		JLabel lDesc = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".description"));
			lDesc.setAlignmentX(LEFT_ALIGNMENT);
		JScrollPane areaPane = new JScrollPane(descriptionArea);
					
		GridBagLayout gbl2 = new GridBagLayout();
		namePanel.setLayout(gbl2);
		
		GridBagConstraints gcLName = GUIToolbox.buildConstraints(0, 0, 1, 1); gcLName.insets = new Insets(5,5,5,5);
		GridBagConstraints gcTName = GUIToolbox.buildConstraints(1, 0, 1, 1); gcTName.insets = new Insets(5,5,5,5);
			gcTName.ipadx = 300;
		GridBagConstraints gcLDesc = GUIToolbox.buildConstraints(0, 1, 1, 1); gcLDesc.insets = new Insets(5,5,5,5);
			gcLDesc.anchor = GridBagConstraints.NORTHWEST;
		GridBagConstraints gcTDesc = GUIToolbox.buildConstraints(1, 1, 1, 1); gcTDesc.insets = new Insets(5,5,5,5);
				gcTDesc.ipady = 50; 
		GridBagConstraints gcApply = GUIToolbox.buildConstraints(0, 2, 1, 1); gcApply.insets = new Insets(5,5,5,5);
		GridBagConstraints gcRestore = GUIToolbox.buildConstraints(0, 3, 1, 1); gcRestore.insets = new Insets(5,5,5,5);
		
		gbl2.setConstraints(lName, gcLName);
		gbl2.setConstraints(nameField, gcTName);
		gbl2.setConstraints(lDesc, gcLDesc);
		gbl2.setConstraints(areaPane, gcTDesc);
		gbl2.setConstraints(applyButton, gcApply);
		gbl2.setConstraints(restoreButton, gcRestore);
		
		namePanel.add(lName); namePanel.add(nameField); namePanel.add(lDesc); namePanel.add(areaPane);
		namePanel.add(applyButton); namePanel.add(restoreButton);

		
		//Add listeners
		categoryBox.addItemListener(categoryItemListener);
		yearBox.addItemListener(yearItemListener);
		
	}
	
	// MODULE ****************************
	// ***********************************
	
	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		OverviewModule module = modules.get(j);
		if(module == null) {
			module = new OverviewModule(node, j);
			modules.put(j, module);
		}
		return module;
	}

	@Override
	public String getID() {
		return "ff2module_overview";
	}

	@Override
	public Icon getTabViewIcon() {
		return tabIcon;
	}

	@Override
	public String getTabViewName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.OverviewModule.tabname");
	}

	@Override
	public String getTabViewTooltip() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.module.OverviewModule.tabtooltip");
	}

	@Override
	public void insertPreferences(Preferences node, Journal j) {
		if(modules.containsKey(j)) {
			modules.get(j).insertPreferences(node);
		}
	}
	
	protected void insertPreferences(Preferences node) {
		if(node != null) {
			node.put("displayyear", Boolean.toString(table.getJournalTableModel().areYearSeparatorsVisible()));
			node.put("displayreading", Boolean.toString(table.getJournalTableModel().areReadingPointsVisible()));
			if(yearBox.getSelectedItem() == null) node.put("year", "null");
			else node.put("year", ((Integer)yearBox.getSelectedItem()).toString());
			(new CategoryFilter((Category)categoryBox.getSelectedItem())).insertMyPreferences(node);
		}
	}
	
	protected void updateFilter() {
		if(yearBox.getSelectedItem() == null && categoryBox.getSelectedItem() == Category.getRootCategory()) {
			table.getJournalTableModel().setFilter(null);
		}
		else {
			if(yearBox.getSelectedItem() == null) {
				table.getJournalTableModel().setFilter(new CategoryFilter((Category)categoryBox.getSelectedItem()));
				return;
			}
			if(categoryBox.getSelectedItem() == Category.getRootCategory()) {
				table.getJournalTableModel().setFilter(DefaultFilters.getYearFilter((Integer)yearBox.getSelectedItem()));
				return;
			}
			table.getJournalTableModel().setFilter(new StackFilter(new Vector<EntryFilter>(
					Arrays.asList(
							DefaultFilters.getYearFilter((Integer)yearBox.getSelectedItem()),
							new CategoryFilter((Category)categoryBox.getSelectedItem()))), 
					null,null));
		}
	}
	
	// RESOURCEDEPENDENT ******************************
	// ************************************************

	@Override
	public void assignReference(ResourceReference r) {
		//Ignore		
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/OverviewModule/tab.png");
		return tree;
	}
}
