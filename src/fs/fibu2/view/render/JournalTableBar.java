package fs.fibu2.view.render;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.fibu2.application.Fsfibu2;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements a toolbar with basic operations for a {@link JournalTable}, such as entry editing and view preferences.
 * @author Simon Hampe
 *
 */
public class JournalTableBar extends JToolBar implements ResourceDependent {

	// DATA *****************************************
	// **********************************************
	
	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = -6594355845943200915L;
	
	private JournalTable table;
	private Journal associatedJournal;
	
	private final static String sgroup = "fs.fibu2.view.JournalTableBar";
	
	// COMPONENTS ***********************************
	// **********************************************
	
	private JButton newButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".newentry"));
	private JButton editButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".editentry"));
	private JButton deleteButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".deleteentry"));
	private JButton editSeparatorsButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".editseparator"));
	private JToggleButton showYearSeparatorButton = new JToggleButton(Fsfibu2StringTableMgr.getString(sgroup  + ".displayyear"));
	private JToggleButton showReadingPointsButton = new JToggleButton(Fsfibu2StringTableMgr.getString(sgroup + ".displayreading"));
	
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
					Fsfibu2StringTableMgr.getString(sgroup + ".confirmdeletetitle"), JOptionPane.YES_NO_OPTION);
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
	
	//Disables/enables buttons according to current table selection
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int[] selection = table.getSelectedRows();
			if(selection.length == 0) {
				editButton.setEnabled(false);
				deleteButton.setEnabled(false);
			}
			else {
				boolean containsEntries = false;
				for(int row : selection) 
					if(table.getJournalTableModel().getValueAt(row, 0) instanceof Entry) { containsEntries = true;break;}
				editButton.setEnabled(containsEntries);
				deleteButton.setEnabled(containsEntries);
			}
		}
	};
	
	// CONSTRUCTOR *************************************
	// *************************************************
	
	public JournalTableBar(JournalTable t) {
		super(SwingConstants.HORIZONTAL);
		table = t == null? new JournalTable(new JournalTableModel(new Journal(),null,false,false)) : t;
		associatedJournal = table.getJournalTableModel().getAssociatedJournal();
		setLayout(new FlowLayout());
		
		//Init buttons
		String path = "graphics/JournalTableBar";
		Fsfibu2DefaultReference ref = Fsfibu2DefaultReference.getDefaultReference();
		newButton.addActionListener(newListener);
			newButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "/new.png")));
			newButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".newtooltip"));
		editButton.addActionListener(editListener);
			editButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "/edit.png")));
			editButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".edittooltip"));
		deleteButton.addActionListener(deleteListener);
			deleteButton.setIcon(new ImageIcon(ref.getFullResourcePath(this, path + "/delete.png")));
			deleteButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".deletetooltip"));
		
			editSeparatorsButton.setIcon(new ImageIcon(ref.getFullResourcePath(this,path + "/editsep.png")));
			editSeparatorsButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".editseptooltip"));
		
		showYearSeparatorButton.setSelected(table.getJournalTableModel().areYearSeparatorsVisible());
		showYearSeparatorButton.addActionListener(showYearSeparatorListener);
			showYearSeparatorButton.setIcon(new ImageIcon(ref.getFullResourcePath(this,path + "/year.png")));
			showYearSeparatorButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".yeartooltip"));
			
		showReadingPointsButton.setSelected(table.getJournalTableModel().areReadingPointsVisible());
		showReadingPointsButton.addActionListener(showReadingPointsListener);
			showReadingPointsButton.setIcon(new ImageIcon(ref.getFullResourcePath(this,path + "/reading.png")));
			showReadingPointsButton.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".readingtooltip"));
		
		table.getSelectionModel().addListSelectionListener(selectionListener);
		selectionListener.valueChanged(new ListSelectionEvent(this,0,0,false));
		
		//Add buttons
		add(newButton);
		add(editButton);
		add(deleteButton);
		add(new JToolBar.Separator());
		add(editSeparatorsButton);
		add(showYearSeparatorButton);
		add(showReadingPointsButton);	
	}
	
	// RESOURCEDEPENDENT METHODS ***********************
	// *************************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignore
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		String path = "graphics/JournalTableBar/";
		tree.addPath(path + "new.png");
		tree.addPath(path + "edit.png");
		tree.addPath(path + "delete.png");
		tree.addPath(path + "editsep.png");
		tree.addPath(path + "year.png");
		tree.addPath(path + "reading.png");
		return tree;
	}

}
