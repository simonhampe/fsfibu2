package fs.fibu2.view.model;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.FilterPool;

/**
 * This class represents the filters available in the {@link FilterPool} of a given {@link Journal}. Optionally, filters of a certain module
 * can be excluded (e.g. a module usually doesn't want to display its own filters), 
 * @author Simon Hampe
 *
 */
public class FilterPoolModel implements ComboBoxModel {

	@Override
	public Object getSelectedItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

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

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

}
