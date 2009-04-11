package fs.fibu2.data.model;

import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This class represents a cash box. It has exactly the same properties and requirements as AbstractAccount, i.e. an invoice number
 * for negative entries is required
 * @author Simon Hampe
 *
 */
public class CashBox extends AbstractAccount {

	private final static String sgroup = "fs.fibu2.CashBox";
	private final static String accountID = "cash_box";
	
	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getDescription()
	 */
	@Override
	public String getDescription() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".description");
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getID()
	 */
	@Override
	public String getID() {
		return accountID;
	}

	/* (non-Javadoc)
	 * @see fs.fibu2.data.model.AbstractAccount#getName()
	 */
	@Override
	public String getName() {
		return Fsfibu2StringTableMgr.getString(sgroup + ".name");
	}
	
	
}
