package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.model.EntrySeparator;

/**
 * Displays {@link EntrySeparator}s simply by displaying their name
 * @author Simon Hampe
 *
 */
public class SeparatorRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated version uid
	 */
	private static final long serialVersionUID = 2258256072637641062L;

	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1,
			int arg2, boolean arg3, boolean arg4) {
		JLabel l = (JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
		if(arg1 instanceof EntrySeparator) l.setText(((EntrySeparator)arg1).getName());
		return l;
	}

}
