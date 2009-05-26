package fs.fibu2.data.model;

/**
 * An EntrySeparator is an entity which separates a set of entries into 'entries which are less than or equal to me' and 'entries which are larger than
 * me'. A {@link ReadingPoint} is a typical example. 
 * @author Simon Hampe
 *
 */
public interface EntrySeparator{

	/**
	 * @return true, if and only if e comes before this Separator, false otherwise
	 */
	public boolean isLessOrEqualThanMe(Entry e);
	
	/**
	 * @return A short description of this separator (e.g. 'Kassenprüfung 03.04.08')
	 */
	public String getName();
	
}
