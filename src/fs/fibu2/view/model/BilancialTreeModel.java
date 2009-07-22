package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.sun.org.apache.xpath.internal.operations.Bool;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.view.event.ProgressListener;

/**
 * This class implements a tree model displaying  the categories actually used in a journal (plus an additional child for each category which has 
 * subcategories AND entries directly in this category), modulo a certain filter. Additionally for each such category it saves data concerning the sum 
 * over all negative and positive entries in that category (plus of course the complete bilancial) and whether this category should be visible (i.e. 
 * integrated in the calculation in any supercategory) or masked (i.e. displayed under a different name - this is relevant for printing). For each account
 * used in the journal the status before and after the selected entries is monitored, where before means: The sum over all entries in this account before
 * the first entry accepted by this filter (the order is imposed by {@link TableModelComparator}) and after means: The last sum + the sum over all entries 
 * accepted by this filter. Visibility and mask information is lost, whenever a node is removed from the model (e.g. the filter changes and there are
 * no more entries in a certain category).
 * @author Simon Hampe
 *
 */
public class BilancialTreeModel implements TreeModel, JournalListener {

	// DATA ************************
	// *****************************
	
	//All categories contained
	private HashSet<ExtendedCategory> used = new HashSet<ExtendedCategory>();
	
	//The direct subcategories of each used category
	private HashMap<ExtendedCategory, Vector<ExtendedCategory>> directSubcategories = new HashMap<ExtendedCategory, Vector<ExtendedCategory>>();
	
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
	
	//Each category contained in here is invisble. 
	private HashSet<ExtendedCategory> invisibles = new HashSet<ExtendedCategory>();
	
	//The mask of each category (null = there is no mask). 
	private HashMap<ExtendedCategory, String> mask = new HashMap<ExtendedCategory, String>();
	
	//The corresponding journal
	private Journal associatedJournal;
	//The filter used by this model
	private StackFilter filter;
	
	private HashSet<TreeModelListener> listenerList = new HashSet<TreeModelListener>();
	private HashSet<ProgressListener<Object, Object>> progressListeners = new HashSet<ProgressListener<Object,Object>>();
	
	private Recalculator runningInstance = null;
	
	// CONSTRUCTOR *****************
	// *****************************
	
	/**
	 * Constructs a new model. If node != null, visibility and mask status is read from it. Unknown categories 
	 * will be ignored in this process. 
	 */
	public BilancialTreeModel(Journal j, StackFilter f, Preferences node) {
		associatedJournal = j == null? new Journal() : j;
		filter = f == null? new StackFilter() : f;
		
		DataVector v = recalculateModel();
			used = v.used;
			directSubcategories = v.directSubcategories;
			before = v.before;
			after = v.after;
			sum = v.sum;
			plus = v.plus;
			minus = v.minus;
			sumIndiv = v.sumIndiv;
			plusIndiv = v.plusIndiv;
			minusIndiv = v.minusIndiv;
		
		//TODO: Extract preferences
	}
	
	// RECALCULATION ***************
	// *****************************
	
	/**
	 * Starts a recalculator in a separate thread. If one is already running, it is cancelled
	 */
	protected void recalculate() {
		if(runningInstance != null) {
			runningInstance.cancel(true);
		}
		runningInstance = new Recalculator();
		fireTaskBegins(runningInstance);
		runningInstance.execute();
	}
	
	protected DataVector recalculateModel() {
		DataVector v = new DataVector();
		
		//Whether an entry has already been accepted by the filter
		boolean entriesAccepted = false;
		
		//The bilancial of all entries 'before'
		BilancialInformation biBefore = new BilancialInformation(associatedJournal);
		//The overall bilancial of all accepted entries (divided into minus, plus, sum) and of each separate category
		BilancialInformation biAcceptedPlus = new BilancialInformation();
		BilancialInformation biAcceptedMinus = new BilancialInformation();
		BilancialInformation biAcceptedSum = new BilancialInformation();
		HashMap<Category, BilancialInformation> biAcceptedPlusIndiv = new HashMap<Category, BilancialInformation>();
		HashMap<Category, BilancialInformation>  biAcceptedMinusIndiv = new HashMap<Category, BilancialInformation>();
		HashMap<Category, BilancialInformation>  biAcceptedSumIndiv = new  HashMap<Category, BilancialInformation>();
		//The overall bilancial of all 'before' and accepted entries
		BilancialInformation biOverall = new BilancialInformation();
		
		//The sorted set of all entries
		TreeSet<Entry> entries = new TreeSet<Entry>(new TableModelComparator());
			entries.addAll(associatedJournal.getEntries());
		
			for(Entry e : entries) {
			if(filter.verifyEntry(e)) {
				entriesAccepted = true;
				
				//Increment bilancial
				if(!biAcceptedMinusIndiv.containsKey(e.getCategory())) biAcceptedMinusIndiv.put(e.getCategory(), new BilancialInformation());
				if(!biAcceptedPlusIndiv.containsKey(e.getCategory())) biAcceptedPlusIndiv.put(e.getCategory(), new BilancialInformation());
				if(!biAcceptedSumIndiv.containsKey(e.getCategory())) biAcceptedSumIndiv.put(e.getCategory(), new BilancialInformation());
				if(e.getValue() >= 0) {
					biAcceptedPlus.increment(e);
					biAcceptedPlusIndiv.get(e.getCategory()).increment(e);
				}
				else {
					biAcceptedMinus.increment(e);
					biAcceptedMinusIndiv.get(e.getCategory()).increment(e);
				}
				
				biAcceptedSumIndiv.get(e.getCategory()).increment(e);
				biAcceptedSum.increment(e);
				biOverall.increment(e);
				
			}
			else {
				if(!entriesAccepted) {
					biBefore.increment(e);
					biOverall.increment(e);
				}
			}
		}
		
		//Copy data:
			
		//Extract categories & subcategories
		v.used.add(new ExtendedCategory(Category.getRootCategory(),false));
		for(Category c : biAcceptedSum.getCategoryMappings().keySet()) {
			while(c != Category.getRootCategory()) {
				ExtendedCategory ec = new ExtendedCategory(c,false); 
				ExtendedCategory ecp = new ExtendedCategory(c.parent,false);
				v.used.add(ec);
				if(v.directSubcategories.get(ecp) == null) v.directSubcategories.put(ecp, new Vector<ExtendedCategory>());
				v.directSubcategories.get(ecp).add(ec);
				c = c.parent;
			}
		}
		
		//Sort subcategories
		for(ExtendedCategory ecp : v.directSubcategories.keySet()) {
			TreeSet<ExtendedCategory> scs = new TreeSet<ExtendedCategory>(new ExtCatComparator());
				scs.addAll(v.directSubcategories.get(ecp));
			v.directSubcategories.put(ecp,new Vector<ExtendedCategory>(scs));
		}
		
		//Find additional nodes and copy bilancials
		for(ExtendedCategory ec : v.used) {
			//Copy own bilancials
			v.minus.put(ec.category(), biAcceptedMinus.getCategoryMappings().get(ec.category()));
			v.plus.put(ec.category(), biAcceptedPlus.getCategoryMappings().get(ec.category()));
			v.sum.put(ec.category(), biAcceptedSum.getCategoryMappings().get(ec.category()));
			//Copy individual bilancials
			if(biAcceptedSumIndiv.containsKey(ec.category())) {
				v.minusIndiv.put(ec.category(), biAcceptedMinusIndiv.get(ec.category()).getCategoryMappings().get(ec.category()));
				v.plusIndiv.put(ec.category(), biAcceptedPlusIndiv.get(ec.category()).getCategoryMappings().get(ec.category()));
				v.sumIndiv.put(ec.category(), biAcceptedSumIndiv.get(ec.category()).getCategoryMappings().get(ec.category()));
				//If in addition this category has subcategories, add the additional node
				if(v.directSubcategories.get(ec) != null && v.directSubcategories.get(ec).size() > 0) {
					ExtendedCategory newec = new ExtendedCategory(ec.category(),true);
					v.used.add(newec);
					v.directSubcategories.get(ec).add(0, newec);
				}
			}
		}
		
		v.before = biBefore.getAccountMappings();
		v.after = biOverall.getAccountMappings();
		
		
		return v;
	}
	
	/**
	 * Copies the values from the given data vector and fires appropriate listener calls. Visibility and mask status of removed categories
	 * are lost. This call is ignored, if v == null
	 */
	protected void adoptChanges(DataVector v) {
		if(v == null) return;
		
		HashSet<TreeModelEvent> removals = new HashSet<TreeModelEvent>();
		HashSet<TreeModelEvent> additions = new HashSet<TreeModelEvent>();
			HashSet<ExtendedCategory> addedNodes = new HashSet<ExtendedCategory>();
		HashSet<TreeModelEvent> changes = new HashSet<TreeModelEvent>();
		
		//First find removed nodes
		for(ExtendedCategory ec : used) {
			if(!v.used.contains(ec)){
				ExtendedCategory ecp = new ExtendedCategory(ec.category() == Category.getRootCategory()? ec.category : ec.category().parent,false);
				removals.add(new TreeModelEvent(this,getPath(ecp),new int[]{getIndex(ec)},new Object[]{ec}));
			}
		}
		//Now find added and changed nodes
		for(ExtendedCategory ec : v.used) {
			if(!used.contains(ec)) addedNodes.add(ec);
			else {
				if(sum.get(ec.category()) != v.sum.get(ec.category()) ||
				   sumIndiv.get(ec.category()) != v.sumIndiv.get(ec.category()) || 
				   minus.get(ec.category()) != v.minus.get(ec.category()) ||
				   minusIndiv.get(ec.category()) != v.minusIndiv.get(ec.category()) ||
				   plus.get(ec.category()) != v.plus.get(ec.category()) ||
				   plusIndiv.get(ec.category()) != v.plusIndiv.get(ec.category())) {
					ExtendedCategory ecp = new ExtendedCategory(ec.category() == Category.getRootCategory() ? ec.category() : ec.category().parent,false);
					changes.add(new TreeModelEvent(this,getPath(ecp),new int[]{getIndex(ec)},new Object[]{ec}));
				}
			}
		}
		
		//Copy data
		used = v.used;
		directSubcategories = v.directSubcategories;
		after = v.after;
		before = v.before;
		sum = v.sum;
		plus = v.plus;
		minus = v.minus;
		sumIndiv = v.sumIndiv;
		plusIndiv = v.plusIndiv;
		minusIndiv = v.minusIndiv;
		
		//TODO: Update mask and visibility
		
		//Construct addition paths
		for(ExtendedCategory eca : addedNodes) {
			ExtendedCategory ecp = new ExtendedCategory(eca.category() == Category.getRootCategory()? eca.category() : eca.category().parent,false);
			additions.add(new TreeModelEvent(this,getPath(ecp),new int[]{getIndex(eca)},new Object[]{eca}));
		}
		
		//Call on listeners
		for(TreeModelEvent e : removals) {
			fireTreeNodesRemoved(e);
		}
		for(TreeModelEvent e : additions) {
			fireTreeNodesInserted(e);
		}
		for(TreeModelEvent e : changes) {
			fireTreeNodesChanged(e);
		}
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
		return invisibles.contains(new ExtendedCategory(c,false));
	}
	
	/**
	 * @return Whether the additional category node (which is inserted whenever there are subcategories and entries in c) is visible. Returns also true, 
	 * whenever there is no such node
	 */
	public boolean isVisibleIndiv(Category c) {
		return invisibles.contains(new ExtendedCategory(c,true));
	}
	
	/**
	 * @return The mask of the given category (NOT the additional child node) or null, if this category is not masked
	 */
	public String getMask(Category c) {
		return mask.get(new ExtendedCategory(c,false));
	}
	
	/**
	 * @return The mask of the additional node for the given category or null, if the node is not masked or does not exist
	 */
	public String getIndividualMask(Category c) {
		return mask.get(new ExtendedCategory(c,true));
	}
	
	/**
	 * Sets the visibility of the given category node (not the additional node). The root category cannot be made invisible.
	 */
	public void setVisibility(Category c, boolean visible) {
		ExtendedCategory ec = new ExtendedCategory(c,false);
		if(c != null && c != Category.getRootCategory() && used.contains(ec)) {
			if(visible) invisibles.remove(ec);
			else invisibles.add(ec);
			fireTreeNodesChanged(new TreeModelEvent(this,getPath(new ExtendedCategory(c.parent,false)),new int[]{getIndex(ec)},new Object[]{ec}));
		}
	}
	
	/**
	 * Sets the visibility of the additional node in the given category (if it exists)
	 */
	public void setIndividualVisibility(Category c, boolean visible) {
		ExtendedCategory ec = new ExtendedCategory(c,true);
		ExtendedCategory ecp = new ExtendedCategory(c,false);
		if(c != null && used.contains(ecp) &&  directSubcategories.get(ecp).contains(ec)) {
			if(visible) invisibles.remove(ec);
			else invisibles.add(ec);
			fireTreeNodesChanged(new TreeModelEvent(this,getPath(ecp),new int[]{getIndex(ec)}, new Object[]{ec}));
		}
	}
	
	/**
	 * Sets the mask of the given category node (not the additional node). If mask == null, the mask is removed
	 */
	public void setMask(Category c, String  maskString) {
		ExtendedCategory ec = new ExtendedCategory(c,false);
		if(c != null && used.contains(ec)) {
			ExtendedCategory ecp = new ExtendedCategory(c == Category.getRootCategory()? c : c.parent,false);
			if(maskString == null) mask.remove(ec);
			else mask.put(ec, maskString);
			fireTreeNodesChanged(new TreeModelEvent(this, getPath(ecp), new int[]{getIndex(ec)},new Object[]{ec}));
		}
	}
	
	/**
	 * Sets the mask of the additional node in the given category, if it exists. A null value removes the mask
	 */
	public void setIndividualMask(Category c, String maskString) {
		ExtendedCategory ec = new ExtendedCategory(c,true);
		ExtendedCategory ecp = new ExtendedCategory(c,false);
		if(c != null && used.contains(ec) && directSubcategories.get(ecp).contains(ec)) {
			if(maskString == null) mask.remove(ec);
			else mask.put(ec, maskString);
			fireTreeNodesChanged(new TreeModelEvent(this, getPath(ecp),new int[]{0},new Object[]{ec}));
		}
	}
	
	// TREEMODEL *******************
	// *****************************
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		if(arg0 != null) listenerList.add(arg0);
	}

	@Override
	public Object getChild(Object c, int index) {
		if(used.contains(c) && index >= 0 && index < directSubcategories.get(c).size()) {
			return directSubcategories.get(c).get(index);
		}
		else return null;
	}

	@Override
	public int getChildCount(Object c) {
		if(used.contains(c)) {
			return directSubcategories.get(c).size();
		}
		else return 0;
	}

	@Override
	public int getIndexOfChild(Object c, Object t) {
		if(used.contains(c)) {
			return directSubcategories.get(c).indexOf(t);
		}
		else return -1;
	}

	@Override
	public Object getRoot() {
		return new ExtendedCategory(Category.getRootCategory(),false);
	}

	@Override
	public boolean isLeaf(Object c) {
		if(used.contains(c)) {
			return directSubcategories.get(c).size() == 0;
		}
		else return true;
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
		//Ignored
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		recalculate();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		recalculate();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		recalculate();
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignored
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		recalculate();
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
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
	
	protected void fireTaskBegins(SwingWorker<Object, Object> source) {
		for(ProgressListener<Object, Object> l : progressListeners) l.taskBegins(source);
	}
	
	protected void fireProgressed(SwingWorker<Object, Object> source) {
		for(ProgressListener<Object, Object> l : progressListeners) l.progressed(source);
	}
	
	protected void fireTaskFinished(SwingWorker<Object, Object> source) {
		for(ProgressListener<Object, Object> l : progressListeners) l.taskFinished(source);
	}
	
	public void addProgressListener(ProgressListener<Object, Object> l) {
		if(l != null) progressListeners.add(l);
	}
	
	public void removeProgressListener(ProgressListener<Object, Object> l) {
		progressListeners.remove(l);
	}
	
	// HELPER METHODS **************************
	// *****************************************
	
	/**
	 * @return The tree path to the given category (or null, if this tree does not contain the category)
	 */
	protected TreePath getPath(ExtendedCategory c) {
		if(c == null  || !used.contains(c)) return null;
		if(c.isAdditional()) {
			return (new TreePath(new ExtendedCategory(c.category,false))).pathByAddingChild(c);
		}
		else {
			if(c.category == Category.getRootCategory()) return new TreePath(c);
			else return (new TreePath(new ExtendedCategory(c.category.parent,false))).pathByAddingChild(c);
		}
	}
	
	/**
	 * @return The index of the given category in its parent category (-1, if this tree does not contain the category and 0 for root)
	 */
	protected int getIndex(ExtendedCategory c) {
		if(c == null || !used.contains(c)) return -1;
		if(c.isAdditional()) return 0;
		else {
			return directSubcategories.get(new ExtendedCategory(c.category.parent,false)).indexOf(c);
		}
	}
	
	// LOCAL CLASSES ***************************
	// *****************************************
	
	/**
	 * This class represents a category, together with the additional information, whether this is a
	 *  'real' category node or the additional category node in the category itself (a boolean flag: It is true, when 
	 *  the object is the additional node)
	 */
	public final class ExtendedCategory {
		private Category category;
		private boolean isAdditional;
		
		/**
		 * Creates an extended category
		 * @param c The category
		 * @param add Whether this node is the additional node in a category
		 */
		public ExtendedCategory(Category c, boolean add) {
			category = c;
			isAdditional = add;
		}
		
		public Category category() {
			return category;
		}
		
		public boolean isAdditional() {
			return isAdditional;
		}
		
		/**
		 * Two extended categories are equal, if and only if their categories are equal and the isAdditional flag has the same value
		 */
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof ExtendedCategory)) return false;
			else return (((ExtendedCategory)o).category == category && ((ExtendedCategory)o).isAdditional == isAdditional);
		}
		
		@Override
		public int hashCode() {
			return category.hashCode() ^ (new Boolean(isAdditional)).hashCode();
		}
	}
	
	/**
	 * Used for the data transfer of the newly calculated data
	 * @author Simon Hampe
	 *
	 */
	private class DataVector {
		public HashSet<ExtendedCategory> used = new HashSet<ExtendedCategory>();
		public HashMap<ExtendedCategory, Vector<ExtendedCategory>> directSubcategories = new HashMap<ExtendedCategory, Vector<ExtendedCategory>>();
		public HashMap<Category, Float> minus = new HashMap<Category, Float>();
		public HashMap<Category, Float> plus = new HashMap<Category, Float>();
		public HashMap<Category, Float> sum = new HashMap<Category, Float>();
		public HashMap<Category, Float> minusIndiv = new HashMap<Category, Float>();
		public HashMap<Category, Float> plusIndiv = new HashMap<Category, Float>();
		public HashMap<Category, Float> sumIndiv = new HashMap<Category, Float>();
		public HashMap<Account, Float> before = new HashMap<Account, Float>();
		public HashMap<Account, Float> after = new HashMap<Account, Float>();
	}
	
	private class ExtCatComparator implements Comparator<ExtendedCategory>{
		@Override
		public int compare(ExtendedCategory o1, ExtendedCategory o2) {
			if(o1 == null && o2 == null) return 0;
			if(o1 == null || o2 == null) return -1;
			return o1.category().compareTo(o2.category());
		}
	};
	
	/**
	 * Recalculator class used for recalculating the model in a separate thread
	 * @author Simon Hampe
	 *
	 */
	private class Recalculator extends SwingWorker<Object, Object> {

		@Override
		protected Object doInBackground() throws Exception {
			return recalculateModel();
		}

		@Override
		protected void done() {
			if(!isCancelled()) {
				try {
					adoptChanges((DataVector)get());
				} catch (Exception e) {
					//Ignore
				}
			}
			fireTaskFinished(this);
		}
	}

}
