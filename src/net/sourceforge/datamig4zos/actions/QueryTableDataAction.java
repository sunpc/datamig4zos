/**
 *  Class: actions.QueryTableDataAction
 *  Description: Start a new Query Editor and query a table's data.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 11/06/2012: Code baseline. (V6.1)
 */
package net.sourceforge.datamig4zos.actions;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.core.RunDatabaseQuery;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.runtime.ICommandIds;
import net.sourceforge.datamig4zos.runtime.IEditorIds;
import net.sourceforge.datamig4zos.ui.editors.QueryEditorInput;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

public class QueryTableDataAction extends Action implements IAction {
	
	public static final int SAMPLE_DATA_MODE = 1;
	public static final int FULL_DATA_MODE = 2;
	public static final int RECORD_COUNT_MODE = 3;
	
	private final IWorkbenchWindow window;
	private final ISelectionProvider provider;
	
	private DB2Table db2_table;
	
	private QueryEditorInput editor_input;
	
	private int rows_limit = -1;
	private String sql_select = "*";
	
	public QueryTableDataAction(IWorkbenchWindow window, ISelectionProvider provider, int mode) {
		this.window = window;
		this.provider = provider;

		switch (mode) {
		case SAMPLE_DATA_MODE:
			setText("Query Sample Data...");
			rows_limit = 100;
			break;
		case FULL_DATA_MODE:
			setText("Query Full Data...");
			break;
		case RECORD_COUNT_MODE:
			setText("Query Record Count...");
			sql_select = "COUNT(*) AS COUNT";
			break;
		}
		
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_QUERY_TABLE_DATA);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_QUERY_TABLE_DATA);
		setImageDescriptor(Activator.getImageDescriptor("/images/new_query.gif"));
	}
	
	@Override
	public void run() {
		// confirmation dialog
		if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
				"Confirm Submit",
				"Are you sure you want to submit this query?"))
			return;

		// define the editor input
		editor_input = new QueryEditorInput();
		
		// populate the editor input
		try {
			String sql = "SELECT " + sql_select + " FROM " 
					+ db2_table.getSchema().getSchemaName() + "." 
					+ db2_table.getTableName();
			
			if (rows_limit > 0) {
				sql += " FETCH FIRST " + Integer.toString(rows_limit) + " ROWS ONLY;";
			} else {
				sql += ";";
			}

			editor_input.setSchema(db2_table.getSchema());
			editor_input.setSqlStmts(sql);			
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
							// open the editor
							if(window != null) {
								try {
									window.getActivePage().openEditor(editor_input, IEditorIds.EDITOR_QUERY);
								} catch (Exception e) {
									MessageDialog.openError(window.getShell(), "Error", 
											"Error opening editor: " + e.getMessage());
								}
							}
						}
					}
				});
			}
		});

		// schedule the job
		job.setUser(true);
		job.schedule();
	}
	
	@Override
	public boolean isEnabled() {
		if (provider == null)
			return true;
		
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				if (sSelection.getFirstElement() instanceof DB2Table) {
					db2_table = (DB2Table) sSelection.getFirstElement();
					return true;
				}
			}
		}
		
		return false;
	}

}