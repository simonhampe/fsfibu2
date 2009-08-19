package fs.fibu2.view.render;

import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.StartValueModel;
import fs.gui.FrameworkDialog;
import fs.xml.PolyglotStringTable;

/**
 * A simple dialog containing a table for editing account start values. There is only one instance of it per Journal
 * @author Simon Hampe
 *
 */
public class StartValueDialog extends FrameworkDialog {
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -7900212183138493568L;
	private static HashMap<Journal, StartValueDialog> global_instance = new HashMap<Journal, StartValueDialog>();
	
	protected StartValueDialog(Journal j) {
		super(Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(), PolyglotStringTable.getGlobalLanguageID());
		setTitle(Fsfibu2StringTableMgr.getString("fs.fibu2.view.StartValueDialog.title"));
		JTable table = new JTable();
			table.setModel(new StartValueModel(j));
			table.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
			table.setDragEnabled(false);
		JScrollPane pane = new JScrollPane(table);
			Dimension dim = table.getPreferredSize();
			pane.setPreferredSize(new Dimension(2*dim.width, 2*dim.height));
		add(pane);
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	public static StartValueDialog getInstance(Journal j) {
		StartValueDialog diag = global_instance.get(j);
		if(diag == null) {
			diag = new StartValueDialog(j);
			global_instance.put(j, diag);
		}
		return diag;
	}
	
}
