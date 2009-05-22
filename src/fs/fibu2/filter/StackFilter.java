package fs.fibu2.filter;

import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.FilterListModel;
import fs.fibu2.view.render.FilterListRenderer;
import fs.gui.SwitchIconLabel;

/**
 * This class implements a stack of filters. Filters can be added / removed via the editor. Each filter can be (de)activated, negated and edited via
 * its own editor which is added to the stack filter's editor as a subcomponent. Finally, an entry will be admitted by this filter, if it is admitted
 * by all active filters in its stack. Each stack filter is associated to a unique editor, which is created the first time getEditor is invoked.
 * @author Simon Hampe
 *
 */
public class StackFilter implements EntryFilter {

	//The stack of filters 
	private Vector<EntryFilter> filterStack = new Vector<EntryFilter>();
	//All filters in this set are active, all others are not
	private HashSet<EntryFilter> isActive = new HashSet<EntryFilter>();
	//All filters in this set are negated, all others are not
	private HashSet<EntryFilter> isNegated = new HashSet<EntryFilter>();
	
	//The editor for this filter
	private StackFilterEditor editor;
	
	// CONSTRUCTOR ****************************
	// ****************************************
	
	/**
	 * Constructs an empty stack filter
	 */
	public StackFilter() {
		this(null,null,null);
	}
	
	/**
	 * Constructs a stack filter.
	 * @param listOfFilters The list of filters in this stack
	 * @param isActive All filters in listOfFilters, which are contained in this list, are active filters, all others inactive
	 * @param isNegated All filters in listOfFilters, which are contained in this list, are applied inversely, all others normally
	 */
	public StackFilter(Vector<EntryFilter> listOfFilters, HashSet<EntryFilter> isActive, HashSet<EntryFilter> isNegated) {
		this.filterStack = listOfFilters != null? new Vector<EntryFilter>(listOfFilters) : new Vector<EntryFilter>();
		this.isActive = isActive != null? new HashSet<EntryFilter>(isActive) : new HashSet<EntryFilter>();
		this.isNegated = isNegated != null? new HashSet<EntryFilter>(isNegated) : new HashSet<EntryFilter>();
	}
	
	// FILTER METHODS *************************
	// ****************************************
	
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.description");
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		if(editor == null) {
			editor = new StackFilterEditor(j);
		}
		return editor;
	}

	@Override
	public String getID() {
		return "ff2filter_stack";
	}

	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.name");
	}

	/**
	 * @return true, if and only if e != null and all active filters verify e (i.e. return true, if they are not
	 * negated, false otherwise)
	 */
	@Override
	public boolean verifyEntry(Entry e) {
		if(e == null) return false;
		for(EntryFilter f : filterStack) {
			//NOT((f active) AND ((e valid wrt f) XOR (f negated)))
			if(!(isActive.contains(f) && (f.verifyEntry(e) ^ isNegated.contains(f)))) return false;
		}
		return true;
	}

	@Override
	public EntryFilter createMeFromPreferences(Preferences filterNode)
			throws IllegalArgumentException {
		if(filterNode == null) throw new IllegalArgumentException("Cannot create filter from null node");
		int i = 1;
		try {
			Vector<EntryFilter> listOfFilters = new Vector<EntryFilter>();
			HashSet<EntryFilter> listOfActive = new HashSet<EntryFilter>();
			HashSet<EntryFilter> listOfNegated = new HashSet<EntryFilter>();
			while(filterNode.nodeExists(Integer.toString(i))) {
				Preferences iNode = filterNode.node(Integer.toString(i));
				boolean active = iNode.getBoolean("isActive", false);
				boolean negated = iNode.getBoolean("isNegated", false);
				if(iNode.nodeExists("filter")) {
					Preferences subNode = iNode.node("filter");
					String id = subNode.get("id", null);
					try {
						EntryFilter newFilter = FilterLoader.getFilter(id).createMeFromPreferences(subNode);
						listOfFilters.add(newFilter);
						if(active) listOfActive.add(newFilter);
						if(negated) listOfNegated.add(newFilter);
					} catch (InstantiationException e) {
						throw new IllegalArgumentException("Cannot create filter: " + e.getMessage());
					}
				}
			}
			return new StackFilter(listOfFilters,listOfActive,listOfNegated);
		}
		catch(BackingStoreException be) {
			throw new IllegalArgumentException("Cannot create filter. Backing store unavailable");
		}
	}

	@Override
	public void insertMyPreferences(Preferences node)
			throws NullPointerException {
		if(node == null) throw new NullPointerException("Cannot insert preferences into null node");
		try {
			//Clear all existing preferences nodes
			if(node.nodeExists("filter")) {
				node.node("filter").removeNode();
			}
		}
		catch(BackingStoreException be) {
			throw new NullPointerException("Cannot insert preferences: Backing store inavailable");
		}
		Preferences fnode = node.node("filter");
		AbstractFilterPreferences.insert(fnode, Selection.EQUALITY, getID(), null, null, null, null); //The type paramter is irrelevant but has to be specified
		for(int i = 1; i <= filterStack.size(); i++) {
			Preferences iNode = fnode.node(Integer.toString(i));
			iNode.put("isActive", Boolean.toString(isActive.contains(filterStack.get(i-1))));
			iNode.put("isNegated", Boolean.toString(isNegated.contains(filterStack.get(i-1))));
			filterStack.get(i-1).insertMyPreferences(iNode);
		}
	}
	
	// LOCAL CLASS FOR EDITOR ******************************************
	// *****************************************************************
	
	private class StackFilterEditor extends EntryFilterEditor {	

		private Journal associatedJournal;
		
		private JLabel listLabel = new JLabel();
		private JComboBox filterList = new JComboBox();
		private JButton addFilterButton = new JButton();
		
		private JPanel editorPanel = new JPanel();
		
		//This listener en/disables the add button depending on whether the filter list is empty
		private ListDataListener comboListener = new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {updateButton();}
			@Override
			public void intervalAdded(ListDataEvent e) {updateButton();}
			@Override
			public void intervalRemoved(ListDataEvent e) {updateButton();}
			protected void updateButton() {
				addFilterButton.setEnabled(filterList.getModel().getSize() > 0);
			}
		};
		
		// CONSTRUCTOR *******************************
		// *******************************************
		
		public StackFilterEditor(Journal j) {
			associatedJournal = j == null? new Journal() : j;
			
			//Init components
			listLabel.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.filter"));
			addFilterButton.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.add"));
			
			filterList.setModel(new FilterListModel());
			filterList.getModel().addListDataListener(comboListener);
			filterList.setRenderer(new FilterListRenderer());
			addFilterButton.setEnabled(filterList.getModel().getSize() > 0);
			
			JScrollPane scrollPane = new JScrollPane(editorPanel);
			
			//Layout
			Box layout = new Box(BoxLayout.Y_AXIS);
			Box firstBox = new Box(BoxLayout.X_AXIS);
				firstBox.setAlignmentX(LEFT_ALIGNMENT);
				firstBox.add(listLabel);
				firstBox.add(filterList);
				firstBox.add(addFilterButton);
				firstBox.add(Box.createHorizontalGlue());
			layout.add(firstBox);
			layout.add(scrollPane);
			add(layout);
		}
		
		// FILTER METHODS ****************************
		// *******************************************
		
		@Override
		public EntryFilter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasValidContent() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

}