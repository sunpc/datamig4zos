/**
 *  Class: wizards.NewRequestPage2
 *  Description: The New Request Wizard - Page 2
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/19/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/23/2011: Add a new constructor to handle the pass-in object. (V6.0 phase 5)
 *  03. 04/02/2011: Change the description. (V6.0 phase 6)
 *  04. 08/17/2013: Use separate text boxes for tables and data sets. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import net.sourceforge.datamig4zos.objects.DB2Table;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


public class NewRequestPage2 extends WizardPage {
	
	private String default_table_name = ""; 		// v6.0.phase5	// v6.3
	
	private Text txt_tables;		// v6.3
	private Text txt_datasets;		// v6.3
	
	private Composite container;
	
	private Group grp_tables;		// v6.3
	private Group grp_datasets;		// v6.3

	public NewRequestPage2() {
		super("Tables and Datasets");		// v6.3
		setTitle("Tables and Datasets");	// v6.3
		setDescription("Enter the table and dataset items with one item per line.\n" +
				"Tables not exist in Source Schema will be ignored.");	// v6.0.phase6	// v6.3
	}

	public NewRequestPage2(Object obj) {
		this();
		
		if (obj instanceof DB2Table) {
			default_table_name = ((DB2Table) obj).getTableName();
		}
	}

	@Override
	public void createControl(Composite parent) {
		// create container
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, true));
		
		// create groups - v6.3
		grp_tables = new Group(container, SWT.NULL);		// v6.3
		grp_tables.setText("Tables");						// v6.3
		grp_tables.setLayout(new GridLayout(1, true));		// v6.3
				
		grp_datasets = new Group(container, SWT.NULL);		// v6.3
		grp_datasets.setText("Datasets");					// v6.3
		grp_datasets.setLayout(new GridLayout(1, true));	// v6.3
		
		// define the text area - tables
		txt_tables = new Text(grp_tables, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);		// v6.3
		txt_tables.setText(default_table_name);				// v6.0.phase5
		txt_tables.setLayoutData(new GridData(450, 80));	// v6.3
		txt_tables.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (!txt_tables.getText().trim().isEmpty()
						|| !txt_datasets.getText().trim().isEmpty()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		
		// define the text area - data sets - v6.3
		txt_datasets = new Text(grp_datasets, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);	// v6.3
		txt_datasets.setLayoutData(new GridData(450, 80));
		txt_datasets.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (!txt_tables.getText().trim().isEmpty()
						|| !txt_datasets.getText().trim().isEmpty()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		
		
		// Required to avoid an error in the system
		setControl(container);
		
		// set page complete to false only if no pass-in text - v6.0.phase5
		if (default_table_name.trim().isEmpty()) {
			setPageComplete(false);
		}
			
	}

	public String getTableItems() {			// v6.3
		return txt_tables.getText();
	}

	public String getDatasetItems() {		// v6.3
		return txt_datasets.getText();
	}

}