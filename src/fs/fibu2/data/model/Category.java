package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

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
public class Category implements XMLConfigurable {

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
		if(tail == null) return parent;
		
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
		if(nearestparent == null) nearestparent = getCategory(null);
		
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

	// XMLCONFIGURATION METHODS **************************************************
	// ***************************************************************************
	
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

}
