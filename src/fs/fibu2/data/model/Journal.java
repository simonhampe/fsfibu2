package fs.fibu2.data.model;

import org.dom4j.Element;
import org.dom4j.Node;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class implements an fsfibu2 journal. A journal is essentially an (unsorted) collection of entries together
 * with some additional data: <br>
 * - ReadingPoints: A reading point is a certain day of the year at which the user whishes to see a bilancial overview and potentially reset
 * all sums. <br>
 * - Start values: A journal contains a start value for each account it uses.<br>
 * <br> 
 * @see 
 * @author Simon Hampe
 *
 */
public class Journal implements XMLConfigurable {

	
	
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
