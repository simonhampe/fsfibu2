package fs.fibu2.lang;

import java.io.File;
import java.util.MissingFormatArgumentException;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLToolbox;

/**
 * This class provides static methods for reloading fsfibu2 string tables and retrieving a polyglot string loader. It also provides delegate methods
 * for direct string queries which automatically use the global language id.
 * @author Simon Hampe
 *
 */
public final class Fsfibu2StringTableMgr implements ResourceDependent{

	//The resource reference for locating the string tables
	private static ResourceReference reference = Fsfibu2DefaultReference.getDefaultReference();
	
	//The only instance of this manager
	private static Fsfibu2StringTableMgr global_instance = null;
	
	//The fsfibu2 string table
	private static PolyglotStringTable fsfibu2Table = null;
	
	//The associated loader
	private static PolyglotStringLoader fsfibu2Loader = null;
	
	//The logger
	private static Logger logger = Logger.getLogger(Fsfibu2StringTableMgr.class); 
	
	// CONSTRUCTORS AND CREATION METHODS *******************
	// *****************************************************
	
	/**
	 * A protected constructor which initializes the resource reference to
	 * be the default reference
	 */
	protected Fsfibu2StringTableMgr() {
	 
	}
	
	public Fsfibu2StringTableMgr getInstance() {
		if(global_instance == null) global_instance = new Fsfibu2StringTableMgr();
		return global_instance;
	}
	
	// TABLE RETRIEVAL METHODS *******************************
	// *******************************************************
	
	/**
	 * This methods reloads the fsfibu2 string table and all update tables located in (fsfibu2)/lang/updatetables
	 */
	public static void reloadTables() {
		//Reload core table
		try {
			fsfibu2Table = new PolyglotStringTable(XMLToolbox.loadXMLFile(new File(reference.getFullResourcePath(global_instance, "lang/fsfibu2StringTable.xml"))),FsfwDefaultReference.getDefaultReference());
		} catch (Exception e) {
			logger.error("Cannot load fsfibu2 string table: " + e.getMessage());
			fsfibu2Table = new PolyglotStringTable("","");
		}
		//Load update tables
		File dir = new File(reference.getFullResourcePath(global_instance, "lang/"));
		for(File f: dir.listFiles()) {
			try {
				//Only load .xml files
				if(!f.getName().endsWith(".xml")) continue;
				Document d = XMLToolbox.loadXMLFile(f);
				fsfibu2Table.configure(d.getRootElement());
			} catch (Exception e) {
				logger.warn("Cannot load update table " + f.getName() + ". " + 
							"Skipping it.");
			}
		}
		//Init loader
		fsfibu2Loader = new PolyglotStringLoader(fsfibu2Table,PolyglotStringTable.getGlobalLanguageID(),"");		
	}
	
	/**
	 * @return Returns the string loader associated to the fsfibu2 string table. If the string 
	 * table has not yet been created, it will be loaded first.
	 */
	public static PolyglotStringLoader getLoader() {
		if(fsfibu2Loader == null) reloadTables();
		return fsfibu2Loader;
	}
	
	/**
	 * Queries for the unformatted string given by the id under the global language id using the fsfibu2 string loader
	 */
	public static String getString(String id) {
		if(fsfibu2Loader == null) reloadTables();
		return fsfibu2Loader.getUnformattedString(id, PolyglotStringTable.getGlobalLanguageID());
	}
	
	/**
	 * Queries for the formatted string given by the id and the arguments under the global language id using the fsfibu2 string loader
	 */
	public static String getString(String id, Object... args) throws MissingFormatArgumentException {
		if(fsfibu2Loader == null) reloadTables();
		return fsfibu2Loader.getString(id, PolyglotStringTable.getGlobalLanguageID(), args);
	}
	

	// RESOURCEDEPENDENT *************************
	// *******************************************
	
	/**
	 * Assigns a resource reference. r == null indicates that the default reference should be used
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = (r != null) ? r : Fsfibu2DefaultReference.getDefaultReference();
	}

	/**
	 * Expects the fsfibu2 main string table: (fsfibu2)/lang/fsfibu2StringTable.xml
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("lang/fsfibu2StringTable.xml");
		return tree;
	}
	
	
	
}
