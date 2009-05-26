package fs.fibu2.data.format;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;

/**
 * An EntryComparator induces (more or less) an ordering of fsfibu2 journal entries
 * @author Simon Hampe
 *
 */
public class EntryComparator implements Comparator<Entry>{

	//A private copy kept to save creation time
	private final EntryDateComparator entryComparator = new EntryDateComparator();
	
	//If 0 is returned, when all fields are equal
	private boolean allowNonStrictEquality = false;
	
	/**
	 * Creates an EntryComparator.
	 * @param allowNonStrictEquality - Whether 0 is returned when all fields of two <i>distince</i> Entry objects are equal. For most cases
	 * this should be <i>false</i>. The reason for this is explained in {@link #compare(Entry, Entry)}.
	 */
	public EntryComparator(boolean allowNonStrictEquality) {
		this.allowNonStrictEquality = allowNonStrictEquality;
	}
	
	/**
	 * @return 
	 * - 0 if and only if o1 == o2 (i.e. even if all fields are equal but the objects are not, this does not return 0) or if all fields are equal
	 * and {@link #allowNonStrictEquality} is true<br>
	 * - k > 0, if o1 is smaller than o2 with respect to the criteria specified below, k < 0 if o1 is smaller. If none is smaller than the other,
	 * 1 is returned (or 0, if {@link #allowNonStrictEquality} is true). The reason for this is that it might very well be, that two distinct entries with completely identical fields might be 
	 * contained in a Journal (imagine adding 'Getränkeeinnahmen' twice the same day with the same value). Since in that case at 
	 * least the order is irrelevant, we accept this small lack of well-definedness (in that case we would have o1 > o2 AND o2 > o1). <br>
	 * If o1 is smaller than o2 is derived by comparing their fields in the following order (whenever for the first time a field of oi is greater than
	 * the field of oj, we return oi > oj): <br>
	 * - Date (ordered by {@link EntryDateComparator}) <br>
	 * - Name (ordered by the natural ordering on String) <br>
	 * - Value (ordered by the natural ordering on float) <br>
	 * - Category (ordered by the natural ordering on {@link Category} <br>
	 * - Account (ordered by the natural ordering on their IDs, i.e. on String) <br>
	 * - AdditionalInformation (ordered by the natural ordering on String) <br>
	 * - Currency (ordered by the natural ordering on their ISO codes, i.e. on String) <br>
	 * - AccountInformation (ordered by the natural ordering on the size of first the key set, i.e. on int. If these are equal,
	 * the strings themselves are compared: The highest string for each key set
	 *  is computed and both are compared. If they are equal, their values are compared, afterwards this procedure continues downwards.)<br> 
	 * - The hash code (ordered by the natural ordering on int. Since this should be the hash code on object it should always be different for
	 * different objects, but this is not guaranteed)
	 *  Remark: null is smaller than any entry != null
	 */
	@Override
	public int compare(Entry o1, Entry o2) {
		if(o1 == o2) return 0;
		if(o1 == null) return 1;
		if(o2 == null) return -1;
		
		int cdate = entryComparator.compare(o1.getDate(), o2.getDate());
		if(cdate != 0) return cdate;
		
		int cname = o1.getName().compareTo(o2.getName());
		if(cname != 0) return cname;
		
		int cvalue = o1.getValue() < o2.getValue()? -1 : (o2.getValue() < o1.getValue()? 1 : 0);
		if(cvalue != 0) return cvalue;
		
		int ccategory = o1.getCategory().compareTo(o2.getCategory());
		if(ccategory != 0) return ccategory;
		
		int caccount = o1.getAccount().getID().compareTo(o2.getAccount().getID());
		if(caccount != 0) return caccount;
		
		int cadditional = o1.getAdditionalInformation().compareTo(o2.getAdditionalInformation());
		if(cadditional != 0) return cadditional;
		
		int ccurrency = o2.getCurrency().getCurrencyCode().compareTo(o2.getCurrency().getCurrencyCode());
		if(ccurrency != 0) return ccurrency;
		
		//Compare sizes of key sets
		int ksize1 = o1.getAccountInformation().keySet().size();
		int ksize2 = o2.getAccountInformation().keySet().size();
		if(ksize1 < ksize2) return -1;
		if(ksize2 < ksize1) return 1;
		
		//Compare strings in key sets
		Vector<String> kset1 = new Vector<String>(new TreeSet<String>(o1.getAccountInformation().keySet()));
		Vector<String> kset2 = new Vector<String>(new TreeSet<String>(o2.getAccountInformation().keySet()));
		for(int i = kset1.size() -1; i >= 0; i--) {
			int ckey = kset1.get(i).compareTo(kset2.get(i));
			if(ckey != 0) return ckey;
			else {
				int cval = o1.getAccountInformation().get(kset1.get(i)).compareTo(o2.getAccountInformation().get(kset2.get(i)));
				if(cval != 0) return cval;
			}
		}
		
		int hash1 = o1.hashCode();
		int hash2 = o2.hashCode();
		if(hash1 < hash2) return -1;
		if(hash2 < hash1) return 1;
	
		//The end... ;-)
		return allowNonStrictEquality? 0 : 1;
	}
	
}
