package fs.fibu2.view.render;

import javax.swing.JTree;

import fs.fibu2.view.model.BilancialTreeModel;

/**
 * This is a JTree using a {@link BilancialTreeModel} and a {@link BilancialTreeRenderer}.
 * @author Simon Hampe
 *
 */
public class BilancialTree extends JTree {

	/**
	 * compiler-generated serial version uid 
	 */
	private static final long serialVersionUID = 2625832048887411460L;

	private BilancialTreeModel model;
	
	public BilancialTree(BilancialTreeModel model) {
		super();
		if(model == null) throw new NullPointerException("Cannot create tree from null model");
		else this.model = model;
		setModel(model);
		setCellRenderer(new BilancialTreeRenderer());
	}
	
	/**
	 * @return The {@link BilancialTreeModel} associated to this tree
	 */
	public BilancialTreeModel getModel() {
		return model;
	}
	
}
