package fs.fibu2.view.model;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import fs.fibu2.data.event.JournalListener;
import fs.fibu2.data.format.EntryComparator;
import fs.fibu2.data.format.EntryDateComparator;
import fs.fibu2.data.format.MoneyDecimal;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.event.ProgressListener;

/**
 * This class implements a time series collection for an fsfibu 2 {@link Journal}. The collection contains a bilancial time series
 * for all entries admitted by a certain filter and optionally a moving average.
 * @author Simon Hampe
 *
 */
public class JournalTimeSeriesCollection extends TimeSeriesCollection implements JournalListener, ChangeListener {

	// DATA ****************************************
	// *********************************************

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 6169851143672341898L;
	
	private TimeSeries averageData;
	private TimeSeries entryData;
	
	private int avgPeriod;
	
	private boolean displayAvg;
	
	private Journal associatedJournal;
	
	private StackFilter filter;
	
	private final static String sgroup = "fs.fibu2.model.JournalTimeSeriesCollection";
	
	private HashSet<ProgressListener<Object, Object>> listenerList = new HashSet<ProgressListener<Object,Object>>();
	
	private Recalculator runningInstance = null;
	
	// CONSTRUCTOR *********************************
	// *********************************************

	/**
	 * Constructs a new TimeSeriesCollection.
	 * @param j The journal from which to take the data. If null, an empty dummy journal is created
	 * @param f The filter which determines the entries to take into account
	 * @param displayAverage Whether a moving average should be displayed additionally
	 * @param averagePeriod The time period in days for the moving average
	 */
	public JournalTimeSeriesCollection(Journal j, StackFilter f, boolean displayAverage, int averagePeriod) {
		associatedJournal = j == null? new Journal(): j;
		filter = f;
		displayAvg = displayAverage;
		avgPeriod = averagePeriod;
		
		associatedJournal.addJournalListener(this);
		if(filter != null) filter.addChangeListener(this);
		
		DataVector v = recalculateData();
		entryData = v.dataSeries;
		averageData = v.avgSeries;
		addSeries(entryData);
		if(displayAverage) {
			addSeries(averageData);
		}
	}
	
	// CONTROL METHODS *****************************
	// *********************************************
	
	private DataVector recalculateData() {
		DataVector v = new DataVector();
		
		TimeSeries data = new TimeSeries(Fsfibu2StringTableMgr.getString(sgroup + ".data"), Day.class);
		TreeSet<Entry> sortedSet = new TreeSet<Entry>(new EntryComparator(false));
		sortedSet.addAll(associatedJournal.getEntries());
		
		if(sortedSet.size() != 0) {
			EntryDateComparator comparator = new EntryDateComparator();
			GregorianCalendar currentDay = sortedSet.first().getDate();
			boolean validEntryOccured = false;
			BigDecimal currentValue = MoneyDecimal.bigd(0);
			
			for(Entry e : sortedSet) {
				BigDecimal evalue = MoneyDecimal.bigd(e.getValue());
				if(filter == null || filter.verifyEntry(e)) {
					//If the entry belongs to the same day, add its value
					if(comparator.compare(e.getDate(), currentDay) == 0) {
						currentValue = MoneyDecimal.add(currentValue, evalue);
					}
					//Otherwise add a time series value (if this is NOT the first entry, which might occur when using  a filter)
					else {
						if(validEntryOccured)data.add(new Day(currentDay.getTime()), currentValue);
						currentDay = e.getDate();
						currentValue = MoneyDecimal.add(currentValue, evalue);					
					}
					validEntryOccured = true;
				}
			}
			//A last chunk of entries is left after the last entry was processed - but only if any valid entry occured
			if(validEntryOccured) data.add(new Day(currentDay.getTime()),currentValue);
		}
		
		TimeSeries avg = MovingAverage.createMovingAverage(data, Fsfibu2StringTableMgr.getString(sgroup + ".average",avgPeriod), avgPeriod, 0);
		v.dataSeries = data;
		v.avgSeries = avg;
		return v;
	}
	
	/**
	 * @return An instance of Recalculator. Cancels a running instance, if it exists 
	 */
	private synchronized Recalculator getRecalculatorInstance() {
		if(runningInstance == null) {
			runningInstance = new Recalculator();
			return runningInstance;
		}
		else {
			runningInstance.cancel(true);
			runningInstance = new Recalculator();
			return runningInstance;
		}
	}
	
	private void doRecalculation() {
		Recalculator c = getRecalculatorInstance();
		fireTaskBegins(c);
		c.execute();
	}
	
	/**
	 * Controls, whethe a moving average is displayed additionally
	 */
	public void setDisplayAverage(boolean display) {
		if(display != displayAvg) {
			displayAvg = display;
			if(displayAvg) addSeries(averageData);
			else removeSeries(averageData);
		}
	}
	
	/**
	 * Sets the period of the moving average
	 * @param days The period in days. If < 1, the value is set to 1
	 */
	public void setMovingAveragePeriod(int days) {
		avgPeriod = days >= 1? days : 1;
		if(displayAvg) removeSeries(averageData);
		averageData = MovingAverage.createMovingAverage(entryData, Fsfibu2StringTableMgr.getString(sgroup + ".average",avgPeriod), avgPeriod, 0);
		if(displayAvg) addSeries(averageData);
	}
	
	/**
	 * @return The period in days for the moving average
	 */
	public int getMovingAveragePeriod() {
		return avgPeriod;
	}
	
	/**
	 * @return Whether a moving average is displayed
	 */
	public boolean doesDisplayMovingAverage() {
		return displayAvg;
	}
	
	// LISTENING ***********************************
	// *********************************************
	
	@Override
	public void descriptionChanged(Journal source, String oldValue,
			String newValue) {
		//Ignore
	}

	@Override
	public void entriesAdded(Journal source, Entry[] newEntries) {
		doRecalculation();
	}

	@Override
	public void entriesRemoved(Journal source, Entry[] oldEntries) {
		doRecalculation();
	}

	@Override
	public void entryReplaced(Journal source, Entry oldEntry, Entry newEntry) {
		doRecalculation();
	}

	@Override
	public void nameChanged(Journal source, String oldValue, String newValue) {
		//Ignored
	}

	@Override
	public void readingPointAdded(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void readingPointRemoved(Journal source, ReadingPoint point) {
		//Ignored
	}

	@Override
	public void startValueChanged(Journal source, Account a, Float oldValue,
			Float newValue) {
		//Ignored
	}

	@Override
	public void dateChanged(ReadingPoint source) {
		//Ignored
	}

	@Override
	public void nameChanged(ReadingPoint source) {
		//Ignored
	}
	

	@Override
	public void stateChanged(ChangeEvent e) {
		doRecalculation();
	}

	public void addProgressListener(ProgressListener<Object, Object> l) {
		if(l != null) listenerList.add(l);
	}
	
	public void removeProgressListener(ProgressListener<Object, Object> l) {
		listenerList.remove(l);
	}
	
	protected void fireTaskBegins(Recalculator source) {
		for(ProgressListener<Object, Object> l : listenerList) l.taskBegins(source);
	}
	
	protected void fireTaskFinished(Recalculator source) {
		for(ProgressListener<Object, Object> l : listenerList) l.taskFinished(source);
	}
	
	// LOCAL RECALCULATION CLASSES **********************
	// **************************************************
	
	private class DataVector {
		public TimeSeries dataSeries;
		public TimeSeries avgSeries;
	}
	
	private class Recalculator extends SwingWorker<Object, Object> {
		
		@Override
		protected Object doInBackground() throws Exception {
			return recalculateData();
		}

		@Override
		protected void done() {
			if(!isCancelled()) {
				DataVector v;
				try {
					v = (DataVector)get();
					removeAllSeries();
					averageData = v.avgSeries;
					entryData = v.dataSeries;
					addSeries(entryData);
					if(displayAvg) {
						addSeries(averageData);
					}
				} catch (Exception e) {
					//Ignore
				}
			}
			fireTaskFinished(this);
		}
		
		
	}

	
	
}
