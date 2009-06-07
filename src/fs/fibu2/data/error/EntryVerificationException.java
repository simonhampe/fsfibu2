package fs.fibu2.data.error;

import java.util.HashMap;
import java.util.Vector;

import fs.fibu2.data.model.Entry;
import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * This exception is thrown whenever an fsfibu2 journal entry contains invalid or insufficient information.
 * It contains a list of field ids which are concerned and the reason why the data is faulty.
 * 
 * @author Simon Hampe
 *
 */
public class EntryVerificationException extends Exception {
	
	/**
	 * Compiler-generated version id 
	 */
	private static final long serialVersionUID = 260009525496259815L;
	
	private final static String color_report_critical = "\"#FF0000\"";
	private final static String color_report_normal = "\"000000\"";
	
	
	private Entry verifiedEntry;
	private Vector<String> listOfFaultyFields;
	private HashMap<String,Boolean> listOfCriticality;
	private HashMap<String,String> faultDescriptions;
	
	/**
	 * Creates an exceptions
	 * @param verifiedEntry The entry which was verified and found faulty
	 * @param listOfFaultyFields The list of IDs of the fields which contain insufficient or incorrect data
	 * @param listOfCriticality A list indicating for each faulty field, whether the error is critical (the entry cannot be accepted that way)
	 * or not (the entry can at least temporarily accepted).
	 * @param faultDescriptions A description of the error for each field given in the previous list. The order should be identical.
	 */
	public EntryVerificationException(Entry verifiedEntry, Vector<String> listOfFaultyFields, 
			HashMap<String,Boolean> listOfCriticality,
			HashMap<String,String> faultDescriptions) {
		super();
		this.verifiedEntry = verifiedEntry;
		this.listOfFaultyFields = listOfFaultyFields == null? new Vector<String>() : new Vector<String>(listOfFaultyFields);
		this.listOfCriticality = listOfCriticality == null? new HashMap<String,Boolean>() : new HashMap<String,Boolean>(listOfCriticality);
		this.faultDescriptions = faultDescriptions == null? new HashMap<String,String>() : new HashMap<String,String>(faultDescriptions);
	}

	/**
	 * @return The list of field IDs of the fields with insufficient or incorrect data. This is a direct object link, so any change is 
	 * a change of this exception.
	 */
	public Vector<String> getListOfFaultyFields() {
		return listOfFaultyFields;
	}

	/**
	 * @return A list of boolean value indicating for each faulty field, whether the error is critical (the entry cannot be accepted that way)
	 * or not (the entry can at least temporarily accepted). This is a direct object link, so any change is 
	 * a change of this exception.
	 */
	public HashMap<String,Boolean> getListOfCriticality() {
		return listOfCriticality;
	}
	
	/**
	 * @return The list of error descriptions for each faulty field This is a direct object link, so any change is 
	 * a change of this exception.
	 */
	public HashMap<String,String> getFaultDescriptions() {
		return faultDescriptions;
	}
	
	/**
	 * @return The entry which has been verified.
	 */
	public Entry getVerifiedEntry() {
		return verifiedEntry;
	}
	
	/**
	 * @return A multi-line representation of the exception
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(String s : getListOfFaultyFields()) {
			b.append(s + ": " + "(Critical: " + getListOfCriticality().get(s) + ") " + getFaultDescriptions().get(s) + "\n");
		}
		return b.toString();
	}
	
	/**
	 * @return A string in html format which can be used for tooltips
	 */
	public String getHTMLRepresentation() {
		StringBuilder b = new StringBuilder();
		b.append("<html><b>");
		b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.error.EntryVerificationException.report"));
		b.append(":</b><br>");
		for(String s : getListOfFaultyFields()) {
			b.append("<font color =");
			b.append(getListOfCriticality().get(s) ? color_report_critical : color_report_normal);
			b.append(">- ");
			b.append(getFaultDescriptions().get(s));
			b.append("</font>");
		}
		b.append("</html>");
		return b.toString();
	}
	
}
