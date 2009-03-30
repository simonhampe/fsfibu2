package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.HashSet;

import org.dom4j.Element;
import org.dom4j.Node;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class can be interpreted in two ways: It either gives information about a certain account or specifies which information is necessary for an
 * account. An AccountInformation object containing information can be verified using another AccountInformation object which is interpreted as 
 * specification. Each information is identified by a unique ID and optionally an AccountInformationVerifier object which can further test 
 * the information for validity.
 * @author Simon Hampe
 *
 */
public class AccountInformation implements XMLConfigurable {

	// MEMBERS *******************************
	// ***************************************
	
	private HashSet<String> idsOfFields = new HashSet<String>();
	private HashMap<String, AccountInformationVerifier> propertiesOfFields = new HashMap<String, AccountInformationVerifier>();
	private HashMap<String, String> valueOfFields = new HashMap<String, String>();
	
	
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
