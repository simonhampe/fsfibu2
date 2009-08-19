package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.model.Account;

/**
 * Renders account list cells by simply inserting the name of the account
 * @author Simon Hampe
 *
 */
public class AccountListRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -4953082950687427242L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel c = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		try {
			c.setText(((Account)value).getName());
			c.setToolTipText(((Account)value).getDescription());
		}
		catch(Exception e) {
			//Do nothing
		}
		return c;
	}
	
	
	
}
