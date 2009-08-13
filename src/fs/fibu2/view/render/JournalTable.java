package fs.fibu2.view.render;

import java.util.Currency;

import javax.swing.JTable;

import fs.fibu2.data.model.Journal;
import fs.fibu2.view.model.JournalTableModel;

/**
 * This class implements a JTable with a few general settings useful for displaying fsfibu2 {@link Journal}s, as well as some
 * more type-specific methods for model retrieval and such
 * @author Simon Hampe
 *
 */
public class JournalTable extends JTable {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = 4204996320101119252L;
	
	private JournalTableModel internalModel;
	
	/**
	 * Creates a new table, using the given model
	 * @throws IllegalArgumentException - if model == null
	 */
	public JournalTable(JournalTableModel model) throws IllegalArgumentException{
		super(model);
		if(model == null) throw new IllegalArgumentException("Cannot create table from null model");
		internalModel = model;
		setDefaultRenderer(Object.class, new JournalTableRenderer(model,Currency.getInstance("EUR")));
		setDragEnabled(false);
		getTableHeader().setReorderingAllowed(false);
		//Adapt column widths
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		columnModel.getColumn(0).setPreferredWidth(36);
		columnModel.getColumn(1).setPreferredWidth(350);
		columnModel.getColumn(2).setPreferredWidth(100);
		columnModel.getColumn(3).setPreferredWidth(100);
		columnModel.getColumn(4).setPreferredWidth(100);
		columnModel.getColumn(5).setPreferredWidth(250);
		columnModel.getColumn(6).setPreferredWidth(150);
		columnModel.getColumn(7).setPreferredWidth(500);
		
		
		
		
	}
	
	public JournalTableModel getJournalTableModel()  {
		return internalModel;
	}
	
}
