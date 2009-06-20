package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import fs.fibu2.data.model.EntrySeparator;

/**
 * A bilancial mapping is an ordered map from {@link EntrySeparator}s to {@link BilancialInformation}. Internally the mapping is kept as a 
 * {@link HashMap} and can be controlled in almost the same way, but there are also methods to retrieve the mappings in an ordered way, where
 * the ordering is induced by the ordering on {@link EntrySeparator} used for {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class BilancialMapping {

	//The internal map from separators to a pairing of this separator and its associated mapping
	private HashMap<EntrySeparator, BilancialPairing> mapping = new HashMap<EntrySeparator, BilancialPairing>();
	//An ordered list of the pairings
	private TreeSet<BilancialPairing> pairing = new TreeSet<BilancialPairing>(new PairingComparator());
	
	// CONSTRUCTORS *****************************************
	// ******************************************************
	
	public BilancialMapping() {}
	
	// A private copy constructor
	private BilancialMapping(HashMap<EntrySeparator,BilancialPairing> mappingtoclone, TreeSet<BilancialPairing> pairingtoclone) {
		mapping = new HashMap<EntrySeparator, BilancialPairing>(mappingtoclone);
		pairing = new TreeSet<BilancialPairing>(pairingtoclone);
	}
	
	// GETTERS / SETTERS ************************************
	// ******************************************************
	
	/**
	 * Adds a mapping for key to the given value. If there already is a mapping for the given key, it is replaced.
	 * This call has no effect, if the value is null
	 * @return The value associated to the key previously or null, if there was no mapping. Null is also returned, if
	 * value is null.
	 */
	public BilancialInformation put(EntrySeparator key, BilancialInformation value) {
		if(value != null) {
			BilancialPairing pair = new BilancialPairing(key, value);
			BilancialPairing previous = mapping.put(key, pair);
			if(previous != null) {
				pairing.remove(previous);
				pairing.add(pair);
				return previous.information;
			}
			else {
				pairing.add(pair);
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Removes the mapping associated to the given key 
	 * @return The value associated to the key before this call or null if there was no mapping
	 */
	public BilancialInformation remove(EntrySeparator key) {
		BilancialPairing previous = mapping.remove(key);
		if(previous != null) {
			pairing.remove(previous);
			return previous.information;
		}
		return null;
	}
	
	/**
	 * @return The pairing associated to the most recent EntrySeparator
	 */
	public BilancialPairing getMostRecent() {
		return pairing.last();
	}
	
	public BilancialPairing getOldest() {
		return pairing.first();
	}
	
	/**
	 * @return An ordered list of all pairings
	 */
	public Vector<BilancialPairing> getOrderedList() {
		return new Vector<BilancialPairing>(pairing);
	}
	
	/**
	 * @return The information associated to the given key or null, if there is no mapping
	 */
	public BilancialInformation get(EntrySeparator key) {
		BilancialPairing pair = mapping.get(key);
		if(pair != null) return pair.information;
		else return null;
	}
	
	/**
	 * @return The set of separators used as keys
	 */
	public Set<EntrySeparator> keySet() {
		return mapping.keySet();
	}
	
	public BilancialMapping clone() {
		return new BilancialMapping(mapping,pairing);
	}
	
	// LOCAL COMPARATOR CLASS *******************************
	// ******************************************************
	
	/**
	 * This class represents a pairing of a separator and a bilancial information
	 * @author Simon Hampe
	 *
	 */
	public class BilancialPairing {
		private EntrySeparator separator;
		private BilancialInformation information;
		
		/**
		 * Creates a new pairing.
		 */
		public BilancialPairing(EntrySeparator s, BilancialInformation b) {
			separator = s;
			information = b;
		}
		
		/**
		 * @return The separator of this pairing
		 */
		public EntrySeparator separator() {
			return separator;
		}
		
		/**
		 * @return The bilancial information of this pairing
		 */
		public BilancialInformation information() {
			return information;
		}
	}
	
	//Compares the pairings. 
	private class PairingComparator implements Comparator<BilancialPairing> {

		private final TableModelComparator internalComparator = new TableModelComparator();

		@Override
		public int compare(BilancialPairing o1, BilancialPairing o2) {
			return internalComparator.compare(o1.separator, o2.separator);
		}
		
		
		
	}
	
}
