package fs.fibu2.print;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Arrays;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.xml.PolyglotStringTable;

/**
 * This class implements a dialog for printing the contents of a {@link JournalTableModel}.
 * @author Simon Hampe
 *
 */
public class JournalPrintDialog extends FrameworkDialog {
	// DATA **********************************
	// ***************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 6391388046628022075L;

	private JournalTableModel model;
	
	private PrintRequestAttributeSet print = new HashPrintRequestAttributeSet();
	private PrinterJob job = PrinterJob.getPrinterJob();
	
	private final static String sgroup = "fs.fibu2.print.JournalPrintDialog";
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	// COMPONENTS ****************************
	// ***************************************
	
	private JTextField titleField = new JTextField();
	private JTextArea subtitleField = new JTextArea();
	private JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(11,1,30,1));
	
	private JCheckBox remarkCheck = new JCheckBox(Fsfibu2StringTableMgr.getString(sgroup + ".printremarks"),true);	
	
	private JLabel formatLabel = new JLabel();
	private JLabel orientationLabel = new JLabel();
	private JLabel printerLabel = new JLabel();
	
	private JButton configButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".config"));
	
	private JLabel pagesNeededLabel = new JLabel();
	
	private JButton okButton = new JButton(Fsfibu2StringTableMgr.getString(sgroup + ".print"));
	private JButton cancelButton = new JButton(Fsfibu2StringTableMgr.getString("fs.fibu2.global.cancel"));
	
	// LISTENERS *****************************
	// ***************************************
	
	private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			fillPrinterData();
		}
	};
	
	private DocumentListener documentListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) {
			fillPrinterData();
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			fillPrinterData();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			fillPrinterData();
		}
	};
	
	private ActionListener configListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			job.printDialog(print);
			fillPrinterData();
		}
	};
	
	private ActionListener printListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				job.print(print);
				dispose();
			} catch (PrinterException e1) {
				logger.error(Fsfibu2StringTableMgr.getString(sgroup + ".error", e1.getMessage()));
			}
		}
	};
	
	private ActionListener cancelListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};
	
	// CONSTRUCTOR ***************************
	// ***************************************
	
	/**
	 * Constructs a new dialog
	 * @param model The model which should be printed. Must not be null
	 */
	public JournalPrintDialog(JournalTableModel model) {
		super(Fsfibu2DefaultReference.getDefaultReference(),Fsfibu2StringTableMgr.getLoader(),PolyglotStringTable.getGlobalLanguageID());
		if(model == null) throw new NullPointerException("Cannot create Print dialog from null model");
		this.model = model;

		print = new HashPrintRequestAttributeSet();
		print.add(OrientationRequested.LANDSCAPE);
		print.add(MediaSizeName.ISO_A4);
		
		//Init GUI
		setTitle(Fsfibu2StringTableMgr.getString(sgroup + ".dialogtitle"));
		
		subtitleField.setLineWrap(true);
		
		//Additional components
		JLabel labelTitle = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".title"));
		JLabel labelSubTitle = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".subtitle"));
		JScrollPane subtitlePane = new JScrollPane(subtitleField);
		JLabel labelSize = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".size"));
		JPanel printPanel = new JPanel();
			printPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".printinfo")));
		JLabel labelPrinter = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".printer") + " ");
		JLabel labelFormat = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".format") + " ");
		JLabel labelOrientation = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".orientation") + " ");
		JPanel previewPanel = new JPanel();
			previewPanel.setBorder(BorderFactory.createTitledBorder(Fsfibu2StringTableMgr.getString(sgroup + ".preview")));
		JLabel neededLabel = new JLabel(Fsfibu2StringTableMgr.getString(sgroup + ".needed")+ " ");
		
		//Layout
		
		//Layout panels
		
		Box printerBox = new Box(BoxLayout.Y_AXIS);
		printerBox.setAlignmentX(LEFT_ALIGNMENT);
			Box hBox1 = new Box(BoxLayout.X_AXIS); hBox1.add(labelPrinter); hBox1.add(printerLabel);
			Box hBox2 = new Box(BoxLayout.X_AXIS); hBox2.add(labelFormat); hBox2.add(formatLabel);
			Box hBox3 = new Box(BoxLayout.X_AXIS); hBox3.add(labelOrientation); hBox3.add(orientationLabel);
			hBox1.setAlignmentX(LEFT_ALIGNMENT);
			hBox2.setAlignmentX(LEFT_ALIGNMENT);
			hBox3.setAlignmentX(LEFT_ALIGNMENT);
		printerBox.add(hBox1); printerBox.add(hBox2); printerBox.add(hBox3); printerBox.add(Box.createVerticalStrut(5));
		printerBox.add(configButton);
		printPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		printPanel.add(printerBox);
		
		Box previewBox = new Box(BoxLayout.Y_AXIS);
		previewBox.setAlignmentX(LEFT_ALIGNMENT);
			Box iBox1 = new Box(BoxLayout.X_AXIS); iBox1.add(neededLabel); iBox1.add(pagesNeededLabel);
			iBox1.setAlignmentX(LEFT_ALIGNMENT);
		previewBox.add(iBox1);
		previewPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		previewPanel.add(previewBox);
		
		//Layout dialog
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		GridBagConstraints gcTitleLabel = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcTitleField = GUIToolbox.buildConstraints(1, 0, 2, 1);
		GridBagConstraints gcSubTitleLabel = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gcSubTitleArea = GUIToolbox.buildConstraints(1, 1, 2, 2); gcSubTitleArea.ipady = 50; 
		GridBagConstraints gcSizeLabel = GUIToolbox.buildConstraints(0, 3, 1, 1);
		GridBagConstraints gcSizeSpinner = GUIToolbox.buildConstraints(1, 3, 1, 1);
		GridBagConstraints gcRemarkCheck = GUIToolbox.buildConstraints(0, 4, 3, 1);
		GridBagConstraints gcPrinterPanel = GUIToolbox.buildConstraints(0, 5, 3, 1);
		GridBagConstraints gcPreviewPanel = GUIToolbox.buildConstraints(0, 6, 3, 1);
		GridBagConstraints gcPrintButton = GUIToolbox.buildConstraints(1, 7, 1, 1);
		GridBagConstraints gcCancelButton = GUIToolbox.buildConstraints(2, 7, 1, 1);
		
		for(GridBagConstraints gc : Arrays.asList(gcTitleLabel, gcTitleField, gcSubTitleArea, gcSubTitleLabel,gcSizeLabel, gcSizeSpinner, gcRemarkCheck, 
				gcPrinterPanel,gcPreviewPanel, gcPrintButton, gcCancelButton)) {
			gc.insets = new Insets(5,5,5,5);
		}
		
		gbl.setConstraints(labelTitle, gcTitleLabel);
		gbl.setConstraints(titleField, gcTitleField);
		gbl.setConstraints(labelSubTitle, gcSubTitleLabel);
		gbl.setConstraints(subtitlePane, gcSubTitleArea);
		gbl.setConstraints(labelSize, gcSizeLabel);
		gbl.setConstraints(heightSpinner, gcSizeSpinner);
		gbl.setConstraints(remarkCheck, gcRemarkCheck);
		gbl.setConstraints(printPanel, gcPrinterPanel);
		gbl.setConstraints(previewPanel, gcPreviewPanel);
		gbl.setConstraints(okButton, gcPrintButton);
		gbl.setConstraints(cancelButton, gcCancelButton);
		
		add(labelTitle); add(titleField); add(labelSubTitle); add(subtitlePane);add(labelSize); add(heightSpinner); add(remarkCheck);  
		add(printPanel); add(previewPanel); add(okButton); add(cancelButton);
		
		//Insert data
		fillPrinterData();
		
		pack();
		setResizable(false);
		
		//Add listeners
		heightSpinner.addChangeListener(spinnerListener);
		configButton.addActionListener(configListener);
		okButton.addActionListener(printListener);
		cancelButton.addActionListener(cancelListener);
		remarkCheck.addChangeListener(spinnerListener);
		titleField.getDocument().addDocumentListener(documentListener);
		subtitleField.getDocument().addDocumentListener(documentListener);
	}
	
	// CONTROL METHODS *********************
	// *************************************
	
	/**
	 * Obtains the attributes of the printer job and copies them to the appropriate labels. Also calculated the pages needed for printing
	 */
	protected void fillPrinterData() {
		printerLabel.setText(job.getPrintService().getName());
		Attribute format = print.get(Media.class);
		Attribute orientation = print.get(OrientationRequested.class);
		if(format != null) formatLabel.setText(format.toString());
		else formatLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".unknown"));
		if(orientation != null) orientationLabel.setText(orientation.toString());
		else orientationLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".unknown"));
		
		try {
			JournalPageable pageable = new JournalPageable(
					titleField.getText().equals("") ? null : titleField.getText(), subtitleField.getText().equals("")? null : subtitleField.getText(),
					model,job.getPageFormat(print),(Integer)heightSpinner.getValue(),
					remarkCheck.isSelected());
			job.setPageable(pageable);
			pagesNeededLabel.setText(Integer.toString(pageable.getNumberOfPages()));
			okButton.setEnabled(true);
		}
		catch(Exception e) {
			pagesNeededLabel.setText(sgroup + ".invalid");
			okButton.setEnabled(false);
		}
		
		repaint();
	}
}
