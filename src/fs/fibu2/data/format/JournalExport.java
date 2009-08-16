package fs.fibu2.data.format;

import java.io.IOException;

import fs.fibu2.data.model.Journal;

/**
 * A JournalExport is a class which can export an fsfibu 2 {@link Journal} to a file 
 * in a certain format, e.g. csv.
 * @author Simon Hampe
 *
 */
public interface JournalExport {

	/**
	 * @return The (possibly unique) ID of this Export. The convention is "ff2export_" + something
	 */
	public String getID();
	
	/**
	 * @return A short name, e.g. "CSV table"
	 */
	public String getName();
	
	/**
	 * @return A description of this export, e.g. "Converts the journal to a csv table, which 
	 * can be imported in any standard table calculation software"
	 */
	public String getDescription();
	
	/**
	 * Exports the journal
	 * @param j The journal to export. If null, this call should simply be ignored
	 * @param fileName The file to save the exported version to.
	 * @throws IOException - If any I/O- errors occur
	 */
	public void exportJournal(Journal j, String fileName) throws IOException;
}
