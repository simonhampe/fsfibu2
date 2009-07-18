package fs.fibu2.view.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;

/**
 * This class implements a tree model displaying  the categories actually used in a journal (plus an additional child for each category which has 
 * subcategories AND entries directly in this category), modulo a certain filter. Additionally for each such category it saves data concerning the sum 
 * over all negative and positive entries in that category (plus of course the complete bilancial) and whether this category should be visible (i.e. 
 * integrated in the calculation in any supercategory) or masked (i.e. displayed under a different name - this is relevant for printing). For each account
 * used in the journal the status before and after the selected entries is monitored, where before means: The sum over all entries in this account before
 * the first entry accepted by this filter (the order is imposed by {@link TableModelComparator}) and after means: The last sum + the sum over all entries 
 * accepted by this filter.
 * @author Simon Hampe
 *
 */
public class BilancialTreeModel implements TreeModel, JournalListener {

	// DATA ************************
	// *****************************
	
	//All categories contained
	private HashSet<Category> used = new HashSet<Category>();
	
	//The direct subcategories of each used category
	private HashMap<Category, Vector<Category>> directSubcategories = new HashMap<Category, Vector<Category>>();
	
	//The bilancials for each category, including subcategories (the last is of course the sum of the first two)
	private HashMap<Category, Float> plus = new HashMap<Category, Float>();
	private HashMap<Category, Float> minus = new HashMap<Category, Float>();
	private HashMap<Category, Float> sum = new HashMap<Category, Float>();
	
	//The bilancials for each category - without subcategories!
	private HashMap<Category, Float> plusIndiv = new HashMap<Category, Float>();
	private HashMap<Category, Float> minusIndiv = new HashMap<Category, Float>();
	private HashMap<Category, Float> sumIndiv = new HashMap<Category, Float>();
	
	//The before/after status of accounts
	private HashMap<Account, Float> before = new HashMap<Account, Float>();
	private HashMap<Account, Float> after = new HashMap<Account, Float>();
	
	//Each category contained in here is invisble. The second set contains the additional category node which
	//	 is inserted, whenever a category has subcategories, but also entries in the category itself.
	private HashSet<Category> invisbles = new HashSet<Category>();
	private HashSet<Category> invisibleIndiv = new HashSet<Category>();
	
	//The mask of each category (null = there is no mask). The second set contains the additional nodes
	private HashMap<Category, String> mask = new HashMap<Category, String>();
	private HashMap<Category, String> maskIndiv = new HashMap<Category, String>();
	
	//The corresponding journal
	private Journal associatedJournal;
	
	private HashSet<TreeModelListener> listenerList = new HashSet<TreeModelListener>();
	
	// CONSTRUCTOR *****************
	// *****************************
	
	/**
	 * Constructs a new model. If node != null, visibility and mask status is read from it. Unknown categories 
	 * will be ignored in this process. 
	 */
	public BilancialTreeModel(Journal j, StackFilter f, Preferences node) {
		
	}
	
	// GETTERS & SETTERS ***********
	// *****************************
	
	/**
	 * @return The sum over all positive entries in this category and subcategories (0, if there are none)
	 */
	public float getCategoryPlus(Category c) {
		Float f = plus.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return The sum over all negative entries in this category and subcategories (0, if there are none)
	 */
	public float getCategoryMinus(Category c) {
		Float f = minus.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return The sum over all entries in this category and subcategories (0, if there are none)
	 */
	public float getCategorySum(Category c) {
		Float f = sum.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return The sum over all positive entries directly in this category (0, if there are none)
	 */
	public float getIndividualPlus(Category c) {
		Float f = plusIndiv.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return The sum over all negative entries directly in this category (0, if there are none)
	 */
	public float getIndividualMinus(Category c) {
		Float f = minusIndiv.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return The sum over all entries directly in this category (0, if there are none)
	 */
	public float getIndividualSum(Category c) {
		Float f = sumIndiv.get(c);
		return f == null? 0 : f;
	}
	
	/**
	 * @return Whether the given catgory node is visible. This does NOT concern the additional node, which might be inserted under a category, whenever
	 * there are subcategories AND entries in a category, but only the overall node of this category
	 */
	public boolean isVisible(Category c) {
		return invisbles.contains(c);
	}
	
	/**
	 * @return Whether the additional category node (which is inserted whenever there are subcategories and entries in c) is visible. Returns also true, 
	 * whenever there is no such node
	 */
	public boolean isVisibleIndiv(Category c) {
		return invisibleIndiv.contains(c);
	}
	
	/**
	 * @return The mask of the given category (NOT the additional child node) or null, if this category is not masked
	 */
	public String getMask(Category c) {
		return mask.get(c);
	}
	
	/**
	 * @return The mask of the additional node for the given category or null, if the node is not masked or does not exist
	 */
	public String getIndividualMask(Category c) {
		return maskIndiv.get(c);
	}
	
	/**
	 * Sets the visibility of the given category node (not the additional node). The root category cannot be made invisible.
	 */
	public void setVisibility(Category c, boolean visible) {
		if(c != null && c != Category.getRootCategory() && used.contains(c)) {
			if(visible) invisbles.remove(c);
			else invisbles.add(c);
			fireTreeNodesChanged(new TreeModelEvent(this,getPath(c),new int[]{getIndex(c)},new Object[]{c}));
		}
	}
	
	// TREEMODEL *******************
	// *****************************
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		if(arg0 != null) listenerList.add(arg0);
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		listenerList.remove(arg0);
	}

	/**
	 * Ignored, since values should only be changed via the corresponding journal.
	 */
	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		//Ignored
	}

	// JOURNALLISTENER ************************
	// ****************************************
	
	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		// TODO Auto-generated method stub
		
	}
	
	// LISTENER MECHANISM ******************
	// *************************************
	
	protected void fireTreeNodesChanged(TreeModelEvent e) {
		for(TreeModelListener l : listenerList) l.treeNodesChanged(e);
	}
	
	protected void fireTreeNodesInserted(TreeModelEvent e) {
		for(TreeModelListener l : listenerList) l.treeNodesInserted(e);
	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		for(TreeModelListener l : listenerList) l.treeNodesRemoved(e);
	}
	
	protected void fireTreeStructureChanged(TreeModelEvent e) {
		for(TreeModelListener l : listenerList) l.treeStructureChanged(e);
	}
	
	/**
	 * @return The tree path to the given category (or null, if this tree does not contain the category)
	 */
	protected TreePath getPath(Category c) {
		if(c == null  || !used.contains(c)) return null;
		if(c == Category.getRootCategory()) return new TreePath(Category.getRootCategory());
		else return getPath(c.parent).pathByAddingChild(c);
	}
	
	/**
	 * @return The index of the given category in its parent category (-1, if this tree does not contain the category and 0 for root)
	 */
	protected int getIndex(Category c) {
		if(c == null || !used.contains(c)) return -1;
		if(c == Category.getRootCategory()) return 0;
		else return directSubcategories.get(c.parent).indexOf(c);
	}

}
