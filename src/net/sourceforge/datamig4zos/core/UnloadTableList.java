/**
 *  Class: core.UnloadTableList
 *  Description: This is to unload the table from a server.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *	01. 03/10/2011: Code baseline. (V6.0 phase 1)
 *	02. 03/20/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *	03. 03/22/2011: Change the class to be based on DB2Schema instead of MFServer. (V6.0 phase 4)
 *	04. 03/26/2011: Use the re-designed DMGProcess. (V6.0 phase 6)
 *	05. 04/02/2011: Check for Failed status while looking for the next available process. (V6.0 phase 6)
 *	06. 04/02/2011: Does not check monitor.isCanceled() on Finish process to avoid dead loop. (V6.0 phase 6)
 *  07. 04/03/2011: Extends CoreJob instead of implements IRunnableWithProgress. (V6.0 beta 1)
 *  08. 11/05/2012: Collapse damage for the enhanced DMGProcess object. (V6.1)
 *  09. 08/06/2013: Enhance unload SQL to support the changed DB2table object. (V6.3)
 */
package net.sourceforge.datamig4zos.core;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.core.base.CoreJob;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.DMGProcess;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * @author SunPC
 * 
 */
public class UnloadTableList extends CoreJob {
	
	private DB2Schema schema;
	private String creator = "";

	// constructor
	public UnloadTableList(DB2Schema db2Schema) {
		super("Unloading Table List from " + db2Schema.getServer().getServerName()
				+ "." + db2Schema.getSchemaName(), "Table List Unload Process");
			
		setLoggerClass(UnloadTableList.class);

		// retain the creator
		creator = db2Schema.getSchemaName();
		schema = db2Schema;
		schema.setSchemaName("SYSIBM");
		
		// set server
		setServers(new MFServer[] { schema.getServer() });
		
		// unload sql - v6.3
		String unload_sql = "SELECT CHAR(RTRIM(AL1.NAME) || ',' " 
			+ "|| RTRIM(AL1.DBNAME) || ',' "
			+ "|| RTRIM(AL1.TSNAME) || ',' "
			+ "|| RTRIM(AL2.NAME) || ',' "
			+ "|| RTRIM(COALESCE(AL3.REFTBNAME,''))) "
			+ "FROM SYSIBM.SYSTABLES AL1 "
			+ "INNER JOIN SYSIBM.SYSCOLUMNS AL2 "
			+ "ON AL1.CREATOR = AL2.TBCREATOR "
			+ "AND AL1.NAME = AL2.TBNAME "
			+ "LEFT OUTER JOIN SYSIBM.SYSRELS AL3 "
			+ "ON AL1.CREATOR = AL3.CREATOR "
			+ "AND AL1.NAME = AL3.TBNAME "
			+ "WHERE AL1.CREATOR = '" + creator + "' "
			+ "AND AL1.TYPE = 'T' "
			+ "ORDER BY AL1.NAME, AL2.COLNO;";
				
		/* REMOVED in v6.3
		String unload_sql = "SELECT CHAR(RTRIM(CHAR(NAME)) || ',' || RTRIM(CHAR(DBNAME)) || ',' || RTRIM(CHAR(TSNAME))) "
			+ "FROM SYSIBM.SYSTABLES WHERE CREATOR = '" + creator + "' AND TYPE = 'T' "
			+ "ORDER BY NAME;";
		*/
		
		// build the DMGProcess array - v6.0.phase6
		DMGProcess[] procs = new DMGProcess[5];
		procs[0] = new DMGProcess(schema);								// v6.1
		procs[0].setProcessMethod("Init", "Initializing the process", 
				"initProcess");
		procs[1] = new DMGProcess(schema, procs[0].getProcessId());		// v6.1
		procs[1].setProcessMethod("Unload", "Submitting the unload job", 
				"unloadSource",	"SYSTABLES", unload_sql);
		procs[2] = new DMGProcess(schema, procs[0].getProcessId());		// v6.1
		procs[2].setProcessMethod("Unload", "Unloading SYSTABLES table", 
				"checkStepCntl", "UNLDSRC", "S");
		procs[3] = new DMGProcess(schema, procs[0].getProcessId());		// v6.1
		procs[3].setProcessMethod("Read", "Reading the unload dataset", 
				"readUnloadDataset", "SYSTABLES");
		procs[4] = new DMGProcess(schema, procs[0].getProcessId());		// v6.1
		procs[4].setProcessMethod("Finish", "Finishing the process", 
				"finishProcess");
		
		// set the procs - v6.0.beta1
		setProcs(procs);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		// call super run - v6.0.beta1
		IStatus status = super.run(monitor);
		
		// set the schema name back to the original - v6.0.phase4
		schema.setSchemaName(creator);
		
		// init array systables data
		String[] array_systables_data = new String[0]; 
		
		// continue to get the table list only if status is OK
		if (status.equals(Status.OK_STATUS)) {
			try {
				// get the unload data
				array_systables_data = getProcs()[3].getMethodUse1().split("\n");
				
				// create array list and variables - v6.3
				List<DB2Table> table_list = new ArrayList<DB2Table>();
				List<String> column_name_list = new ArrayList<String>();
				List<String> ref_table_name_list = new ArrayList<String>();
				
				String curr_table_name = "";
				String curr_db_name = "";
				String curr_ts_name = "";
				String curr_col_name = "";
				String curr_ref_name = "";
				
				String prev_table_name = "";
				String prev_db_name = "";
				String prev_ts_name = "";
				
				// process the unload data - v6.3
				for (int i = 0; i < array_systables_data.length; i++) {
					// set prev_ names
					prev_table_name = curr_table_name;
					prev_db_name = curr_db_name;
					prev_ts_name = curr_ts_name;
					
					// set curr_ names
					String[] array_table = array_systables_data[i].split(",");
					curr_table_name = array_table[0];
					curr_db_name = array_table[1];
					curr_ts_name = array_table[2];
					curr_col_name = array_table[3];
					
					if (array_table.length > 4)
						curr_ref_name = array_table[4];
					else
						curr_ref_name = "";
					
					// if table name changes, populate table_list					
					if (i > 0 && !prev_table_name.equals(curr_table_name)) {
						String[] array_column_names = column_name_list.toArray(new String[column_name_list.size()]);
						String[] array_ref_names = ref_table_name_list.toArray(new String[ref_table_name_list.size()]);
						table_list.add(new DB2Table(prev_table_name, prev_db_name, prev_ts_name, 
								array_column_names, array_ref_names, schema));
						column_name_list.clear();
						ref_table_name_list.clear();
					}
					
					// populate column_name_list
					if (column_name_list.indexOf(curr_col_name) == -1)
						column_name_list.add(curr_col_name);
					
					// populate ref_table_name_list
					if (ref_table_name_list.indexOf(curr_ref_name) == -1 && !curr_ref_name.isEmpty())
						ref_table_name_list.add(curr_ref_name);
				}
				
				// add the last table to table_list - v6.3
				String[] array_column_names = column_name_list.toArray(new String[column_name_list.size()]);
				String[] array_ref_names = ref_table_name_list.toArray(new String[ref_table_name_list.size()]);
				table_list.add(new DB2Table(curr_table_name, curr_db_name, curr_ts_name, 
						array_column_names, array_ref_names, schema));
				
				// set tables to the schema
				DB2Table[] tables = table_list.toArray(new DB2Table[table_list.size()]);	// v6.3
				schema.setTables(tables);
			} catch (Exception ex) {
				ex.printStackTrace();
				getLogger().error(getProcName() + " Failed");
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 8, ex.getMessage(), ex);
			}
		}

		return status;
	}

}
