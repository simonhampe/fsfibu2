package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.format.JournalExport;

/**
 * Renders a {@link JournalExport} by displaying its name
 * @author Simon Hampe
 *
 */
public class JournalExportRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 6074179498216517662L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if(value instanceof JournalExport) {
			label.setText(((JournalExport)value).getName());
		}
		return label;
	}

}
