/**
 *  Class: core.RunDatabaseQuery
 *  Description: This is to run a database query.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *	01. 11/06/2012: Code baseline. (V6.1)
 */
package net.sourceforge.datamig4zos.core;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.core.base.CoreJob;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DMGProcess;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.ui.editors.QueryEditorInput;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * @author SunPC
 * 
 */
public class RunDatabaseQuery extends CoreJob {
	
	private DB2Schema schema;
	
	private String sql_stmts = "";
	
	private QueryEditorInput editor_input;

	// constructor
	public RunDatabaseQuery(QueryEditorInput editorInput) {
		super("Running Database Query on " + editorInput.getSchema().getServer().getServerName()
				+ "." + editorInput.getSchema().getSchemaName(), "Database Query Process");
		
		editor_input = editorInput;
		schema = editor_input.getSchema();
			
		setLoggerClass(RunDatabaseQuery.class);
		
		// set server
		setServers(new MFServer[] { schema.getServer() });
		
		// set sql statements
		sql_stmts = editor_input.getSqlStmts();
		
		// build the DMGProcess array - v6.0.phase6
		DMGProcess[] procs = new DMGProcess[5];
		procs[0] = new DMGProcess(schema);
		procs[0].setProcessMethod("Init", "Initializing the process", 
				"initProcess");
		procs[1] = new DMGProcess(schema, procs[0].getProcessId());
		procs[1].setProcessMethod("Query", "Submitting the query job", 
				"runQuery",	sql_stmts);
		procs[2] = new DMGProcess(schema, procs[0].getProcessId());
		procs[2].setProcessMethod("Query", "Running the query", 
				"checkStepCntl", "QUERYDB", "S");
		procs[3] = new DMGProcess(schema, procs[0].getProcessId());
		procs[3].setProcessMethod("Finish", "Reading the query results", 
				"readQueryResults");
		procs[4] = new DMGProcess(schema, procs[0].getProcessId());
		procs[4].setProcessMethod("Finish", "Finishing the process", 
				"finishProcess");
		
		// set the procs
		setProcs(procs);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		// call super run
		IStatus status = super.run(monitor);
		
		// continue to get the table list only if status is OK
		if (status.equals(Status.OK_STATUS)) {
			try {
				// read the file into the table panel
				editor_input.setQueryResults(getProcs()[3].getMethodUse1());	
			} catch (Exception ex) {
				ex.printStackTrace();
				getLogger().error(getProcName() + " Failed");
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 8, ex.getMessage(), ex);
			}
		}

		return status;
	}

}
