package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.model.Account;
import fs.fibu2.view.model.AccountInformation;

/**
 * Displays an {@link AccountInformation} object by displaying its name and the names of the accounts using it
 * @author Simon Hampe
 *
 */
public class AccountInformationListRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -944173920896324883L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		AccountInformation info = (AccountInformation) value;
		StringBuilder b = new StringBuilder();
		b.append(info.getName());
		b.append(" (");
		for(Account a : info.getAccounts()) {
			b.append(a.getName());
			b.append(", ");
		}
		b.delete(b.length()-2, b.length());
		b.append(")");
		l.setText(b.toString());
		return l;
	}

	
	
}
