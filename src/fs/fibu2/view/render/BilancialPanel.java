package fs.fibu2.view.render;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Currency;
import java.util.Iterator;

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

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.model.AccountTableModel;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.SeparatorModel;
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
	private JournalTableModel tableModel;
	private ListSelectionModel selectionModel;
	
	//Listeners
	
	private TableModelListener tableListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			
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
		
		//Init GUI ----------------------------
		
		//Additional components
		JLabel labelTitle = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".title"));
		JLabel labelFrom = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".from"));
			labelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel labelTo = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".to"));
			labelTo.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel labelCategory = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".category"));
		JLabel labelCategoryResult = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".categoryresult"));
		JLabel labelOverall = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".overall"));
		
		//Init member components
		radioAll.setText(Fsfibu2StringTableMgr.getString(sgroup + ".radioall"));
		radioSelection.setText(Fsfibu2StringTableMgr.getString(sgroup + ".radioselected"));
			ButtonGroup group = new ButtonGroup();
			group.add(radioAll);
			group.add(radioSelection);
		
		comboFrom.setModel(new SeparatorModel(tableModel));
			comboFrom.setRenderer(new SeparatorRenderer());
		comboTo.setModel(new SeparatorModel(tableModel));
			comboTo.setRenderer(new SeparatorRenderer());
			if(comboTo.getModel().getSize() > 0) comboTo.setSelectedItem(comboTo.getModel().getElementAt(comboTo.getModel().getSize()-1));
		
		comboCategory.setModel(new CategoryListModel(tableModel.getAssociatedJournal(),false));
			comboCategory.setRenderer(new CategoryListRenderer(" > "));
			
		//Layout ----------------------------
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
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
			GridBagConstraints gcFirst = GUIToolbox.buildConstraints(0, 1, 1, 1);
			gcFirst.weightx = 33;
			layout.setConstraints(firstPanel, gcFirst);
		add(firstPanel);
			
		JScrollPane secondPanel = new JScrollPane(tableAccount);
			GridBagConstraints gcSecond = GUIToolbox.buildConstraints(1, 1, 1, 1);
			gcSecond.weightx = 33;
			layout.setConstraints(secondPanel, gcSecond);
		add(secondPanel);
			
		JPanel thirdPanel = new JPanel();
			GridBagConstraints gcThird = GUIToolbox.buildConstraints(2, 1, 1, 1);
			gcThird.weightx = 33;
			layout.setConstraints(thirdPanel, gcThird);
		add(thirdPanel);
			
			
//		GridLayout layout = new GridLayout(1,3);
//		setLayout(layout);
//		
//		//First panel
//		JPanel firstPanel = new JPanel();
//			firstPanel.setBorder(BorderFactory.createEtchedBorder());
//			Box firstBox = new Box(BoxLayout.Y_AXIS);
//				Box fromBox = new Box(BoxLayout.X_AXIS);
//				fromBox.setAlignmentX(LEFT_ALIGNMENT);
//					fromBox.add(labelFrom); fromBox.add(comboFrom);
//				Box toBox = new Box(BoxLayout.X_AXIS);
//				toBox.setAlignmentX(LEFT_ALIGNMENT);
//					toBox.add(labelTo); toBox.add(comboTo);
//			firstBox.add(fromBox);firstBox.add(Box.createVerticalStrut(5)); firstBox.add(toBox);
//			firstPanel.add(firstBox);
//		add(firstPanel);
//		
//		//Second panel
//		JScrollPane secondPanel = new JScrollPane(tableAccount);
//		add(secondPanel);
//		
//		//Third panel
//		JPanel thirdPanel = new JPanel();
//			Box thirdBox = new Box(BoxLayout.Y_AXIS);
//				Box catBox = new Box(BoxLayout.X_AXIS);
//					catBox.add(labelCategory); catBox.add(comboCategory);
//				Box crBox = new Box(BoxLayout.X_AXIS);
//					crBox.add(labelCategoryResult); crBox.add(labelCategorySum);
//				Box ovBox = new Box(BoxLayout.X_AXIS);
//					ovBox.add(labelOverall); ovBox.add(labelOverallSum);
//			thirdBox.add(catBox); thirdBox.add(Box.createVerticalStrut(5)); 
//			thirdBox.add(crBox); thirdBox.add(Box.createVerticalStrut(5)); 
//			thirdBox.add(ovBox);
//		thirdPanel.add(thirdBox);
//		add(thirdPanel);	
	}
	
}
