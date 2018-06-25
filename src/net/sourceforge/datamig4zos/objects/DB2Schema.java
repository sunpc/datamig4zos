/**
 *  Class: objects.DB2Schema
 *  Description: This is to store and operate the DB2 schemas.
 * 
 * 	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 03/21/2011: Code baseline. (V6.0 phase 4)
 *  Code mainly copied from the original MFServer.class.
 *  02. 03/24/2011: DB2Table setup code defect fixes. (V6.0 phase 5)
 * 		1) Re-write the setTables(DB2Table[]) method to retain the existing tables.
 * 		2) Re-write the updateTable(DB2Table) method to update the variables only instead of replacing the entire object.
 * 	03. 03/26/2011: Sort the tables by table names when selecting the Hsql db. (V6.0 phase 6)
 *  04. 04/04/2011: Use DmigHsqlConn to replace the new HsqlConn object to save the Hsql sessions. (V6.0 beta 1)
 *  05. 02/29/2012: Add JCL Settings. (V6.0.0)
 *  06. 11/02/2012: Enhance methods getJobBatchId() and getDatasetPrefix(). (V6.0.1)
 *  07. 11/07/2012: Enhance methods getJobBatchId() and getDatasetPrefix(). (V6.1.1)
 *  08. 08/06/2013: Support changed DB2Table object. (V6.3)
 *  	1) Fix setTables() to use another DB2Table constructor.
 *  	2) Change updateTable() to refresh columns and references.
 */
package net.sourceforge.datamig4zos.objects;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.datamig4zos.runtime.DmigHsqlConn;
import net.sourceforge.datamig4zos.util.TextProcessor;

import com.enterprisedt.util.debug.Level;
import com.enterprisedt.util.debug.Logger;

/**
 * @author SunPC
 *
 */
public class DB2Schema {

	private MFServer owner_server;
	
	private String schema_name = "";
	private String job_batch_id = "";
	private String dataset_prefix = "";
	private String jcl_settings = "";	// v6.0.0
	
	private DB2Table[] tables = new DB2Table[0];
	
	private Logger log = Logger.getLogger(DB2Schema.class);

	// constructor 1 - for existing schema
	public DB2Schema(String schemaName, MFServer ownerServer) throws Exception {
		owner_server = ownerServer;
		schema_name = schemaName;
		
		Logger.setLevel(Level.INFO);

		// run SQL to get the server detail
		ResultSet rs = DmigHsqlConn.getConn().query("select * from DB2SCHEMA where SERVER_NAME = '"
						+ owner_server.getServerName() + "' and SCHEMA_NAME = '"
						+ schema_name + "'");

		if (rs.next()) {
			if (job_batch_id.equals(""))
				job_batch_id = rs.getString("JOB_BATCH_ID");
			if (dataset_prefix.equals(""))
				dataset_prefix = rs.getString("DATASET_PREFIX");
			if (jcl_settings.equals(""))		// v6.0.0
				jcl_settings = rs.getString("JCL_SETTINGS");
		}

		rs.close();
	}
	
	// constructor 2 - for new schema without JCL settings
	public DB2Schema(String schemaName, String jobBatchId,
			String datasetPrefix, MFServer ownerServer)
			throws Exception {
		owner_server = ownerServer;
		schema_name = schemaName;
		job_batch_id = jobBatchId;
		dataset_prefix = datasetPrefix;

		Logger.setLevel(Level.INFO);
	}
	
	// constructor 3 - for new schema with JCL settings - v6.0.0
	public DB2Schema(String schemaName, String jobBatchId,
			String datasetPrefix, String jclSettings, MFServer ownerServer)
			throws Exception {
		owner_server = ownerServer;
		schema_name = schemaName;
		job_batch_id = jobBatchId;
		dataset_prefix = datasetPrefix;
		jcl_settings = jclSettings;

		Logger.setLevel(Level.INFO);
	}

	// delete schema from hsql
	public void deleteSchemaFromHsql() throws Exception {
		log.info("Deleting the schema entry for schema " + schema_name);
		DmigHsqlConn.getConn().execute("delete from DB2SCHEMA where SERVER_NAME = '"
				+ owner_server.getServerName() + "' and SCHEMA_NAME = '"
				+ schema_name + "'");
		
		log.info("Schema " + schema_name + " deleted successfully");
	}
	
	// display all the variables
	public void displaySchemaDetail() throws Exception {
		log.info("Displaying Schema Details");

		System.out.println("SCHEMA_NAME     = " + schema_name);
		System.out.println("JOB_BATCH_ID    = " + job_batch_id);
		System.out.println("DATASET_PREFIX  = " + dataset_prefix);
	}
	

	@Override
	public boolean equals(Object object) {
		if (object instanceof DB2Schema) {
			String temp_schema_name = ((DB2Schema) object).schema_name;
			MFServer temp_owner_server = ((DB2Schema) object).owner_server;

			if (schema_name.equals(temp_schema_name)
					&& owner_server.equals(temp_owner_server)) {
				return true;
			}
		}

		return false;
	}
	
	// insert schema info into hsql
	public void insertSchemaIntoHsql() throws Exception {
		// create new server entry
		log.info("Saving schema " + schema_name);
		DmigHsqlConn.getConn().execute("insert into DB2SCHEMA values ('" + owner_server.getServerName()
				+ "','" + schema_name + "','" + job_batch_id
				+ "','" + dataset_prefix + "', '" 
				+ TextProcessor.replaceStr(jcl_settings, "'", "''")		// v6.0.0
				+ "')");

		// create new table list
		log.info("Saving new table list");
		for (int i = 0; i < tables.length; i++) {
			tables[i].insertTableIntoHsql();
		}
		
		log.info("Schema saved successfully");
	}
	
	// add a new table into the list - v6.0.phase2
	public void addTable(DB2Table table) {
		List<DB2Table> list = new ArrayList<DB2Table>(Arrays.asList(tables));
		table.setSchema(this);			// v6.0.phase5
		list.add(table);
		tables = list.toArray(new DB2Table[tables.length+1]);
	}
	
	// update a table in the list - v6.0.phase3
	public void updateTable(DB2Table newTable) {
		// search the table name
		for(int i=0; i<tables.length; i++) {
			if(tables[i].equals(newTable)) {
				tables[i].setDbName(newTable.getDbName());
				tables[i].setTsName(newTable.getTsName());
				tables[i].setColumnNames(newTable.getColumnNames());		// v6.3
				tables[i].setRefTableNames(newTable.getRefTableNames());	// v6.3
			}
		}
	}
	
	// delete a table from the list - v6.0.phase2
	public void removeTable(DB2Table table) {
		// search the table name
		for(int i=0; i<tables.length; i++) {
			if(tables[i].equals(table)) {
				List<DB2Table> list = new ArrayList<DB2Table>(Arrays.asList(tables));
				table.setSchema(null);			// v6.0.phase5
				list.remove(i);
				tables = list.toArray(new DB2Table[tables.length-1]);
			}
		}
	}

	public MFServer getServer() {
		return owner_server;
	}

	public void setServer(MFServer ownerServer) {
		owner_server = ownerServer;
	}

	public String getSchemaName() {
		return schema_name;
	}

	public void setSchemaName(String schemaName) {
		schema_name = schemaName;
	}

	public String getJobBatchId() throws Exception {								// v6.0.1
		return getJobBatchId(false);
	}

	public String getJobBatchId(boolean resolveUserId) throws Exception {			// v6.0.1
		if (resolveUserId) {
			if (job_batch_id.isEmpty()) {											// v6.1.1
				return owner_server.getLogonUser();
			} else {
				return TextProcessor.replaceStr(job_batch_id, "<USERID>", owner_server.getLogonUser());
			}
		} else {
			return job_batch_id;
		}
	}

	public void setJobBatchId(String jobBatchId) {
		job_batch_id = jobBatchId;
	}
	
	public String getDatasetPrefix() throws Exception {								// v6.0.1
		return getDatasetPrefix(false);
	}

	public String getDatasetPrefix(boolean resolveUserId) throws Exception {		// v6.0.1
		if (resolveUserId) {
			if (dataset_prefix.isEmpty()) {											// v6.1.1
				return owner_server.getLogonUser();
			} else {
				return TextProcessor.replaceStr(dataset_prefix, "<USERID>", owner_server.getLogonUser());
			}
		} else {
			return dataset_prefix;
		}
	}

	public void setDatasetPrefix(String datasetPrefix) {
		dataset_prefix = datasetPrefix;
	}

	public String getJclSettingsBatch() {				// v6.0.0
		String [] jcl_array = jcl_settings.split("\n");
		String tmp_jcl_settings = "";
		
		for (int i = 0; i < jcl_array.length; i++) {
			String row = jcl_array[i].toUpperCase().trim();
			
			// if not a jcl row, skip it
			if (!row.startsWith("//")) {
				continue;
			}
			
			// add the row to tmp_jcl_settings
			if (tmp_jcl_settings.length() > 0) {
				tmp_jcl_settings += "\n";
			}
			tmp_jcl_settings += row;
			
		}
		
		return tmp_jcl_settings;
	}

	public String getJclSettingsDisplay() {				// v6.0.0
		return jcl_settings;
	}

	public void setJclSettings(String jclSettings) {	// v6.0.0
		jcl_settings = jclSettings;
	}
	
	// get a table - v6.0.phase3
	public DB2Table getTable(String tableName) throws Exception {
		DB2Table table = new DB2Table(tableName, this);
		
		for (int i = 0; tables != null && i < tables.length; i++) {
			if (tables[i].getTableName().equals(tableName)) {
				table = tables[i];
				break;
			}
		}
		
		return table;
	}

	public DB2Table[] getTables() {
		return tables;
	}

	public void setTables() throws Exception {
		// run SQL to get the table list count and re-define the table list
		ResultSet rs = DmigHsqlConn.getConn().query("select count(*) from DB2TABLE where SERVER_NAME = '"
						+ owner_server.getServerName() + "' and SCHEMA_NAME = '"
						+ schema_name + "'");
		rs.next();
		int table_size = rs.getInt(1);
		rs.close();

		// initialize a new MFServer array
		DB2Table[] new_tables = new DB2Table[table_size];
		//new_tables = new DB2Table[table_size];

		int row_id = 0;

		// run SQL to get the table list
		rs = DmigHsqlConn.getConn().query("select * from DB2TABLE where SERVER_NAME = '"
				+ owner_server.getServerName() + "' and SCHEMA_NAME = '"
				+ schema_name	+ "' order by TABLE_NAME");	// v6.0.phase6

		// go thru the resultset one by one
		while (rs.next()) {
			new_tables[row_id] = new DB2Table(rs.getString("TABLE_NAME"), this);	// v6.3
			row_id++;
		}

		rs.close();

		// set tables
		setTables(new_tables);
	}

	public void setTables(DB2Table[] newTables) {
		// do a comparison between the existing tables and new tables,
		// to refresh the tables from the parameter - v6.0.phase5
		if (tables != null) {
			// remove the existing unmatched tables
			for (int i=0; i<tables.length; i++) {
				boolean is_table_matching = false;
				for (int j=0; j<newTables.length; j++) {
					if (tables[i].equals(newTables[j])) {
						// if a match can be found, update it
						is_table_matching = true;
						updateTable(newTables[j]);
					}
				}
				// if a match can not be found
				if (!is_table_matching) {
					removeTable(tables[i]);
				}
			}
			
			// add the new unmatched tables
			for (int i=0; i<newTables.length; i++) {
				boolean is_table_matching = false;
				for (int j=0; j<tables.length; j++) {
					if (newTables[i].equals(tables[j])) {
						// if a match can be found
						is_table_matching = true;
					}
				}
				// if a match can not be found
				if (!is_table_matching) {
					addTable(newTables[i]);
				}
			}
		} else {
			tables = newTables;
			
			for(int i=0; i<tables.length; i++) {
				tables[i].setSchema(this);
			}
		}
	}
}
