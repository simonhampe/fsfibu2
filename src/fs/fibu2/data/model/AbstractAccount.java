package fs.fibu2.data.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This represents the minimal information an fsfibu2 account usually requires: An invoice number which has
 * to exist, if the entry value is negative.
 */
public class AbstractAccount extends Account {
	
	private static String		 	invoiceID 	= "invoice";
	private static String			accountID 	= "abstract_account";
	
	private static String			sgroup		= "fs.fibu2.AbstractAccount";
	
	// ACCOUNT METHODS ************************************
	// ****************************************************
	
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	@Override
	public HashMap<String,String> getFieldDescriptions() {
		HashMap<String,String> returnValue = new HashMap<String,String>();
		returnValue.put(invoiceID,Fsfibu2StringTableMgr.getString(sgroup + ".invoicedescription"));
		return returnValue;
	}

	@Override
	public HashMap<String,String> getFieldNames() {
		HashMap<String,String> returnValue = new HashMap<String,String>();
		returnValue.put(invoiceID,Fsfibu2StringTableMgr.getString(sgroup + ".invoicename"));
		return returnValue;
	}

	@Override
	public Vector<String> getFieldIDs() {
		return new Vector<String>(Arrays.asList(invoiceID));
	}

	@Override
	public String getID() {
		return accountID;
	}

	@Override
	public String getName() {
		 return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}

	/**
	 * If the value of e is negative and there is no invoice registration number (i.e. there is no information field or the trimmed string is empty),
	 * then it throws a non-critical error for the field 'invoice'.
	 */
	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		if(e == null) return;
		if(e.getValue() < 0) {
			HashMap<String,String> info = e.getAccountInformation();
			if(info.size() == 0 || info.get(invoiceID) == null || info.get(invoiceID).trim().equals("")) {
				Vector<String> faultyFields = new Vector<String>();
					faultyFields.add(invoiceID);
				HashMap<String,Boolean> criticality = new HashMap<String, Boolean>();
					criticality.put(invoiceID,false);
				HashMap<String,String> descriptions = new HashMap<String, String>();
					descriptions.put(invoiceID,Fsfibu2StringTableMgr.getString(sgroup + ".faultyinvoice"));
				throw new EntryVerificationException(e,faultyFields,criticality,descriptions);
			}
		}
	}
	
	/**
	 * Returns a multi-line summary of this account containing ID, Name, Description, List of
	 * fields with names and descriptions
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("ID: " + getID() + "\n" +  
				 Fsfibu2StringTableMgr.getString("fs.fibu2.global.name") + 
				 		": " + getName() + "\n" + 
				 Fsfibu2StringTableMgr.getString("fs.fibu2.global.description") + 
				 		": " + getDescription() + "\n" + 
				 Fsfibu2StringTableMgr.getString(sgroup + ".fieldlegend") + 
				 	"\n" );
		Vector<String> ids = getFieldIDs();
		HashMap<String,String> names = getFieldNames();
		HashMap<String,String> descs = getFieldDescriptions();
		for(int i = 0; i < ids.size(); i++) {
			b.append(ids.get(i) + " (");
			b.append((i < names.size()? names.get(ids.get(i)) : " - " ) + "): ");
			b.append((i < descs.size()? descs.get(ids.get(i)) : " - " ) + "\n");
		}
		
		return b.toString();
	}

}
