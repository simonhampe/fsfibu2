package fs.fibu2.view.render;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.model.AccountTableModel;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.SeparatorModel;
import fs.fibu2.view.model.TableModelComparator;
import fs.gui.GUIToolbox;
import fs.polyglot.view.TableEditPane;

/**
 * The bilancial panel is used in Journal table views to indicate the bilancials of either all the displayed data or
 * a certain selection. It updates automatically, when the selection or the data changes
 * @author Simon Hampe
 *
 */
public class BilancialPanel extends JPanel {

	//Associated data
	private JTable table;
	private JournalTableModel tableModel;
	private ListSelectionModel selectionModel;
	
	//Listeners
	
	private TableModelListener tableListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			updateValues();
		}
	};
	
	//Updates the selection description and toggles availability of the Selection mode
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(selectionModel.isSelectionEmpty()) {
				radioSelection.setEnabled(false);
				if(radioSelection.isSelected()) toggleOverallMode(true);
			}
			else radioSelection.setEnabled(true);
			updateDescription();
		}
	};
	
	//Makes sure 'from' is always smaller than to
	private ItemListener itemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(!((new TableModelComparator().compare(comboFrom.getSelectedItem(),comboTo.getSelectedItem())) <= 0)) {
				if(e.getSource() == comboFrom) {
					int index = comboFrom.getSelectedIndex();
					comboTo.setSelectedIndex(index > 0 ? index : 0);
				}
				if(e.getSource() == comboTo) {
					int index = comboTo.getSelectedIndex();
					comboFrom.setSelectedIndex(index < comboFrom.getItemCount()? index : comboFrom.getItemCount()-1);
				}
			}
			updateValues();
		}
	};
	
	private ActionListener radioListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			toggleOverallMode(radioAll.isSelected());
		}
	};
	
	// GUI COMPONENTS *************************
	// ****************************************
	
	private JRadioButton radioAll = new JRadioButton();
	private JRadioButton radioSelection = new JRadioButton();
	private JLabel labelDescription = new JLabel();
		
	private JComboBox comboFrom = new JComboBox();
	private JComboBox comboTo = new JComboBox();
	
	private JTable tableAccount = new JTable();
	
	private JComboBox comboCategory = new JComboBox();
	private JLabel labelCategorySum = new JLabel();
	
	private JLabel labelOverallSum = new JLabel();
	
	// MISC ***********************************
	// ****************************************
	
	private final static String sgroup = "fs.fibu2.view.BilancialPanel";
	
	// CONSTRUCTOR ****************************
	// ****************************************
	
	/**
	 * Creates a bilancial panel associated to the given table
	 * @param table The table from which the panel gets the {@link ListSelectionModel} and the {@link JournalTableModel}.
	 * @throws ClassCastException - if the table model is not of type {@link JournalTableModel}.
	 */
	public BilancialPanel(JTable table) throws ClassCastException {
		//Copy data and listen
		tableModel = (JournalTableModel) table.getModel();
			tableModel.addTableModelListener(tableListener);
		selectionModel = table.getSelectionModel();
			selectionModel.addListSelectionListener(selectionListener);
		this.table = table == null? new JTable() : table;
		
		//Init GUI ----------------------------
		
		//Additional components
		JLabel labelTitle = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".title"));
		JLabel labelFrom = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".from"));
			labelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel labelTo = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".to"));
			labelTo.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel labelCategory = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".category"));
		JLabel labelColon = new JLabel(":"); 
		JLabel labelOverall = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".overall")); 
		
		//Init member components
		radioAll.setText(Fsfibu2StringTableMgr.getString(sgroup + ".radioall"));
			radioAll.addActionListener(radioListener);
		radioSelection.setText(Fsfibu2StringTableMgr.getString(sgroup + ".radioselected"));
			radioSelection.addActionListener(radioListener);
			ButtonGroup group = new ButtonGroup();
			group.add(radioAll);
			group.add(radioSelection);
			radioAll.setSelected(true);
		
		comboFrom.setModel(new SeparatorModel(tableModel));
			comboFrom.setRenderer(new SeparatorRenderer());
			comboFrom.addItemListener(itemListener);
		comboTo.setModel(new SeparatorModel(tableModel));
			comboTo.setRenderer(new SeparatorRenderer());
			if(comboTo.getModel().getSize() > 0) comboTo.setSelectedItem(comboTo.getModel().getElementAt(comboTo.getModel().getSize()-1));
			comboTo.addItemListener(itemListener);
		
		comboCategory.setModel(new CategoryListModel(tableModel.getAssociatedJournal(),false));
			comboCategory.setRenderer(new CategoryListRenderer(" > "));
		
			
		//This is done to get a proper table size in the label:
		tableAccount.setModel(new AccountTableModel(tableModel,null,null, Currency.getInstance("EUR")));
		labelCategorySum.setText("t");
		labelOverallSum.setText("t");
		
		selectionListener.valueChanged(new ListSelectionEvent(selectionModel,0,tableModel.getRowCount()-1,false));
		
		//Layout ----------------------------
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		setBorder(BorderFactory.createEtchedBorder());
		
		JPanel titlePanel = new JPanel();
			titlePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			GridBagConstraints gcTitle = GUIToolbox.buildConstraints(0, 0, 3, 1);
				gcTitle.weightx = 100;
			layout.setConstraints(titlePanel, gcTitle);
			FlowLayout titleLayout = new FlowLayout(FlowLayout.LEFT);
			titlePanel.setLayout(titleLayout);
			titlePanel.add(labelTitle); titlePanel.add(radioAll); titlePanel.add(radioSelection); titlePanel.add(labelDescription);
		add(titlePanel);
		
		JPanel firstPanel = new JPanel();
			firstPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString("fs.fibu2.view.BilancialPanel.range")));
			GridBagConstraints gcFirst = GUIToolbox.buildConstraints(0, 1, 1, 1);
			gcFirst.insets = new Insets(5,0,5,0);
			gcFirst.weightx = 20;
			layout.setConstraints(firstPanel, gcFirst);
			GridBagLayout firstLayout = new GridBagLayout();
			firstPanel.setLayout(firstLayout);
				GridBagConstraints gcFrom = GUIToolbox.buildConstraints(0, 0, 1, 1);
				GridBagConstraints gcFromCombo = GUIToolbox.buildConstraints(1, 0, 1, 1); gcFromCombo.weightx = 100; gcFromCombo.insets = new Insets(5,5,5,5);
				GridBagConstraints gcTo = GUIToolbox.buildConstraints(0, 1, 1, 1);
				GridBagConstraints gcToCombo = GUIToolbox.buildConstraints(1, 1, 1, 1); gcToCombo.weightx = 100; gcToCombo.insets = new Insets(5,5,5,5);
				firstLayout.setConstraints(labelFrom, gcFrom); firstLayout.setConstraints(comboFrom, gcFromCombo);
				firstLayout.setConstraints(labelTo, gcTo); firstLayout.setConstraints(comboTo, gcToCombo);
				firstPanel.add(labelFrom); firstPanel.add(labelTo); firstPanel.add(comboFrom); firstPanel.add(comboTo);
		add(firstPanel);
			
		JScrollPane secondPanel = new JScrollPane(tableAccount);
			secondPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".accounttitle")));
			Dimension tableSize = tableAccount.getPreferredSize();
			secondPanel.setPreferredSize(new Dimension(tableSize.width*2,tableSize.height*2));
			GridBagConstraints gcSecond = GUIToolbox.buildConstraints(1, 1, 1, 1);
				gcSecond.insets = new Insets(5,0,5,0);
			layout.setConstraints(secondPanel, gcSecond);
		add(secondPanel);
			
		JPanel thirdPanel = new JPanel();
			thirdPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".overalltitle")));
			GridBagConstraints gcThird = GUIToolbox.buildConstraints(2, 1, 1, 1);
			gcThird.weightx = 33;
			gcThird.insets = new Insets(5,0,5,0);
			layout.setConstraints(thirdPanel, gcThird);
			GridBagLayout thirdLayout = new GridBagLayout();
			thirdPanel.setLayout(thirdLayout);
			Insets insets = new Insets(5,5,5,5);
			GridBagConstraints gcCat = GUIToolbox.buildConstraints(0, 0, 1, 1); gcCat.insets = insets;
			GridBagConstraints gcComboCat = GUIToolbox.buildConstraints(1, 0, 1, 1); gcComboCat.insets = insets;
			GridBagConstraints gcColon = GUIToolbox.buildConstraints(2, 0, 1, 1); gcColon.insets = insets;
			GridBagConstraints gcResultCat = GUIToolbox.buildConstraints(3, 0, 1, 1); gcResultCat.weightx = 100;
																						gcResultCat.insets = new Insets(5,5,5,5);
			GridBagConstraints gcOverall = GUIToolbox.buildConstraints(0, 2, 1, 1); gcOverall.insets = new Insets(5,5,10,5);
			GridBagConstraints gcOverallResult = GUIToolbox.buildConstraints(1, 2, 3, 1); gcOverallResult.weightx = 100;
																						gcOverallResult.insets = new Insets(5,5,10,5);
			GridBagConstraints gcFill = GUIToolbox.buildConstraints(0, 1, 4, 1); gcFill.weighty = 100;
			thirdLayout.setConstraints(labelCategory, gcCat);
			thirdLayout.setConstraints(comboCategory, gcComboCat);
			thirdLayout.setConstraints(labelColon, gcColon);
			thirdLayout.setConstraints(labelCategorySum, gcResultCat);
			thirdLayout.setConstraints(labelOverall, gcOverall);
			thirdLayout.setConstraints(labelOverallSum, gcOverallResult);
			JPanel fillPanel = new JPanel();
			thirdLayout.setConstraints(fillPanel, gcFill);
			thirdPanel.add(labelCategory); thirdPanel.add(comboCategory); thirdPanel.add(labelColon);thirdPanel.add(labelCategorySum);
			thirdPanel.add(labelOverall); thirdPanel.add(labelOverallSum); thirdPanel.add(fillPanel);
		add(thirdPanel);

		updateValues();
	}
	
	// CONTROL METHODS **********************************
	// **************************************************
	
	/**
	 * Reloads all values and inserts them accordingly
	 */
	protected void updateValues() {
		boolean unconnectedRange = false; //Whether we are in selection mode with an unconnected selection
		int[] selected = table.getSelectedRows();
		float categorySum = 0;
		float overallSum = 0;
		for (int i = 0; i < selected.length; i++) {
			selected[i] = table.convertRowIndexToModel(selected[i]);
		}
		if(radioSelection.isSelected() && !(containsOnlySeparators(selected))) {
			if(selected[selected.length-1] - selected[0] != selected.length -1) unconnectedRange = true;
		}
		//Easy: Connected range (or only separators)
		if(!unconnectedRange) {
			int first = 0;
			int last = tableModel.getRowCount()-1;
			//Calculate first and last index
			if(radioSelection.isSelected()) {
				first = table.getSelectedRows()[0];
				last = table.getSelectedRows()[table.getSelectedRowCount()-1];
			}
			//Insert values
			tableAccount.setModel(new AccountTableModel(tableModel,tableModel.getValueAt(first, 0), tableModel.getValueAt(last, 0),Currency.getInstance("EUR")));
			categorySum = 
				tableModel.getBilancialMapping(last).getOldest().information().getCategoryMappings().get((Category)comboCategory.getSelectedItem()) - 
				tableModel.getBilancialMapping(first).getOldest().information().getCategoryMappings().get((Category)comboCategory.getSelectedItem());
			overallSum = 
				tableModel.getBilancialMapping(last).getOldest().information().getOverallSum() - 
				tableModel.getBilancialMapping(first).getOldest().information().getOverallSum();
		}
		//Difficult: Unconnected 
		else {
			//Uh, need a better account model here
		}
		
	}
	
	/**
	 * Sets the description of the 'selection' bilancials
	 */
	protected void updateDescription() {
		String description = "";
		if(selectionModel.isSelectionEmpty()) {
			description = Fsfibu2StringTableMgr.getString(sgroup+".selectionempty");
		}
		else {
			//Get selected rows and convert to model
			int[] selected = table.getSelectedRows();
			for (int i = 0; i < selected.length; i++) {
				selected[i] = table.convertRowIndexToModel(selected[i]);
			}
			TreeSet<EntrySeparator> separators = new TreeSet<EntrySeparator>(new TableModelComparator());
			boolean hasEntries = false;
			for (int i : selected) {
				Object row = tableModel.getValueAt(i, 0) ; 
				if(row instanceof EntrySeparator) separators.add((EntrySeparator)row);
				if(row instanceof Entry) hasEntries = true;
			}
			if(!hasEntries) {
				description = Fsfibu2StringTableMgr.getString(sgroup + ".selectionseparator",separators.first().getName(),
						separators.size() == 1? tableModel.getDisplayedSeparators().lastElement().getName() : separators.last().getName());
			}
			else {
				//The bilancial description depends on whether the selection is connected or not
				description = (selected[selected.length-1] - selected[0] == selected.length -1) ?
						Fsfibu2StringTableMgr.getString(sgroup + ".selectionrange") :
						Fsfibu2StringTableMgr.getString(sgroup + ".selectionentry");
			}
		}
		labelDescription.setText("<html><i>" + description + "</i></html>");
	}
	
	/**
	 * If true, selects the overall mode and inserts values, otherwise selects selection mode (if active) and inserts value 
	 */
	public void toggleOverallMode(boolean flag) {
		if(flag) {
			radioAll.setSelected(true);
			comboFrom.setEnabled(true);
			comboTo.setEnabled(true);
		}
		else {
			if(radioSelection.isEnabled()) radioSelection.setSelected(true);
			comboFrom.setEnabled(false);
			comboTo.setEnabled(false);
		}
		updateValues();
	}
	
	/**
	 * @return true, if and only if the objects in the table model associated to the given indices are all of
	 * type EntrySeparator
	 */
	protected boolean containsOnlySeparators(int[] indices) {
		for (int i : indices) {
			if(!(tableModel.getValueAt(i, 0) instanceof EntrySeparator)) {
				return false;
			}
		}
		return true;
	}
	
}
