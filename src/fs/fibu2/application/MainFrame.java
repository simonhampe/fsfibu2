package fs.fibu2.application;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import sun.applet.AppletPanel;

import com.sun.security.auth.UserPrincipal;

import fs.event.DocumentChangeFlag;
import fs.fibu2.data.model.Journal;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.XMLToolbox;
/**
 * This class implements the main frame of an fsfibu2 application. It keeps a list of open journals (fsfibu2 is a multi-document application) and (de)serializes 
 * this list from/to the user {@link Preferences}.
 * @author Simon Hampe
 *
 */
public class MainFrame extends JFrame {

	private Logger logger = Logger.getLogger(this.getClass());
	
	//A list of all journals open, together with the associated view
	private Vector<Journal> listOfJournals = new Vector<Journal>();
	private Vector<JournalView> listOfJournalViews = new Vector<JournalView>();
	
	//The currently selected Journal
	private Journal currentlySelected = null;
	
	//A map giving the document flag for each open journal
	private HashMap<Journal, DocumentChangeFlag> changeFlags = new HashMap<Journal, DocumentChangeFlag>();
	
	/**
	 * Constructs a main frame. A list of journals which were open the last time is retrieved from the user preferences and the associated 
	 * {@link JournalView}s are constructed.
	 */
	public MainFrame() {
		super();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//Create file list from preferences
		try {
			logger.info(Fsfibu2StringTableMgr.getString("fs.fibu2.init.prefjournals"));
			Preferences openNode = Preferences.userRoot().node("fsfibu2/session/openjournals");
			int i = 1;
			while(openNode.nodeExists(Integer.toString(i))) {
				Preferences journalNode = openNode.node(Integer.toString(i));
				String path = journalNode.get("path", null);
				if(path != null) {
					 Document d = XMLToolbox.loadXMLFile(new File(path));
					 Journal j = new Journal(d.getRootElement());
				}
				i++;
			}
			
		}
		catch(Exception e) {
			logger.warn(Fsfibu2StringTableMgr.getString("fs.fibu2.init.prefjournalserror",e.getMessage()));
		}
		
		
		updateTitle();
	}
	
	protected void updateTitle() {
		StringBuilder b = new StringBuilder();
		b.append("fsfibu 2 - ");
		if(currentlySelected == null) b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.MainFrame.nojournal"));
		else {
			b.append(currentlySelected.getName());
			if(changeFlags.get(currentlySelected).hasBeenChanged()) b.append("*");
		}
		setTitle(b.toString());
	}
	
	/**
	 * Adds the journal
	 */
	protected addJournal(Journal j) {
		
	}
	
}
