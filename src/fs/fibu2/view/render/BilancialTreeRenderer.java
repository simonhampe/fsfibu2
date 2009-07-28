package fs.fibu2.view.render;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dom4j.Document;

import fs.fibu2.data.model.Category;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.fibu2.view.model.BilancialTreeModel.ExtendedCategory;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * For any {@link ExtendedCategory} as value, displays the tail of the associated category (in brackets, if it is the additional node). In addition, 
 * an icon indicates whether this category is masked and whether it has children. A grey text color indicates that the category is invisible.
 * @author Simon Hampe
 *
 */
public class BilancialTreeRenderer extends DefaultTreeCellRenderer implements ResourceDependent {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -5200098751530673930L;

	private final static String rpath = "graphics/BilancialTreeRenderer/";
	
	private final static Color color_invisible = new Color(176,176,176);

	private final BilancialTreeModel model;
	
	
	private final ImageIcon iconMask = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, rpath + "mask.png"));
	
	// CONSTRUCTOR ***********************
	// ***********************************
	
	/**
	 * Creates a renderer, which obtains visibility and mask status from the model
	 */
	public BilancialTreeRenderer(BilancialTreeModel model) {
		if(model == null) throw new NullPointerException("Cannot create renderer from null model");
		this.model = model;
	}
	
	// RENDERER **************************
	// ***********************************
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if(value instanceof ExtendedCategory) {
			ExtendedCategory ec = (ExtendedCategory)value;
			
			label.setText(ec.category().tail);
			if(ec.category() == Category.getRootCategory()) label.setText(Fsfibu2StringTableMgr.getString("fs.fibu2.view.BilancialTreeRenderer.root"));
			if(ec.isAdditional()) label.setText("(" + label.getText() + ")");
			
			if(!model.isInheritedVisible(ec.category(),ec.isAdditional())) label.setForeground(color_invisible);
			else label.setForeground(Color.BLACK);
			
			String mask = model.getMask(ec);
			if(mask == null) {
				label.setIcon(null);
			}
			else {
				label.setIcon(iconMask);
				label.setText(label.getText() + " = '" + mask + "'");
			}
			
		}
		
		return label;
	}

	// RESOURCEDEPENDENT ************************
	// ******************************************
	
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath(rpath + "mask.png");
		return tree;
	}

}
