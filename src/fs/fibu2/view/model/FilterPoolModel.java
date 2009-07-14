package fs.fibu2.view.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.FilterPool;
import fs.fibu2.filter.FilterPool.StackFilterTripel;
import fs.fibu2.view.event.YearSeparatorListener;

/**
 * This class represents the filters available in the {@link FilterPool} of a given {@link Journal}. Optionally, filters of a certain module
 * can be excluded (e.g. a module usually doesn't want to display its own filters) and standard year filters can be displayed.
 * @author Simon Hampe
 *
 */
public class FilterPoolModel implements ComboBoxModel, YearSeparatorListener, ChangeListener {

	// DATA ********************************
	// *************************************
	
	private HashSet<ListDataListener> listeners = new HashSet<ListDataListener>();
	
	//Whether year filters are automatically added
	private boolean addYearFilters = true;
	
	//Which module to exclude (if null, no module is excluded)
	private JournalModule excludeModule = null;
	
	//The associated journal
	private Journal associatedJournal = null;
	
	private Vector<Object> data = new Vector<Object>();
	
	private Object selectedItem = null;
	
	private Comparator<StackFilterTripel> comparator = new Comparator<StackFilterTripel>() {
		@Override
		public int compare(StackFilterTripel o1, StackFilterTripel o2) {
			int r1 = o1.module.getTabViewName().compareTo(o2.module.getTabViewName());
			if(r1 != 0) return r1;
			
			int r2 = o1.name.compareTo(o2.name);
			if(r2 != 0) return r2;
			
			return -1;
		}
	};
	
	// CONSTRUCTOR *************************
	// *************************************
	
	/**
	 * Constructs a new model
	 * @param journal The journal from which to take the data. This is essentially used to retrive an instance of {@link FilterPool} and 
	 * to obtain the year filters. If null, a dummy journal is created.
	 * @param exclude The module, the filters of which you do not want included in this model. If null, no module is excluded.
	 * @param yearfilters Whether the year filters for the corr. journal should be included additionally
	 */
	public FilterPoolModel(Journal journal, JournalModule exclude, boolean yearfilters) {
		associatedJournal = journal == null? new Journal() : journal;
		excludeModule = exclude;
		addYearFilters = yearfilters;
		computeModel();
		YearSeparators.getInstance(associatedJournal).addYearSeparatorListener(this);
		FilterPool.getPool(associatedJournal).addChangeListener(this);
	}
	
	// MODEL *******************************
	// *************************************
	
	private void computeModel() {
		data = new Vector<Object>();
		//First of all, add year filters
		if(addYearFilters) {
			data.addAll(YearSeparators.getInstance(associatedJournal).getUsedYears());
		}
		//Now add filters
		TreeSet<StackFilterTripel> tripels = new TreeSet<StackFilterTripel>(comparator);
		HashSet<StackFilterTripel> exclude = new HashSet<StackFilterTripel>();
			tripels.addAll(FilterPool.getPool(associatedJournal).getListOfFilters());
			if(excludeModule != null) {
				for(StackFilterTripel t : tripels) if(t.module == excludeModule) exclude.add(t);
				tripels.removeAll(exclude);
			}
		data.addAll(tripels);
		//Set selected item
		if(!data.contains(selectedItem)) selectedItem = data.size() > 0 ? data.get(0) : null;
		//Notify
		fireContentChanged();
	}
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		if(data.contains(anItem)) selectedItem = anItem;
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		if(l != null) listeners.add(l);
	}

	@Override
	public Object getElementAt(int index) {
		if(index < 0 || index >= data.size()) return null;
		else return data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	protected void fireContentChanged() {
		for(ListDataListener l : listeners) l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,getSize()-1));
	}

	// LISTENING *************************
	// ***********************************
	
	@Override
	public void separatorAdded(Journal source, ReadingPoint yearSeparator) {
		computeModel();		
	}

	@Override
	public void separatorRemoved(Journal source, ReadingPoint yearSeparator) {
		computeModel();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		computeModel();
	}

}
