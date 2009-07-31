package fs.fibu2.module;

import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.dom4j.Document;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.view.model.BilancialAccountModel;
import fs.fibu2.view.model.BilancialTableModel;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.fibu2.view.render.BilancialTree;
import fs.fibu2.view.render.MoneyCellRenderer;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * Contains a bilancial view of a journal together with a toolbar for editing filters, etc.
 * @author Simon Hampe
 *
 */
public class BilancialPane extends JPanel implements ResourceDependent {

	// COMPONENTS *************************
	// ************************************
	
	private BilancialTree tree;
	private JTable table;
	private JTable accountTable;
	
	private JToolBar bar;
	
	private JButton filterButton = new JButton();
	
	// DATA *******************************
	// ************************************
	
	private StackFilter filter;
	private Journal associatedJournal;
	
	// LISTENERS **************************
	// ************************************
	
	// MISC *******************************
	// ************************************
	
	private final static String sgroup = "fs.fibu2.module.BilancialPane";
	
	// CONSTRUCTOR ************************
	// ************************************
	
	/**
	 * Construct a filter pane for the given journal, using the given filter and the given preferences. If node == null, default values are used.
	 * Filter values from node override f.
	 */
	public BilancialPane(Journal j, StackFilter f, Preferences node) {
		associatedJournal = j == null? new Journal() : j;
		filter = f == null? new StackFilter() : f;
		
		//Read out preferences
		//TODO: Read out preferences ( And insert them into the model!!)
		
		//Init GUI
		BilancialTreeModel model = new BilancialTreeModel(associatedJournal,filter,null);
		tree = new BilancialTree(model);
		table = new JTable();
			table.setModel(new BilancialTableModel(tree));
			table.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
		accountTable = new JTable();
			accountTable.setModel(new BilancialAccountModel(model));
			accountTable.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
		
	}
	
	// RESOURCEDEPENDENT ******************
	// ************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		// TODO Auto-generated method stub
		return null;
	}

}
