package fs.fibu2.test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Theoretically I wrote this class to test the {@link Preferences} class - but didn't really need it, so I'm using it for small one-time tests.
 * @author Simon Hampe
 *
 */
public class PreferencesTest {

	/**
	 * @param args
	 * @throws BackingStoreException 
	 */
	public static void main(String[] args) throws BackingStoreException {
		Preferences.userRoot().flush();
	}

}
