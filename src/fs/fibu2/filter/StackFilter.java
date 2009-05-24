package fs.fibu2.filter;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dom4j.Document;

import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.FilterListModel;
import fs.fibu2.view.render.FilterListRenderer;
import fs.gui.GUIToolbox;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements a stack of filters. Filters can be added / removed via the editor. Each filter can be (de)activated, negated and edited via
 * its own editor which is added to the stack filter's editor as a subcomponent. Finally, an entry will be admitted by this filter, if it is admitted
 * by all active filters in its stack. Each stack filter is associated to a unique editor, which is created the first time getEditor is invoked.
 * @author Simon Hampe
 *
 */
public class StackFilter implements EntryFilter {

	//The stack of filters 
	private Vector<StackFilterElement> filterList;
	
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
	 * @param isActive All filters in listOfFilters, which are contained in this list, are active filters, all others inactive. If null,
	 * all filters are by default active
	 * @param isNegated All filters in listOfFilters, which are contained in this list, are applied inversely, all others normally. If null,
	 * all filters are by default NOT negated
	 */
	public StackFilter(Vector<EntryFilter> listOfFilters, HashSet<EntryFilter> isActive, HashSet<EntryFilter> isNegated) {
		filterList = new Vector<StackFilterElement>();
		for(EntryFilter f : listOfFilters) {
			if(f != null) filterList.add(new StackFilterElement(f,isActive != null? isActive.contains(f) : true,
										isNegated != null? isNegated.contains(f) : false));
		}
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
		for(StackFilterElement f : filterList) {
			//NOT((f active) AND ((e valid wrt f) XOR (f negated)))
			if(!(f.isActive && (f.filter.verifyEntry(e) ^ f.isNegated))) return false;
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
		for(int i = 1; i <= filterList.size(); i++) {
			Preferences iNode = fnode.node(Integer.toString(i));
			iNode.put("isActive", Boolean.toString(filterList.get(i-1).isActive));
			iNode.put("isNegated", Boolean.toString(filterList.get(i-1).isNegated));
			filterList.get(i-1).filter.insertMyPreferences(iNode);
		}
	}
	
	// LOCAL CLASS FOR EDITOR ******************************************
	// *****************************************************************
	
	private class StackFilterEditor extends EntryFilterEditor {	

		/**
		 * compiler-generated serial version uid 
		 */
		private static final long serialVersionUID = 8582487728108859212L;

		private Journal associatedJournal;
		
		private JLabel listLabel = new JLabel();
		private JComboBox filterBox = new JComboBox();
		private JButton addFilterButton = new JButton();
		
		private JPanel editorPanel = new JPanel();
		
		// LISTENERS **********************************
		// ********************************************
		
		//This listener en/disables the add button depending on whether the filter list is empty
		private ListDataListener comboListener = new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {updateButton();}
			@Override
			public void intervalAdded(ListDataEvent e) {updateButton();}
			@Override
			public void intervalRemoved(ListDataEvent e) {updateButton();}
			protected void updateButton() {
				addFilterButton.setEnabled(filterBox.getModel().getSize() > 0);
			}
		};

		//This listener adds filters to the stack when the add button is pressed
		private ActionListener addListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					//Create new instance
					EntryFilter filter = FilterLoader.getFilter(((EntryFilter)filterBox.getSelectedItem()).getID());
					//At first a new filter is active and not negated
					StackFilterElement element = new StackFilterElement(filter,true,false);
					//Add it
					filterList.add(element);
					//Create component
					StackElementEditor editComp = new StackElementEditor(element, associatedJournal);
					editComp.addChangeListener(elementListener);
					editorPanel.add(editComp);
					editComp.setEditing(true);
					editorPanel.revalidate();
				} catch (Exception ex) {
					//This should not happen (by definition of the FilterListModel)
					ex.printStackTrace();
				}
			}
		};
		
		//THis listener listens to changes in the element editors
		private ChangeListener elementListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fireStateChanged();
			}
		};
		
		// CONSTRUCTOR *******************************
		// *******************************************
		
		public StackFilterEditor(Journal j) {
			associatedJournal = j == null? new Journal() : j;
			
			//Init components
			
			listLabel.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.filter") + ": ");
			addFilterButton.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.add"));
			
			filterBox.setModel(new FilterListModel());
			filterBox.getModel().addListDataListener(comboListener);
			filterBox.setRenderer(new FilterListRenderer());
			addFilterButton.setEnabled(filterBox.getModel().getSize() > 0);
			addFilterButton.addActionListener(addListener);
			
			//Layout
			GridBagLayout gbl = new GridBagLayout();
			setLayout(gbl);
			JPanel topPanel = new JPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				topPanel.add(listLabel);
				topPanel.add(filterBox);
				topPanel.add(addFilterButton);
				GridBagConstraints topConst = GUIToolbox.buildConstraints(0, 0, 1, 1);
				topConst.weightx = 100;
				gbl.setConstraints(topPanel, topConst);
				add(topPanel);
			JPanel scrollPanel = new JPanel();
				GridBagLayout scrollLayout = new GridBagLayout();
				scrollPanel.setLayout(scrollLayout);
				GridBagConstraints editorConst = GUIToolbox.buildConstraints(0, 0, 1, 1);
				editorConst.weightx = 100;
				scrollLayout.setConstraints(editorPanel, editorConst);
				scrollPanel.add(editorPanel);
				GridBagConstraints fillConst = GUIToolbox.buildConstraints(0, 1, 1, 1);
				fillConst.weighty = 100;
				JPanel fillPanel = new JPanel();
				scrollLayout.setConstraints(fillPanel, fillConst);
				scrollPanel.add(fillPanel);
			JScrollPane listingPane = new JScrollPane(scrollPanel);
				listingPane.setBorder(null);
				
				editorPanel.setLayout(new BoxLayout(editorPanel,BoxLayout.Y_AXIS));
				for(StackFilterElement e : filterList) {
					StackElementEditor editor = new StackElementEditor(e,associatedJournal);
					editor.addChangeListener(elementListener);
					editorPanel.add(editor);
				}
				GridBagConstraints scrollConst = GUIToolbox.buildConstraints(0, 1, 1, 1);
				scrollConst.weighty = 100;
				gbl.setConstraints(listingPane, scrollConst);
				add(listingPane);
		}
		
		// CONTROL METHODS ***************************
		// *******************************************
		
		// Deletes the ith filter and removes the given component from the editor 
		public void removeFilter(int index, StackElementEditor component) {
			editorPanel.remove(component);
			filterList.remove(index);
			revalidate();
			fireStateChanged();
		}
		
		// FILTER METHODS ****************************
		// *******************************************
		
		//Always returns the stack filter associated to this editor
		@Override
		public EntryFilter getFilter() {
			return StackFilter.this;
		}

		//This editor always has valid content
		@Override
		public boolean hasValidContent() {
			return true;
		}
		
	}
	
	/**
	 * A simple editor for editing a {@link StackFilterElement}
	 * @author Simon Hampe
	 *
	 */
	private class StackElementEditor extends JPanel implements ResourceDependent {

		/**
		 * compiler-generated serial version uid
		 */
		private static final long serialVersionUID = -5819068884030075187L;
		
		private StackFilterElement element;
		private Journal associatedJournal;
		
		private HashSet<ChangeListener> listeners = new HashSet<ChangeListener>(); 
		
		private JLabel descriptionLabel = new JLabel();
		private JToggleButton negateButton = new JToggleButton();
		private JToggleButton activeButton = new JToggleButton();
		private JButton okButton = new JButton();
		private JButton editCancelButton = new JButton();
		private JButton deleteButton = new JButton();
		
		private JPanel descriptionPanel = new JPanel();
		private JPanel editorPanel = new JPanel();
		
		private EntryFilterEditor nextEditor;
		
		private boolean isEditing = false;
		
		private ImageIcon negate 	= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/negate.png"));
		private ImageIcon on		= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/on.png"));
		private ImageIcon off		= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/off.png"));
		private ImageIcon cancel 	= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/cancel.png"));
		private ImageIcon ok		= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/ok.png"));
		private ImageIcon edit 		= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/edit.png"));
		private ImageIcon delete	= new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/StackFilterEditor/delete.png"));
		
		// LISTENERS *************************************************
		// ***********************************************************
		
		private ActionListener negateListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				element.isNegated = !element.isNegated;
				fireStateChanged(new ChangeEvent(element));
			}
		};
		
		private ActionListener activateListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				element.isActive = !element.isActive;
				activeButton.setIcon(element.isActive? on : off);
				fireStateChanged(new ChangeEvent(element));
			}
		};
		
		private ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Stop editing
				setEditing(false);
				//Extract filter
				EntryFilter newfilter = nextEditor.getFilter();
				StackFilterElement newelement = new StackFilterElement(newfilter,element.isActive,element.isNegated);
				//Replace element
				filterList.set(filterList.indexOf(element), newelement);
				element = newelement;
				setEditor(element.filter.getEditor(associatedJournal));
				descriptionLabel.setText(element.filter.getDescription());
				//Notify
				fireStateChanged(new ChangeEvent(element));
			}
		};
		
		private ActionListener editCancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEditing(!isEditing);
			}
		};
		
		private ChangeListener editorListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				okButton.setEnabled(nextEditor.hasValidContent());
			}
		};
		
		private ActionListener deleteListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};
		
		// CONSTRUCTOR ***********************************************
		// ***********************************************************
		
		public StackElementEditor(StackFilterElement e, Journal j) {
			element = e;
			associatedJournal = j == null? new Journal() : j;
			if(element != null) setEditor(element.filter.getEditor(associatedJournal));
			
			//Init components
			setBorder(BorderFactory.createEtchedBorder());
			
			descriptionLabel.setText(element != null? element.filter.getDescription() : "");
			negateButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.negatetooltip"));
			negateButton.setIcon(negate);
			negateButton.setSelected(element != null? element.isNegated : false);
			negateButton.addActionListener(negateListener);
			activeButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.activetooltip"));
			activeButton.setIcon(element != null? (element.isActive? on : off) : off);
			activeButton.setSelected(element != null? element.isActive : false);
			activeButton.addActionListener(activateListener);
			okButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.oktooltip"));
			okButton.setIcon(ok);
			okButton.setVisible(false);
			okButton.addActionListener(okListener);
			editCancelButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.edittooltip"));
			editCancelButton.setIcon(edit);
			editCancelButton.addActionListener(editCancelListener);
			deleteButton.setToolTipText(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.deletetooltip"));
			deleteButton.setIcon(delete);
			deleteButton.addActionListener(deleteListener);
			
			//Layout
			GridBagLayout gbl = new GridBagLayout();
			setLayout(gbl);
			
			JPanel buttonPanel = new JPanel();
				buttonPanel.setBorder(BorderFactory.createEtchedBorder());
				GridBagLayout buttonLayout = new GridBagLayout();
				buttonPanel.setLayout(buttonLayout);
				buttonLayout.setConstraints(negateButton, GUIToolbox.buildConstraints(0, 1, 1, 1));
				buttonPanel.add(negateButton);
				buttonLayout.setConstraints(activeButton, GUIToolbox.buildConstraints(1, 1, 1, 1));
				buttonPanel.add(activeButton);
				JPanel buttonFillPanel = new JPanel();
				GridBagConstraints fillConst = GUIToolbox.buildConstraints(2,1, 1, 1);
				fillConst.weightx = 100;
				buttonLayout.setConstraints(buttonFillPanel, fillConst);
				buttonPanel.add(buttonFillPanel);
				buttonLayout.setConstraints(okButton, GUIToolbox.buildConstraints(3, 1, 1, 1));
				buttonPanel.add(okButton);
				buttonLayout.setConstraints(editCancelButton, GUIToolbox.buildConstraints(4, 1, 1, 1));
				buttonPanel.add(editCancelButton);
				buttonLayout.setConstraints(deleteButton, GUIToolbox.buildConstraints(5, 1, 1, 1));
				buttonPanel.add(deleteButton);
				GridBagConstraints buttonConst = GUIToolbox.buildConstraints(0, 0, 1, 1);
				buttonConst.weightx = 100;
				gbl.setConstraints(buttonPanel, buttonConst);
				add(buttonPanel);
			descriptionPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				descriptionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				descriptionPanel.add(descriptionLabel);
				GridBagConstraints descriptionConst = GUIToolbox.buildConstraints(0, 1, 1, 1);
				descriptionConst.weightx = 100;
				gbl.setConstraints(descriptionPanel, descriptionConst);
				add(descriptionPanel);
			editorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				if(element != null) editorPanel.add(nextEditor);
				GridBagConstraints editorConst = GUIToolbox.buildConstraints(0, 2, 1, 1);
				editorConst.weightx = 100;
				gbl.setConstraints(editorPanel, editorConst);
				add(editorPanel);
				editorPanel.setVisible(false);
		}
		
		// CONTROL METHODS *******************************************
		// ***********************************************************
		
		/**
		 * Manually sets the editing status of this editor. If the flag coincides with the current
		 * status, nothing happens. If it is set to false, while the component is editing, the edit is
		 * cancelled
		 */
		public void setEditing(boolean flag) {
			if(! flag == isEditing) {
				isEditing = flag;
				descriptionPanel.setVisible(!isEditing);
				editorPanel.setVisible(isEditing);
				okButton.setVisible(isEditing);
				editCancelButton.setToolTipText(isEditing? Fsfibu2StringTableMgr.getString("fs.fibu2.filter.StackFilter.canceltooltip") : 
												Fsfibu2StringTableMgr.getString("fs.fibu2.StackFilter.edittooltip"));
				editCancelButton.setIcon(isEditing? cancel : edit);				
			}
		}
		
		protected void setEditor(EntryFilterEditor e) {
			if(nextEditor != null) {
				editorPanel.remove(nextEditor);
				nextEditor.removeChangeListener(editorListener);
			}
			nextEditor = e;
			nextEditor.addChangeListener(editorListener);
			editorPanel.add(nextEditor);
		}
		
		/**
		 * Deletes this filter from the stack
		 */
		public void delete() {
			editor.removeFilter(filterList.indexOf(element), this);
		}
		
		// LISTENER METHODS ******************************************
		// ***********************************************************
		
		public void addChangeListener(ChangeListener l) {
			if(l != null) listeners.add(l);
		}
		
		public void removeChangeListener(ChangeListener l) {
			listeners.remove(l);
		}
		
		protected void fireStateChanged(ChangeEvent e) {
			for(ChangeListener l : listeners) l.stateChanged(e);
		}
		
		// RESOURCEDEPENDENT METHODS *********************************
		// ***********************************************************
		
		@Override
		public void assignReference(ResourceReference r) {
			//Ignored
		}

		@Override
		public Document getExpectedResourceStructure() {
			XMLDirectoryTree tree = new XMLDirectoryTree();
			
			return tree;
		}
		
	}
	
	/**
	 * A stack filter element is a tripel consisting of a filter and two boolean values indicating whether this filter is 
	 * active and negated.
	 * @author Simon Hampe
	 */
	private class StackFilterElement {
		public EntryFilter filter;
		public boolean isActive;
		public boolean isNegated;
		
		public StackFilterElement(EntryFilter filter, boolean isActive, boolean isNegated) {
			this.filter = filter;
			this.isActive = isActive;
			this.isNegated = isNegated;
		}
	}

}
