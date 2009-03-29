package fs.fibu2.data.model;

import org.dom4j.Element;
import org.dom4j.Node;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

public class Entry implements XMLConfigurable {

	// MEMBERS ******************************
	
	private String name;
	
	
	
	
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

	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

}
