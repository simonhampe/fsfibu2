package fs.fibu2.data.model;

import java.util.GregorianCalendar;

import org.dom4j.Element;
import org.dom4j.Node;

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
	private boolean isActive;
	private boolean isVisible;
	
	
	
	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

}
