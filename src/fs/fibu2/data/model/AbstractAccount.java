package fs.fibu2.data.model;

import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;

/**
 * This represents the minimal information an fsfibu2 account usually requires: An invoice number which has
 * to exist, if the entry value is negative.
 */
public class AbstractAccount implements Account {

	
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getFieldDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getFieldsIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		// TODO Auto-generated method stub

	}

}
