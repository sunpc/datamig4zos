/**
 *  Class: wizards.NewRequestPage4
 *  Description: The New Request Wizard - Page 4
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/19/2011: Code baseline. (V6.0 phase 2)
 *  02. 08/17/2013: Rename from NewRequestPage3 to NewRequestPage4. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NewRequestPage4 extends WizardPage {
	
	private Button chk_same_table_name;
	private Button chk_rename_dataset_name;
	
	private Composite container;

	public NewRequestPage4() {
		super("Settings");
		setTitle("Settings");
		setDescription("Define the data migration settings.");
	}

	@Override
	public void createControl(Composite parent) {
		// create container
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;

		// chk_same_table_name
		chk_same_table_name = new Button(container, SWT.CHECK);
		chk_same_table_name.setSelection(true);
		Label lab_same_table_name = new Label(container, SWT.NONE);
		lab_same_table_name.setText("Use the same table names on the Target Server");

		// chk_rename_dataset_name
		chk_rename_dataset_name = new Button(container, SWT.CHECK);
		chk_rename_dataset_name.setSelection(true);
		Label lab_rename_dataset_name = new Label(container, SWT.NONE);
		lab_rename_dataset_name.setText("Replace the prefix of the dataset names on the Target Server");
		
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(true);
	}

	// return the chk_same_table_name
	public boolean getChkSameTableName() {
		return chk_same_table_name.getSelection();
	}

	// return the chk_rename_dataset_name
	public boolean getChkRenameDatasetName() {
		return chk_rename_dataset_name.getSelection();
	}
	

}