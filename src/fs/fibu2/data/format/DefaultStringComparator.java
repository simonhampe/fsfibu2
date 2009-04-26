package fs.fibu2.data.format;

import java.util.Comparator;

/**
 * Compares to strings according to their lexicographical ordering. The empty string, however, is interpreted as
 * the infinite bound.
 * @author Simon Hampe
 *
 */
public class DefaultStringComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		if(o1.equals("") && !o2.equals("")) return 1;
		if(o2.equals("") && !o1.equals("")) return -1;
		return o1.compareTo(o2);
	}

}
