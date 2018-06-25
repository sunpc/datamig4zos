/**
 *  Class: util.HsqlConn
 *  Description: Connect to HSQL database
 *
 *  Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 06/14/2010: Initial version. (V6.0 phase 1)
 *  2. 03/19/2011: Use IHsqlDb to pass in the file name and user. (V6.0 phase 2)
 */
package net.sourceforge.datamig4zos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.datamig4zos.runtime.IHsqlDb;


public class HsqlConn {

	Connection conn; // our connnection to the db - presist for life of program

	// we dont want this garbage collected until we are done
	public HsqlConn() throws Exception { // note more general exception

		// Load the HSQL Database Engine JDBC driver
		// hsqldb.jar should be in the class path or made part of the current
		// jar
		Class.forName("org.hsqldb.jdbcDriver");

		// connect to the database. This will load the db files and start the
		// database if it is not alread running.
		// db_file_name_prefix is used to open or create files that hold the
		// state
		// of the db.
		// It can contain directory names relative to the
		// current working directory
		conn = DriverManager.getConnection("jdbc:hsqldb:" + IHsqlDb.FILE_NAME,
				IHsqlDb.USER_NAME, IHsqlDb.PASS_WORD);			// v6.0.phase2
		// System.out.println(System.getProperty("user.dir"));

	}

	public void shutdown() throws SQLException {

		Statement st = conn.createStatement();

		// db writes out to files and performs clean shuts down
		// otherwise there will be an unclean shutdown
		// when program ends
		st.execute("SHUTDOWN");
		conn.close(); // if there are no other open connection
	}

	// use for SQL command SELECT
	public synchronized ResultSet query(String expression) throws SQLException {

		Statement st = null;
		ResultSet rs = null;
		ResultSet rtn = null;

		st = conn.createStatement(); // statement objects can be reused with

		// repeated calls to execute but we
		// choose to make a new one each time
		rs = st.executeQuery(expression); // run the query

		// do something with the result set.
		rtn = rs;

		st.close(); // NOTE!! if you close a statement the associated ResultSet
					// is
		// closed too
		// so you should copy the contents to some other object.
		// the result set is invalidated also if you recycle an Statement
		// and try to execute some other query before the result set has been
		// completely examined.

		return rtn;
	}

	// use for SQL commands CREATE, DROP, INSERT and UPDATE
	public synchronized boolean execute(String expression) throws SQLException {

		Statement st = null;

		st = conn.createStatement(); // statements

		int i = st.executeUpdate(expression); // run the query

		if (i == -1) {
			return false;
		}

		st.close();

		return true;
	}

	public static void dump(ResultSet rs) throws SQLException {

		// the order of the rows in a cursor
		// are implementation dependent unless you use the SQL ORDER statement
		ResultSetMetaData meta = rs.getMetaData();
		int colmax = meta.getColumnCount();
		int i;
		Object o = null;

		// the result set is a cursor into the data. You can only
		// point to one row at a time
		// assume we are pointing to BEFORE the first row
		// rs.next() points to next row and returns true
		// or false if there is no next row, which breaks the loop
		for (; rs.next();) {
			for (i = 0; i < colmax; ++i) {
				o = rs.getObject(i + 1); // Is SQL the first column is indexed

				// with 1 not 0
				System.out.print(o.toString() + " ");
			}

			System.out.println(" ");
		}
	} // void dump( ResultSet rs )

} // class Testdb