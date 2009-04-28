package fs.fibu2.view.model;

import javax.swing.AbstractListModel;

/**
 * This class implements a simple list of categories of a journal. It listens to the journal and reloads its content, every time the journal changes.
 * It orders the categories hierarchically, i.e. the top categories are ordered alhabetically and all subcategories are then insertes in between
 * and again sorted alphabetically and so on.
 * @author Simon Hampe
 *
 */
public class CategoryListModel extends AbstractListModel {

	@Override
	public Object getElementAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
