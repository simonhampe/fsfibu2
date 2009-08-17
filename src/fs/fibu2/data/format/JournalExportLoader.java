package fs.fibu2.data.format;

import java.util.HashMap;
import java.util.HashSet;

import fs.fibu2.export.CSVTableExport;
import fs.fibu2.export.OldJournalExport;
import fs.fibu2.export.StandardJournalExport;
import fs.fibu2.export.VeryOldJournalExport;

/**
 * This class provides access to {@link JournalExport}s via their ID. It also provides methods
 * to add a .class file of a JournalExport.
 * @author Simon Hampe
 *
 */
public class JournalExportLoader {

	//Map of String (ID) -> class object for instance retrieval
	private static HashMap<String, JournalExport> exportMap = new HashMap<String, JournalExport>();
	
	//Init code, load all basic filters
	static {
		exportMap.put((new StandardJournalExport()).getID(), new StandardJournalExport());
		exportMap.put((new OldJournalExport()).getID(), new OldJournalExport());
		exportMap.put((new VeryOldJournalExport()).getID(),new VeryOldJournalExport());
		exportMap.put((new CSVTableExport()).getID(), new CSVTableExport());
	}
	
	/**
	 * Creates an instance of the export associated to this id and returns it
	 * @throws IllegalArgumentException - If there is no export class for this id 
	 */
	public static JournalExport getExport(String id) throws IllegalArgumentException{
		if(id == null) throw new NullPointerException("Null id invalid for export creation");
		try {
			return exportMap.get(id);
		}
		catch(NullPointerException ne)  {
			throw new IllegalArgumentException("No export class found for id '" + id + "'");
		}
	}
	
	/**
	 * Sets the export class for the given id. If there already is a class for this id, it is overwritten. If filter == null, the corresponding class
	 * is removed. If id == null, this call is ignored
	 */
	public static void setExport(String id, JournalExport export) {
		if(id == null) return;
		if(export == null) exportMap.remove(id);
		else exportMap.put(id, export);
	}
	
	/**
	 * This tries to create an {@link JournalExport} instance from the given class and will then create a mapping for its id. If there is already a mapping for this
	 * id, this call is ignored
	 * @throws UnsupportedOperationException - If any error occurs during instantiation
	 */
	public static void loadExport(Class<?> exportClass) {
		try {
			JournalExport f = (JournalExport) exportClass.newInstance();
			if(!exportMap.containsKey(f.getID())) {
				exportMap.put(f.getID(), f);
			}
		}
		catch(Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * @return A list of all ids for which this loader has a mapping
	 */
	public static HashSet<String> getExportIDs() {
		return new HashSet<String>(exportMap.keySet());
	}
	
	
}
