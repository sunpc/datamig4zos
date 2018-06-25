/**
 *  Class: install.DataInstall
 *  Description: This is the installation process for the first run.
 * 
 * 	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 11/07/2012: Code baseline. (V6.1.1)
 *  02. 07/30/2013: Use version file to refresh the JCL templates. (V6.2)
 *  03. 08/06/2013: Add DB2COLUMN, DB2TBREL and DB2TBMIG. (V6.3)
 *  04. 08/12/2013: Add DB2FILTER. (V6.3)
 */
package net.sourceforge.datamig4zos.install;

import java.io.File;
import java.sql.ResultSet;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.install.data.DDLTables;
import net.sourceforge.datamig4zos.install.data.JCLTemplates;
import net.sourceforge.datamig4zos.runtime.DmigHsqlConn;
import net.sourceforge.datamig4zos.runtime.IJclDefaults;
import net.sourceforge.datamig4zos.ui.preferences.PreferenceConstants;
import net.sourceforge.datamig4zos.util.HsqlConn;
import net.sourceforge.datamig4zos.util.TextProcessor;

/**
 * @author SunPC
 *
 */
public class DataInstall {
	
	private static String data_folder_name = "";
	private static String data_hsq_folder_name = "";
	private static String data_tmp_folder_name = "";
	private static String data_tpl_folder_name = "";
	
	public static void runInstall() throws Exception {
		createFolders();
		createTables();
		createTemplates();
	}
	
	private static void createFolders() throws Exception {
		// define the folder names
		data_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/";
		
		data_hsq_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/hsql/";
		
		data_tmp_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/temp/";
		
		data_tpl_folder_name = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DATA_DIRECTORY) + "/templates/";
		
		// check data folder
		File data_folder_file = new File(data_folder_name);
		if (!data_folder_file.exists()) {
			data_folder_file.mkdir();
		}
		
		// check hsql folder
		File data_hsq_folder_file = new File(data_hsq_folder_name);
		if (!data_hsq_folder_file.exists()) {
			data_hsq_folder_file.mkdir();
		}
		
		// check temp folder
		File data_tmp_folder_file = new File(data_tmp_folder_name);
		if (!data_tmp_folder_file.exists()) {
			data_tmp_folder_file.mkdir();
		}
		
		// check template folder
		File data_tpl_folder_file = new File(data_tpl_folder_name);
		if (!data_tpl_folder_file.exists()) {
			data_tpl_folder_file.mkdir();
		}
	}

	private static void createTables() throws Exception {
		// connect to Hsql
		DmigHsqlConn.setConn(new HsqlConn());
		
		// query system tables
		ResultSet rs;
		
		rs = DmigHsqlConn.getConn().query(DDLTables.MFSERVER_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.MFSERVER_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.MFSERVER_DDL2);
		}			
		rs.close();
		
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2SCHEMA_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2SCHEMA_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2SCHEMA_DDL2);
			DmigHsqlConn.getConn().execute(DDLTables.DB2SCHEMA_DDL3);
		}
		rs.close();
		
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2TABLE_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2TABLE_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TABLE_DDL2);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TABLE_DDL3);
		}
		rs.close();
		
		// v6.3 added
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2COLUMN_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2COLUMN_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2COLUMN_DDL2);
			DmigHsqlConn.getConn().execute(DDLTables.DB2COLUMN_DDL3);
		}
		rs.close();
		
		// v6.3 added
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2TBREF_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBREF_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBREF_DDL2);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBREF_DDL3);
		}
		rs.close();
		
		// v6.3 added
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2TBMIG_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBMIG_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBMIG_DDL2);
			DmigHsqlConn.getConn().execute(DDLTables.DB2TBMIG_DDL3);
		}
		rs.close();
		
		// v6.3 added
		rs = DmigHsqlConn.getConn().query(DDLTables.DB2FILTER_SEL);
		if (!rs.next()) {
			DmigHsqlConn.getConn().execute(DDLTables.DB2FILTER_DDL1);
			DmigHsqlConn.getConn().execute(DDLTables.DB2FILTER_DDL2);
		}
		rs.close();
	}
	
	private static void createTemplates() throws Exception {
		// allocate the version file - v6.2
		String file_ver_name = data_tpl_folder_name + IJclDefaults.JCL_TEMPLATE_VERSION;
		File file_ver = new File(file_ver_name);
		
		// create the template and version files
		if (!file_ver.exists()) {		
			String file_alloc_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_ALLOC_CNTL);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_CNTL_ALLOC_JCL, file_alloc_name);
			
			String file_set_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_SET_CNTL);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_CNTL_SET_JCL, file_set_name);
			
			String file_ftp_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_FTP_DATASET);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_DATASET_FTP_JCL, file_ftp_name);
			
			String file_load_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_LOAD_TARGET);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_TABLE_LOAD_JCL, file_load_name);
			
			String file_query_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_QUERY_DB);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_TABLE_QUERY_JCL, file_query_name);
			
			String file_unload_name = data_tpl_folder_name + Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_JCL_TEMPLATE_UNLOAD_SOURCE);
			TextProcessor.writeFile(JCLTemplates.TEMPLATE_TABLE_UNLOAD_JCL, file_unload_name);
			
			TextProcessor.writeFile("VERSION: " + file_ver_name, file_ver_name);
		}
	}
}
