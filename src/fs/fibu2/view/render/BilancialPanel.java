package fs.fibu2.view.render;

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

import fs.fibu2.view.model.JournalTableModel;
import fs.gui.SwitchIconLabel;
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
	
	private JComboBox comboAccount = new JComboBox();
	private JLabel labelAccountBefore = new JLabel();
	private JLabel labelAccountAfter = new JLabel();
	
	private JComboBox comboCategory = new JComboBox();
	private JLabel labelCategory = new JLabel();
	
	
	
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
		
		//Init GUI
		
	}
	
}
