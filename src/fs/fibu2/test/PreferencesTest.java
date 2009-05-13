package fs.fibu2.test;

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Preferences p = Preferences.userRoot();
		Preferences n = p.node("bla");
		n.put("id", "ff2filter_name");
		try {
			p.exportSubtree(System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
