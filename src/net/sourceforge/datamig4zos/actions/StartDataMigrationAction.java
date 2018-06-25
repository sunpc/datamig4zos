/**
 *  Class: actions.StartDataMigrationAction
 *  Description: Start a new Request Editor.
 * 
 * 	Author: Peng Cheng Sun
 *  
 * 	Modification History
 *  01. 03/22/2011: Code baseline. (V6.0 phase 4)
 *  Code mainly copied from the original RequestEditor.class.
 *  02. 03/29/2011: Add Editor components. (V6.0 phase 6)
 *  03. 04/01/2011: Fix a defect in the section of getting combo selections - wrong combo names. (V6.0 phase 6)
 *  04. 04/02/2011: Add a logic to set the request status to CANCELLED or FAILED even the job returns OK. (V6.0 phase 6)
 *  05. 04/03/2011: Use syncExec instead of asyncExec in the job listener. (V6.0 beta 1)
 *  06. 04/15/2011: Retain the process ids on reruns. (V6.0 beta 1) 
 *  07. 03/08/2012: Add lnk_replace_tables, lnk_build_unload and lnk_replace_datasets. (V6.0.0)
 *  08. 03/09/2012: Change CANCELLED to CANCELED. (V6.0.0)
 *  09. 10/30/2012: Enhance the form validation logic. (V6.0.1)
 */
package net.sourceforge.datamig4zos.actions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.core.ProcessDataMigration;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.ui.editors.RequestEditorInput;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;


/**
 * @author SunPC
 * 
 */
public class StartDataMigrationAction extends Action implements IAction {

	private Label lbl_status;
	private Label lbl_upt_time;

	private CCombo cmb_source_server;
	private CCombo cmb_source_schema;
	private CCombo cmb_target_server;
	private CCombo cmb_target_schema;

	private Table tbl_tables;
	private Table tbl_datasets;

	private Text txt_desc;

	private Hyperlink lnk_add_table;
	private Hyperlink lnk_replace_tables;		// v6.0.0
	private Hyperlink lnk_build_unload;			// v6.0.0
	private Hyperlink lnk_add_dataset;
	private Hyperlink lnk_replace_datasets;		// v6.0.0
	
	private RequestEditorInput editor_input;

	public StartDataMigrationAction(Label lblStatus, Label lblUptTime,
			CCombo cmbSourceServer, CCombo cmbSourceSchema,
			CCombo cmbTargetServer, CCombo cmbTargetSchema, Table tblTables,
			Table tblDatasets, Text txtDesc, Hyperlink lnkAddTable,
			Hyperlink lnkReplaceTables, Hyperlink lnkBuildUnload,
			Hyperlink lnkAddDataset, Hyperlink lnkReplaceDatasets, 
			RequestEditorInput editorInput) {
		super("Start Data Migration");

		// Set the private variables
		lbl_status = lblStatus;
		lbl_upt_time = lblUptTime;
		cmb_source_server = cmbSourceServer;
		cmb_source_schema = cmbSourceSchema;
		cmb_target_server = cmbTargetServer;
		cmb_target_schema = cmbTargetSchema;
		tbl_tables = tblTables;
		tbl_datasets = tblDatasets;
		txt_desc = txtDesc;
		lnk_add_table = lnkAddTable;
		lnk_replace_tables = lnkReplaceTables;			// v6.0.0
		lnk_build_unload = lnkBuildUnload;				// v6.0.0
		lnk_add_dataset = lnkAddDataset;
		lnk_replace_datasets = lnkReplaceDatasets;		// v6.0.0
		editor_input = editorInput;

		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_START_DATA_MIGRATION);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(ICommandIds.CMD_START_DATA_MIGRATION);
		setImageDescriptor(Activator.getImageDescriptor("/images/run_action.gif"));
	}

	@Override
	public void run() {
		// for test purposes
		// System.out.println("Button clicked");
		
		// confirmation dialog
		if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				"Confirm Submit",
				"Are you sure you want to submit this request?"))
			return;

		// verify the form before submission - v6.0.1
		if (cmb_source_server.getSelectionIndex() == -1
				|| cmb_source_schema.getSelectionIndex() == -1
				|| cmb_target_server.getSelectionIndex() == -1
				|| cmb_target_schema.getSelectionIndex() == -1) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Error", "All servers and schemas must be selected!");
			return;
		}
		
		// get the combo selections - v6.0.phase6
		String source_server_name = 
			cmb_source_server.getItem(cmb_source_server.getSelectionIndex());
		String source_schema_name = 
			cmb_source_schema.getItem(cmb_source_schema.getSelectionIndex());
		String target_server_name = 
			cmb_target_server.getItem(cmb_target_server.getSelectionIndex());
		String target_schema_name = 
			cmb_target_schema.getItem(cmb_target_schema.getSelectionIndex());
		
		// verify source and target details - v6.0.1
		if (source_server_name.equals(target_server_name)
				& source_schema_name.equals(target_schema_name)) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Error", "Same server and schema selected in both source and target is not allowed!");
			return;
		}
		

		// verify table list - v6.0.1
		DB2Schema tmp_source_schema;
		DB2Schema tmp_target_schema;
		
		try {
			tmp_source_schema = new DB2Schema(source_schema_name, new MFServer(source_server_name));
			tmp_source_schema.setTables();
			
			tmp_target_schema = new DB2Schema(target_schema_name, new MFServer(target_server_name));
			tmp_target_schema.setTables();			
			
			for (int i = 0; i < tbl_tables.getItemCount(); i++) {
				String tmp_source_table_name = tbl_tables.getItem(i).getText(2).toUpperCase().trim();
				String tmp_target_table_name = tbl_tables.getItem(i).getText(3).toUpperCase().trim();
				
				if (tmp_source_table_name.isEmpty()
						|| tmp_target_table_name.isEmpty()) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), 
							"Error", "All active source and target table names must be entered!");
					return;
				}
				
				if (tmp_source_schema.getTable(tmp_source_table_name).getDbName().equals("")) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), 
							"Error", "Table " + tmp_source_table_name + " was not found on the Source Server!");
					return;
				}
				
				if (tmp_target_schema.getTable(tmp_target_table_name).getDbName().equals("")) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), 
							"Error", "Table " + tmp_target_table_name + " was not found on the Target Server!");
					return;
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Error", e.getMessage());
			return;
		}
		
		// verify data set list - v6.0.1
		for (int i = 0; i < tbl_datasets.getItemCount(); i++) {
			if (tbl_datasets.getItem(i).getText(2).trim().isEmpty()
					|| tbl_datasets.getItem(i).getText(3).trim().isEmpty()) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), 
						"Error", "All active source and target dataset names must be entered!");
				return;
			}
		}
		
		// set the status and last upt time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		lbl_status.setText("PROCESSING");
		lbl_upt_time.setText(formatter.format(cal.getTime()));
		
		// set the components as not enabled
		cmb_source_server.setEnabled(false);
		cmb_source_schema.setEnabled(false);
		cmb_target_server.setEnabled(false);
		cmb_target_schema.setEnabled(false);
		
		txt_desc.setEnabled(false);
		
		lnk_add_table.setEnabled(false);
		lnk_replace_tables.setEnabled(false);			// v6.0.0
		lnk_build_unload.setEnabled(false);				// v6.0.0
		lnk_add_dataset.setEnabled(false);
		lnk_replace_datasets.setEnabled(false);			// v6.0.0
		
		this.setEnabled(false);

		// dialog saying request submitted
		// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
		//		"Submitted", "Request submitted successfully!");
		
		ProcessDataMigration job = new ProcessDataMigration(txt_desc.getText().trim(), 
				source_server_name, source_schema_name, target_server_name, target_schema_name, 
				tbl_tables, tbl_datasets, editor_input);

		// add job change listener - v6.0.phase6
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(final IJobChangeEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// set status
						int event_sev = event.getResult().getSeverity();
						
						if (event_sev == IStatus.OK
								|| event_sev == IStatus.INFO
								|| event_sev == IStatus.WARNING) {
							// check if there is any canceled item - v6.0.phase6
							for (int i = 0; i < tbl_tables.getItemCount(); i++) {
								if (tbl_tables.getItem(i).getText(1).startsWith("Canceled")) {
									lbl_status.setText("CANCELED");		// v6.0.0
									break;
								}
							}
							
							for (int i = 0; i < tbl_datasets.getItemCount(); i++) {
								if (tbl_datasets.getItem(i).getText(1).startsWith("Canceled")) {
									lbl_status.setText("CANCELED");		// v6.0.0
									break;
								}
							}
							
							// check if there is any failed item - v6.0.phase6
							for (int i = 0; i < tbl_tables.getItemCount(); i++) {
								if (tbl_tables.getItem(i).getText(1).startsWith("Failed")) {
									lbl_status.setText("FAILED");
									break;
								}
							}
							
							for (int i = 0; i < tbl_datasets.getItemCount(); i++) {
								if (tbl_datasets.getItem(i).getText(1).startsWith("Failed")) {
									lbl_status.setText("FAILED");
									break;
								}
							}
							
							// else set the status to COMPLETED - v6.0.phase6
							if (lbl_status.getText().equals("PROCESSING")) {
								lbl_status.setText("COMPLETED");
							}
						} else if (event_sev == IStatus.CANCEL) {
							lbl_status.setText("CANCELED");		// v6.0.0
						} else {
							lbl_status.setText("FAILED");
						}
						
						// set last upt time
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");				
						lbl_upt_time.setText(formatter.format(cal.getTime()));
						
						// set the components as not enabled
						cmb_source_server.setEnabled(true);
						cmb_source_schema.setEnabled(true);
						cmb_target_server.setEnabled(true);
						cmb_target_schema.setEnabled(true);
						
						txt_desc.setEnabled(true);
						
						lnk_add_table.setEnabled(true);
						lnk_replace_tables.setEnabled(true);			// v6.0.0
						lnk_build_unload.setEnabled(true);				// v6.0.0
						lnk_add_dataset.setEnabled(true);
						lnk_replace_datasets.setEnabled(true);			// v6.0.0
						
						StartDataMigrationAction.this.setEnabled(true);
					}
				});
			}
		});

		// schedule the job
		job.setUser(true);
		job.schedule();
	}
}
