/**
 *  Class: objects.DB2Table
 *  Description: This is to store and operate the DB2 Tables.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/20/2011: Code baseline. (V6.0 phase 3)
 *  02. 03/21/2011: Move the owner from MFServer to DB2Schema. (V6.0 phase 4)
 *  03. 04/04/2011: Use DmigHsqlConn to replace the new HsqlConn object to save the Hsql sessions. (V6.0 beta 1)
 *  04. 03/02/2012: Add getShortTsName() function. (V6.0.0)
 *  05. 08/06/2013: Add new functions to support column, reference and migration data. (V6.3)
 */
package net.sourceforge.datamig4zos.objects;

import java.sql.ResultSet;

import net.sourceforge.datamig4zos.runtime.DmigHsqlConn;
import net.sourceforge.datamig4zos.util.TextProcessor;


/**
 * @author SunPC
 *
 */
public class DB2Table {
	
	private DB2Schema owner_schema;
	
	private String table_name = "";
	private String db_name = "";
	private String ts_name = "";
	
	private String[] array_column_names = new String[0];		// v6.3
	private String[] array_ref_table_names = new String[0];		// v6.3
	
	public DB2Table(String tableName, DB2Schema ownerSchema) throws Exception {
		owner_schema = ownerSchema;
		table_name = tableName;
		
		ResultSet rs;
		
		// get DB_NAME and TS_NAME
		rs = DmigHsqlConn.getConn().query("select DB_NAME, TS_NAME from DB2TABLE where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		
		if (rs.next()) {
			if (db_name.equals(""))
				db_name = rs.getString("DB_NAME");
			if (ts_name.equals(""))
				ts_name = rs.getString("TS_NAME");
		}

		rs.close();
		
		// get COLUMN_NAME - v6.3
		rs = DmigHsqlConn.getConn().query("select count(*) from DB2COLUMN where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		rs.next();
		int column_size = rs.getInt(1);
		rs.close();

		array_column_names = new String[column_size];	
		
		rs = DmigHsqlConn.getConn().query("select COLUMN_NAME from DB2COLUMN where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		
		int row_id = 0;	
		
		while (rs.next()) {
			array_column_names[row_id] = rs.getString("COLUMN_NAME");
			row_id++;
		}

		rs.close();
		
		// get REF_TABLE_NAME - v6.3
		rs = DmigHsqlConn.getConn().query("select count(*) from DB2TBREF where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		rs.next();
		int ref_size = rs.getInt(1);
		rs.close();

		array_ref_table_names = new String[ref_size];	
		
		rs = DmigHsqlConn.getConn().query("select REF_TABLE_NAME from DB2TBREF where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		
		int ref_id = 0;	
		
		while (rs.next()) {
			array_ref_table_names[ref_id] = rs.getString("REF_TABLE_NAME");
			ref_id++;
		}

		rs.close();
		
	}

	public DB2Table(String tableName, String dbName, String tsName, 
			String[] columnNames, String[] refTableNames, 
			DB2Schema ownerSchema) throws Exception {			// v6.3
		owner_schema = ownerSchema;
		table_name = tableName;
		db_name = dbName;
		ts_name = tsName;
		
		setColumnNames(columnNames);		// v6.3
		setRefTableNames(refTableNames);	// v6.3
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof DB2Table) {
			String temp_table_name = ((DB2Table) object).table_name;
			DB2Schema temp_owner_schema = ((DB2Table) object).owner_schema;

			if (table_name.equals(temp_table_name)
					&& owner_schema.equals(temp_owner_schema)) {
				return true;
			}
		}

		return false;
	}
	
	// insert a table into HSql
	public void insertTableIntoHsql() throws Exception {
		// insert DB2TABLE
		DmigHsqlConn.getConn().execute("insert into DB2TABLE values ('" + owner_schema.getServer().getServerName()
				+ "','" + owner_schema.getSchemaName() + "','" + table_name + "','"
				+ db_name + "','" + ts_name + "')");
		
		// insert DB2COLUMN - v6.3
		for (int i = 0; i < array_column_names.length; i++) {
			DmigHsqlConn.getConn().execute("insert into DB2COLUMN values ('" + owner_schema.getServer().getServerName()
					+ "','" + owner_schema.getSchemaName() + "','" + table_name + "','"
					+ array_column_names[i] + "')");
		}
		
		// insert DB2TBREF - v6.3
		for (int i = 0; i < array_ref_table_names.length; i++) {
			DmigHsqlConn.getConn().execute("insert into DB2TBREF values ('" + owner_schema.getServer().getServerName()
					+ "','" + owner_schema.getSchemaName() + "','" + table_name + "','"
					+ array_ref_table_names[i] + "')");
		}
	}
	
	// delete a table from HSql
	public void deleteTableFromHsql() throws Exception {
		DmigHsqlConn.getConn().execute("delete from DB2TABLE where SERVER_NAME = '" + owner_schema.getServer().getServerName()
				+ "' and SCHEMA_NAME = '" + owner_schema.getSchemaName() 
				+ "' and TABLE_NAME = '" + table_name + "'");
	}

	public DB2Schema getSchema() {
		return owner_schema;
	}

	public void setSchema(DB2Schema ownerSchema) {
		owner_schema = ownerSchema;
	}

	public String getTableName() {
		return table_name;
	}

	public void setTableName(String tableName) {
		table_name = tableName;
	}

	public String getDbName() {
		return db_name;
	}

	public void setDbName(String dbName) {
		db_name = dbName;
	}

	public String getTsName() {
		return ts_name;
	}

	public String getShortTsName(String process_id) {	// v6.0.0
		if (ts_name.equals("")) {
			String tmp_ts_name = "";
			try {
				String tmp_table_name = TextProcessor.replaceStr(table_name, "_", "$");
				if (tmp_table_name.length() > 8) {
					tmp_ts_name = tmp_table_name.substring(0, 8);
				} else {
					tmp_ts_name = tmp_table_name;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return tmp_ts_name;
		} else if (ts_name.length() > 8) {
			return ts_name.substring(0, 6) + process_id.substring(6, 8);
		} else {
			return ts_name;
		}
	}

	public void setTsName(String tsName) {
		ts_name = tsName;
	}

	public String getMigStatus() throws Exception {		// v6.3
		String mig_status = "";
		
		ResultSet rs = DmigHsqlConn.getConn().query("select MIG_STATUS from DB2TBMIG where SERVER_NAME = '"
				+ owner_schema.getServer().getServerName() + "' and SCHEMA_NAME = '"
				+ owner_schema.getSchemaName() + "' and TABLE_NAME = '"
				+ table_name + "'");
		
		if (rs.next()) {
			mig_status = rs.getString("MIG_STATUS");
		}

		rs.close();
		
		return mig_status;
	}
	
	public void setMigStatus(String migStatus) throws Exception {		// v6.3
		String mig_status = migStatus;
		
		if (mig_status.equals("S") || mig_status.equals("C") || mig_status.equals("E")) {
			DmigHsqlConn.getConn().execute("delete from DB2TBMIG where SERVER_NAME = '" + owner_schema.getServer().getServerName()
					+ "' and SCHEMA_NAME = '" + owner_schema.getSchemaName() 
					+ "' and TABLE_NAME = '" + table_name + "'");
		}
		
		if (mig_status.equals("S") || mig_status.equals("E")) {
			DmigHsqlConn.getConn().execute("insert into DB2TBMIG values ('" + owner_schema.getServer().getServerName()
					+ "','" + owner_schema.getSchemaName() + "','" + table_name + "','"
					+ mig_status + "')");
		}
	}
	
	public void setMigStart() throws Exception {		// v6.3
		setMigStatus("S");
	}
	
	public void setMigComplete() throws Exception {		// v6.3
		setMigStatus("C");
	}
	
	public void setMigError() throws Exception {		// v6.3
		setMigStatus("E");
	}

	public String[] getColumnNames() {		// v6.3
		return array_column_names;
	}

	public void setColumnNames(String[] columnNames) {			// v6.3
		array_column_names = new String[columnNames.length];
		
		for (int i = 0; i < array_column_names.length; i++) {
			array_column_names[i] = columnNames[i];
		}
	}

	public String[] getRefTableNames() {	// v6.3
		return array_ref_table_names;
	}

	public void setRefTableNames(String[] refTableNames) {		// v6.3
		array_ref_table_names = new String[refTableNames.length];
		
		for (int i = 0; i < array_ref_table_names.length; i++) {
			array_ref_table_names[i] = refTableNames[i];
		}
	}
	
}
