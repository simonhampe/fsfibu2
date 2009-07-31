package fs.fibu2.view.render;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.view.model.BilancialTableModel;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;

/**
 * This class renders money cells for a {@link BilancialTableModel}. It needs the corresponding {@link BilancialTree} to obtain 
 * visibility information.
 * @author Simon Hampe
 *
 */
public class BilancialTableRenderer extends DefaultTableCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 5644224913751597342L;
	private BilancialTree tree;
	private MoneyCellRenderer moneyRenderer = new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency);
	
	public BilancialTableRenderer(BilancialTree t) {
		if(t == null) throw new NullPointerException("Cannot create renderer from null tree");
		tree = t;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof Float) {
			JLabel label = (JLabel)moneyRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			ExtendedCategory ec = (ExtendedCategory)tree.getPathForRow(row).getLastPathComponent();
			if(!tree.getModel().isInheritedVisible(ec.category(), ec.isAdditional())) {
				label.setForeground(Color.GRAY);
			}
			return label;
		}
		else return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);		
	}

}
