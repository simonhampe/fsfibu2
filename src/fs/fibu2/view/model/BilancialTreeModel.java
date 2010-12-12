package fs.fibu2.view.model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.SortOrder;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.format.MoneyDecimal;
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
 * no more entries in a certain category).<br>
 * Preferences are stored in the following way: In the node passed on in {@link #insertMyPreferences(Preferences)}, for each invisible category there is a 
 * (recursively added) node named 'invisible', which contains a further node 'category' and a key-value pair ('additional', true/false), identifying the category
 * (and optionally a further 'invisible' node). For each masked category there is a recursively added node named 'masked' containing a node 'category' and
 * the same key-value pair and another key-value pair ('mask', String) identifying the mask.
 * @author Simon Hampe
 *
 */
public class BilancialTreeModel implements TreeModel, JournalListener, ChangeListener {

	// DATA ************************
	// *****************************
	
	//All categories contained
	private HashSet<ExtendedCategory> used = new HashSet<ExtendedCategory>();
	
	//The direct subcategories of each used category
	private HashMap<ExtendedCategory, Vector<ExtendedCategory>> directSubcategories = new HashMap<ExtendedCategory, Vector<ExtendedCategory>>();
	
	//The bilancials for each category, including subcategories (the last is of course the sum of the first two)
	private HashMap<Category, BigDecimal> plus = new HashMap<Category, BigDecimal>();
	private HashMap<Category, BigDecimal> minus = new HashMap<Category, BigDecimal>();
	private HashMap<Category, BigDecimal> sum = new HashMap<Category, BigDecimal>();
	
	//The bilancials for each category - without subcategories!
	private HashMap<Category, BigDecimal> plusIndiv = new HashMap<Category, BigDecimal>();
	private HashMap<Category, BigDecimal> minusIndiv = new HashMap<Category, BigDecimal>();
	private HashMap<Category, BigDecimal> sumIndiv = new HashMap<Category, BigDecimal>();
	
	//The before/after status of accounts
	private HashMap<Account, BigDecimal> before = new HashMap<Account, BigDecimal>();
	private HashMap<Account, BigDecimal> after = new HashMap<Account, BigDecimal>();
	
	//Each category contained in here is invisble. 
	private HashSet<ExtendedCategory> invisibles = new HashSet<ExtendedCategory>();
	
	//The mask of each category (null = there is no mask). 
	private HashMap<ExtendedCategory, String> mask = new HashMap<ExtendedCategory, String>();
	
	//Whether account bilancials are calculated regardless of invisibility parameters
	private boolean accountsIgnoreInvisibility = false;
	
	//The corresponding journal
	private Journal associatedJournal;
	//The filter used by this model
	private StackFilter filter;
	
	//Misc
	
	private HashSet<TreeModelListener> listenerList = new HashSet<TreeModelListener>();
	private HashSet<ProgressListener<Object, Object>> progressListeners = new HashSet<ProgressListener<Object,Object>>();
	
	private Recalculator runningInstance = null;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	// CONSTRUCTOR *****************
	// *****************************
	
	/**
	 * Constructs a new model. If node != null, visibility and mask status is read from it. Unknown categories 
	 * will be ignored in this process. 
	 */
	public BilancialTreeModel(Journal j, StackFilter f, Preferences node) {
		associatedJournal = j == null? new Journal() : j;
		associatedJournal.addJournalListener(this);
		filter = f;
		if(filter != null) filter.addChangeListener(this);
		
		//Read out preferences
		if(node != null) {
			try {
				//First read out invisibility
				Preferences ivNode = node;
				while(ivNode.nodeExists("invisible")) {
					ivNode = ivNode.node("invisible");
					if(!ivNode.nodeExists("category")) break;
					Preferences categoryNode = ivNode.node("category");
					Category c = Category.createFromPreferences(categoryNode);
					boolean add = Boolean.parseBoolean(ivNode.get("additional", "false"));
					ExtendedCategory ec = new ExtendedCategory(c,add);
					invisibles.add(ec);
				}
				//Now read out masks
				Preferences maskNode = node;
				while(maskNode.nodeExists("masked")) {
					maskNode = maskNode.node("masked");
					if(!maskNode.nodeExists("category")) break;
					Preferences categoryNode = maskNode.node("category");
					Category c = Category.createFromPreferences(categoryNode);
					boolean add = Boolean.parseBoolean(maskNode.get("additional", "false"));
					ExtendedCategory ec = new ExtendedCategory(c,add);
						String maskString = maskNode.get("mask", null);
						if(maskString != null) mask.put(ec, maskString);
				}
			} catch (BackingStoreException e) {
				//Just abort
				e.printStackTrace();
			}
		}
		
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
		
		//Clean up invisibles and masked
		for(ExtendedCategory ec : new HashSet<ExtendedCategory>(invisibles)) {
			if(!used.contains(ec)) invisibles.remove(ec);
		}
		for(ExtendedCategory ec : new HashSet<ExtendedCategory>(mask.keySet())) {
			if(!used.contains(ec)) mask.remove(ec);
		}
			
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
		
		//The bilancial of all entries 'before'
		BilancialInformation biBefore = new BilancialInformation(associatedJournal);
		//The individual bilancials of all accepted entries
		HashMap<Category, BigDecimal> biAcceptedPlusIndiv = new HashMap<Category, BigDecimal>();
		HashMap<Category, BigDecimal>  biAcceptedMinusIndiv = new HashMap<Category, BigDecimal>();
		HashMap<Category, BigDecimal>  biAcceptedSumIndiv = new  HashMap<Category, BigDecimal>();
		//The overall bilancial of all 'before' and accepted entries
		BilancialInformation biOverall = new BilancialInformation(associatedJournal);
		
		//Whether an entry has already been accepted by the filter
		boolean entriesAccepted = false;
		
		//The sorted set of all entries
		TreeSet<Entry> entries = new TreeSet<Entry>(new TableModelComparator());
			entries.addAll(associatedJournal.getEntries());
		
		for(Entry e : entries) {
			//Add accepted entries to the individual bilancial
			if(filter == null || filter.verifyEntry(e)) {
				entriesAccepted = true;
				if(isInheritedVisible(e.getCategory(), true) && isInheritedVisible(e.getCategory(),false)) biOverall = biOverall.increment(e);
				//If accounts ignore invisibility, we have to add this entry to the account bilancials anyway
				else  {
					if(accountsIgnoreInvisibility) {
						biOverall = biOverall.incrementAccount(e);
					}
				}
				
				if(!biAcceptedMinusIndiv.keySet().contains(e.getCategory())) biAcceptedMinusIndiv.put(e.getCategory(), MoneyDecimal.bigd(0));
				if(!biAcceptedPlusIndiv.keySet().contains(e.getCategory())) biAcceptedPlusIndiv.put(e.getCategory(), MoneyDecimal.bigd(0));
				if(!biAcceptedSumIndiv.keySet().contains(e.getCategory())) biAcceptedSumIndiv.put(e.getCategory(), MoneyDecimal.bigd(0));
				
				if(e.getValue() >= 0) {
					biAcceptedPlusIndiv.put(e.getCategory(), MoneyDecimal.add(biAcceptedPlusIndiv.get(e.getCategory()), MoneyDecimal.bigd(e.getValue())));
				}
				else {
					biAcceptedMinusIndiv.put(e.getCategory(), MoneyDecimal.add(biAcceptedMinusIndiv.get(e.getCategory()), MoneyDecimal.bigd(e.getValue())));
				}
				
				biAcceptedSumIndiv.put(e.getCategory(), MoneyDecimal.add(biAcceptedSumIndiv.get(e.getCategory()), MoneyDecimal.bigd(e.getValue())));
				
			}
			//Add non-accepted entries to the 'before' bilancials as long as no entry has been accepted
			else {
				if(!entriesAccepted) {
					biBefore = biBefore.increment(e);
					biOverall = biOverall.increment(e);
				}
			}
		}
		
		//Create categories and subcategory hierarchy
		v.used.add(new ExtendedCategory(Category.getRootCategory(),false));
		for(Category c : biAcceptedSumIndiv.keySet()) {
			while(c != Category.getRootCategory()) {
				ExtendedCategory ec = new ExtendedCategory(c,false);
				v.used.add(ec);
				c = c.parent;
				ExtendedCategory ecp = new ExtendedCategory(c,false);
				if(v.directSubcategories.get(ecp) == null) v.directSubcategories.put(ecp, new Vector<ExtendedCategory>());
				v.directSubcategories.get(ecp).add(ec);
			}
		}
		//Sort subcategory lists and detect additional nodes
		for(ExtendedCategory c : new HashSet<ExtendedCategory>(v.directSubcategories.keySet())) {
			TreeSet<ExtendedCategory> sortedSet = new TreeSet<ExtendedCategory>(new ExtCatComparator());
				sortedSet.addAll(v.directSubcategories.get(c));
			v.directSubcategories.put(c, new Vector<ExtendedCategory>(sortedSet));
			if(sortedSet.size() > 0 && biAcceptedSumIndiv.keySet().contains(c.category)) {
				ExtendedCategory addNode = new ExtendedCategory(c.category(),true);
				v.used.add(addNode);
				v.directSubcategories.get(c).add(0, addNode);
			}
		}
		
		//Now calculate bilancials
		v.plusIndiv = biAcceptedPlusIndiv;
		v.minusIndiv = biAcceptedMinusIndiv;
		v.sumIndiv = biAcceptedSumIndiv;
		for(ExtendedCategory c : v.used) {
			//We only have to calculate this for leafs:
			if((v.directSubcategories.get(c) != null && v.directSubcategories.get(c).size() > 0) || 
					(c.category() == Category.getRootCategory() && !c.isAdditional())) continue;
			//For leafs, just pass up the bilancial value while you're still in a visible range
			BigDecimal plus = v.plusIndiv.get(c.category());
			BigDecimal minus = v.minusIndiv.get(c.category());
			BigDecimal sum = v.sumIndiv.get(c.category());
			//Non-additional leaf nodes add their bilancials to their sum anyway
			if(!c.isAdditional) {
				if(!v.sum.containsKey(c.category())) v.sum.put(c.category(), sum);
				else v.sum.put(c.category(), MoneyDecimal.add(v.sum.get(c.category()),sum));
				if(!v.plus.containsKey(c.category())) v.plus.put(c.category(), plus);
				else v.plus.put(c.category(), MoneyDecimal.add(v.plus.get(c.category()),plus));
				if(!v.minus.containsKey(c.category())) v.minus.put(c.category(), minus);
				else v.minus.put(c.category(), MoneyDecimal.add(v.minus.get(c.category()),minus));
			}
			while(!invisibles.contains(c) && c != null) {
				ExtendedCategory ecp = new ExtendedCategory(c.isAdditional()? c.category() : c.category().parent,false);
				if(!v.sum.containsKey(ecp.category())) v.sum.put(ecp.category(), sum);
				else v.sum.put(ecp.category(), MoneyDecimal.add(v.sum.get(ecp.category()),sum));
				if(!v.plus.containsKey(ecp.category())) v.plus.put(ecp.category(), plus);
				else v.plus.put(ecp.category(), MoneyDecimal.add(v.plus.get(ecp.category()),plus));
				if(!v.minus.containsKey(ecp.category())) v.minus.put(ecp.category(), minus);
				else v.minus.put(ecp.category(), MoneyDecimal.add(v.minus.get(ecp.category()), minus));
				c = ecp.category() == Category.getRootCategory()? null : ecp;
			}
		}
		
		//Finally copy account values
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
		
		//Update mask and visibility
		HashSet<ExtendedCategory> noLongerInvisible = new HashSet<ExtendedCategory>();
		HashSet<ExtendedCategory> noLongerMasked = new HashSet<ExtendedCategory>();
		for(ExtendedCategory ec : invisibles) {
			if(!used.contains(ec)) noLongerInvisible.add(ec);
		}
		for(ExtendedCategory ec : mask.keySet()) {
			if(!used.contains(ec)) noLongerMasked.add(ec);
		}
		invisibles.removeAll(noLongerInvisible);
		for(ExtendedCategory ec : noLongerMasked) mask.remove(ec);
		
		//Construct addition paths
		for(ExtendedCategory eca : addedNodes) {
			ExtendedCategory ecp = new ExtendedCategory(eca.category() == Category.getRootCategory()? eca.category() : eca.category().parent,false);
			additions.add(new TreeModelEvent(this,getPath(ecp),new int[]{getIndex(eca)},new Object[]{eca}));
		}
		
		/*
		 * REMARK: Using the detailed tree change methods seems to 'confuse' the jtree quite a lot. Maybe there is some calculation problem
		 * above, maybe I would have to fire these events in a certain order. However: Whenever there is some node addition or removal going on,
		 * I just fire treeStructureChanged
		 */
		
		if(additions.size() > 0 || removals.size() > 0) {
			fireTreeStructureChanged(new TreeModelEvent(this,new Object[]{new ExtendedCategory(Category.getRootCategory(),false)}));
		}
		else {
			//Call on listeners
			for(TreeModelEvent e : removals) {
				System.out.println("Removing " + e.getChildIndices() + " in " + ((ExtendedCategory)e.getTreePath().getLastPathComponent()).category().toString());
				fireTreeNodesRemoved(e);
			}
			for(TreeModelEvent e : additions) {
				fireTreeNodesInserted(e);
			}
			for(TreeModelEvent e : changes) {
				fireTreeNodesChanged(e);
			}
		}
	}
	
	// GETTERS & SETTERS ***********
	// *****************************
	
	/**
	 * @return The sum over all positive entries in this category and subcategories (0, if there are none)
	 */
	public float getCategoryPlus(Category c) {
		BigDecimal f = plus.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return The sum over all negative entries in this category and subcategories (0, if there are none)
	 */
	public float getCategoryMinus(Category c) {
		BigDecimal f = minus.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return The sum over all entries in this category and subcategories (0, if there are none)
	 */
	public float getCategorySum(Category c) {
		BigDecimal f = sum.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return The sum over all positive entries directly in this category (0, if there are none)
	 */
	public float getIndividualPlus(Category c) {
		BigDecimal f = plusIndiv.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return The sum over all negative entries directly in this category (0, if there are none)
	 */
	public float getIndividualMinus(Category c) {
		BigDecimal f = minusIndiv.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return The sum over all entries directly in this category (0, if there are none)
	 */
	public float getIndividualSum(Category c) {
		BigDecimal f = sumIndiv.get(c);
		return f == null? 0 : f.floatValue();
	}
	
	/**
	 * @return Whether the given catgory node is visible (returns also true for nodes, which do not exist)
	 */
	public boolean isVisible(ExtendedCategory c) {
		return !invisibles.contains(c);
	}
	
	/**
	 * @return Whether this node AND all its parent nodes are visible (this also might return true for non-existing nodes, if a parent
	 * category actually IS in this model).
	 */
	public boolean isInheritedVisible(Category c, boolean additionalNode) {
		ExtendedCategory ec = new ExtendedCategory(c,additionalNode);
		if(c == Category.getRootCategory()) return !invisibles.contains(ec);
		else return (!invisibles.contains(ec) && isInheritedVisible(additionalNode? c : c.parent, false));
	}
	
	/**
	 * @return The mask of the given category 
	 */
	public String getMask(ExtendedCategory c) {
		return mask.get(c);
	}
	
	/**
	 * Sets the visibility of the given category node (not the additional node). The root category cannot be made invisible.
	 */
	public void setVisibility(Category c, boolean visible) {
		ExtendedCategory ec = new ExtendedCategory(c,false);
		if(c != null && c != Category.getRootCategory() && used.contains(ec)) {
			if(visible) invisibles.remove(ec);
			else invisibles.add(ec);
			recalculate();
			fireAllTreeNodesChanged();
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
			recalculate();
			fireAllTreeNodesChanged();
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
	
	/**
	 * Sets whether invisibility settings are ignored for account bilancials
	 */
	public void setAccountsIgnoreInvisibility(boolean ignore) {
		accountsIgnoreInvisibility = ignore;
		recalculate();
		fireTreeNodesChanged(new TreeModelEvent(this, new TreePath(getRoot())));
	}
	
	/**
	 * @return Whether account bilancial ignores invisibility settings
	 */
	public boolean doAccountsIgnoreInvisibility() {
		return accountsIgnoreInvisibility;
	}
	
	/**
	 * @return The status of a before the regarded entries (or 0, if the account does not figure in this bilancial)
	 */
	public float getAccountBefore(Account a) {
		BigDecimal f = before.get(a);
		if(f == null) return 0;
		else return f.floatValue();
	}
	
	/**
	 * @return The status of a after the regarded entries (or 0, if the account does not figure in this bilancial)
	 */
	public float getAccountAfter(Account a) {
		BigDecimal f = after.get(a);
		if(f == null) return 0;
		else return f.floatValue();
	}
	
	/**
	 * @return The list of accounts used in the current setting
	 */
	public HashSet<Account> getAccounts() {
		return new HashSet<Account>(after.keySet());
	}
	
	/**
	 * Saves the preferences of this model in the given node (i.e. the mask and visibility status), if it isn't null. The same node
	 * should be passed to the constructor when whishing to create a model with the same preferences
	 */
	public void insertMyPreferences(Preferences  node) {
		if(node == null) return;

		try {
			//First remove any existing 'invisible' and 'masked' node
			if(node.nodeExists("invisible")) node.node("invisible").removeNode();
			if(node.nodeExists("masked")) node.node("masked").removeNode();
			
			//Save invisibility status
			Preferences ivNode = node;
			for(ExtendedCategory ec : invisibles) {
				ivNode = ivNode.node("invisible");
				Preferences categoryNode = ivNode.node("category");
				ec.category().insertMyPreferences(categoryNode);
				ivNode.put("additional", Boolean.toString(ec.isAdditional()));
			}
			
			//Save mask status
			Preferences maskNode = node;
			for(ExtendedCategory ec : mask.keySet()) {
				maskNode = maskNode.node("masked");
				Preferences categoryNode = maskNode.node("category");
				ec.category().insertMyPreferences(categoryNode);
				maskNode.put("additional",Boolean.toString(ec.isAdditional()));
				maskNode.put("mask", mask.get(ec));
			}
			
		} catch (BackingStoreException e) {
			logger.warn("Could not save preferences of BilancialTreeModel: " + e.getMessage());
		}
		
	}
	
	/**
	 * Sets the filter of this model. Null indicates that all entries should be accepted.
	 */
	public void setFilter(StackFilter filter) {
		if(this.filter != null) this.filter.removeChangeListener(this);
		this.filter = filter;
		if(this.filter != null) this.filter.addChangeListener(this);
		recalculate();
	}
	
	/**
	 * This returns a {@link PieDataset} which can be used to display a pie plot representing
	 * the distribution of either the incomes or expenditures of a category
	 * @param c The category which should be used for calculation
	 * @param includeOnlyFirstLevel If true, only the sums of the categories directly below c 
	 * are used for calculation. If false, all lowest-level categories are used
	 * @param positive If true, income is used for calculation, if false, expenditure is used.
	 * @param bilancial If true, positive/negative values of the overall bilancial are used, otherwise pure income/expenditure is used
	 */
	public PieDataset getPieDataSet(Category c, boolean includeOnlyFirstLevel, boolean positive, boolean bilancial) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		c = c == null? Category.getRootCategory() : c;
		ExtendedCategory ec = new ExtendedCategory(c,false);
		Vector<ExtendedCategory> subcats = directSubcategories.get(ec);
		
		//If it has no subcategories, simply return a trivial, one-element dataset
		if(subcats == null || subcats.size() == 0) {
			if(invisibles.contains(ec)) dataset.setValue(c,0);
			else {
				float value = 0.0f;
				if(bilancial && sum.get(c) != null) {
					if(positive && sum.get(c).compareTo(MoneyDecimal.bigd(0)) > 0) value = sum.get(c).floatValue();
					if(!positive && sum.get(c).compareTo(MoneyDecimal.bigd(0)) < 0) value = -sum.get(c).floatValue();
				}
				else {
					if(positive && plus.get(c) != null) value = plus.get(c).floatValue();
					if(!positive && minus.get(c) != null) value = -minus.get(c).floatValue();
				}
				dataset.setValue(c,value);
				return dataset;
			}
		}
		
		//Go through direct subcategories
		if(includeOnlyFirstLevel) {
			for(ExtendedCategory ecc : subcats) {
				if(!invisibles.contains(ecc)) {
					float value = 0.0f;
					Category cat = ecc.category();
					if(bilancial && sum.get(cat) != null) {
						if(positive && sum.get(cat).compareTo(MoneyDecimal.bigd(0)) > 0) value = sum.get(cat).floatValue();
						if(!positive && sum.get(cat).compareTo(MoneyDecimal.bigd(0)) < 0) value = -sum.get(cat).floatValue();
					}
					else {
						if(positive && plus.get(cat) != null) value = plus.get(cat).floatValue();
						if(!positive && minus.get(cat) != null) value = -minus.get(cat).floatValue();
					}
					dataset.setValue(ecc.category(), value);
				}
			}
		}
		//Go through lowest-level children
		else {
			for(ExtendedCategory ecc : getLowestLevelChildren(ec)) {
				if(!invisibles.contains(ecc)){
					float value = 0.0f;
					Category cat = ecc.category();
					if(bilancial && sum.get(cat) != null) {
						if(positive && sum.get(cat).compareTo(MoneyDecimal.bigd(0)) > 0) value = sum.get(cat).floatValue();
						if(!positive && sum.get(cat).compareTo(MoneyDecimal.bigd(0)) < 0) value = -sum.get(cat).floatValue();
					}
					else {
						if(positive && plus.get(cat) != null) value = plus.get(cat).floatValue();
						if(!positive && minus.get(cat) != null) value = -minus.get(cat).floatValue();
					}
					dataset.setValue(ecc.category(), value);
				}
			}
		}
		
		dataset.sortByKeys(SortOrder.ASCENDING);
		return dataset;	
	}
	
	/**
	 * @return A list of the lowest-level children of ec
	 */
	protected Vector<ExtendedCategory> getLowestLevelChildren(ExtendedCategory ec) {
		Vector<ExtendedCategory> children = new Vector<ExtendedCategory>();
		
		Vector<ExtendedCategory> subcategories = directSubcategories.get(ec);
		if(subcategories == null || subcategories.size() == 0) {
			children.add(ec);
		}
		else {
			for(ExtendedCategory child : subcategories) {
				children.addAll(getLowestLevelChildren(child));
			}
		}
		
		return children;
	}
	
	// TREEMODEL *******************
	// *****************************
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		if(arg0 != null) listenerList.add(arg0);
	}

	@Override
	public Object getChild(Object c, int index) {
		if(used.contains(c) && index >= 0 &&  directSubcategories.get(c) != null && index < directSubcategories.get(c).size()) {
			return directSubcategories.get(c).get(index);
		}
		else return null;
	}

	@Override
	public int getChildCount(Object c) {
		if(used.contains(c)) {
			Vector<ExtendedCategory> children = directSubcategories.get(c);
			return children == null? 0 : children.size();
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
			return directSubcategories.get(c) == null || directSubcategories.get(c).size() == 0;
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
	
	//Helper method firing a nodeschanged for EVERY node. This is used when all nodes' bilancials might be changed
	//to preserve tree expansion status
	protected void fireAllTreeNodesChanged() {
		for(ExtendedCategory ec : used) {
			ExtendedCategory parent = ec.category == Category.getRootCategory()? new ExtendedCategory(ec.category, false) : 
																				 new ExtendedCategory(ec.isAdditional? ec.category : ec.category.parent , false);
			fireTreeNodesChanged(new TreeModelEvent(this,getPath(parent),new int[]{getIndex(ec)},new Object[]{ec}));
		}
	}
	
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
	
	@Override
	public void stateChanged(ChangeEvent e) {
		recalculate();
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
		if(c.category == Category.getRootCategory()) return 0;
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
	public static final class ExtendedCategory {
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
		public HashMap<Category, BigDecimal> minus = new HashMap<Category, BigDecimal>();
		public HashMap<Category, BigDecimal> plus = new HashMap<Category, BigDecimal>();
		public HashMap<Category, BigDecimal> sum = new HashMap<Category, BigDecimal>();
		public HashMap<Category, BigDecimal> minusIndiv = new HashMap<Category, BigDecimal>();
		public HashMap<Category, BigDecimal> plusIndiv = new HashMap<Category, BigDecimal>();
		public HashMap<Category, BigDecimal> sumIndiv = new HashMap<Category, BigDecimal>();
		public HashMap<Account, BigDecimal> before = new HashMap<Account, BigDecimal>();
		public HashMap<Account, BigDecimal> after = new HashMap<Account, BigDecimal>();
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
