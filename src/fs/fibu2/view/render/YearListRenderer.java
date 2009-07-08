package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.YearListModel;

/**
 * This class renders cells for a {@link YearListModel}, by diplaying Integers as themselves, null as a 'All years' string and everything else
 * using toString()
 * @author Simon Hampe
 *
 */
public class YearListRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -8576051758261006719L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if(value == null) l.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.view.YearListRenderer.allyears"));
		else {
			l.setText(value.toString());
		}
		return l;
	}

}
