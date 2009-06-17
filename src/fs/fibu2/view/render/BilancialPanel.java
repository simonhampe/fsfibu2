package fs.fibu2.view.render;

import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.AccountListModel;
import fs.fibu2.view.model.CategoryListModel;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.model.SeparatorModel;

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
	
	private JComboBox comboAccount = new JComboBox();
	private JLabel labelAccountBefore = new JLabel();
	private JLabel labelAccountAfter = new JLabel();
	
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
		JLabel labelTo = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".to"));
		JLabel labelAccount = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".account"));
		JLabel labelBefore = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".before"));
		JLabel labelAfter = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".after"));
		JLabel labelCategory = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".category"));
		JLabel labelCategoryResult = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".categoryresult"));
		JLabel labelOverall = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".overall"));
		
		//Init member components
		radioAll.setText(sgroup + ".radioall");
		radioSelection.setText(sgroup + ".radioselected");
		
		comboFrom.setModel(new SeparatorModel(tableModel));
			comboFrom.setRenderer(new SeparatorRenderer());
		comboTo.setModel(new SeparatorModel(tableModel));
			comboTo.setRenderer(new SeparatorRenderer());
			if(comboTo.getModel().getSize() > 0) comboTo.setSelectedItem(comboTo.getModel().getElementAt(comboTo.getModel().getSize()-1));
			
		comboAccount.setModel(new AccountListModel(tableModel.getAssociatedJournal()));
			comboAccount.setRenderer(new AccountListRenderer());
		
		comboCategory.setModel(new CategoryListModel(tableModel.getAssociatedJournal(),false));
			comboCategory.setRenderer(new CategoryListRenderer(" > "));
		
		//Layout ----------------------------
		
		GridLayout layout = new GridLayout(1,3);
		setLayout(layout);
		
		//First panel
		JPanel firstPanel = new JPanel();
			Box firstBox = new Box(BoxLayout.Y_AXIS);
		
	}
	
}
