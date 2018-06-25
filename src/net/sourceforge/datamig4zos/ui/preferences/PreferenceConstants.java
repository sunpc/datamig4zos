/**
 *  Class: ui.preferences.PreferenceConstants
 *  Description: This is the Preference Constants.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/20/2012: Code baseline. (V6.0.0)
 *  02. 11/02/2012: Add P_JCL_TEMPLATE_QUERY_DB. (V6.1)
 *  03. 11/07/2012: Add JCL parameters. (V6.1.1) 
 */
package net.sourceforge.datamig4zos.ui.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_DATA_DIRECTORY = "DMT_DATA_DIRECTORY";

	public static final String P_JCL_PARAMETER_DB2LOAD = "DMT_PARAMETER_DB2LOAD";		// v6.1.1
	public static final String P_JCL_PARAMETER_DSNLOAD = "DMT_PARAMETER_DSNLOAD";		// v6.1.1
	public static final String P_JCL_PARAMETER_SORTLIB = "DMT_PARAMETER_SORTLIB";		// v6.1.1
	public static final String P_JCL_PARAMETER_SPACE = "DMT_PARAMETER_SPACE";			// v6.1.1
	public static final String P_JCL_PARAMETER_UNIT = "DMT_PARAMETER_UNIT";				// v6.1.1

	public static final String P_JCL_TEMPLATE_ALLOC_CNTL = "DMT_TEMPLATE_ALLOC_CNTL";
    public static final String P_JCL_TEMPLATE_SET_CNTL = "DMT_TEMPLATE_SET_CNTL";
    public static final String P_JCL_TEMPLATE_UNLOAD_SOURCE = "DMT_TEMPLATE_UNLOAD_SOURCE";
    public static final String P_JCL_TEMPLATE_FTP_DATASET = "DMT_TEMPLATE_FTP_DATASET";
    public static final String P_JCL_TEMPLATE_LOAD_TARGET = "DMT_TEMPLATE_LOAD_TARGET";
    public static final String P_JCL_TEMPLATE_QUERY_DB = "DMT_TEMPLATE_QUERY_DB";		// v6.1
	
}
