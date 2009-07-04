package fs.fibu2.view.render;

import javax.swing.JToolBar;

import org.dom4j.Document;

import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * This class implements a toolbar with basic operations for a {@link JournalTable}, such as entry editing and view preferences.
 * @author Simon Hampe
 *
 */
public class JournalTableBar extends JToolBar implements ResourceDependent {

	@Override
	public void assignReference(ResourceReference r) {
		// TODO Auto-generated method stub

	}

	@Override
	public Document getExpectedResourceStructure() {
		// TODO Auto-generated method stub
		return null;
	}

}
