package fs.fibu2.print;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import fs.fibu2.module.BilancialPane;
import fs.fibu2.view.model.BilancialTreeModel;

/**
 * This class contains the configuration for printing a the data represented by a {@link BilancialPane}. Apart from a {@link PrinterJob}, it
 * also contains further information concerning line height and a title and the printing policy
 * @author Simon Hampe
 *
 */
public class BilancialPrintConfiguration {

	private int lineHeight; //The height of one line in points
	private String title;   //The title of the first page
	
	private BilancialTreeModel model; //The model from which to obtain the data
	
	private PrinterJob job; //The printer job from which to create the final pageable
	
	private PageFormat format; //The page format
	
	private PrintPolicy policy; //The print policy (a unit is always printed on one page, everything is on one page, a new page is
								//created only if the lines reach the end of the page)
	
	public enum PrintPolicy {PRESERVE_UNIT, ONE_PAGE, NO_CONSTRAINT}; 

	/**
	 * Creates a configuration
	 * @param lineHeight The height of one printed line in points
	 * @param title The title of the page
	 * @param model The tree model from which to retrieve the visibility and mask data
	 * @param job The print job which provides page format and other information
	 * @param format The page format which should be used
	 * @param policy The page break policy: <br>
	 * 	ONE_PAGE: Everything should be printed on one page. This will ignore the given line height and use the maximal possible line height<br>
	 * PRESERVE_UNIT: This takes care that every category unit (i.e. the bilancial of a node with subnodes) is printed on one and the same page, i.e.
	 * this prints with the given line height and induces a page break if necessary<br>
	 * NO_CONSTRAINT: This just prints with the given line height and induces a page break whenever necessary.
	 * 
	 */
	public BilancialPrintConfiguration(int lineHeight, String title,
			BilancialTreeModel model, PrinterJob job, PageFormat format, PrintPolicy policy) {
		this.lineHeight = lineHeight;
		this.title = title;
		this.model = model;
		this.job = job;
		this.format = format;
		this.policy = policy;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public String getTitle() {
		return title;
	}

	public BilancialTreeModel getModel() {
		return model;
	}

	public PrinterJob getJob() {
		return job;
	}
	
	public PageFormat getFormat() {
		return format;
	}

	public PrintPolicy getPolicy() {
		return policy;
	}
	
	
	
}
