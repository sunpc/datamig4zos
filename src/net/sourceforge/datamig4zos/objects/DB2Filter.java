/**
 *  Class: objects.DB2Filter
 *  Description: This is to store and operate the DB2 Filters.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 08/19/2013: Code baseline. (V6.3)
 */
package net.sourceforge.datamig4zos.objects;

import java.sql.ResultSet;

import net.sourceforge.datamig4zos.runtime.DmigHsqlConn;
import net.sourceforge.datamig4zos.util.TextProcessor;


/**
 * @author SunPC
 *
 */
public class DB2Filter {
	
	private String filter_name = "";
	private String[][] filter_data = new String[20][3];
	
	public DB2Filter(String filterName) throws Exception {
		filter_name = filterName;
		
		ResultSet rs;
		
		// get filter data
		rs = DmigHsqlConn.getConn().query(
				"select SELECT_FIELD, SELECT_OPER, SELECT_DATA from DB2FILTER "
						+ "where FILTER_NAME = '" + filter_name
						+ "' ORDER BY SELECT_SEQ");
		
		int row_id = 0;
		
		while (rs.next()) {
			filter_data[row_id][0] = rs.getString("SELECT_FIELD");
			filter_data[row_id][1] = rs.getString("SELECT_OPER");
			filter_data[row_id][2] = rs.getString("SELECT_DATA");
			row_id++;
		}
		
		rs.close();
	}

	public DB2Filter(String filterName, String[][] filterData) throws Exception {
		filter_name = filterName;
		
		for (int i = 0; i < filterData.length; i++) {
			for (int j = 0; j < filterData[i].length; j++) {
				filter_data[i][j] = filterData[i][j];
			}
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof DB2Filter) {
			String temp_filter_name = ((DB2Filter) object).filter_name;

			if (filter_name.equals(temp_filter_name)) {
				return true;
			}
		}

		return false;
	}
	
	// insert a filter into HSql
	public void saveFilterIntoHsql() throws Exception {
		// delete DB2FILTER
		DmigHsqlConn.getConn().execute("delete from DB2FILTER " +
				"where FILTER_NAME = '" + filter_name + "'");
		
		// insert DB2FILTER
		for (int i = 0; i < filter_data.length; i++) {
			if (!filter_data[i][0].trim().isEmpty()
					&& !filter_data[i][1].trim().isEmpty()
					&& !filter_data[i][2].trim().isEmpty())
				DmigHsqlConn.getConn().execute(
						"insert into DB2FILTER values ('"
								+ filter_name
								+ "',"
								+ i
								+ ",'"
								+ filter_data[i][0].toUpperCase().trim()
								+ "','"
								+ filter_data[i][1].toUpperCase().trim()
								+ "','"
								+ TextProcessor.replaceStr(filter_data[i][2].trim(), "'", "''") 
								+ "')");
		}
	}
	
	public String getFilterName() {
		return filter_name;
	}
	
	public String[][] getFilterData() {		// TODO: debug
		return filter_data;
	}

	public String getSqlClause(String serverName, String schemaName, String tableName) throws Exception {
		String sql_stmt = "";
		
		ResultSet rs = DmigHsqlConn.getConn().query(
				"select SELECT_FIELD, SELECT_OPER, SELECT_DATA "
						+ "from DB2COLUMN A, DB2FILTER B "
						+ "where A.COLUMN_NAME = B.SELECT_FIELD "
						+ "and A.SERVER_NAME = '" + serverName + "' "
						+ "and A.SCHEMA_NAME = '" + schemaName + "' "
						+ "and A.TABLE_NAME = '" + tableName + "' "
						+ "and B.FILTER_NAME = '" + filter_name + "' "
						+ "order by SELECT_SEQ");
		
		while (rs.next()) {
			if (!rs.getString("SELECT_FIELD").trim().isEmpty()
					&& !rs.getString("SELECT_OPER").trim().isEmpty()
					&& !rs.getString("SELECT_DATA").trim().isEmpty()) {

				if (!sql_stmt.isEmpty()) {
					sql_stmt += " AND ";
				}
				
				sql_stmt += rs.getString("SELECT_FIELD") + " "
						+ rs.getString("SELECT_OPER") + " "
						+ rs.getString("SELECT_DATA");
			}
		}

		rs.close();
		
		return sql_stmt;
	}
	
	// get all filters
	public static String[] getAllFilterNames() throws Exception {
		// get total server count
		ResultSet rs = DmigHsqlConn.getConn().query(
				"select count(distinct FILTER_NAME) from DB2FILTER");
		rs.next();
		int filter_size = rs.getInt(1);
		rs.close();

		// define the return array
		String[] new_filters = new String[filter_size];
		int row_id = 0;

		// retrieve the server name for each row
		rs = DmigHsqlConn.getConn().query(
				"select distinct FILTER_NAME from DB2FILTER order by FILTER_NAME");

		while (rs.next()) {
			new_filters[row_id] = rs.getString("FILTER_NAME");
			row_id++;
		}

		rs.close();

		// return
		return new_filters;
	}
	
}
