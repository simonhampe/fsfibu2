package fs.fibu2.view.render;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
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

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.DefaultCurrencyFormat;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.AccountTableModel;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.SeparatorModel;
import fs.fibu2.view.model.TableModelComparator;
import fs.gui.GUIToolbox;

/**
 * The bilancial panel is used in Journal table views to indicate the bilancials of either all the displayed data or
 * a certain selection. It updates automatically, when the selection or the data changes
 * @author Simon Hampe
 *
 */
public class BilancialPanel extends JPanel {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -5068136129774162459L;
	//Associated data
	private JournalTable table;
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
			updateValues();
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
	public BilancialPanel(JournalTable table) throws ClassCastException {
		//Copy data 
		tableModel = table.getJournalTableModel();
		selectionModel = table.getSelectionModel();
			
		this.table = table;
		
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
		
		tableAccount.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
			
		comboCategory.setModel(new CategoryListModel(tableModel.getAssociatedJournal(),false));
			comboCategory.setRenderer(new CategoryListRenderer(" > "));
			comboCategory.addItemListener(itemListener);
		
			
		//This is done to get a proper table size in the label:
		tableAccount.setModel(new AccountTableModel(tableModel,null,null));
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
			gcFirst.weightx = 30;
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
				gcSecond.weightx = 40;
				gcSecond.ipadx = 200;
			layout.setConstraints(secondPanel, gcSecond);
		add(secondPanel);
			
		JPanel thirdPanel = new JPanel();
			thirdPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".overalltitle")));
			GridBagConstraints gcThird = GUIToolbox.buildConstraints(2, 1, 1, 1);
			gcThird.weightx = 30;
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
		
		//Add listeners
		tableModel.addTableModelListener(tableListener);
		selectionModel.addListSelectionListener(selectionListener);
	}
	
	// CONTROL METHODS **********************************
	// **************************************************
	
	/**
	 * Takes care that only range values are selected, which actually exist in the table model
	 */
	protected void updateRange() {
		if(!tableModel.getDisplayedSeparators().contains(comboFrom.getSelectedItem())) {
			comboFrom.removeItemListener(itemListener);
			comboFrom.setSelectedIndex(0);
			comboFrom.addItemListener(itemListener);
			comboFrom.setEnabled(radioAll.isSelected());
		}
		if(!tableModel.getDisplayedSeparators().contains(comboTo.getSelectedItem())) {
			comboTo.removeItemListener(itemListener);
			comboTo.setSelectedIndex(comboTo.getItemCount()-1);
			comboTo.addItemListener(itemListener);
			comboTo.setEnabled(true);
		}
		
	}
	
	/**
	 * Reloads all values and inserts them accordingly
	 */
	protected void updateValues() {
		updateRange();
		boolean unconnectedRange = false; //Whether we are in selection mode with an unconnected selection
		int[] selected = table.getSelectedRows();
		float categorySum = 0;
		float overallSum = 0;
		for (int i = 0; i < selected.length; i++) {
			selected[i] = table.convertRowIndexToModel(selected[i]);
		}
		//If we have a disconnected range which does not only contain separators, we choose a special approach
		if(radioSelection.isSelected() && !(containsOnlySeparators(selected))) {
			if(selected[selected.length-1] - selected[0] != selected.length -1) unconnectedRange = true;
			//For technical reasons we choose the same approach for one single entry
			if(selected.length == 1 && tableModel.getValueAt(selected[0], 0) instanceof Entry) unconnectedRange = true;
		}
		//Easy: Connected range (or only separators)
		if(!unconnectedRange) {
			Object first = comboFrom.getSelectedItem();
			Object last = comboTo.getSelectedItem();
			//Calculate first and last object
			if(radioSelection.isSelected()) {
				first = tableModel.getValueAt(table.getSelectedRows()[0],0);
				last = tableModel.getValueAt(table.getSelectedRows()[table.getSelectedRowCount()-1],0);
			}
			//Choose the last element as last point if only one separator is selected
			if(first == last && first instanceof EntrySeparator) last = tableModel.getValueAt(tableModel.getRowCount()-1, 0);
			//In general, if the first point is an entry, we have to choose the element before it
			if(first instanceof Entry) first = tableModel.getValueAt(tableModel.indexOf(first)-1, 0);
			//Insert values
			((AccountTableModel)tableAccount.getModel()).setRange(first, last);
			categorySum = 
				saveValueOf(tableModel.getBilancialMapping(last).getOldest().information().getCategoryMappings().get((Category)comboCategory.getSelectedItem())) -
				(first == last? 0 : 
					saveValueOf(tableModel.getBilancialMapping(first).getOldest().information().getCategoryMappings().get((Category)comboCategory.getSelectedItem()))); 
				
			overallSum = 
				saveValueOf(tableModel.getBilancialMapping(last).getOldest().information().getOverallSum()) - 
				(first == last? 0 : 
					saveValueOf(tableModel.getBilancialMapping(first).getOldest().information().getOverallSum()));
			
		}
		//Difficult: Unconnected 
		else {
			Vector<Entry> entries = new Vector<Entry>();
			for (int i = 0; i < selected.length; i++) {
				Object o = tableModel.getValueAt(selected[i], 0);
				if(o instanceof Entry) {
					overallSum += ((Entry)o).getValue();
					if(((Entry)o).getCategory() == comboCategory.getSelectedItem()) {
						categorySum += ((Entry)o).getValue();
					}
					entries.add((Entry)o);
				}
			}
			((AccountTableModel)tableAccount.getModel()).setEntries(entries);
		}
		
		labelCategorySum.setText(DefaultCurrencyFormat.formatAsHTML(categorySum, Fsfibu2Constants.defaultCurrency));
		labelOverallSum.setText(DefaultCurrencyFormat.formatAsHTML(overallSum, Fsfibu2Constants.defaultCurrency));
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
	
	// HELP METHODS ********************************
	// *********************************************
	
	/**
	 * @return true, if and only if the objects in the table model associated to the given indices are all of
	 * type EntrySeparator
	 */
	private boolean containsOnlySeparators(int[] indices) {
		for (int i : indices) {
			if(!(tableModel.getValueAt(i, 0) instanceof EntrySeparator)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return 0, if f == null, the value of f otherwise
	 */
	private static float saveValueOf(Float f) {
		if(f == null) return 0;
		else return f;
	}
	
}
