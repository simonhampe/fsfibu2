package fs.fibu2.filter;

import java.util.prefs.Preferences;

import fs.fibu2.filter.StandardFilterComponent.Selection;

/**
 * This class provides static methods for inserting general filter preferences into a given {@link Preferences} node and for retrieving them.
 * @author Simon Hampe
 *
 */
public class AbstractFilterPreferences {

	/**
	 * Inserts a few basic key-value pairs into a given node. node and type must not be null, all strings 
	 * might be null (If so, no entry is inserted)
	 * @throws NullPointerException - if node or type is null
	 */
	public static void insert(Preferences node, Selection type,String id, String equality, String pattern, String min, String max ) {
		if(node == null || type == null) throw new NullPointerException("Cannot insert preferences: Node == null or Type == null");

		node.put("type", type.toString());
		if(id != null) node.put("id", id);
		if(equality != null) node.put("equality", equality);
		if(pattern != null) node.put("pattern", pattern);
		if(min != null) node.put("min", min);
		if(max != null) node.put("max", max);
	}
	
	/**
	 * Looks for an entry of key 'type' and returns its value. Returns null, if the
	 * entry does not exist or does not contain a value which represents a {@link Selection} or if node == null
	 */
	public static Selection getType(Preferences node) {
		if(node == null) return null;
		String val = node.get("type", null);
		try {
			Selection s = Selection.valueOf(val);
			return s;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * @return The value of the entry with key 'id' or null, if it doesn't exist
	 */
	public static String getIdString(Preferences node) {
		if(node == null) return null;
		return node.get("id", null);
	}
	
	/**
	 * @return The value of the entry with key 'equality' or null, if it doesn't exist
	 */
	public static String getEqualityString(Preferences node) {
		if(node == null) return null;
		return node.get("equality", null);
	}
	
	/**
	 * @return The value of the entry with key 'pattern' or null, if it doesn't exist
	 */
	public static String getPatternString(Preferences node) {
		if(node == null) return null;
		return node.get("pattern",null);
	}
	
	/**
	 * @return The value of the entry with key 'min' or null, if it doesn't exist
	 */
	public static String getMinString(Preferences node) {
		if(node == null) return null;
		return node.get("min", null);
	}
	
	/**
	 * @return The value of the entry with key 'max' or null, if it doesn't exist
	 */
	public static String getMaxString(Preferences node) {
		if(node == null) return null;
		return node.get("max", null);
	}
}
