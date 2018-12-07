/**
 *  Class: objects.MFServer
 *  Description: This is to store and operate the server.
 * 
 * 	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 02/18/2009: Code baseline. (V5.0)
 *  02. 04/18/2009: Fixed Tracker 2751960 - (V5.0 RC2)
 *  	Problem: target FTP dies when source activities taking too much time. 
 *  	Solution: use dir command to keep the connection on both servers.
 *  03. 04/24/2009: Fixed Tracker 2776870 - (V5.0 Final)
 *  	Improve open() to have it be able to handle invalid server config files.
 *  04. 05/06/2009: Security Patch. Encrypt user details on the server config file. (V5.1)
 *  05. 06/18/2010: Misc changes. (V6.0 phase 1) 
 *  06. 03/05/2011: Class re-design. (V6.0 phase 1)
 *  	1) New methods - saveServer, deleteServer.
 *  	2) New constructor method for new created server.
 *  	3) Change all method names.
 *  	4) Merge open() into the existing constructor method.
 *  07. 03/09/2011: Display encrypted passcode only in the displayServerDetail() method. (V6.0 phase 1)
 *  08. 03/10/2011: Add a new logic in getTablespaceName() to return the table name if tablespace name is not found. (V6.0 phase 1)
 *  09. 03/11/2011: Change pass-in parameters to standard. (V6.0 phase 1) 
 *  10. 03/12/2011: Add a new getAllServers() method. (V6.0 phase 2)
 *  11. 03/18/2011: Add new methods addNewTable() and removeTable(). (V6.0 phase 2)
 *  12. 03/19/2011: Add new method isTableExist(). (V6.0 phase 2)
 *  13. 03/19/2011: Return an empty string array instead of null in getAllServers() methods to avoid null errors on UI. (V6.0 phase 2)
 *  14. 03/20/2011: MFServer object-oriented functional enhancements. (V6.0 phase 3)
 *  	1) Rename saveServer() to addNewServer() and remove the invoke of deleteServer() in it.
 *  	2) Rename the class to MFServer.class.
 *  	3) Use MFServerType and DB2Table objects instead of the Strings.
 *  	4) Move getAllServers() function to MFServerType.class.
 *  	5) Change all public variables to private with getters and setters.
 *  	6) Use getTable() to replace isTableExist(), getDatabaseName() and getTablespaceName().
 *  	7) Move table set function from the constructor into setTables().
 *  15. 03/21/2011: MFServer object-oriented functional enhancements. (V6.0 phase 4)
 *  	1) Remove the parental MFServerType object.
 *  	2) Replace DB2Schema objects as children instead of DB2Table.
 *  16. 03/23/2011: Add a new constructor with no parameter to create a dummy server. (V6.0 phase 5)
 *  17. 03/24/2011: DB2Schema setup code defect fixes. (V6.0 phase 5)
 *  	1) Re-write the setSchemas(DB2Schema[]) method to retain the existing schemas.
 *  	2) Re-write the updateSchema(DB2Schema) method to update the variables only instead of replacing the entire object.
 *  18. 03/26/2011: Sort the servers and schemas by their names when selecting the Hsql db. (V6.0 phase 6)
 *  19. 04/04/2011: Use DmigHsqlConn to replace the new HsqlConn object to save the Hsql sessions. (V6.0 beta 1)
 *  20. 03/01/2012: Add JCL Settings column to DB2Schema. (V6.0.0)
 *  21. 03/02/2012: Enhance the getJclHeader functions. (V6.0.0)
 *  22. 11/02/2012: Throw exceptions in updateSchema() and setSchemas(). (V6.0.1)
 *  23. 11/07/2012: Add JCL parameters. (V6.1.1)
 *  24. 11/12/2012: Fix a bug that the user specified job name doesn't work. (V6.1.3) 
 *  25. 07/30/2013: Add a parameter to uploadFile to indicate LR. (V6.2)
 *  26. 12/06/2018: Use Apache Commons Net for FTPS support. (V7.0 beta)
 *  27. 12/07/2018: Add file protocol and port number. (V7.0.0)
 */
package net.sourceforge.datamig4zos.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.runtime.DmigHsqlConn;
import net.sourceforge.datamig4zos.runtime.IJclDefaults;
import net.sourceforge.datamig4zos.ui.preferences.PreferenceConstants;
import net.sourceforge.datamig4zos.util.DesEncrypter;
import net.sourceforge.datamig4zos.util.TextProcessor;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

//import com.enterprisedt.net.ftp.FTPTransferType;
//import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.util.debug.Level;
import com.enterprisedt.util.debug.Logger;


/**
 * @author SunPC
 * 
 */
public class MFServer {
	
	private Object root;
	
	private String server_name = "";
	private String host_ip = "";
	private String logon_user = "";
	private String logon_pwd = "";
	private String db_ssid = "";
	private String jcl_header = "";
	
	private String file_protocol = "";	// v7.0.0
	private int port_number = 0;		// v7.0.0
	
	private String param_db2load = "";	// v6.1.1
	private String param_dsnload = "";	// v6.1.1
	private String param_sortlib = "";	// v6.1.1
	private String param_space = "";	// v6.1.1
	private String param_unit = "";		// v6.1.1
	
	private DB2Schema[] schemas = new DB2Schema[0];

	private Logger log = Logger.getLogger(MFServer.class);

	// FTP client - v7.0 beta
	// private FileTransferClient ftp = new FileTransferClient();
	private FTPClient ftp = null;
	private FTPSClient ftps = null;
	
	// constructor 0 - for dummy server - v6.0.phase5
	public MFServer() throws Exception {		
		root = new CNFRoot();
		Logger.setLevel(Level.INFO);
	}

	// constructor 1 - for existing server
	public MFServer(String serverName) throws Exception {
		server_name = serverName;

		Logger.setLevel(Level.INFO);

		// run SQL to get the server detail
		ResultSet rs = DmigHsqlConn.getConn().query("select * from MFSERVER where SERVER_NAME = '"
						+ server_name + "'");

		if (rs.next()) {
			if (host_ip.equals(""))
				host_ip = rs.getString("HOST_IP");
			if (logon_user.equals(""))
				logon_user = DesEncrypter.decrypt(rs.getString("LOGON_USER"));
			if (logon_pwd.equals(""))
				logon_pwd = DesEncrypter.decrypt(rs.getString("LOGON_PWD"));
			if (db_ssid.equals(""))
				db_ssid = rs.getString("DB_SSID");
			if (jcl_header.equals(""))
				jcl_header = rs.getString("JCL_HEADER");
			if (file_protocol.equals(""))	// v7.0.0
				file_protocol = rs.getString("FILE_PROTOCOL");
			if (port_number == 0)			// v7.0.0
				port_number = rs.getInt("PORT_NUMBER");
			if (param_db2load.equals(""))	// v6.1.1
				param_db2load = rs.getString("PARAM_DB2LOAD");
			if (param_dsnload.equals(""))	// v6.1.1
				param_dsnload = rs.getString("PARAM_DSNLOAD");
			if (param_sortlib.equals(""))	// v6.1.1
				param_sortlib = rs.getString("PARAM_SORTLIB");
			if (param_space.equals(""))		// v6.1.1
				param_space = rs.getString("PARAM_SPACE");
			if (param_unit.equals(""))		// v6.1.1
				param_unit = rs.getString("PARAM_UNIT");
		}

		rs.close();
	}

	// constructor 2 - for new server - v6.0.phase1
	public MFServer(String serverName, String hostIp,
			String logonUser, String logonPwd, String dbSsid, 
			String jclHeader, String fileProtocol, int portNumber, 
			String paramDb2load, String paramDsnload,
			String paramSortlib, String paramSpace, String paramUnit) throws Exception {
		server_name = serverName;
		host_ip = hostIp;
		logon_user = logonUser;
		logon_pwd = logonPwd;
		db_ssid = dbSsid;
		jcl_header = jclHeader;
		file_protocol = fileProtocol;	// v7.0.0
		port_number = portNumber;		// v7.0.0
		param_db2load = paramDb2load;	// v6.1.1
		param_dsnload = paramDsnload;	// v6.1.1
		param_sortlib = paramSortlib;	// v6.1.1
		param_space = paramSpace;		// v6.1.1
		param_unit = paramUnit;			// v6.1.1

		Logger.setLevel(Level.INFO);
	}

	// disconnect from server
	public void closeConnection() throws Exception {
		// Shut down client - v7.0 beta
		log.info("Quitting client");
		if (ftp != null && ftp.isConnected())
			ftp.disconnect();
		if (ftps != null && ftps.isConnected())
			ftps.disconnect();
		//if (conn != null)		-- no need 
		//	conn.shutdown();
		log.info("Server connection closed");
	}

	// connect to server
	public void connectServer() throws Exception {
		// create client
		// log.info("Creating FTP client");
		// ftp.setRemoteHost(host_ip);
		// ftp.setUserName(logon_user);
		// ftp.setPassword(logon_pwd);
		// ftp.setContentType(FTPTransferType.ASCII);

		// connect - v7.0 beta
		log.info("Connecting to server " + host_ip);
		try {
			// create new FTP client - v7.0.0
			if (this.file_protocol.equals("FTP")) {
				if (ftp == null)
					ftp = new FTPClient();
				// ftp.connect();
				ftp.connect(host_ip, port_number);
				// login
				if (ftp.login(logon_user, logon_pwd)) {
					log.info("Connected and logged in to server " + host_ip);
				} else {
					throw new IOException("Could not log in to server, RC=" + ftp.getReplyCode());
				}
				// set to ASCII
				if (ftp.setFileType(FTP.ASCII_FILE_TYPE)) {
					log.info("Set file type to ASCII");
				} else {
					throw new IOException("Could not set file type, RC=" + ftp.getReplyCode());
				}
			} else if (this.file_protocol.equals("FTPS")) {
				if (ftps == null)
					ftps = new FTPSClient();
				// ftp.connect();
				ftps.connect(host_ip, port_number);
				// login
				if (ftps.login(logon_user, logon_pwd)) {
					log.info("Connected and logged in to server " + host_ip);
				} else {
					throw new IOException("Could not log in to server, RC=" + ftp.getReplyCode());
				}
				// set to ASCII
				if (ftps.setFileType(FTP.ASCII_FILE_TYPE)) {
					log.info("Set file type to ASCII");
				} else {
					throw new IOException("Could not set file type, RC=" + ftp.getReplyCode());
				}
				// for FTPS - v7.0 beta
				ftps.execPBSZ(0);
				ftps.execPROT("P");
			}
		} catch (Exception ex) {
			if (ftp != null && ftp.isConnected())
				ftp.disconnect();
			if (ftps != null && ftps.isConnected())
				ftps.disconnect();
			throw ex;
		}
	}

	// delete server from hsql - v6.0.p1
	public void deleteServerFromHsql() throws Exception {
		log.info("Deleting the server " + server_name);
		DmigHsqlConn.getConn().execute("delete from MFSERVER where SERVER_NAME = '"
				+ server_name + "'");
		
		log.info("Server " + server_name + " deleted successfully");
	}

	// display all the variables
	public void displayServerDetail() throws Exception {
		log.info("Displaying Server Details");
		System.out.println("SERVER_NAME     = " + server_name);
		System.out.println("HOST_IP         = " + host_ip);
		System.out.println("LOGON_USER      = " + logon_user);
		System.out.println("LOGON_PWD       = "
				+ DesEncrypter.encrypt(logon_pwd) + " (encrypted)");
		System.out.println("DATABASE_SSID   = " + db_ssid);
	}

	// download a file
	public void downloadFile(String localFileName, String serverDatasetName)
			throws Exception {
		String command_line = "SITE FILETYPE=SEQ";

		// check if connected - v7.0 beta
		if ((ftp != null && !ftp.isConnected()) || (ftps != null && !ftps.isConnected())) {
			this.connectServer();
		}
		// download - v7.0 beta
		log.info("QUOTE " + command_line);
		// log.info(ftp.executeCommand(command_line));
		if (ftp != null)
			ftp.sendCommand(command_line);
		else if (ftps != null)
			ftps.sendCommand(command_line);
		log.info("Downloading " + localFileName + " From " + serverDatasetName);
		// ftp.downloadFile(localFileName, serverDatasetName);
		FileOutputStream output = new FileOutputStream(localFileName);
		if (ftp != null)
			ftp.retrieveFile(serverDatasetName, output);
		else if (ftps != null)
			ftps.retrieveFile(serverDatasetName, output);
		output.close();
		log.info(serverDatasetName + " downloaded");
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof MFServer) {
			String temp_server_name = ((MFServer) object).server_name;

			if (server_name.equals(temp_server_name)) {
				return true;
			}
		}

		return false;
	}

	// insert server info into hsql - v6.0.phase1
	public void insertServerIntoHsql() throws Exception {
		// create new server entry
		log.info("Saving server " + server_name);
		DmigHsqlConn.getConn().execute("insert into MFSERVER values ('" + server_name
				+ "','" + host_ip + "','"
				+ DesEncrypter.encrypt(logon_user) + "','"
				+ DesEncrypter.encrypt(logon_pwd) + "','" 
				+ db_ssid + "','" 
				+ TextProcessor.replaceStr(jcl_header, "'", "''") + "','"	// v6.0.0
				+ file_protocol + "',"		// v7.0.0
				+ port_number + ",'"		// v7.0.0
				+ param_db2load + "','" 	// v6.1.1
				+ param_dsnload + "','" 	// v6.1.1
				+ param_sortlib + "','" 	// v6.1.1
				+ param_space + "','" 		// v6.1.1
				+ param_unit + "')");		// v6.1.1

		// create new table list
		log.info("Saving new schema list");
		for (int i = 0; i < schemas.length; i++) {
			schemas[i].insertSchemaIntoHsql();
			log.info((i+1) + " out of " + schemas.length + " schemas saved");
		}

		log.info("Server saved successfully");
	}

	// list the file details - code cut from StepCheckTrigger.java
	public String[][] listFile(String fileName) throws Exception {
		// CommonProc.java
		String command_line = "SITE FILETYPE=SEQ";
		String[][] return_array = new String[0][0];

		// check if connected - v7.0 beta
		if ((ftp != null && !ftp.isConnected()) || (ftps != null && !ftps.isConnected())) {
			this.connectServer();
		}
		
		// count - v7.0 beta
		log.info("QUOTE " + command_line);
		// log.info(ftp.executeCommand(command_line));
		if (ftp != null)
			ftp.sendCommand(command_line);
		else if (ftps != null)
			ftps.sendCommand(command_line);		

		// verify if the filename is valid - V5.0RC2
		if (fileName == null || fileName.trim().length() == 0)
			return return_array;

		// get file list from server - v7.0 beta
		log.info("Listing " + fileName);
		
		//String[] files = new String [0];
		//try {	// cater for PDS library
		//	files = ftp.directoryNameList(fileName, true);
		//} catch (Exception ex) {
		//}
		FTPFile[] files = null;
		if (ftp != null)
			files = ftp.listFiles(fileName);
		else if (ftps != null)
			files = ftps.listFiles(fileName);
		
		int total_cnt = files.length;
		// if (files.length > 0)
		//	total_cnt = files.length - 1; // get rid of the header
		// else
		//	total_cnt = 0;
		
		log.info("Current total file count: " + total_cnt);
		
		for (int i = 0; i < total_cnt; i++) {
			System.out.println(files[i].getRawListing());
		}

		// get file info
		if (total_cnt > 0)
			return_array = new String[total_cnt][4];
		for (int i = 1; i < files.length; i++) {
			// read current file
			String currFileName = files[i].getRawListing();	// v7.0 beta
			return_array[i - 1][0] = currFileName.substring(56, currFileName.length()).trim(); // filename
			return_array[i - 1][1] = currFileName.substring(39, 44).trim(); // Lrecl
			return_array[i - 1][2] = currFileName.substring(45, 50).trim(); // BlkSz
			return_array[i - 1][3] = currFileName.substring(33, 38).trim(); // Recfm
		}

		return return_array;
	}

	// reset user name & pwd in Hsql
	public void resetUserPwd(String newLogonUser, String newLogonPwd)
			throws Exception {
		// encrypt the user and pwd
		String en_new_logon_user = DesEncrypter.encrypt(newLogonUser);
		String en_new_logon_pwd = DesEncrypter.encrypt(newLogonPwd);

		// update the database
		DmigHsqlConn.getConn().execute("update MFSERVER set LOGON_USER = '"
				+ en_new_logon_user + "', LOGON_PWD = '" + en_new_logon_pwd
				+ "'" + " where SERVER_NAME = '" + server_name + "'");
		
		// reset variables
		logon_user = newLogonUser;
		logon_pwd = newLogonPwd;
		
		log.info("Pwd reset successful");
	}

	// submit a job
	public void submitJob(String jobFileName) throws Exception {

		String command_line = "SITE FILETYPE=JES";

		// check if connected - v7.0 beta
		if ((ftp != null && !ftp.isConnected()) || (ftps != null && !ftps.isConnected())) {
			this.connectServer();
		}
		// submit - v7.0 beta
		log.info("QUOTE " + command_line);
		// log.info(ftp.executeCommand(command_line));
		int rc = -1;
		if (ftp != null)
			rc = ftp.sendCommand(command_line);
		else if (ftps != null)
			rc = ftps.sendCommand(command_line);
		log.info("QUOTE RC=" + rc);
		//ftp.uploadFile(jobFileName, jobFileName);
		FileInputStream input = new FileInputStream(new File(jobFileName));
		String jobName = new File(jobFileName).getName();	
		log.info("Submitting " + jobName);
		boolean storeRC = false;
		if (ftp != null)
			storeRC = ftp.storeFile(jobName, input);
		else if (ftps != null)
			storeRC = ftps.storeFile(jobName, input);		
		if (storeRC) {
			log.info("Job submitted");
		} else {
			int replyCode = -1;
			if (ftp != null)
				replyCode = ftp.getReplyCode();
			else if (ftps != null)
				replyCode = ftps.getReplyCode();
			throw new IOException("Job submission failed, RC=" + replyCode);
		}
		input.close();
	}

	// upload a file
	public void uploadFile(String localFileName, String serverDatasetName, int recordLength)	// v6.2
			throws Exception {

		String command_line = "";

		// check if connected - v7.0 beta
		if ((ftp != null && !ftp.isConnected()) || (ftps != null && !ftps.isConnected())) {
			this.connectServer();
		}
		// upload - v7.0 beta
		command_line = "SITE FILETYPE=SEQ";
		log.info("QUOTE " + command_line);
		// log.info(ftp.executeCommand(command_line));
		if (ftp != null)
			ftp.sendCommand(command_line);
		else if (ftps != null)
			ftps.sendCommand(command_line);		

		command_line = "SITE LR=" + recordLength;		// v6.2
		log.info("QUOTE " + command_line);				// v6.2
		// log.info(ftp.executeCommand(command_line));		// v6.2
		if (ftp != null)
			ftp.sendCommand(command_line);
		else if (ftps != null)
			ftps.sendCommand(command_line);	
		
		log.info("Uploading " + localFileName + " To " + serverDatasetName);
		// ftp.uploadFile(localFileName, serverDatasetName);
		FileInputStream input = new FileInputStream(new File(localFileName));
		if (ftp != null)
			ftp.storeFile(serverDatasetName, input);
		else if (ftps != null)
			ftps.storeFile(serverDatasetName, input);
		log.info(localFileName + " Uploaded");
	}
	
	// add a new schema into the list - v6.0.phase4
	public void addSchema(DB2Schema schema) {
		List<DB2Schema> list = new ArrayList<DB2Schema>(Arrays.asList(schemas));
		schema.setServer(this);			// v6.0.phase5
		list.add(schema);
		schemas = list.toArray(new DB2Schema[schemas.length+1]);
	}
	
	// update a schema in the list - v6.0.phase4
	public void updateSchema(DB2Schema newSchema) throws Exception {		// v6.0.1
		// search the schema name
		for(int i=0; i<schemas.length; i++) {
			if(schemas[i].equals(newSchema)) {
				// set each attribute - v6.0.phase5
				schemas[i].setJobBatchId(newSchema.getJobBatchId());
				schemas[i].setDatasetPrefix(newSchema.getDatasetPrefix());
				
				/* we cannot refresh tables either. 
				schemas[i].setTables(newSchema.getTables());
				*/
				
				/* we cannot simply replace the object, it's causing issues. 
				 * as well in DB2Schema.updateTable(DB2Table).
				List<DB2Schema> list = new ArrayList<DB2Schema>(Arrays.asList(schemas));
				newSchema.setServer(this);			// v6.0.phase5
				list.set(i, newSchema);
				schemas = list.toArray(new DB2Schema[schemas.length]);
				*/
			}
		}
	}
	
	// delete a schema from the list - v6.0.phase4
	public void removeSchema(DB2Schema schema) {
		// search the table name
		for(int i=0; i<schemas.length; i++) {
			if(schemas[i].equals(schema)) {
				List<DB2Schema> list = new ArrayList<DB2Schema>(Arrays.asList(schemas));
				schema.setServer(null);			// v6.0.phase5
				list.remove(i);
				schemas = list.toArray(new DB2Schema[schemas.length-1]);
			}
		}
	}

	public String getServerName() {
		return server_name;
	}

	public void setServerName(String serverName) {
		server_name = serverName;
	}

	public String getHostIp() {
		return host_ip;
	}

	public void setHostIp(String hostIp) {
		host_ip = hostIp;
	}

	public String getLogonUser() {
		return logon_user;
	}

	public void setLogonUser(String logonUser) {
		logon_user = logonUser;
	}

	public String getLogonPwd() {
		return logon_pwd;
	}

	public void setLogonPwd(String logonPwd) {
		logon_pwd = logonPwd;
	}

	public String getDbSsid() {
		return db_ssid;
	}

	public void setDbSsid(String dbSsid) {
		db_ssid = dbSsid;
	}

	public String getJclHeaderBatch() {			// v6.0.0
		String [] jcl_array = jcl_header.split("\n");
		String tmp_jcl_header = "";
		boolean jcl_header_flag = true;
		boolean user_found_flag = false;
		
		if (jcl_header.trim().length() == 0) {
			log.warn("Cannot find JCL header, default JCL applies.");
			return IJclDefaults.DEFAULT_JCL_HEADER;
		}
		
		for (int i = 0; i < jcl_array.length; i++) {
			String row = jcl_array[i].toUpperCase().trim();
			
			// if not a jcl row, skip it
			if (!row.startsWith("//")) {
				if (i == 0) {
					log.warn("JCL syntax invalid, default JCL applies.");
					return IJclDefaults.DEFAULT_JCL_HEADER;
				}
				continue;
			}
			
			// replace jcl header
			if (jcl_header_flag) {	
				// replace job name
				if (i == 0) {
					int job_idx = row.indexOf(" JOB ");
					
					if (job_idx <= 0) {		// v6.1.3
						log.warn("Keyword 'JOB' is not found, default JCL applies.");
						return IJclDefaults.DEFAULT_JCL_HEADER;
					}
				}
				
				// replace user
				if (row.indexOf("USER=") > 0) {
					user_found_flag = true;
					
					int user_idx_strt = row.indexOf("USER=");
					int user_idx_end = row.indexOf(",", user_idx_strt);
					
					if (user_idx_end > 0) {
						row = row.substring(0, user_idx_strt) + "USER=#BATCH_ID#" 
								+ row.substring(user_idx_end); 
					} else {
						row = row.substring(0, user_idx_strt) + "USER=#BATCH_ID#";
					}
				}
				
				// check if the header is end
				if (!row.endsWith(",")) {
					jcl_header_flag = false;
					
					// add user if not found
					if (!user_found_flag) {
						row += ",\n";
						row += "//        USER=#BATCH_ID#";
					}
				}
			}
			
			// add the row to tmp_jcl_header
			if (tmp_jcl_header.length() > 0) {
				tmp_jcl_header += "\n";
			}
			tmp_jcl_header += row;
			
		}
		
		return tmp_jcl_header;
	}

	public String getJclHeaderDisplay() {		// v6.0.0
		return jcl_header;
	}

	public void setJclHeader(String jclHeader) {
		jcl_header = jclHeader;
	}
	
	public String getFileProtocol() {		// v7.0.0
		return file_protocol;
	}

	public void setFileProtocol(String fileProtocol) {	// v7.0.0
		file_protocol = fileProtocol;
	}
	
	public int getPortNumber() {			// v7.0.0
		return port_number;
	}

	public void setPortNumber(int portNumber) {		// v7.0.0
		port_number = portNumber;
	}
	
	// get a schema - v6.0.phase4
	public DB2Schema getSchema(String schemaName) throws Exception {
		DB2Schema schema = new DB2Schema(schemaName, this);
		
		for (int i = 0; schemas != null && i < schemas.length; i++) {
			if (schemas[i].getSchemaName().equals(schemaName)) {
				schema = schemas[i];
				break;
			}
		}
		
		return schema;
	}
	
	public String getParamDb2load() throws Exception {
		return getParamDb2load(false);
	}
	
	public String getParamDb2load(boolean resolveDefault) throws Exception {
		if (resolveDefault) {
			String str_default = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_PARAMETER_DB2LOAD);
			if (param_db2load.isEmpty()) {		// v6.1.1
				return str_default;
			} else {
				return TextProcessor.replaceStr(param_db2load, "<DEFAULT>", str_default);
			}
		} else {
			return param_db2load;
		}
	}

	public void setParamDb2load(String paramDb2load) {
		param_db2load = paramDb2load;
	}

	public String getParamDsnload() throws Exception {
		return getParamDsnload(false);
	}

	public String getParamDsnload(boolean resolveDefault) throws Exception {
		if (resolveDefault) {
			String str_default = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_PARAMETER_DSNLOAD);
			if (param_dsnload.isEmpty()) {		// v6.1.1
				return str_default;
			} else {
				return TextProcessor.replaceStr(param_dsnload, "<DEFAULT>", str_default);
			}
		} else {
			return param_dsnload;
		}
	}

	public void setParamDsnload(String paramDsnload) {
		param_dsnload = paramDsnload;
	}

	public String getParamSortlib() throws Exception {
		return getParamSortlib(false);
	}

	public String getParamSortlib(boolean resolveDefault) throws Exception {
		if (resolveDefault) {
			String str_default = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_PARAMETER_SORTLIB);
			if (param_sortlib.isEmpty()) {		// v6.1.1
				return str_default;
			} else {
				return TextProcessor.replaceStr(param_sortlib, "<DEFAULT>", str_default); 
			}
		} else {
			return param_sortlib;
		}
	}

	public void setParamSortlib(String paramSortlib) {
		param_sortlib = paramSortlib;
	}

	public String getParamSpace() throws Exception {
		return getParamSpace(false);
	}

	public String getParamSpace(boolean resolveDefault) throws Exception {
		if (resolveDefault) {
			String str_default = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_PARAMETER_SPACE);
			if (param_space.isEmpty()) {		// v6.1.1
				return str_default;
			} else {
				return TextProcessor.replaceStr(param_space, "<DEFAULT>", str_default);
			}
		} else {
			return param_space;
		}
	}

	public void setParamSpace(String paramSpace) {
		param_space = paramSpace;
	}

	public String getParamUnit() throws Exception {
		return getParamUnit(false);
	}

	public String getParamUnit(boolean resolveDefault) throws Exception {
		if (resolveDefault) {
			String str_default = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_PARAMETER_UNIT);
			if (param_unit.isEmpty()) {		// v6.1.1
				return str_default;
			} else {
				return TextProcessor.replaceStr(param_unit, "<DEFAULT>", str_default);
			}
		} else {
			return param_unit;
		}
	}

	public void setParamUnit(String paramUnit) {
		param_unit = paramUnit;
	}

	public DB2Schema[] getSchemas() {
		return schemas;
	}
	

	public void setSchemas() throws Exception {
		// run SQL to get the schema list count and re-define the schema list
		ResultSet rs = DmigHsqlConn.getConn().query("select count(*) from DB2SCHEMA where SERVER_NAME = '"
						+ server_name + "'");
		rs.next();
		int schema_size = rs.getInt(1);
		rs.close();

		// initialize a new MFSchema array
		DB2Schema[] new_schemas = new DB2Schema[schema_size];

		new_schemas = new DB2Schema[schema_size];
		int row_id = 0;

		// run SQL to get the table list
		rs = DmigHsqlConn.getConn().query("select * from DB2SCHEMA where SERVER_NAME = '"
				+ server_name + "' order by SCHEMA_NAME");	// v6.0.phase6

		// go thru the resultset one by one
		while (rs.next()) {
			new_schemas[row_id] = new DB2Schema(rs.getString("SCHEMA_NAME"), 
					rs.getString("JOB_BATCH_ID"), rs.getString("DATASET_PREFIX"),
					rs.getString("JCL_SETTINGS"), this);
			row_id++;
		}

		rs.close();

		// set tables
		setSchemas(new_schemas);
	}

	public void setSchemas(DB2Schema[] newSchemas) throws Exception {		// v6.0.1
		// do a comparison between the existing schemas and new schemas,
		// to refresh the schemas from the parameter - v6.0.phase5
		if (schemas != null) {
			// remove the existing unmatched schemas
			for (int i=0; i<schemas.length; i++) {
				boolean is_schema_matching = false;
				for (int j=0; j<newSchemas.length; j++) {
					if (schemas[i].equals(newSchemas[j])) {
						// if a match can be found, update it
						is_schema_matching = true;
						updateSchema(newSchemas[j]);
					}
				}
				// if a match can not be found
				if (!is_schema_matching) {
					removeSchema(schemas[i]);
				}
			}
			
			// add the new unmatched schemas
			for (int i=0; i<newSchemas.length; i++) {
				boolean is_schema_matching = false;
				for (int j=0; j<schemas.length; j++) {
					if (newSchemas[i].equals(schemas[j])) {
						// if a match can be found
						is_schema_matching = true;
					}
				}
				// if a match can not be found
				if (!is_schema_matching) {
					addSchema(newSchemas[i]);
				}
			}
		} else {
			schemas = newSchemas;
			
			for(int i=0; i<schemas.length; i++) {
				schemas[i].setServer(this);
			}
		}
		
		/* V6.0.phase5: we cannot simply null all existing schemas 
		 * and add new ones again, as well in DB2Schema.setTables(DB2Table[])
		if(this.schemas != null) {
			for(int i=0; i<this.schemas.length; i++) {
				this.schemas[i].setServer(null);
			}
		}
		
		this.schemas = schemas;
		
		for(int i=0; i<this.schemas.length; i++) {
			this.schemas[i].setServer(this);
		} */
	}
	
	public Object getRoot() {
		return root;
	}

	public void setRoot(Object rootElement) {
		root = rootElement;
	}
	
	// code originally set as static String[][] getAllServers()
	public static MFServer[] getAllServers() throws Exception {
		return getAllServers(new CNFRoot());
	}
	
	// code originally set as static String[][] getAllServers()
	public static MFServer[] getAllServers(CNFRoot root) throws Exception {
		// get total server count
		ResultSet rs = DmigHsqlConn.getConn().query("select count(*) from MFSERVER");
		rs.next();
		int server_size = rs.getInt(1);
		rs.close();

		// define the return array
		MFServer[] new_servers = new MFServer[server_size];
		int row_id = 0;

		// retrieve the server name for each row
		rs = DmigHsqlConn.getConn().query("select * from MFSERVER order by SERVER_NAME");	// v6.0.phase6

		while (rs.next()) {
			String server_name = rs.getString("SERVER_NAME");
			String host_ip = rs.getString("HOST_IP");
			String logon_user = DesEncrypter.decrypt(rs.getString("LOGON_USER"));
			String logon_pwd = DesEncrypter.decrypt(rs.getString("LOGON_PWD"));
			String db_ssid = rs.getString("DB_SSID");
			String jcl_header = rs.getString("JCL_HEADER");
			String file_protocol = rs.getString("FILE_PROTOCOL");		// v7.0.0
			int port_number = rs.getInt("PORT_NUMBER");					// v7.0.0
			String param_db2load = rs.getString("PARAM_DB2LOAD");		// v6.1.1
			String param_dsnload = rs.getString("PARAM_DSNLOAD");		// v6.1.1
			String param_sortlib = rs.getString("PARAM_SORTLIB");		// v6.1.1
			String param_space = rs.getString("PARAM_SPACE");			// v6.1.1
			String param_unit = rs.getString("PARAM_UNIT");				// v6.1.1
			
			new_servers[row_id] = new MFServer(server_name, host_ip,
					 logon_user, logon_pwd, db_ssid, jcl_header, file_protocol, port_number, 
					 param_db2load, param_dsnload, param_sortlib, param_space, param_unit);		// v6.1.1
			new_servers[row_id].setRoot(root);
			row_id++;
		}

		rs.close();

		// set servers
		return new_servers;
	}
}
