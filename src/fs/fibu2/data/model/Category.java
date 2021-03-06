package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * Implements a category specification for fsfibu2 entries. A category is a sequence of strings, where latter strings specify a more detailed
 * category (e.g. 'Living being' - 'Animal' - 'Fish' - 'Shark'). Category objects are immutable and there is only one category object for each category, so
 * there is no public constructor. The root category is represented by the empty list. <br>
 * Though this class implements XMLConfigurable, only read access is allowed. Any call of configure() will cause an XMLWriteConfiurationException
 * @author Simon Hampe
 *
 */
public final class Category implements XMLConfigurable, Comparable<Category>{

	//A reference to the root group
	private static Category root = null;
	//A list of existing categories: Which category has string s at position n (counting from most general = 1)?
	private static HashMap<String, HashMap<Integer,HashSet<Category>>> categories = new HashMap<String, HashMap<Integer,HashSet<Category>>>();
	
	//The category represented by this object
	public final Category parent;
	public final String tail;
	private final int hashCode;
	private final Vector<String> orderedList = new Vector<String>(); //We keep a double copy of the data, since both formats might be efficient depending on the task 
	
	//A logger
	private static Logger logger = Logger.getLogger(Category.class);
	
	// CONSTRUCTORS AND CREATION METHODS *************************************
	// ***********************************************************************
	
	protected Category(Category parent, String tail) {
		//Copy values
		this.parent = parent;
		//Tail can only be null, when parent is null as well
		this.tail = (parent != null && tail == null)? "" : tail;
		if(parent != null) orderedList.addAll(parent.getOrderedList());
		if(tail != null) orderedList.add(tail);
		
		//Compute hashcode
		int hash = 42;
		for(String s : orderedList) hash = 31*hash + s.hashCode();
		hashCode = hash;
		
		//Register in categories map
		for(int i = 0; i < orderedList.size(); i++) {
			String s = orderedList.get(i);
			HashMap<Integer,HashSet<Category>> mapat = categories.get(s);
			if(mapat == null) mapat = new HashMap<Integer, HashSet<Category>>();
			HashSet<Category> cmap = mapat.get(i+1);
			if(cmap == null) cmap = new HashSet<Category>();
			cmap.add(this);
			mapat.put(i+1, cmap);
			categories.put(s, mapat);
		}
		
		logger.trace("Created category: " + toString());	
	}
	
	public static Category getCategory(Category parent, String tail) {
		//If tail == null, this is just the parent category
		if(tail == null) return parent == null? getCategory((Vector<String>)null) : parent;
		
		//Create associated string sequence
		Vector<String> sequence = (parent == null? new Vector<String>() : parent.getOrderedList());
		sequence.add(tail);
		
		return getCategory(sequence);			
	}
	
	public static Category getCategory(Vector<String> sequence) {
		//If this is the empty or null list, the root group is returned
		if(sequence == null || sequence.size() == 0) {
			if(root != null) return root;
			else {
				root = new Category(null,null);
				return root;
			}
		}
		
		//Otherwise determine, if this category already exists
		
		//Will contain at position n: A list of categories containing the first n+1 strings of the sequence at the right position
		//So of course vector(n) contains vector(m) for all n <= m
		Vector<HashSet<Category>> nthOrderParents = new Vector<HashSet<Category>>(sequence.size());
		
		//Create first order parents
		nthOrderParents.add(new HashSet<Category>());
		HashMap<Integer,HashSet<Category>> containfirst = categories.get(sequence.get(0));
		if(containfirst != null) {
			nthOrderParents.set(0,containfirst.get(1));
		}
		
		//Create higher order parents
		for(int i = 1; i < sequence.size(); i++) {
			//Start with the last parent list
			HashSet<Category> ithParents = new HashSet<Category>(nthOrderParents.get(i-1));
			if(ithParents.size() == 0) {
				nthOrderParents.add(ithParents); 
				continue;
			}

			HashMap<Integer,HashSet<Category>> containthis = categories.get(sequence.get(i));
			//If there's no category containing this string, we're done.
			if(containthis == null) {
				nthOrderParents.add(new HashSet<Category>());
				continue;
			}
			
			//Else remove all categories from the list of possible ones that do not contain the string at the right position
			HashSet<Category> containsthisat = containthis.get(i+1);
			if(containsthisat == null) containsthisat = new HashSet<Category>();
			for(Category c : nthOrderParents.get(i-1)) {
				if(!containsthisat.contains(c)) ithParents.remove(c);
			}
			nthOrderParents.add(ithParents);
		}		
		
		//Find nearest existing parent
		int s = sequence.size();
		Category nearestparent = null;
		for(int i = s-1; i >= 0; i--) {
			if(nthOrderParents.get(i).size() > 0) {
				for(Category c : nthOrderParents.get(i)) if(c.getOrder() == i+1) {
					nearestparent= c; break;
				}
				if(nearestparent!= null) break;
			}
		}
		
		//If nearestparent == null, it is the root category
		if(nearestparent == null) nearestparent = getRootCategory();
		
		//If the nearest parent has the same order as the sequence, it IS the category
		if(nearestparent.getOrder() == s) return nearestparent;
		
		//Else create it, starting with the highest order parent
		int o = nearestparent.getOrder();
		for(int i = o + 1; i <= s; i++) {
			nearestparent = new Category(nearestparent,sequence.get(i-1));
		}
		return nearestparent;
		
		
		
	}
	
	// GETTER METHODS ********************************************************
	// ***********************************************************************
	
	/**
	 * Returns a list of the strings defining this category, ordered from general to specific.
	 */
	public Vector<String> getOrderedList() {
		return new Vector<String>(orderedList);
	}
	
	/**
	 * Returns the length of the string sequence defining this category
	 */
	public int getOrder() {
		return orderedList.size();
	}
	
	/**
	 * Returns a list of all existing categories (except for the root category)
	 */
	public static HashSet<Category> getExistingCategories() {
		HashSet<Category> returnValue = new HashSet<Category>();
		for(HashMap<Integer,HashSet<Category>> map : categories.values()) {
			for(HashSet<Category> set : map.values()) {
				returnValue.addAll(set);
			}
		}
		return returnValue;
	}
	
	/**
	 * @return The root category
	 */
	public static Category getRootCategory() {
		return root != null? root : getCategory(null,null);
	}
	
	/**
	 * @return Whether this category is a subcategory of p, i.e. p is a (direct or indirect) parent of this category.
	 * If p == null, this returns false.
	 */
	public boolean isSubCategoryOf(Category p) {
		if(p == null) return false;
		return this == p || (parent == null? false : parent.isSubCategoryOf(p));
	}
	
	/**
	 * @return Whether this category is a supercategory of c, i.e. is a (direct or indirect) parent of c.
	 */
	public boolean isSuperCategoryOf(Category c) {
		if(c == null) return false;
		return c.isSubCategoryOf(this);
	}
	
	/**
	 * Finds the greatest (in terms of order) category, such that both this category and o are subcategories of it. if o == null, this
	 * returns the root category
	 */
	public Category getGreatestCommonParent(Category o) {
		if(o == null) return getRootCategory();
		//Choose the shorter category of both
		Category shorter = o.getOrder() > getOrder()? o : this;
		Category longer = shorter == o? this : o;
		while(!longer.isSubCategoryOf(shorter)) {
			shorter = shorter.parent;
		}
		return shorter;
		
	}
	
	// EQUALS, HASHCODE, TOSTRING ***************************************************
	// ******************************************************************************
		
	/**
	 * Returns true, if and only if obj == this. This works, since there is only one object for each category.
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	/**
	 * Returns the hashcode associated to this category. 
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Returns the string sequence defining this category, separated by ' - ' 
	 */
	@Override
	public String toString() {
		return (parent == root || parent == null? "" : parent.toString() + " - ") + tail;
	}

	// PREFERENCES METHODS *******************************************************
	// ***************************************************************************
	
	/**
	 * Will first remove any 'tail' node it finds and then insert a recursive sequence of 'tail' nodes for each element of the category 
	 * sequence and within each tail node a key value pair ('tail', ...) indicating the actual value in the sequence
	 */
	public void insertMyPreferences(Preferences node) {
		if(node != null) { 
			//First of all delete any existing tail node
			try {
				if(node.nodeExists("tail")) {
					node.node("tail").removeNode();
				}
			} catch (BackingStoreException e) {
				logger.warn("Cannot save category " + toString() + " to preferences: " + e.getMessage());
				return;
			}
			//Insert representation
			for(String s : orderedList) {
				node = node.node("tail");
				node.put("tail", s);
			}
		}
	}
	
	/**
	 * Creates a category from the given node. For each node named 'tail' in node it obtains the value of the key 'tail' in this node and 
	 * appends it to the string sequence defining the resulting category. As soon as this node does not exist or the key does not exist, the process ends
	 * and the resulting category is returned
	 */
	public static Category createFromPreferences(Preferences node) {
		if(node == null) return getRootCategory();
		Vector<String> sequence = new  Vector<String>();
		try {
			while(node.nodeExists("tail")) {
				node = node.node("tail");
				String s = node.get("tail", null);
				if(s == null) break;
				else sequence.add(s);
			}
		} catch (BackingStoreException e) {
			//Ignore
		}
		return getCategory(sequence);
	}
	
	// XMLCONFIGURATION METHODS **************************************************
	// ***************************************************************************
	
	/**
	 * @return A category with a sequence element for each 'tail' node / The root category for the null node
	 */
	public static Category getCategory(Node n) {
		if(n == null) return getRootCategory();
		//Create sequence
		Vector<String> sequence = new Vector<String>();
		for(Object o : n.selectNodes("./tail")) {
			try {
				sequence.add(((Node)o).getText());
			}
			catch(ClassCastException ce) {
				//Skip
			}
		}
		return getCategory(sequence);		
	}
	
	/**
	 * Invalid operation, since Category is immutable
	 * @throws XMLWriteConfigurationException - always
	 */
	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		throw new XMLWriteConfigurationException("Can't configure immutable Category object. Only read access allowed."); 
	}

	/**
	 * Returns a node of name category containing a sequence of nodes named 'tail' containing the actual strings of the defining sequence
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement rootnode = new DefaultElement("category");
		for(String s : orderedList) {
			DefaultElement tailnode = new DefaultElement("tail");
			tailnode.setText(s);
			rootnode.add(tailnode);
		}
		return rootnode;
	}

	/**
	 * Returns 'category'
	 */
	@Override
	public String getIdentifier() {
		return "category";
	}

	/**
	 * Returns true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	// COMPARABLE ************************************
	// ***********************************************
	
	/**
	 * Establishes an order on categories. <br>
	 * - null is smaller than any category <br>
	 * - The root category is the minimal category <br>
	 * - Every category is larger than each of its supercategories <br>
	 * - If two categories A,B are not subcategories of one another, then let l 
	 * be the largest order such that A_upto_l = B_upto_l and then A <= B iff A.(l+1) <= B.(l+1) in 
	 * terms of lexicographical order, where A_upto_l denotes the category defined by the first l strings and A.(l+1) denotes the l+1-st string
	 * @return x>0,0,x<0 depending on whether this category is larger than, equal to or less than o
	 */
	@Override
	public int compareTo(Category o) {
		//Equality
		if(this == o) return 0;
		//Null is smaller than any
		if(o == null) return 1;
		//Root is smaller than any
		if(o == getRootCategory()) return 1;
		//Subcategory
		if(isSubCategoryOf(o)) return 1;
		//Supercategory
		if(isSuperCategoryOf(o)) return -1;
		//Find gcp and do lexicographical comparison
		Category gcp = getGreatestCommonParent(o);
		return orderedList.get(gcp.getOrder()).compareTo(o.getOrderedList().get(gcp.getOrder()));
	}

}
