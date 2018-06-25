/**
 *  Class: actions.StartDatabaseQueryAction
 *  Description: Run a database query.
 * 
 * 	Author: Peng Cheng Sun
 *  
 * 	Modification History
 *  01. 11/06/2012: Code baseline. (V6.1)
 *  02. 11/07/2012: Allow unselected schema. (V6.1.1)
 *  03. 11/12/2012: remove formatSql(). (V6.1.2)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.core.RunDatabaseQuery;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.ui.editors.QueryEditor;
import net.sourceforge.datamig4zos.ui.editors.QueryEditorInput;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;


/**
 * @author SunPC
 * 
 */
public class RunDatabaseQueryAction extends Action implements IAction {

	private CCombo cmb_server;
	private CCombo cmb_schema;

	private Text txt_sql;

	private QueryEditor editor;
	private QueryEditorInput editor_input;
	
	public RunDatabaseQueryAction(CCombo cmbServer, CCombo cmbSchema,
			Text txtSql, QueryEditor editor) {
		super("Run Database Query");
		
		// Set the private variables
		this.cmb_server = cmbServer;
		this.cmb_schema = cmbSchema;
		this.txt_sql = txtSql;
		this.editor = editor;
		this.editor_input = (QueryEditorInput) editor.getEditorInput();
		
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_RUN_DATABASE_QUERY);
		// Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_RUN_DATABASE_QUERY);
		setImageDescriptor(Activator.getImageDescriptor("/images/run_action.gif"));
	}

	@Override
	public void run() {
		// confirmation dialog
		if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				"Confirm Submit",
				"Are you sure you want to submit this query?"))
			return;

		// verify the form before submission
		if (cmb_server.getSelectionIndex() == -1) {					// v6.1.1
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error", "Server must be selected!");			// v6.1.1
			return;
		}

		// verify the sql statements before submission
		if (txt_sql.getText().trim().isEmpty()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error", "SQL statements must be entered!");	// v6.1.1
			return;
		}

		// get the combo selections
		String server_name = cmb_server.getItem(cmb_server.getSelectionIndex());
		String schema_name = "";
		
		if (cmb_schema.getSelectionIndex() > -1)					// v6.1.1
			schema_name = cmb_schema.getItem(cmb_schema.getSelectionIndex());
				
		// set the components as not enabled
		cmb_server.setEnabled(false);
		cmb_schema.setEnabled(false);
		txt_sql.setEnabled(false);
		
		this.setEnabled(false);
		
		// populate the editor input
		try {
			MFServer server = new MFServer(server_name);
			
			DB2Schema schema;
			
			if (schema_name.isEmpty()) {							// v6.1.1
				schema = new DB2Schema(server.getLogonUser(), server.getLogonUser(),
						server.getLogonUser(), server);
			} else {
				schema = new DB2Schema(schema_name, server);
			}
			
			editor_input.setSchema(schema);
			editor_input.setSqlStmts(txt_sql.getText());	// v6.1.2
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Error", e.getMessage());
			return;
		}
		
		// run the query
		RunDatabaseQuery job = new RunDatabaseQuery(editor_input);
		
		// add job change listener
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(final IJobChangeEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// set status
						int event_sev = event.getResult().getSeverity();

						if (event_sev == IStatus.OK
								|| event_sev == IStatus.INFO
								|| event_sev == IStatus.WARNING) {
							editor.setPageResultsActive();
						} else if (event_sev == IStatus.CANCEL) {
							editor_input.setQueryResults("");
						} else {
							editor_input.setQueryResults("Query failed.");
							editor.setPageResultsActive();
						}

						// set the components as enabled
						cmb_server.setEnabled(true);
						cmb_schema.setEnabled(true);
						txt_sql.setEnabled(true);

						RunDatabaseQueryAction.this.setEnabled(true);
					}
				});
			}
		});

		// schedule the job
		job.setUser(true);
		job.schedule();
	}
}
