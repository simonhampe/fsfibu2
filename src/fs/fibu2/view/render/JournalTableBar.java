package fs.fibu2.view.render;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.fibu2.application.Fsfibu2;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.JournalTableModel;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * This class implements a toolbar with basic operations for a {@link JournalTable}, such as entry editing and view preferences.
 * @author Simon Hampe
 *
 */
public class JournalTableBar extends JToolBar implements ResourceDependent {

	// DATA *****************************************
	// **********************************************
	
	private JournalTable table;
	private Journal associatedJournal;
	
	private final static String sgroup = "fs.fibu2.view.	JournalTableBar";
	
	// COMPONENTS ***********************************
	// **********************************************
	
	private JButton newButton = new JButton();
	private JButton editButton = new JButton();
	private JButton deleteButton = new JButton();
	private JButton editSeparatorsButton = new JButton();
	private JToggleButton showYearSeparatorButton = new JToggleButton();
	private JToggleButton showReadingPointsButton = new JToggleButton();
	
	// LISTENERS *************************************
	// ***********************************************
	
	private ActionListener newListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			EntryDialog newDialog = new EntryDialog(Fsfibu2.getFrame(),associatedJournal,null);
			newDialog.addDataRetrievalListener(newRetrievalListener);
			newDialog.setVisible(true);
		}
	};
	
	private DataRetrievalListener newRetrievalListener = new DataRetrievalListener() {
		@Override
		public void dataReady(Object source, Object data) {
			if(data != null) {
				associatedJournal.addEntryUndoable((Entry)data);
			}
		}
	};
	
	private ActionListener editListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int[] selection = table.getSelectedRows();
			for(int row : selection) {
				Object rowObject = table.getJournalTableModel().getValueAt(row, 0);
				if(rowObject instanceof Entry) {
					EntryDialog editDialog = EntryDialogFactory.getInstance(associatedJournal).getDialog((Entry)rowObject);
					editDialog.addDataRetrievalListener(editRetrievalListener);
					editDialog.setVisible(true);
				}
			}
		}
	};
	
	private DataRetrievalListener editRetrievalListener = new DataRetrievalListener() {
		@Override
		public void dataReady(Object source, Object data) {
			if(data != null) {
				associatedJournal.replaceEntryUndoable(((EntryDialog)source).getOriginalEntry(), (Entry)data);
			}
		}
	};
	
	private ActionListener deleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int[] selection = table.getSelectedRows();
			HashSet<Entry> entriesToDelete = new HashSet<Entry>();
			for(int row : selection) {
				Object rowObject = table.getJournalTableModel().getValueAt(row, 0);
				if(rowObject instanceof Entry) {
					entriesToDelete.add((Entry)rowObject);
				}
			}
			int ans = JOptionPane.showConfirmDialog(Fsfibu2.getFrame(), 
					entriesToDelete.size() > 1? Fsfibu2StringTableMgr.getString(sgroup + ".confirmdelete") : 
												Fsfibu2StringTableMgr.getString(sgroup + ".confirmdeletesingular"), 
					Fsfibu2StringTableMgr.getString(sgroup + ".confirmdelettitle"), JOptionPane.YES_NO_OPTION);
			if(ans == JOptionPane.YES_OPTION) {
				associatedJournal.removeAllEntriesUndoable(entriesToDelete);
			}
		}
	};
	
	//TODO: Add separator editor
	
	private ActionListener showYearSeparatorListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			table.getJournalTableModel().setYearSeparatorsVisible(!table.getJournalTableModel().areYearSeparatorsVisible());
		}
	};
	
	private ActionListener showReadingPointsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			table.getJournalTableModel().setReadingPointsVisible(!table.getJournalTableModel().areReadingPointsVisible());
		}
	};
	
	// CONSTRUCTOR *************************************
	// *************************************************
	
	public JournalTableBar(JournalTable t) {
		super(SwingConstants.HORIZONTAL);
		table = t == null? new JournalTable(new JournalTableModel(new Journal(),null,false,false,false)) : table;
		associatedJournal = table.getJournalTableModel().getAssociatedJournal();
		
		//Init buttons
		newButton.addActionListener(newListener);
		editButton.addActionListener(editListener);
		deleteButton.addActionListener(deleteListener);
		
		showYearSeparatorButton.addActionListener(showYearSeparatorListener);
		showReadingPointsButton.addActionListener(showReadingPointsListener);
		
		
		//Add buttons
		add(newButton);
		add(editButton);
		add(deleteButton);
		add(new JToolBar.Separator());
		add(showYearSeparatorButton);
		add(showReadingPointsButton);
		
	}
	
	// RESOURCEDEPENDENT METHODS ***********************
	// *************************************************
	
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
