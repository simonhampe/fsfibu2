package fs.fibu2.filter;

import java.util.GregorianCalendar;

import fs.fibu2.filter.StandardFilterComponent.Selection;

/**
 * This class provides factory methods to create filters which are frequently used, such as Year filters
 * @author Simon Hampe
 *
 */
public class DefaultFilters {

	public static DateFilter getYearFilter(int year) {
		DateFilter filter = new DateFilter(Selection.RANGE,null,new GregorianCalendar(year,0,1),new GregorianCalendar(year,11,31),null);
		return filter;
	}
	
	
}
