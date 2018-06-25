/**
 *  Class: core.ProcessDataMigration
 *  Description: This is to process a data migration job.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/12/2011: Code baseline as a demo look and feel. (V6.0 phase 2)
 *  02. 04/01/2011: Fill up with functional code. (V6.0 phase 6)
 *  03. 04/03/2011: Extends CoreJob instead of Job. (V6.0 beta 1)
 *  04. 04/15/2011: Retain the process ids on reruns. (V6.0 beta 1)
 *  05. 02/29/2012: Fix a bug that the data set process was using tbl_tables. (V6.0.0)
 *  06. 11/12/2012: Remove FTP step in table migration process. (V6.1.2)
 *  07. 11/12/2012: Bug fixes on ref_proc_id lookup. (V6.1.2)
 *  08. 08/08/2013: Use new methods for table migration. (V6.3)
 */
package net.sourceforge.datamig4zos.core;

import net.sourceforge.datamig4zos.core.base.CoreJob;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DMGProcess;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.ui.editors.RequestEditorInput;
import net.sourceforge.datamig4zos.util.ProcessIdentifier;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;


/**
 * @author SunPC
 * 
 */
public class ProcessDataMigration extends CoreJob {

	private String source_server_name = "";
	private String source_schema_name = "";
	private String target_server_name = "";
	private String target_schema_name = "";

	private Table tbl_tables;
	private Table tbl_datasets;
	
	private RequestEditorInput editor_input;	// v6.0 beta 1

	private DMGProcess[] procs;
	
	private int table_count = 0;
	private int dataset_count = 0;

	private String item_status = "";		// v6.0 beta 1
	private String source_table_name = "";
	private String target_table_name = "";
	private String source_dataset_name = "";
	private String target_dataset_name = "";
	private String unload_sql = "";

	private String[][] ref_proc_ids;	// v6.0 beta 1
	private String[][] new_ref_proc_ids;	// v6.0 beta 1

	private int i = 0;

	/**
	 * @param jobName
	 * @param sourceServerName
	 * @param sourceSchemaName
	 * @param targetServerName
	 * @param targetSchemaName
	 * @param tblTables
	 * @param tblDatasets
	 */
	public ProcessDataMigration(String jobName, String sourceServerName,
			String sourceSchemaName, String targetServerName,
			String targetSchemaName, Table tblTables, Table tblDatasets,
			RequestEditorInput editorInput) {
		super(jobName, "Data Migration Process");
		
		source_server_name = sourceServerName;
		source_schema_name = sourceSchemaName;
		target_server_name = targetServerName;
		target_schema_name = targetSchemaName;

		tbl_tables = tblTables;
		tbl_datasets = tblDatasets;
		
		editor_input = editorInput;
		ref_proc_ids = editorInput.getRefProcIds();

		setLoggerClass(ProcessDataMigration.class);
		
		buildProcs();
	}

	private void buildProcs() {
		try {
			// create the servers and schemas
			MFServer source_server = new MFServer(source_server_name);
			DB2Schema source_schema = new DB2Schema(source_schema_name, source_server);
			source_schema.setTables();

			MFServer target_server = new MFServer(target_server_name);
			DB2Schema target_schema = new DB2Schema(target_schema_name, target_server);
			target_schema.setTables();

			// set servers
			setServers(new MFServer[] { source_server, target_server });

			// set restart rules
			setRestartRules(new String[] { "Unload", "Ftp", "Preload", "Load" });
			
			// initialize the proc array index
			int index = 0;
			int index_id = 0;		// v6.0 beta 1
	
			// initialize the DMGProcess array
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					table_count = tbl_tables.getItemCount();
					dataset_count = tbl_datasets.getItemCount();
				}
			});
	
			// calculate total process count
			int process_count = table_count * 7 + dataset_count * 4;	// v6.1.2
			int process_id_count = table_count + dataset_count;		// v6.0 beta 1
			
			procs = new DMGProcess[process_count];
			new_ref_proc_ids = new String[process_id_count][2];		// v6.0 beta 1
	
			// build the DMGProcess array with table items
			for (i = 0; i < table_count; i++) {
				// get the table details
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						item_status = tbl_tables.getItem(i).getText(1).trim();
						source_table_name = tbl_tables.getItem(i).getText(2).trim().toUpperCase();
						target_table_name = tbl_tables.getItem(i).getText(3).trim().toUpperCase();
						unload_sql = tbl_tables.getItem(i).getText(4).trim();
					}
				});
				
				// look up the proc id - v6.0 beta 1
				String process_id = "";
				String process_ref = "T." + i;
				
				for (int r = 0; r < ref_proc_ids.length; r++) {
					if (!item_status.equals("New")
							&& ref_proc_ids[r] != null			// v6.1.2
							&& ref_proc_ids[r].length == 2		// v6.1.2
							&& ref_proc_ids[r][0] != null		// v6.1.2
							&& ref_proc_ids[r][1] != null		// v6.1.2
							&& ref_proc_ids[r][1].equals(process_ref)) {
						process_id = ref_proc_ids[r][0];
					}
				}
				
				if (process_id.isEmpty()) {
					process_id = ProcessIdentifier.getNewIdentifier();
				}
	
				// init
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Init", "Initializing the process",
						"initProcess", target_table_name);		// v6.3
				
				// retain the proc id - v6.0 beta 1
				new_ref_proc_ids[index_id++] = new String[] { process_id, process_ref }; 
	
				// unload
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Unload",
						"Submitting the unload job", "unloadSource",
						source_table_name, unload_sql);
	
				// check unload
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Unload", "Unloading "
						+ source_table_name + " table", "checkStepCntl", "UNLDSRC",
						"S", target_table_name);	// v6.3
	
				/* REMOVED in v6.1.2
				// ftp
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Ftp", "Submitting the ftp job",
						"ftpTable", source_table_name, target_table_name);
	
				// check ftp
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Ftp", "Ftping the unload dataset",
						"checkStepCntl", "FTPFILE", "T");
				*/
	
				// pre-load
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Preload",
						"Preparing the load card", "preloadTarget",
						source_table_name, target_table_name);
	
				// load
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Load", "Submitting the load job",
						"loadTarget", source_table_name, target_table_name);	// v6.1.2
	
				// check load
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Load", "Loading "
						+ target_table_name + " table", "checkStepCntl", "LOADTGT",
						"T", target_table_name);	// v6.3
	
				// finish
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_table_name
						+ " to " + target_table_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Finish", "Finishing the process",
						"finishProcess", target_table_name);	// v6.3
			}
	
			// build the DMGProcess array with dataset items
			for (i = 0; i < dataset_count; i++) {
				// get the dataset details
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						item_status = tbl_datasets.getItem(i).getText(1).trim();	// v6.0.0 bug fix
						source_dataset_name = tbl_datasets.getItem(i).getText(2)
								.trim().toUpperCase();
						target_dataset_name = tbl_datasets.getItem(i).getText(3)
								.trim().toUpperCase();
					}
				});
				
				// look up the proc id - v6.0 beta 1
				String process_id = "";
				String process_ref = "D." + i;
				
				for (int r = 0; r < ref_proc_ids.length; r++) {
					if (!item_status.equals("New")
							&& ref_proc_ids[r] != null			// v6.1.2
							&& ref_proc_ids[r].length == 2		// v6.1.2
							&& ref_proc_ids[r][0] != null		// v6.1.2
							&& ref_proc_ids[r][1] != null		// v6.1.2
							&& ref_proc_ids[r][1].equals(process_ref)) {
						process_id = ref_proc_ids[r][0];
					}
				}
				
				if (process_id.isEmpty()) {
					process_id = ProcessIdentifier.getNewIdentifier();
				}
	
				// init
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_dataset_name
						+ " to " + target_dataset_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Init", "Initializing the process",
						"initProcess");
	
				// ftp
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_dataset_name
						+ " to " + target_dataset_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Ftp", "Submitting the ftp job",
						"ftpDataset", source_dataset_name, target_dataset_name);
	
				// check ftp
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_dataset_name
						+ " to " + target_dataset_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Ftp", "Ftping the unload dataset "
						+ procs[index - 1].getMethodUse1() + " to "
						+ procs[index - 1].getMethodUse2(), "checkStepCntl",
						"FTPFILE", "T");
	
				// finish
				procs[index] = new DMGProcess(source_schema, target_schema, process_id);
				procs[index].setTaskDesc("Migrating from " + source_dataset_name
						+ " to " + target_dataset_name);
				procs[index].setProcessRef(process_ref);
				procs[index++].setProcessMethod("Finish", "Finishing the process",
						"finishProcess");
			}
			
			// refresh the editor input - v6.0.beta1
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					editor_input.setRefProcIds(new_ref_proc_ids);
				}
			});
	
			// set the procs - v6.0.beta1
			Table[] array_tables = new Table[] { tbl_tables, tbl_datasets };
			String[] array_table_ids = new String[] { "T", "D" };
			
			setProcs(procs, array_tables, array_table_ids);
		
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().error(getProcName() + " Failed");
		}
	}

}
