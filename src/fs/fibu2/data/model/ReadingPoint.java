package fs.fibu2.data.model;

import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import fs.fibu2.data.event.ReadingPointListener;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * A reading point is essentially a day of the year at which the user wants to take a bilancial overview of his journal and perhaps
 * reset all bilancial sums. It can for example be used by tables displaying a journal to insert visual components displaying a bilancial overview.
 * A reading point can be active (i.e. all sums should be reset) or passive (only a bilancial overview is taken) and it can 
 * be visible or not. Both properties are reflected by boolean fields which can be set. The date associated to a reading point is a day of the year 
 * (more precise time information like hours, minutes, etc. will be ignored) and is considered <i>included</i> when it comes to taking a bilancial overview
 * of all entries <i>up to this reading point</i> and <i>excluded</i> when all entries <i>after this point</i> are taken. 
 * @author Simon Hampe
 *
 */
public class ReadingPoint implements XMLConfigurable {

	// FIELDS **********************************
	// *****************************************
	
	private GregorianCalendar readingDay;
	private String name;
	private boolean isActive;
	private boolean isVisible;
	
	private HashSet<ReadingPointListener> listeners = new HashSet<ReadingPointListener>();
	
	// CONSTRUCTOR ****************************
	// ****************************************
	
	/**
	 * Constructs a reading point.
	 * @param name The name of the point. If null, the empty string is used
	 * @param readingDay The date associated to the reading. Must not be null
	 * @param isActive Whether this reading point should be regarded as active (bilancial sums are reset)
	 * @param isVisible Whether this reading point should be displayed or not
	 * @throws NullPointerException - If readingDay == null
	 */
	public ReadingPoint(String name, GregorianCalendar readingDay, boolean isActive,
			boolean isVisible) throws NullPointerException {
		if(readingDay == null) throw new NullPointerException("Can't construct reading point from null date");
		this.name = name == null? "" : name;
		this.readingDay = (GregorianCalendar)readingDay.clone();
		this.isActive = isActive;
		this.isVisible = isVisible;
	}
	
	/**
	 * Constructs a reading point from the given node
	 * @throws XMLWriteConfigurationException - If the configuration fails
	 */
	public ReadingPoint(Node n) throws XMLWriteConfigurationException {
		configure(n);
	}
	
	// GETTERS AND SETTERS ********************
	// ****************************************
	
	/**
	 * @return The day associated to this reading
	 */
	public GregorianCalendar getReadingDay() {
		return (GregorianCalendar)readingDay.clone();
	}
	
	/**
	 * Sets the day associated to this reading
	 * @param readingDay - The new date. Must not be null
	 * @throws NullPointerException - If readingDay == null
	 */
	public void setReadingDay(GregorianCalendar readingDay) throws NullPointerException{
		if(readingDay == null) throw new NullPointerException("Null date invalid for reading point");
		this.readingDay = (GregorianCalendar)readingDay.clone();
		fireDateChanged();
	}
	
	/**
	 * @return Whether this reading point is currentyl active
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Sets whether this reading point should be active
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
		fireActivityChanged();
	}

	/**
	 * @return Whether this reading point is currently visible
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Sets whether this reading point should be visible
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		fireVisibilityChanged();
	}
	
	/**
	 * @return The name of this reading point
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this reading point. If newname == null, the empty string is used
	 */
	public void setName(String newname) {
		name = newname == null? "" : newname;
		fireNameChanged();
	}
	
	// ENTRY COMPARISON ***********************
	// ****************************************
	
	/**
	 * @return An integer value depending on whether: <br>
	 * - the day of the entry is less or equal (i.e. before or identical) than the date of this 
	 * entry (-1) or <br>
	 * - the day of the entry is greater (i.e. after) than this reading point's date (1) <br>
	 * Only day/month/year values are considered in the comparison. More precise information is ignored.
	 * If entry == null, 0 is returned
	 */
	public int compareTo(Entry e) {
		if(e == null) return 0;
		GregorianCalendar d = e.getDate();
		
		int thisyear = readingDay.get(GregorianCalendar.YEAR);
		int thatyear = d.get(GregorianCalendar.YEAR);
		if(thisyear < thatyear) return 1;
		if(thisyear > thatyear) return -1;
		
		int thismonth = readingDay.get(GregorianCalendar.MONTH);
		int thatmonth = d.get(GregorianCalendar.MONTH);
		if(thismonth < thatmonth) return 1;
		if(thismonth > thatmonth) return -1;
		
		int thisday = readingDay.get(GregorianCalendar.DAY_OF_MONTH);
		int thatday = d.get(GregorianCalendar.DAY_OF_MONTH);
		if(thisday < thatday) return 1;
		else return -1;
	}
	
	/**
	 * @return true, if and only if compareTo(e) returns -1, i.e. the date of entry is before this date or identical to it
	 */
	public boolean isBefore(Entry e) {
		return compareTo(e) == -1;
	}
	
	/**
	 * @return true, if and only if compareTo(e) returns 1 ,i.e. if the date of the entry lies after this date.
	 */
	public boolean isAfter(Entry e) {
		return compareTo(e) == 1;
	}
	
	// LISTENER MECHANISM *********************
	// ****************************************
	
	/**
	 * Adds l to the list of listeners (if l != null)
	 */
	public void addReadingPointListener(ReadingPointListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes l from the list of listeners
	 */
	public void removeReadingPointListener(ReadingPointListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Calls visibiltyChanged on all listeners
	 */
	protected void fireVisibilityChanged() {
		for(ReadingPointListener l : listeners) l.visibilityChanged(this);
	}
	
	/**
	 * Calls activityChanged on all listeners
	 */
	protected void fireActivityChanged() {
		for(ReadingPointListener l : listeners) l.activityChanged(this);
	}
	
	/**
	 * Calls dateChanged on all listeners
	 */
	protected void fireDateChanged() {
		for(ReadingPointListener l : listeners) l.dateChanged(this);
	}
	
	/**
	 * Calls nameChanged on all listeners
	 */
	protected void fireNameChanged() {
		for(ReadingPointListener l : listeners) l.nameChanged(this);
	}
	
	// XMLCONFIGURABLE ************************
	// ****************************************
	
	/**
	 * Expects three subnodes with appropriately formatted values: <br>
	 * - name (String) <br>
	 * - readingday (date: dd.MM.yyyy)<br>
	 * - isactive (boolean)<br>
	 * - isvisible (boolean)<br>
	 * Any value of this object will only be changed, if all values are present and correct.
	 * @throws XMLWriteConfigurationException - if any of these node does not exist or contains faulty data
	 */
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		if(n == null) throw new XMLWriteConfigurationException("Cannot configure reading point from null node");
		
		String newname;
		GregorianCalendar newdate;
		boolean newactive;
		boolean newvisible;
		
		Node nameNode = n.selectSingleNode("./name");
		if(nameNode == null) throw new XMLWriteConfigurationException("Invalid reading point configuration: name node missing");
		newname = nameNode.getText();
		
		Node dateNode = n.selectSingleNode("./readingday");
		if(dateNode == null) throw new XMLWriteConfigurationException("Invalid reading point configuration: date node missing");
		try {
			newdate = new GregorianCalendar();
			newdate.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(dateNode.getText()));
		}
		catch(ParseException pe) {
			throw new XMLWriteConfigurationException("Invalid reading point configuration: Date node contains invalid data.");
		}
		
		Node activeNode = n.selectSingleNode("./isactive");
		if(activeNode == null) throw new XMLWriteConfigurationException("Invalid reading point configuration: Activity node missing");
		newactive = Boolean.parseBoolean(activeNode.getText());
		
		Node visibleNode = n.selectSingleNode("./isvisible");
		if(visibleNode == null) throw new XMLWriteConfigurationException("Invalid reading point configuration: Visibility node missing");
		newvisible = Boolean.parseBoolean(visibleNode.getText());
		
		setName(newname);
		setReadingDay(newdate);
		setActive(newactive);
		setVisible(newvisible);
	}

	/**
	 * @return A node named 'readingpoint' containing subnodes as specified by configure(Node n)
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement root = new DefaultElement("readingpoint");
		
		DefaultElement nameNode = new DefaultElement("name");
		nameNode.setText(name);
		root.add(nameNode);
		DefaultElement dateNode = new DefaultElement("readingday");
		dateNode.setText(Fsfibu2DateFormats.getEntryDateFormat().format(readingDay.getTime()));
		root.add(dateNode);
		DefaultElement activeNode = new DefaultElement("isactive");
		activeNode.setText(Boolean.toString(isActive));
		root.add(activeNode);
		DefaultElement visibleNode = new DefaultElement("isvisible");
		visibleNode.setText(Boolean.toString(isVisible));
		root.add(visibleNode);
		
		return root;
	}

	/**
	 * @return 'readingpoint'
	 */
	@Override
	public String getIdentifier() {
		return "readingpoint";
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

}
