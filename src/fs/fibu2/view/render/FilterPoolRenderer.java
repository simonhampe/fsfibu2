package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.filter.FilterPool.StackFilterTripel;
import fs.fibu2.view.model.FilterPoolModel;

/**
 * Displays an entry of a {@link FilterPoolModel}. If it's an integer, it just displays an Integer. If its a StackFilterTripel, it displays the name of
 * the module + the name of the filter
 * @author Simon Hampe
 *
 */
public class FilterPoolRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -4873983588590947335L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if(value instanceof Integer)  {
			l.setText(value.toString());
		}
		if(value instanceof StackFilterTripel) {
			l.setText(((StackFilterTripel)value).module.getTabViewName() + ": " + ((StackFilterTripel)value).name);
		}
		return l;
	}

}
