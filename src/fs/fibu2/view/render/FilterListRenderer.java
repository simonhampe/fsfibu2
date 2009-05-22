package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.filter.EntryFilter;

/**
 * Renders {@link EntryFilter} entries in lists simply by displaying the name of the filter
 * @author Simon Hampe
 *
 */
public class FilterListRenderer extends DefaultListCellRenderer {
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -792918860906832907L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if(!(value instanceof EntryFilter)){
			return label;
		}
		label.setText(((EntryFilter)value).getName());
		return label;
	}

	
	
}
