package fs.fibu2.filter;

import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;

/**
 * Filters entries according to the information supplied for an account
 * @author Simon Hampe
 *
 */
class AccountInformationFilter implements EntryFilter {

	
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntryFilterEditor getEditor(Journal j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyEntry(Entry e) {
		// TODO Auto-generated method stub
		return false;
	}

}
