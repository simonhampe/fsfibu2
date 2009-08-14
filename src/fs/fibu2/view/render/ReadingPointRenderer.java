package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.ReadingPoint;

/**
 * Renders a reading point by displaying its name and its date
 * @author Simon Hampe
 *
 */
public class ReadingPointRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 6261877559870718669L;

	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1,
			int arg2, boolean arg3, boolean arg4) {
		JLabel label = (JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
		if(arg1 instanceof ReadingPoint) {
			label.setText(((ReadingPoint)arg1).getName() + ", " + Fsfibu2DateFormats.getEntryDateFormat().format(((ReadingPoint)arg1).getReadingDay().getTime()));
		}
		return label;
	}

}
