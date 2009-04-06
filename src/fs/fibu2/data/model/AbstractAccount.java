package fs.fibu2.data.model;

import java.util.Arrays;
import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.PolyglotStringTable;

/**
 * This represents the minimal information an fsfibu2 account usually requires: An invoice number which has
 * to exist, if the entry value is negative.
 */
public class AbstractAccount extends Account {
	
	private static Vector<String> 	fieldIDs 	= new Vector<String>(Arrays.asList("invoice"));
	private static String			accountID 	= "abstract_account";
	
	private static String			sgroup		= "fs.fibu2.AbstractAccount";
	
	// ACCOUNT METHODS ************************************
	// ****************************************************
	
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".description", PolyglotStringTable.getGlobalLanguageID());
	}

	@Override
	public Vector<String> getFieldDescriptions() {
		Vector<String> returnValue = new Vector<String>();
		returnValue.add(Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".invoicedescription", PolyglotStringTable.getGlobalLanguageID()));
		return returnValue;
	}

	@Override
	public Vector<String> getFieldNames() {
		Vector<String> returnValue = new Vector<String>();
		returnValue.add(Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".invoicename", PolyglotStringTable.getGlobalLanguageID()));
		return returnValue;
	}

	@Override
	public Vector<String> getFieldIDs() {
		return new Vector<String>(fieldIDs);
	}

	@Override
	public String getID() {
		return accountID;
	}

	@Override
	public String getName() {
		 return Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".name", PolyglotStringTable.getGlobalLanguageID());
	}

	/**
	 * If the value of e is negative and there is no invoice registration number (i.e. there is no information field or the trimmed string is empty),
	 * then it throws a non-critical error for the field 'invoice'.
	 */
	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		if(e == null) return;
		if(e.getValue() < 0) {
			Vector<String> info = e.getAccountInformation();
			if(info.size() == 0 || info.get(0) == null || info.get(0).trim().equals("")) {
				Vector<String> faultyFields = new Vector<String>();
					faultyFields.add("invoice");
				Vector<Boolean> criticality = new Vector<Boolean>();
					criticality.add(false);
				Vector<String> descriptions = new Vector<String>();
					descriptions.add(Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".faultyinvoice", PolyglotStringTable.getGlobalLanguageID()));
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
				 Fsfibu2StringTableMgr.getLoader().getString("fs.fibu2.global.name", PolyglotStringTable.getGlobalLanguageID()) + 
				 		": " + getName() + "\n" + 
				 Fsfibu2StringTableMgr.getLoader().getString("fs.fibu2.global.description", PolyglotStringTable.getGlobalLanguageID()) + 
				 		": " + getDescription() + "\n" + 
				 Fsfibu2StringTableMgr.getLoader().getString(sgroup + ".fieldlegend", PolyglotStringTable.getGlobalLanguageID()) + 
				 	"\n" );
		Vector<String> ids = getFieldIDs();
		Vector<String> names = getFieldNames();
		Vector<String> descs = getFieldDescriptions();
		for(int i = 0; i < ids.size(); i++) {
			b.append(ids.get(i) + " (");
			b.append((i < names.size()? names.get(i) : " - " ) + "): ");
			b.append((i < descs.size()? descs.get(i) : " - " ) + "\n");
		}
		
		return b.toString();
	}

}
