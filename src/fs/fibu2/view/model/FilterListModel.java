package fs.fibu2.view.model;

import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.FilterLoader;

/**
 * This class implements a list model for the filters available from the {@link FilterLoader}.
 * @author Simon Hampe
 *
 */
public class FilterListModel extends AbstractListModel implements ComboBoxModel, ChangeListener {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 7917133065933672658L;

	private Vector<EntryFilter> filterList = new Vector<EntryFilter>();
	
	private DefaultComboBoxModel model;
	
	// CONSTRUCTOR ********************************
	// ********************************************
	
	public FilterListModel() {
		updateModel();
	}
	
	// COMBOBOX METHODS ***************************
	// ********************************************
	
	@Override
	public Object getSelectedItem() {
		return model.getSelectedItem();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedItem(anItem);
	}

	@Override
	public Object getElementAt(int index) {
		return filterList.get(index);
	}

	@Override
	public int getSize() {
		return filterList.size();
	}

	// CHANGE LISTENING **************************
	// *******************************************
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateModel();
		fireContentsChanged(this, 0, getSize());		
	}
	
	protected void updateModel() {
		TreeSet<String> idSet = new TreeSet<String>(FilterLoader.getFilterIDs());
		filterList = new Vector<EntryFilter>();
		for(String id : idSet) {
			try {
				filterList.add(FilterLoader.getFilter(id));
			} catch (Exception e) {
				//Ignore
			}
		}
		model = new DefaultComboBoxModel(filterList);
	}

}
