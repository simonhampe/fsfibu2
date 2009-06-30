package fs.fibu2.application;

import java.util.prefs.Preferences;

/**
 * This class provides static methods to obtain the user preferences nodes for this application
 * @author Simon Hampe
 *
 */
public class AppPreferences {

	/**
	 * The root path to the fsfibu2 preferences 
	 */
	public static final String path_global = "fsfibu2/";
	
	/**
	 * The path to the fsfibu2 preferences which concern rendering (i.e. colors and similar)
	 */
	public static final String path_view = path_global + "view/";
	
	/**
	 * The path to the fsfibu2 preferences which concern the last session (i.e. open files, filters, etc.)
	 */
	public static final String path_session = path_global + "session/";
	
	/**
	 * @return  The root path to the fsfibu2 preferences
	 */
	public static Preferences getGlobalPreferences() {
		return Preferences.userRoot().node(path_global);
	}
	
	/**
	 * @return The path to the fsfibu2 preferences which concern rendering (i.e. colors and similar)
	 */
	public static Preferences getViewPreferences() {
		return Preferences.userRoot().node(path_view);
	}
	
	/**
	 * @return The path to the fsfibu2 preferences which concern the last session (i.e. open files, filters, etc.)
	 */
	public static Preferences getSessionPreferences() {
		return Preferences.userRoot().node(path_session);
	}
	
}
