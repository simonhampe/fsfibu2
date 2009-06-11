package fs.fibu2.data.model;

/**
 * An extreme separator is a separator which comes before all entries or
 * after all entries. Optionally, a filter can be given when creating the separator.
 * @author Simon Hampe
 *
 */
public class ExtremeSeparator implements EntrySeparator {

	private String name;
	private boolean isBeforeAll;
	
	/**
	 * Creates a ExtremeSeparator with a short description which is displayed as its name
	 * @param description The name of this separator
	 * @param isBeforeAll Whether this is a separator at the beginning of all entry lists. If false, it is placed at the end.
	 */
	public ExtremeSeparator(String description, boolean isBeforeAll) {
		name = description == null? "" : description;
		this.isBeforeAll = isBeforeAll;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isLessOrEqualThanMe(Entry e) {
		return !isBeforeAll;
	}
	
	public boolean isBeforeAll() {
		return isBeforeAll;
	}

}
