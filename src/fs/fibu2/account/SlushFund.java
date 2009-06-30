package fs.fibu2.account;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.data.model.*;

/**
 * An illegal slush fund for keeping our drug sale income (a simple example which can be used for demonstrating the account plugin mechanism)
f * @author Simon Hampe
 *
 */
public class SlushFund extends AbstractAccount {

	@Override
	public String getDescription() {
		return "An illegal slush fund for keeping our drug sale income";
	}

	@Override
	public HashMap<String, String> getFieldDescriptions() {
		return super.getFieldDescriptions();
	}

	@Override
	public Vector<String> getFieldIDs() {
		return super.getFieldIDs();
	}

	@Override
	public HashMap<String, String> getFieldNames() {
		return super.getFieldNames();
	}

	@Override
	public String getID() {
		return "slush_fund";
	}

	@Override
	public String getName() {
		return "Slush fund";
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * @throws EntryVerificationException - if any invoice has been specified. This is an ILLEGAl depot, so we DON'T keep
	 * any paperwork, stupid.
	 */
	@Override
	public void verifyEntry(Entry e) throws EntryVerificationException {
		if(e != null) {
			HashMap<String, String> accinf = e.getAccountInformation();
			if(accinf.get("invoice") != null && !accinf.get("invoice").trim().equals("")) {
				Vector<String> faulty = new Vector<String>(Arrays.asList("invoice"));
				HashMap<String,Boolean> crit = new HashMap<String, Boolean>();
					crit.put("invoice",true);
				HashMap<String,String> desc = new HashMap<String, String>();
					desc.put("invoice", "This is an ILLEGAL cash box, so we don't keep any paperwork, stupid!");
				throw new EntryVerificationException(e,faulty,crit,desc);
			}
		}
	}

}

