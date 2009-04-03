package fs.fibu2.resource;

import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

public class Fsfibu2DefaultReference implements ResourceReference {

	/**
	 * The base directory of fsfibu2. By default
	 * it is assumed to be the working directory
	 */
	private static String basedir = ".";

	/**
	 * The only Fsfibu2 reference
	 */
	private static Fsfibu2DefaultReference globalReference = null;
	
	/**
	 * Constructs the reference with the given base directory
	 */
	private Fsfibu2DefaultReference(String newbasedir) {
		basedir = newbasedir;
	}

	/**
	 * Returns the FsfwDefaultReference
	 */
	public static Fsfibu2DefaultReference getDefaultReference() {
		// If it doesn't exist, it is created with the current base dir
		if (globalReference == null) {
			globalReference = new Fsfibu2DefaultReference(basedir);
		}
		return globalReference;
	}

	/**
	 * Sets the base path of the <i>global</i> reference for fsfibu2. A null
	 * string is interpreted as the empty string.
	 */
	public static void setFsfibuDirectory(String newDirectory) {
		basedir = newDirectory == null ? "" : newDirectory;
	}

	/**
	 * Always returns (base path) + "/" + path
	 */
	public String getFullResourcePath(ResourceDependent r, String path) {
		return basedir + "/" + path;
	}
}
