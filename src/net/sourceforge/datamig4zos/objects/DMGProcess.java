/**
 *  Class: objects.DMGProcess
 *  Description: This is to handle all processes in DataMiG Tool.
 *  
 *  Class re-designed from V5.x ObjectItem.class.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Parameters needed on each JCL:
 *  
 *  0. COMMON
 *  #PROCESS_ID#: process id - from ProcessIdentifier
 *  #JOB_NAME#: job name - the last 6 chars of ProcessIdentifier with a 2-char prefix
 *  #BATCH_ID#: job batch id -<Server.job_batch_id>
 *  #USER_ID#: user id - <Server.logon_user>
 *  #CNTL_LIBRARY#: control library - <Server.dataset_prefix>.#USER_ID#.#PROCESS_ID#	(v6.0.0)
 *  #CNTL_MEMBER#: control member name - hard code in the program in each method (8 chars)
 *  #DB2LOAD#: DB2 load library (v6.1.1)
 *  #DSNLOAD#: DSN load library (v6.1.1)
 *  #SORTLIB#: sort exec library (v6.1.1)
 *  #SPACE#: default space allocation (v6.1.1) 
 *  #UNIT#: default DASD unit (v6.1.1)
 *  
 *  1. UNLOAD
 *  #DB_SSID#: database ssid - <Server.db_ssid>
 *  #DB_NAME#: database name - from <Server.table_list>
 *  #DB_SCHEMA#: database schema - <Server.db_schema>
 *  #TS_NAME#: tablespace name - from <Server.table_list>
 *  #TABLE_NAME#: table name - from input parameter
 *  #DATASET_SYSPUNCH#: sys punch file name - <Server.dataset_prefix>.#TS_NAME#.SYSCNTL	(v6.0.0)
 *  #DATASET_SYSREC#: sys rec file name - <Server.dataset_prefix>.#TS_NAME#.SYSREC	(v6.0.0)
 *  #UNLOAD_SQL#: unload sql - from input parameter or by pgm
 *  
 *  2. FTP
 *  #HOST_NAME#: remote host name - <Server.host_ip>
 *  #DS_NETRC_NAME#: NETRC dataset name which contains user id and password - by pgm (v6.2)
 *  #DS_SOURCE_NAME#: source dataset name - from input parameter or by pgm
 *  #DS_TARGET_NAME#: target dataset name - from input parameter or by pgm
 *  #DS_SOURCE_PREFIX#: source dataset prefix - by pgm (v6.2)
 *  #DS_TARGET_PREFIX#: target dataset prefix - by pgm (v6.2)
 *  #DS_SOURCE_MEMBER#: source dataset member - by pgm (v6.2)
 *  #DS_TARGET_MEMBER#: target dataset member - by pgm (v6.2)
 *  #DS_LRECL#: dataset rec length - by pgm
 *  #DS_BLKSZ#: dataset block size - by pgm
 *  #DS_RECFM#: dataset rec format - by pgm
 *  #DS_SPACE#: dataset space unit - by pgm (v6.2)
 *  #DS_PRI#: dataset primary space - by pgm (v6.2)
 *  #DS_SEC#: dataset secondary space - by pgm (v6.2)
 *  #DS_UNIT#: dataset device unit type - by pgm (v6.2)
 *  #DS_UCOUNT#: dataset device unit count - by pgm (v6.2)
 *  
 *  3. LOAD
 *  #DB_SSID#: database ssid - <Server.db_ssid>
 *  #DB_NAME#: database name - from <Server.table_list>
 *  #DB_SCHEMA#: database schema - <Server.db_schema>
 *  #TS_NAME#: tablespace name - from <Server.table_list>
 *  #TABLE_NAME#: table name - from input parameter
 *  #DATASET_SYSPUNCH#: sys punch file name - <Server.dataset_prefix>.#TS_NAME#.SYSCNTL	(v6.0.0)
 *  #DATASET_SYSREC#: sys rec file name - <Server.dataset_prefix>.#TS_NAME#.SYSREC	(v6.0.0)
 *  #DATASET_SYSDISCD#: sys discard file name - <Server.dataset_prefix>.#TS_NAME#.SYSDISCD	(v6.0.0)
 *  
 *  4. QUERY (v6.1)
 *  #DB_SSID#: database ssid - <Server.db_ssid>
 *  #DB_SCHEMA#: database schema - <Server.db_schema>
 *  #DATASET_OUTPUT#: sys print file name - <Server.dataset_prefix>.#PROCESS_ID#.QRYOUT
 *  #SQL_STMTS#: sql from input parameter or by pgm
 * 
 *  Modification History
 *  01. 02/18/2009: Code baseline. (V5.0 ObjectItem.class)
 *  02. 03/05/2011: Re-designed from ObjectItem.class. (V6.0 phase 1)
 *  03. 03/11/2011: Add downloadTable() method. (V6.0 phase 1) 
 *  04. 03/12/2011: Add a new constructor with existing process id. (V6.0 phase 2)
 *  05. 03/19/2011: Use IFileNames on folder and file names. (V6.0 phase 2)
 *  06. 03/20/2011: Rename from Process.class to DMGProcess.class. (V6.0 phase 3)
 *  07. 03/20/2011: Collapse damage for new data model objects. (V6.0 phase 3) 
 *  08. 03/22/2011: Change DMGProcess to be based on DB2Schema instead of MFServer. (V6.0 phase 4)
 *  09. 03/24/2011: Process re-design. (V6.0 phase 6)
 *  	1) Add DMGProcess attributes and common public methods including getter / setter and run.
 *  	2) Change all process related methods from public to private. 
 *  	3) Change downloadTable() to readUnloadDataset().
 *  10. 04/01/2011: Functional enhancements and Defect fixes. (V6.0 phase 6)
 *  	1) Add a logic in unloadSource() method to enrich the unloadSql.
 *  	2) Modify ftpTable, ftpDataset and preloadTarget to allow a blank target parameter.
 *  	3) Set the status to Completed only on Finish methods.
 *  	4) Add core_use2.
 *  	5) Add #FTP_GET_CMD# in ftpDataset() method.
 *  	6) Fix the load card in preloadTarget() method.
 *  11. 04/03/2011: Change core_use1/2 to task_desc and process_ref. (V6.0 beta 1)
 *  12. 04/15/2011: Fix a defect in initProcess() that target process was using sourceSchema. (V6.0 beta 1)
 *  13. 03/02/2012: Functional enhancements (V6.0.0)
 *  	1) Add JCL settings.
 *  	2) Shorten the data set names.
 *  14. 03/20/2012: Use preference for folder and file names. (V6.0.0)
 *  15. 11/02/2012: Use getJobBatchId(true) and getDatasetPrefix(true). (V6.0.1)
 *  16. 11/05/2012: Functional enhancements (V6.1)
 *  	1) Add DB query functions.
 *  	2) Create 2 new constructors with one schema only.
 *  17. 11/07/2012: Templates standardization. (V6.1.1)
 *  18. 11/12/2012: Performance tuning. (V6.1.2)
 *  	1) Use generateJcl() in all methods.
 *  	2) Enhance the functional methods.
 *  	3) Wrap the FTP command in ftpDataset() method.
 *  	4) Wrap the SQL statements in unloadSource() and runQuery() methods.
 *  19. 07/30/2013: FTP enhancements. (V6.2)
 *  	1) Handle big data sets FTP.
 *		2) Handle load modules FTP.
 *		3) Avoid security exposure.
 *	20. 08/08/2013: Support parent tables check. (V6.3)
 *		1) Create new methods checkStepCntl(), initProcess() and finishProcess() for table migration.
 *		2) Enhance preloadTarget() to check the migration status.
 *		3) Enhance run() to support 3 parameters in maximum.
 *  21. 11/14/2014: Fix the pre-load step to handle TBLNAME replacement. (V6.3.1)
 */
package net.sourceforge.datamig4zos.objects;

import java.io.File;
import java.lang.reflect.Method;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.IFileNames;
import net.sourceforge.datamig4zos.runtime.IJclDefaults;
import net.sourceforge.datamig4zos.ui.preferences.PreferenceConstants;
import net.sourceforge.datamig4zos.util.ProcessIdentifier;
import net.sourceforge.datamig4zos.util.TextProcessor;

import com.enterprisedt.util.debug.Level;
import com.enterprisedt.util.debug.Logger;

/**
 * @author SunPC
 * 
 */
public class DMGProcess {

	private Logger log = Logger.getLogger(DMGProcess.class);

	// data folder path
	// private String data_folder_name = "C:/eclipse/data/";
	private String data_tpl_folder_name = "";
	private String data_tmp_folder_name = "";

	// servers
	private DB2Schema source_schema;
	private DB2Schema target_schema;

	// DMGProcess attributes - v6.0.phase6
	private String process_id = ""; // process identifier
	private String process_name = "";	// process name
	private String process_desc = ""; // process description
	private String method_name = ""; // method to be run
	private String[] method_params; // method parameters
	private String process_status = ""; // process status
	private int return_code = -1; // method return code: '-1' means not yet run.
	private String method_use1 = ""; // reference data for method's internal use
	private String method_use2 = ""; // reference data for method's internal use
	private String task_desc = ""; // the task description of this process
	private String process_ref = ""; // v.s. user table reference

	// internal work variables
	private String jobname_suffix = ""; // the first 6 chars of process_id

	// constructor with source schema only - v6.1
	public DMGProcess(DB2Schema sourceSchema) {
		this(sourceSchema, null, ProcessIdentifier.getNewIdentifier());
	}

	// constructor with source schema and process id - v6.1
	public DMGProcess(DB2Schema sourceSchema, String processId) {
		this(sourceSchema, null, processId);
	}

	// constructor with both source and target schemas
	public DMGProcess(DB2Schema sourceSchema, DB2Schema targetSchema) {
		this(sourceSchema, targetSchema, ProcessIdentifier.getNewIdentifier());
	}

	// constructor with existing process id
	public DMGProcess(DB2Schema sourceSchema, DB2Schema targetSchema,
			String processId) {
		process_id = processId;
		source_schema = sourceSchema;
		target_schema = targetSchema;

		data_tpl_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/templates/";	// v6.0.0
		
		data_tmp_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/temp/"
				+ process_id + "/";													// v6.0.0

		jobname_suffix = process_id.substring(2);

		Logger.setLevel(Level.INFO);
	}

	// dynamically run the process based on method_name and method_parm,
	// and store the return code to return_code. - v6.0.phase6
	public void run() {
		process_status = process_name + "ing";

		try {
			// get the params
			int params_count = method_params.length;
			Class<?> params_class[] = new Class[params_count];

			// initialize the params class
			for (int i = 0; i < params_count; i++) {
				params_class[i] = String.class;
			}

			// set the method
			Method thisMethod = this.getClass().getDeclaredMethod(method_name,
					params_class);

			// invoke the method and set the return code
			if (method_params.length == 0) {
				return_code = Integer.parseInt(thisMethod.invoke(this)
						.toString());
			} else if (method_params.length == 1) {
				return_code = Integer.parseInt(thisMethod.invoke(this,
						method_params[0]).toString());
			} else if (method_params.length == 2) {
				return_code = Integer.parseInt(thisMethod.invoke(this,
						method_params[0], method_params[1]).toString());
			} else if (method_params.length == 3) {			// v6.3
				return_code = Integer.parseInt(thisMethod.invoke(this,
						method_params[0], method_params[1], method_params[2]).toString());
			} else {
				throw new IllegalArgumentException();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return_code = 8;
			process_status = "Failed to " + process_name;
		}
		
		if (process_name.equals("Finish")) {	// v6.0.phase6
			process_status = "Completed";
		} else if (return_code == 8){
			process_status = "Failed to " + process_name;
		}
	}

	// return the process id - v6.0.phase6
	public String getProcessId() {
		return process_id;
	}

	// return the process name - v6.0.phase6
	public String getProcessName() {
		return process_name;
	}

	// return the process desc - v6.0.phase6
	public String getProcessDesc() {
		return process_desc;
	}

	// set the method name - v6.0.phase6
	public void setProcessMethod(String processName, String processDesc, 
			String methodName, String ... methodParams) {
		process_name = processName;
		process_desc = processDesc;
		method_name = methodName;
		method_params = methodParams;
	}

	// get the return code - v6.0.phase6
	public int getReturnCode() {
		return return_code;
	}

	// get the core reference value - v6.0.phase6
	public String getTaskDesc() {
		return task_desc;
	}

	// set the core reference value - v6.0.phase6
	public void setTaskDesc(String coreUse) {
		task_desc = coreUse;
	}

	// get the core reference value - v6.0.phase6
	public String getProcessRef() {
		return process_ref;
	}

	// set the core reference value - v6.0.phase6
	public void setProcessRef(String coreUse) {
		process_ref = coreUse;
	}

	// get the method reference value - v6.0.phase6
	public String getMethodUse1() {
		return method_use1;
	}

	// get the method reference value - v6.0.phase6
	public String getMethodUse2() {
		return method_use2;
	}

	// get the data template folder name - v6.0.phase6
	public String getDataTplFolderName() {
		return data_tpl_folder_name;
	}

	// get the data temporary folder name - v6.0.phase6
	public String getDataTmpFolderName() {
		return data_tmp_folder_name;
	}

	// get process status - v6.0.phase6
	public String getProcessStatus() {
		return process_status;
	}

	// set process status - v6.0.phase6
	public void setProcessStatus(String processStatus) {
		process_status = processStatus;
	}
	
	// step - check the job running status on servers for tables - v6.3
	@SuppressWarnings("unused")
	private int checkStepCntl(String memberName, String schemaType, String tableName)
			throws Exception {
		int status = checkStepCntl(memberName, schemaType);
		
		if (status == 8) {
			DB2Table table = new DB2Table(tableName, target_schema);
			table.setMigError();
		}
		
		return status;
	}

	// step - check the job running status on servers
	private int checkStepCntl(String memberName, String schemaType)
			throws Exception {
		int status = -1;

		// retrieve server type
		DB2Schema schema = source_schema;
		if (schemaType.equals("T")) {
			schema = target_schema;
		}

		String dataset_name_success = "'" + schema.getDatasetPrefix(true) + "."		// v6.0.1
				+ schema.getServer().getLogonUser() + "." + process_id
				+ "(" + memberName + "C)'";		// v6.0.0

		String dataset_name_failure = "'" + schema.getDatasetPrefix(true) + "."		// v6.0.1
				+ schema.getServer().getLogonUser() + "." + process_id
				+ "(" + memberName + "E)'";		// v6.0.0

		// check success : if count > 0, return 0 means job completed
		// successfully
		log.info("Checking " + dataset_name_success);
		if (schema.getServer().listFile(dataset_name_success).length > 0) {
			status = 0;
		}

		// check failure : if count > 0, return 8 means job failed
		log.info("Checking " + dataset_name_failure);
		if (schema.getServer().listFile(dataset_name_failure).length > 0) {
			status = 8;
		}

		return status;
	}
	
	// step - Initialize for tables - v6.3
	@SuppressWarnings("unused")
	private int initProcess(String tableName) throws Exception {
		DB2Table table = new DB2Table(tableName, target_schema);
		table.setMigStart();
		
		return initProcess();
	}

	// step - Initialize
	private int initProcess() throws Exception {
		// create a temp folder for the process
		int return_code = 0;
		File temp_folder_file = new File(data_tmp_folder_name);

		log.info("Creating Temporary Folder");

		// if the folder exists, clean up the folder
		if (!temp_folder_file.exists()) {
			temp_folder_file.mkdir();
		} else {
			String[] temp_file_list = temp_folder_file.list();
			for (int i = 0; i < temp_file_list.length; i++) {
				File del_file = new File(data_tmp_folder_name
						+ temp_file_list[i]);
				if (del_file != null) {
					del_file.delete();
				}
			}
		}

		log.info("Temporary Folder " + data_tmp_folder_name + " Created");
		
		/* REMOVED in v6.1.2
		// create a control library on source server
		if (source_schema instanceof DB2Schema) {
			// generate template
			String jcl_main = TextProcessor.readFile(data_tpl_folder_name
					+ Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));	// v6.0.0
			String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
					+ Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));	// v6.0.0

			// generate parameter
			String[][] global_parm = new String[11][2];
			String job_name = IJclDefaults.JOBNAME_PREFIX_ALLOC + jobname_suffix;

			global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
			global_parm[1] = new String[] {"#JOB_NAME#", job_name};
			global_parm[2] = new String[] {"#BATCH_ID#", source_schema.getJobBatchId(true)};	// v6.0.1
			global_parm[3] = new String[] {"#USER_ID#", source_schema.getServer().getLogonUser()};
			global_parm[4] = new String[] {"#CNTL_LIBRARY#",
					source_schema.getDatasetPrefix(true) + "."					// v6.0.1
					+ source_schema.getServer().getLogonUser() + "."
					+ process_id};	// v6.0.0
			global_parm[5] = new String[] {"#CNTL_MEMBER#", "INITCTL"};
			global_parm[6] = new String[] {"#DB2LOAD#", source_schema.getServer().getParamDb2load(true)};	// v6.1.1
			global_parm[7] = new String[] {"#DSNLOAD#", source_schema.getServer().getParamDsnload(true)};	// v6.1.1
			global_parm[8] = new String[] {"#SORTLIB#", source_schema.getServer().getParamSortlib(true)};	// v6.1.1
			global_parm[9] = new String[] {"#SPACE#", source_schema.getServer().getParamSpace(true)};		// v6.1.1
			global_parm[10] = new String[] {"#UNIT#", source_schema.getServer().getParamUnit(true)};		// v6.1.1

			// generate output JCL
			TextProcessor.generateJcl(source_schema.getServer().getJclHeaderBatch(), 
					source_schema.getJclSettingsBatch(), 
					jcl_main, jcl_cntl, global_parm, 
					data_tmp_folder_name + IFileNames.OUTPUT_INIT_SOURCE_NAME);		// v6.1.2

			// submit job
			log.info("Submitting Source Initialization Job");
			source_schema.getServer().submitJob(
					data_tmp_folder_name + IFileNames.OUTPUT_INIT_SOURCE_NAME);

			// end
			log.info("Source Initialization Job " + job_name + " Submitted");

			// verify the control library on source
			int chk = checkStepCntl("INITCTL", "S");
			if (chk == 0) {
				return_code = 0;
			} else if (chk == 8) {
				return_code = 8;
			} else {
				Thread.sleep(3000);
				chk = checkStepCntl("INITCTL", "S");
				if (chk != 0) {
					return_code = 8;
				}
			}
		} else {
			return_code = 1;
		}

		// create a control library on target server
		if (target_schema instanceof DB2Schema) {
			// generate template
			String jcl_main = TextProcessor.readFile(data_tpl_folder_name
					+ Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));		// v6.0.0
			String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
					+ Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));		// v6.0.0

			// generate parameter
			String[][] global_parm = new String[11][2];
			String job_name = IJclDefaults.JOBNAME_PREFIX_ALLOC + jobname_suffix;

			global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
			global_parm[1] = new String[] {"#JOB_NAME#", job_name};
			global_parm[2] = new String[] {"#BATCH_ID#", target_schema.getJobBatchId(true)};		// v6.0.1
			global_parm[3] = new String[] {"#USER_ID#", target_schema.getServer().getLogonUser()};
			global_parm[4] = new String[] {"#CNTL_LIBRARY#",
					target_schema.getDatasetPrefix(true) + "."						// v6.0.1
					+ target_schema.getServer().getLogonUser() + "."
					+ process_id};	// v6.0.0
			global_parm[5] = new String[] {"#CNTL_MEMBER#", "INITCTL"};
			global_parm[6] = new String[] {"#DB2LOAD#", target_schema.getServer().getParamDb2load(true)};	// v6.1.1
			global_parm[7] = new String[] {"#DSNLOAD#", target_schema.getServer().getParamDsnload(true)};	// v6.1.1
			global_parm[8] = new String[] {"#SORTLIB#", target_schema.getServer().getParamSortlib(true)};	// v6.1.1
			global_parm[9] = new String[] {"#SPACE#", target_schema.getServer().getParamSpace(true)};		// v6.1.1
			global_parm[10] = new String[] {"#UNIT#", target_schema.getServer().getParamUnit(true)};		// v6.1.1

			// generate output JCL
			TextProcessor.generateJcl(target_schema.getServer().getJclHeaderBatch(), 
					target_schema.getJclSettingsBatch(), 
					jcl_main, jcl_cntl, global_parm, 
					data_tmp_folder_name + IFileNames.OUTPUT_INIT_TARGET_NAME);		// v6.1.2
			

			// submit job
			log.info("Submitting Target Initialization Job");
			target_schema.getServer().submitJob(	// v6.0 beta 1
					data_tmp_folder_name + IFileNames.OUTPUT_INIT_TARGET_NAME);

			// end
			log.info("Target Initialization Job " + job_name + " Submitted");

			// verify the control library on target
			try {
				int chk = checkStepCntl("INITCTL", "T");
				if (chk == 0) {
					return_code = 0;
				} else if (chk == 8) {
					return_code = 8;
				} else {
					Thread.sleep(3000);
					chk = checkStepCntl("INITCTL", "T");
					if (chk != 0) {
						return_code = 8;
					}
				}
			} catch (Exception ex) {
				return_code = 8;
				ex.printStackTrace();
			}
		} else {
			return_code = 2;
		}
		*/

		// end
		return return_code;
	}

	// unload a table from source server - without unloadSql
	private int unloadSource(String tableName) throws Exception {
		String sql = "SELECT * FROM " + source_schema.getSchemaName() + "."
				+ tableName + ";";
		return unloadSource(tableName, sql);
	}

	// unload a table from source server - with unloadSql
	private int unloadSource(String tableName, String unloadSql)
			throws Exception {
		// step 2: unload
		int return_code = 0;
		
		// enrich the unloadSql - v6.0.phase6
		String unload_sql = unloadSql.trim();

		if (unload_sql.isEmpty()) {	// produce the sql
			return unloadSource(tableName);
		} else if (unload_sql.toUpperCase().startsWith("FROM")) {
			unload_sql = "SELECT * " + unload_sql;
		} else if (unload_sql.toUpperCase().startsWith("WHERE")) {
			unload_sql = "SELECT * FROM " + source_schema.getSchemaName() + "."
			+ tableName + " " + unload_sql;
		} else if (!unload_sql.toUpperCase().startsWith("SELECT")) {
			unload_sql = "SELECT * FROM " + source_schema.getSchemaName() + "."
			+ tableName + " WHERE " + unload_sql;
		}
		
		if (!unload_sql.endsWith(";")) {	// add the semicolon
			unload_sql += ";";
		}
		
		if (unload_sql.indexOf(source_schema.getSchemaName()) == -1) {	// add the schema
			unload_sql = TextProcessor.replaceStr(unload_sql, tableName, 
					source_schema.getSchemaName() + "."	+ tableName);
		}
		
		unload_sql = TextProcessor.wrapText(unload_sql, 70);						// v6.1.2
		
		// method start
		log.info("Generating Unload JCL");

		// define the source dataset name
		String source_dataset_name = source_schema.getDatasetPrefix(true) + "."		// v6.0.1
				+ source_schema.getTable(tableName).getShortTsName(process_id);		// v6.0.0
		method_use1 = source_dataset_name; // v6.0.phase6

		// generate template
		String jcl_allo = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));			// v6.1.2
		String jcl_main = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_UNLOAD_SOURCE));		// v6.0.0
		String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));			// v6.0.0

		// generate parameter
		String[][] global_parm = new String[19][2];
		String job_name = IJclDefaults.JOBNAME_PREFIX_UNLOAD + jobname_suffix;

		global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
		global_parm[1] = new String[] {"#JOB_NAME#", job_name};
		global_parm[2] = new String[] {"#BATCH_ID#", source_schema.getJobBatchId(true)};		// v6.0.1
		global_parm[3] = new String[] {"#USER_ID#", source_schema.getServer().getLogonUser()};
		global_parm[4] = new String[] {"#CNTL_LIBRARY#", 
				source_schema.getDatasetPrefix(true) + "."						// v6.0.1
				+ source_schema.getServer().getLogonUser() + "."
				+ process_id};	// v6.0.0
		global_parm[5] = new String[] {"#CNTL_MEMBER#", "UNLDSRC"};
		global_parm[6] = new String[] {"#DB2LOAD#", source_schema.getServer().getParamDb2load(true)};	// v6.1.1
		global_parm[7] = new String[] {"#DSNLOAD#", source_schema.getServer().getParamDsnload(true)};	// v6.1.1
		global_parm[8] = new String[] {"#SORTLIB#", source_schema.getServer().getParamSortlib(true)};	// v6.1.1
		global_parm[9] = new String[] {"#SPACE#", source_schema.getServer().getParamSpace(true)};		// v6.1.1
		global_parm[10] = new String[] {"#UNIT#", source_schema.getServer().getParamUnit(true)};		// v6.1.1
		global_parm[11] = new String[] {"#DB_SSID#", source_schema.getServer().getDbSsid()};
		global_parm[12] = new String[] {"#DB_NAME#", source_schema.getTable(tableName).getDbName()};
		global_parm[13] = new String[] {"#DB_SCHEMA#", source_schema.getSchemaName()};
		global_parm[14] = new String[] {"#TS_NAME#", source_schema.getTable(tableName).getTsName()};
		global_parm[15] = new String[] {"#TABLE_NAME#", tableName};
		global_parm[16] = new String[] {"#DATASET_SYSREC#", source_dataset_name + ".SYSREC"};
		global_parm[17] = new String[] {"#DATASET_SYSPUNCH#", source_dataset_name + ".SYSCNTL"};
		global_parm[18] = new String[] {"#UNLOAD_SQL#", unload_sql};

		// generate output JCL
		TextProcessor.generateJcl(data_tmp_folder_name + IFileNames.OUTPUT_UNLOAD_NAME, 
				global_parm, source_schema.getServer().getJclHeaderBatch(), 
				source_schema.getJclSettingsBatch(), jcl_allo, jcl_main, jcl_cntl);		// v6.1.2

		// submit job
		log.info("Submitting Unload Job");
		source_schema.getServer().submitJob(
				data_tmp_folder_name + IFileNames.OUTPUT_UNLOAD_NAME);

		// end
		log.info("Unload Job " + job_name + " Submitted");

		return return_code;
	}

	// ftp a table from source to target - with default sysrec file
	private int ftpTable(String tableName) throws Exception {
		return ftpTable(tableName, tableName);
	}

	// ftp a table from source to target - with default sysrec file
	private int ftpTable(String sourceTableName, String targetTableName)
			throws Exception {
		if (targetTableName.trim().isEmpty()) {
			return ftpTable(sourceTableName);
		}
		
		String source_dataset_name = source_schema.getDatasetPrefix(true) + "."						// v6.0.1
				+ source_schema.getTable(sourceTableName).getShortTsName(process_id) + ".SYSREC";	// v6.0.0

		String target_dataset_name = target_schema.getDatasetPrefix(true) + "."						// v6.0.1
				+ target_schema.getTable(targetTableName).getShortTsName(process_id) + ".SYSREC";	// v6.0.0

		return ftpDataset(source_dataset_name, target_dataset_name);
	}

	// ftp a file from source to target - with same file name
	private int ftpDataset(String datasetName) throws Exception {
		return ftpDataset(datasetName, datasetName);
	}

	// ftp a file from source to target - with diff source and target file names
	private int ftpDataset(String sourceDatasetName, String targetDatasetName)
			throws Exception {
		// step 4: FTP
		int return_code = 0;
		
		if (targetDatasetName.trim().isEmpty()) {
			return ftpDataset(sourceDatasetName);
		}
		
		// method start
		log.info("Generating Dataset FTP JCL");

		method_use1 = sourceDatasetName; // v6.0.phase6
		method_use2 = targetDatasetName; // v6.0.phase6

		// get dataset info
		String filename_src = sourceDatasetName;
		String filename_tgt = targetDatasetName;
		String[] dataset_info = source_schema.getServer()
				.listFile("'" + filename_src + "'")[0];

		// generate template
		String jcl_allo = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));		// v6.1.2
		String jcl_main = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_FTP_DATASET));	// v6.0.0
		String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));		// v6.0.0
		
		// upload NETRC - v6.2
		String netrc_dataset_name = target_schema.getDatasetPrefix(true) 
				+ "." + process_id + ".NETRC";
		String netrc_dataset_cmd = "machine " + source_schema.getServer().getHostIp()
				+ " login " + source_schema.getServer().getLogonUser() 
				+ " password " + source_schema.getServer().getLogonPwd();
		TextProcessor.writeFile(netrc_dataset_cmd, data_tmp_folder_name + IFileNames.FTP_CNTL_NETRC_NAME);
		
		log.info("Uploading " + netrc_dataset_name);
		target_schema.getServer().uploadFile(data_tmp_folder_name + IFileNames.FTP_CNTL_NETRC_NAME, 
				"'" + netrc_dataset_name + "'", 80);

		// generate parameter
		String[][] global_parm = new String[27][2];
		String job_name = IJclDefaults.JOBNAME_PREFIX_FTP + jobname_suffix;

		global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
		global_parm[1] = new String[] {"#JOB_NAME#", job_name};
		global_parm[2] = new String[] {"#BATCH_ID#", target_schema.getJobBatchId(true)};		// v6.0.1
		global_parm[3] = new String[] {"#USER_ID#", target_schema.getServer().getLogonUser()};
		global_parm[4] = new String[] {"#CNTL_LIBRARY#", 
				target_schema.getDatasetPrefix(true) + "."						// v6.0.1
				+ target_schema.getServer().getLogonUser() + "."
				+ process_id};	// v6.0.0
		global_parm[5] = new String[] {"#CNTL_MEMBER#", "FTPFILE"};
		global_parm[6] = new String[] {"#DB2LOAD#", target_schema.getServer().getParamDb2load(true)};	// v6.1.1
		global_parm[7] = new String[] {"#DSNLOAD#", target_schema.getServer().getParamDsnload(true)};	// v6.1.1
		global_parm[8] = new String[] {"#SORTLIB#", target_schema.getServer().getParamSortlib(true)};	// v6.1.1
		global_parm[9] = new String[] {"#SPACE#", target_schema.getServer().getParamSpace(true)};		// v6.1.1
		global_parm[10] = new String[] {"#UNIT#", target_schema.getServer().getParamUnit(true)};		// v6.1.1
		global_parm[11] = new String[] {"#HOST_NAME#", source_schema.getServer().getHostIp()};
		global_parm[12] = new String[] {"#DS_NETRC_NAME#", netrc_dataset_name};							// v6.2
		global_parm[13] = new String[] {"#DS_SOURCE_NAME#", filename_src};
		global_parm[14] = new String[] {"#DS_TARGET_NAME#", filename_tgt};
		
		// build data set prefixes and members - v6.2
		filename_src = TextProcessor.replaceStr(filename_src, "(", ".");
		filename_src = TextProcessor.replaceStr(filename_src, ")", "");
		String prefix_src = filename_src.substring(0, filename_src.lastIndexOf("."));
		String member_src = filename_src.substring(filename_src.lastIndexOf(".") + 1);
		
		filename_tgt = TextProcessor.replaceStr(filename_tgt, "(", ".");
		filename_tgt = TextProcessor.replaceStr(filename_tgt, ")", "");
		String prefix_tgt = filename_tgt.substring(0, filename_tgt.lastIndexOf("."));
		String member_tgt = filename_tgt.substring(filename_tgt.lastIndexOf(".") + 1);

		global_parm[15] = new String[] {"#DS_SOURCE_PREFIX#", prefix_src};
		global_parm[16] = new String[] {"#DS_TARGET_PREFIX#", prefix_tgt};
		global_parm[17] = new String[] {"#DS_SOURCE_MEMBER#", member_src};
		global_parm[18] = new String[] {"#DS_TARGET_MEMBER#", member_tgt};
		
		// build data set allocation parameters - v6.2
		global_parm[19] = new String[] {"#DS_LRECL#", dataset_info[1]};
		global_parm[20] = new String[] {"#DS_BLKSZ#", dataset_info[2]};
		global_parm[21] = new String[] {"#DS_RECFM#", dataset_info[3]};
		
		String param_space = target_schema.getServer().getParamSpace(true); 
		param_space = TextProcessor.replaceStr(param_space, "(", "");
		param_space = TextProcessor.replaceStr(param_space, ")", "");
		
		String [] array_space = param_space.split(",");
		String ds_space = array_space[0];
		String ds_pri = "0";
		if (array_space.length > 1)
			ds_pri = array_space[1];
		String ds_sec = "0";
		if (array_space.length > 2)
			ds_sec = array_space[2];
		
		String param_unit = target_schema.getServer().getParamUnit(true);
		param_unit = TextProcessor.replaceStr(param_unit, "(", "");
		param_unit = TextProcessor.replaceStr(param_unit, ")", "");
		
		String [] array_unit = param_unit.split(",");
		String ds_unit = array_unit[0];
		String ds_ucount = "1";
		if (array_unit.length > 1)
			ds_ucount = array_unit[1];

		global_parm[22] = new String[] {"#DS_SPACE#", ds_space};
		global_parm[23] = new String[] {"#DS_PRI#", ds_pri};
		global_parm[24] = new String[] {"#DS_SEC#", ds_sec};
		global_parm[25] = new String[] {"#DS_UNIT#", ds_unit};
		global_parm[26] = new String[] {"#DS_UCOUNT#", ds_ucount};
		
		/* REMOVED in v6.2
		// build FTP_GET_CMD - v6.0.phase6
		String ftp_get_cmd = "GET '" + filename_src + "'";
		for (int i = ftp_get_cmd.length(); i < 71; i++)
			ftp_get_cmd += " ";
		
		ftp_get_cmd += "+\n   '" + filename_tgt + "'";					// v6.1.2
		
		global_parm[19] = new String[] {"#FTP_GET_CMD#", ftp_get_cmd};
		*/

		// generate output JCL
		TextProcessor.generateJcl(data_tmp_folder_name + IFileNames.OUTPUT_FTP_NAME, 
				global_parm, target_schema.getServer().getJclHeaderBatch(), 
				target_schema.getJclSettingsBatch(), jcl_allo, jcl_main, jcl_cntl);		// v6.1.2

		// submit job
		log.info("Submitting FTP Job");
		target_schema.getServer().submitJob(
				data_tmp_folder_name + IFileNames.OUTPUT_FTP_NAME);

		// end
		log.info("Dataset FTP Job " + job_name + " Submitted");

		return return_code;
	}

	// prepare a load card for a table
	private int preloadTarget(String tableName) throws Exception {
		return preloadTarget(tableName, tableName);
	}

	// prepare a load card for a table
	private int preloadTarget(String sourceTableName, String targetTableName)
			throws Exception {
		// step 6: pre-load
		int return_code = 0;
		
		if (targetTableName.trim().isEmpty()) {
			return preloadTarget(sourceTableName);
		}
		
		// check reference tables migration status - v6.3
		String[] array_ref_names = new DB2Table(targetTableName, target_schema).getRefTableNames();
		
		for (int i = 0; i < array_ref_names.length; i++) {
			String status = new DB2Table(array_ref_names[i], target_schema).getMigStatus();
			
			if (status.equals("S")) {
				log.info("Preload on hold - " + targetTableName
						+ "'s parent table " + array_ref_names[i]
						+ " is migrating");
				return_code = -1;
				return return_code;
			} else if (status.equals("E")) {
				log.info("Preload failed - " + targetTableName
						+ "'s parent table " + array_ref_names[i]
						+ " is failed");
				return_code = 8;
				return return_code;
			}
		}
		
		// method start
		log.info("Preparing Table Load Card");

		String source_dataset_name = "'" + source_schema.getDatasetPrefix(true) + "."				// v6.0.1
				+ source_schema.getTable(sourceTableName).getShortTsName(process_id) + ".SYSCNTL'";	// v6.0.0

		String target_dataset_name = "'" + target_schema.getDatasetPrefix(true) + "."				// v6.0.1
				+ target_schema.getTable(targetTableName).getShortTsName(process_id) + ".SYSCNTL'";	// v6.0.0

		method_use1 = source_dataset_name; // v6.0.phase6
		method_use2 = target_dataset_name; // v6.0.phase6

		// download file
		log.info("Downloading " + source_dataset_name);
		source_schema.getServer().downloadFile(
				data_tmp_folder_name + IFileNames.LOAD_CNTL_SOURCE_NAME, source_dataset_name);		// v6.2

		// reformat current file - from StepPreLoad.java
		// RE-DESIGNED in V6.0 phase 1 : use TextProcessor instead of
		// BufferedReader/Writer.
		// old: LOAD DATA LOG NO INDDN SYSREC00 INTO TABLE
		// <source db schema>.<source_table_name>
		// new: LOAD DATA LOG NO INDDN SYSREC RESUME NO REPLACE INTO TABLE
		// <source db schema>.<target_table_name>
		// REPAIR OBJECT LOG NO
		// SET TABLESPACE <database>.<tablespace>
		// NOCOPYPEND
		log.info("Re-Formatting " + data_tmp_folder_name
				+ IFileNames.LOAD_CNTL_SOURCE_NAME);

		String load_card = TextProcessor.readFile(data_tmp_folder_name
				+ IFileNames.LOAD_CNTL_SOURCE_NAME);

		/*load_card = TextProcessor.replaceStr(load_card, "SYSREC00",
				"SYSREC RESUME NO REPLACE");
		load_card = TextProcessor.replaceStr(load_card, source_schema
				.getSchemaName(), target_schema.getSchemaName());
		load_card = TextProcessor.replaceStr(load_card, sourceTableName,
				targetTableName);		removed in v6.3.1 */
		
		int first_br = load_card.indexOf("\n");						// v6.3.1
		int second_br = load_card.indexOf("\n", first_br + 1);		// v6.3.1
		
		load_card = load_card.substring(second_br + 1);		// v6.3.1
		load_card = "  LOAD DATA LOG NO INDDN SYSREC RESUME NO REPLACE INTO TABLE\n" 
					+ "  " + target_schema.getSchemaName() + "." + targetTableName + "\n"
					+ load_card;		// v6.3.1
		load_card += "  REPAIR OBJECT LOG NO\n";
		load_card += "  SET TABLESPACE "
				+ target_schema.getTable(targetTableName).getDbName() + "."
				+ target_schema.getTable(targetTableName).getTsName() + "\n";
		load_card += "  NOCOPYPEND";

		TextProcessor.writeFile(load_card, data_tmp_folder_name
				+ IFileNames.LOAD_CNTL_TARGET_NAME);

		// upload current file
		log.info("Uploading " + target_dataset_name);
		target_schema.getServer().uploadFile(
				data_tmp_folder_name + IFileNames.LOAD_CNTL_TARGET_NAME,
				target_dataset_name, 80);	// v6.2
		
		// upload NETRC - v6.2
		String netrc_dataset_name = target_schema.getDatasetPrefix(true) 
				+ "." + process_id + ".NETRC";
		String netrc_dataset_cmd = "machine " + source_schema.getServer().getHostIp()
				+ " login " + source_schema.getServer().getLogonUser() 
				+ " password " + source_schema.getServer().getLogonPwd();
		TextProcessor.writeFile(netrc_dataset_cmd, data_tmp_folder_name + IFileNames.FTP_CNTL_NETRC_NAME);
		
		log.info("Uploading " + netrc_dataset_name);
		target_schema.getServer().uploadFile(data_tmp_folder_name + IFileNames.FTP_CNTL_NETRC_NAME, 
				"'" + netrc_dataset_name + "'", 80);

		return return_code;
	}
	
	@SuppressWarnings("unused")
	private int loadTarget(String tableName) throws Exception {
		return loadTarget(tableName, tableName);
	}

	// ftp and load a table into target server
	private int loadTarget(String sourceTableName, String targetTableName) throws Exception {
		// step 7: load
		int return_code = 0;		
		log.info("Getting FTP Information");
		String source_dataset_name = source_schema.getDatasetPrefix(true) + "."			// v6.1.2
				+ source_schema.getTable(sourceTableName).getShortTsName(process_id);	// v6.1.2
		String target_dataset_name = target_schema.getDatasetPrefix(true) + "."			// v6.0.1
				+ target_schema.getTable(targetTableName).getShortTsName(process_id);	// v6.0.0
		
		String[] dataset_info = source_schema.getServer()
				.listFile("'" + source_dataset_name + ".SYSREC'")[0];					// v6.1.2
	
		method_use1 = source_dataset_name;
		method_use2 = target_dataset_name;

		// generate template
		log.info("Generating Load JCL");
		String jcl_allo = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));			// v6.1.2
		String jcl_main1 = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_FTP_DATASET));		// v6.1.2
		String jcl_main2 = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_LOAD_TARGET));		// v6.0.0
		String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));			// v6.0.0
		
		// generate parameter
		String[][] global_parm = new String[35][2];
		String job_name = IJclDefaults.JOBNAME_PREFIX_LOAD + jobname_suffix;

		global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
		global_parm[1] = new String[] {"#JOB_NAME#", job_name};
		global_parm[2] = new String[] {"#BATCH_ID#", target_schema.getJobBatchId(true)};		// v6.0.1
		global_parm[3] = new String[] {"#USER_ID#", target_schema.getServer().getLogonUser()};
		global_parm[4] = new String[] {"#CNTL_LIBRARY#", 
				target_schema.getDatasetPrefix(true) + "."						// v6.0.1
				+ target_schema.getServer().getLogonUser() + "."
				+ process_id};														// v6.0.0
		global_parm[5] = new String[] {"#CNTL_MEMBER#", "LOADTGT"};
		global_parm[6] = new String[] {"#DB2LOAD#", target_schema.getServer().getParamDb2load(true)};	// v6.1.1
		global_parm[7] = new String[] {"#DSNLOAD#", target_schema.getServer().getParamDsnload(true)};	// v6.1.1
		global_parm[8] = new String[] {"#SORTLIB#", target_schema.getServer().getParamSortlib(true)};	// v6.1.1
		global_parm[9] = new String[] {"#SPACE#", target_schema.getServer().getParamSpace(true)};		// v6.1.1
		global_parm[10] = new String[] {"#UNIT#", target_schema.getServer().getParamUnit(true)};		// v6.1.1
		global_parm[11] = new String[] {"#HOST_NAME#", source_schema.getServer().getHostIp()};			// v6.1.2
		global_parm[12] = new String[] {"#DB_SSID#", target_schema.getServer().getDbSsid()};
		global_parm[13] = new String[] {"#DB_NAME#", target_schema.getTable(targetTableName).getDbName()};
		global_parm[14] = new String[] {"#DB_SCHEMA#", target_schema.getSchemaName()};
		global_parm[15] = new String[] {"#TS_NAME#", target_schema.getTable(targetTableName).getTsName()};
		global_parm[16] = new String[] {"#TABLE_NAME#", targetTableName};
		global_parm[17] = new String[] {"#DATASET_SYSREC#", target_dataset_name + ".SYSREC"};
		global_parm[18] = new String[] {"#DATASET_SYSPUNCH#", target_dataset_name + ".SYSCNTL"};
		global_parm[19] = new String[] {"#DATASET_SYSDISCD#", target_dataset_name + ".SYSDISCD"};
		
		// build data set prefixes and members - v6.2
		String filename_src = source_dataset_name + ".SYSREC";
		String prefix_src = filename_src.substring(0, filename_src.lastIndexOf("."));
		String member_src = filename_src.substring(filename_src.lastIndexOf(".") + 1);
		
		String filename_tgt = target_dataset_name + ".SYSREC";
		String prefix_tgt = filename_tgt.substring(0, filename_tgt.lastIndexOf("."));
		String member_tgt = filename_tgt.substring(filename_tgt.lastIndexOf(".") + 1);

		global_parm[20] = new String[] {"#DS_SOURCE_NAME#", filename_src};
		global_parm[21] = new String[] {"#DS_TARGET_NAME#", filename_tgt};
		global_parm[22] = new String[] {"#DS_SOURCE_PREFIX#", prefix_src};
		global_parm[23] = new String[] {"#DS_TARGET_PREFIX#", prefix_tgt};
		global_parm[24] = new String[] {"#DS_SOURCE_MEMBER#", member_src};
		global_parm[25] = new String[] {"#DS_TARGET_MEMBER#", member_tgt};
		
		// build data set allocation parameters - v6.2
		global_parm[26] = new String[] {"#DS_LRECL#", dataset_info[1]};
		global_parm[27] = new String[] {"#DS_BLKSZ#", dataset_info[2]};
		global_parm[28] = new String[] {"#DS_RECFM#", dataset_info[3]};
		
		String param_space = target_schema.getServer().getParamSpace(true); 
		param_space = TextProcessor.replaceStr(param_space, "(", "");
		param_space = TextProcessor.replaceStr(param_space, ")", "");
		
		String [] array_space = param_space.split(",");
		String ds_space = array_space[0];
		String ds_pri = "0";
		if (array_space.length > 1)
			ds_pri = array_space[1];
		String ds_sec = "0";
		if (array_space.length > 2)
			ds_sec = array_space[2];
		
		String param_unit = target_schema.getServer().getParamUnit(true);
		param_unit = TextProcessor.replaceStr(param_unit, "(", "");
		param_unit = TextProcessor.replaceStr(param_unit, ")", "");
		
		String [] array_unit = param_unit.split(",");
		String ds_unit = array_unit[0];
		String ds_ucount = "1";
		if (array_unit.length > 1)
			ds_ucount = array_unit[1];

		global_parm[29] = new String[] {"#DS_SPACE#", ds_space};
		global_parm[30] = new String[] {"#DS_PRI#", ds_pri};
		global_parm[31] = new String[] {"#DS_SEC#", ds_sec};
		global_parm[32] = new String[] {"#DS_UNIT#", ds_unit};
		global_parm[33] = new String[] {"#DS_UCOUNT#", ds_ucount};
		
		// build NETRC dataset name - v6.2
		String netrc_dataset_name = target_schema.getDatasetPrefix(true) 
				+ "." + process_id + ".NETRC";
		global_parm[34] = new String[] {"#DS_NETRC_NAME#", netrc_dataset_name};
		
		/* REMOVED in v6.2
		// build FTP_GET_CMD - v6.1.2
		String ftp_get_cmd = "GET '" + source_dataset_name + ".SYSREC'";
		for (int i = ftp_get_cmd.length(); i < 71; i++)
			ftp_get_cmd += " ";
			
		ftp_get_cmd += "+\n   '" + target_dataset_name + ".SYSREC'";					// v6.1.2
			
		global_parm[27] = new String[] {"#FTP_GET_CMD#", ftp_get_cmd};
		*/

		// generate output JCL
		TextProcessor.generateJcl(data_tmp_folder_name + IFileNames.OUTPUT_LOAD_NAME, 
				global_parm, target_schema.getServer().getJclHeaderBatch(), 
				target_schema.getJclSettingsBatch(), jcl_allo, jcl_main1, jcl_main2, jcl_cntl);	// v6.1.2

		// submit job
		log.info("Submitting Load Job");
		target_schema.getServer().submitJob(
				data_tmp_folder_name + IFileNames.OUTPUT_LOAD_NAME);

		// end
		log.info("Load Job " + job_name + " Submitted");

		return return_code;
	}
	
	// run query - v6.1
	@SuppressWarnings("unused")
	private int runQuery(String sqlStmts) throws Exception {
		// run SQL
		int return_code = 0;
		
		// method start
		log.info("Generating DB2 Query JCL");

		// define the output dataset name
		String output_dataset_name = source_schema.getDatasetPrefix(true) + "."
				+ process_id + ".QRYOUT";
		method_use1 = output_dataset_name;
		
		// wrap the sql statements - v6.1.2
		String sql_stmts = TextProcessor.wrapText(sqlStmts, 70);

		// generate template
		String jcl_allo = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL));			// v6.1.2
		String jcl_main = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_QUERY_DB));
		String jcl_cntl = TextProcessor.readFile(data_tpl_folder_name
				+ Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL));

		// generate parameter
		String[][] global_parm = new String[15][2];
		String job_name = IJclDefaults.JOBNAME_PREFIX_QUERY + jobname_suffix;

		global_parm[0] = new String[] {"#PROCESS_ID#", process_id};
		global_parm[1] = new String[] {"#JOB_NAME#", job_name};
		global_parm[2] = new String[] {"#BATCH_ID#", source_schema.getJobBatchId(true)};
		global_parm[3] = new String[] {"#USER_ID#", source_schema.getServer().getLogonUser()};
		global_parm[4] = new String[] {"#CNTL_LIBRARY#", 
				source_schema.getDatasetPrefix(true) + "."
				+ source_schema.getServer().getLogonUser() + "."
				+ process_id};
		global_parm[5] = new String[] {"#CNTL_MEMBER#", "QUERYDB"};
		global_parm[6] = new String[] {"#DB2LOAD#", source_schema.getServer().getParamDb2load(true)};	// v6.1.1
		global_parm[7] = new String[] {"#DSNLOAD#", source_schema.getServer().getParamDsnload(true)};	// v6.1.1
		global_parm[8] = new String[] {"#SORTLIB#", source_schema.getServer().getParamSortlib(true)};	// v6.1.1
		global_parm[9] = new String[] {"#SPACE#", source_schema.getServer().getParamSpace(true)};		// v6.1.1
		global_parm[10] = new String[] {"#UNIT#", source_schema.getServer().getParamUnit(true)};		// v6.1.1
		global_parm[11] = new String[] {"#DB_SSID#", source_schema.getServer().getDbSsid()};
		global_parm[12] = new String[] {"#DB_SCHEMA#", source_schema.getSchemaName()};
		global_parm[13] = new String[] {"#DATASET_OUTPUT#", output_dataset_name};
		global_parm[14] = new String[] {"#SQL_STMTS#", sql_stmts};

		// generate output JCL
		TextProcessor.generateJcl(data_tmp_folder_name + IFileNames.OUTPUT_QUERY_NAME, 
				global_parm, source_schema.getServer().getJclHeaderBatch(),
				source_schema.getJclSettingsBatch(), jcl_allo, jcl_main, jcl_cntl);		// v6.1.2

		// submit job
		log.info("Submitting Query Job");
		source_schema.getServer().submitJob(
				data_tmp_folder_name + IFileNames.OUTPUT_QUERY_NAME);

		// end
		log.info("Query Job " + job_name + " Submitted");

		return return_code;
	}
	
	// finish and clean up the temp files
	@SuppressWarnings("unused")
	private int finishProcess(String tableName) throws Exception {
		DB2Table table = new DB2Table(tableName, target_schema);
		table.setMigComplete();
		
		return finishProcess();
	}

	// finish and clean up the temp files
	private int finishProcess() throws Exception {
		// delete temp folder
		File temp_folder_file = new File(data_tmp_folder_name);

		log.info("Deleting Temporary Folder");

		if (!temp_folder_file.exists()) {
			return 4;
		} else {
			String[] temp_file_list = temp_folder_file.list();
			for (int i = 0; i < temp_file_list.length; i++) {
				File del_file = new File(data_tmp_folder_name
						+ temp_file_list[i]);
				if (del_file != null) {
					del_file.delete();
				}
			}
			temp_folder_file.delete();
		}

		// end
		log.info("Temporary Folder " + data_tmp_folder_name + " Deleted");
		return 0;
	}
	
	// read unload file
	@SuppressWarnings("unused")
	private int readUnloadDataset(String tableName) {
		int return_code = 0;
		
		try {
			String local_file_name = data_tmp_folder_name + tableName.toLowerCase() + ".csv";
			
			String source_dataset_name = "'" + source_schema.getDatasetPrefix(true) + "."			// v6.0.1
					+ source_schema.getTable(tableName).getShortTsName(process_id) + ".SYSREC'";	// v6.0.0
	
			source_schema.getServer().downloadFile(local_file_name, source_dataset_name);
			
			method_use1 = TextProcessor.readFile(local_file_name);
		} catch (Exception ex) {
			ex.printStackTrace();
			return_code = 8;
		}
		
		return return_code;
	}
	
	// read query results - v6.0.1
	@SuppressWarnings("unused")
	private int readQueryResults() {
		int return_code = 0;
		
		try {
			String local_file_name = data_tmp_folder_name + process_id.toLowerCase() + ".sql";
			
			String source_dataset_name = "'" + source_schema.getDatasetPrefix(true) + "."
					+ process_id + ".QRYOUT'";
	
			source_schema.getServer().downloadFile(local_file_name, source_dataset_name);
			
			method_use1 = TextProcessor.readFile(local_file_name);
		} catch (Exception ex) {
			ex.printStackTrace();
			return_code = 8;
		}
		
		return return_code;
	}

}
