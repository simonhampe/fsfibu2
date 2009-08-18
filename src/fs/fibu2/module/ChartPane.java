package fs.fibu2.module;

import javax.swing.JPanel;

import org.dom4j.Document;

import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * This class implements a panel which displays a time series chart corresponding to a certain entry collection (determined by a filter).
 * It essentially displays a continuous bilancial of the chosen entries. There also is a toolbar used 
 * @author Simon Hampe
 *
 */
public class ChartPane extends JPanel implements ResourceDependent {

	// DATA ************************************
	// *****************************************
	
	// COMPONENTS ******************************
	// *****************************************
	
	// LISTENERS *******************************
	// *****************************************
	
	// CONSTRUCTOR *****************************
	// *****************************************
	
	// CONTROL METHODS *************************
	// *****************************************
	
	// RESOURCEDEPENDENT ***********************
	// *****************************************
	
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
