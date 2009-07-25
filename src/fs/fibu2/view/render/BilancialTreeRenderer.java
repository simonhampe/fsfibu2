package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import fs.fibu2.data.model.Category;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;

/**
 * For any {@link ExtendedCategory} as value, displays the tail of the associated category (in brackets, if it is the additional node)
 * @author Simon Hampe
 *
 */
public class BilancialTreeRenderer extends DefaultTreeCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -5200098751530673930L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if(value instanceof ExtendedCategory) {
			label.setText(((ExtendedCategory)value).category().tail);
			if(((ExtendedCategory)value).isAdditional()) label.setText("(" + label.getText() + ")");
			if(((ExtendedCategory)value).category() == Category.getRootCategory()) label.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.view.BilancialTreeRenderer.root"));
		}
		
		return label;
	}

}
