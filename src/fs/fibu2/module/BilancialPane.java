package fs.fibu2.module;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.dom4j.Document;

import fs.fibu2.view.render.BilancialTree;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * Contains a bilancial view of a journal together with a toolbar for editing filters, etc.
 * @author Simon Hampe
 *
 */
public class BilancialPane extends JPanel implements ResourceDependent {

	// COMPONENTS *************************
	// ************************************
	
	private BilancialTree tree;
	private JTable table;
	
	
	// DATA *******************************
	// ************************************
	
	// LISTENERS **************************
	// ************************************
	
	// MISC *******************************
	// ************************************
	
	// CONSTRUCTOR ************************
	// ************************************
	
	// RESOURCEDEPENDENT ******************
	// ************************************
	
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
