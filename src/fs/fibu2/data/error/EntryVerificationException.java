package fs.fibu2.data.error;

import java.util.Vector;

import fs.fibu2.data.model.Entry;

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
	
	
	private Entry verifiedEntry;
	private Vector<String> listOfFaultyFields;
	private Vector<Boolean> listOfCriticality;
	private Vector<String> faultDescriptions;
	
	/**
	 * Creates an exceptions
	 * @param verifiedEntry The entry which was verified and found faulty
	 * @param listOfFaultyFields The list of IDs of the fields which contain insufficient or incorrect data
	 * @param listOfCriticality A list indicating for each faulty field, whether the error is critical (the entry cannot be accepted that way)
	 * or not (the entry can at least temporarily accepted).
	 * @param faultDescriptions A description of the error for each field given in the previous list. The order should be identical.
	 */
	public EntryVerificationException(Entry verifiedEntry, Vector<String> listOfFaultyFields, 
			Vector<Boolean> listOfCriticality,
			Vector<String> faultDescriptions) {
		super();
		this.verifiedEntry = verifiedEntry;
		this.listOfFaultyFields = listOfFaultyFields == null? new Vector<String>() : new Vector<String>(listOfFaultyFields);
		this.listOfCriticality = listOfCriticality == null? new Vector<Boolean>() : new Vector<Boolean>(listOfCriticality);
		this.faultDescriptions = faultDescriptions == null? new Vector<String>() : new Vector<String>(faultDescriptions);
	}

	/**
	 * @return The list of field IDs of the fields with insufficient or incorrect data.
	 */
	public Vector<String> getListOfFaultyFields() {
		return new Vector<String>(listOfFaultyFields);
	}

	/**
	 * @return A list of boolean value indicating for each faulty field, whether the error is critical (the entry cannot be accepted that way)
	 * or not (the entry can at least temporarily accepted).
	 */
	public Vector<Boolean> getListOfCriticality() {
		return new Vector<Boolean>(listOfCriticality);
	}
	
	/**
	 * @return The list of error descriptions for each faulty field
	 */
	public Vector<String> getFaultDescriptions() {
		return new Vector<String>(faultDescriptions);
	}
	
	/**
	 * @return The entry which has been verified.
	 */
	public Entry getVerifiedEntry() {
		return verifiedEntry;
	}
	
	
}
