package net.sourceforge.datamig4zos.runtime;

public interface IFileNames {
	
	// data folders
	public static final String DATA_FOLDER_NAME = "data";
    
    // common template file names
    public static final String JCL_TEMPLATE_ALLOC_CNTL = "template_cntl_allo.jcl";
    public static final String JCL_TEMPLATE_SET_CNTL = "template_cntl_set.jcl";
    public static final String JCL_TEMPLATE_UNLOAD_SOURCE = "template_table_unload.jcl";
    public static final String JCL_TEMPLATE_FTP_DATASET = "template_dataset_ftp.jcl";
    public static final String JCL_TEMPLATE_LOAD_TARGET = "template_table_load.jcl";
    public static final String JCL_TEMPLATE_QUERY_DB = "template_table_query.jcl";
    
    // output JCL file names
    public static final String OUTPUT_UNLOAD_NAME = "unload.jcl";
    public static final String OUTPUT_FTP_NAME = "ftp.jcl";
    public static final String OUTPUT_LOAD_NAME = "load.jcl";
    public static final String OUTPUT_QUERY_NAME = "query.jcl";
    
    // load card control files
    public static final String LOAD_CNTL_SOURCE_NAME = "loadsrc.ctl";
    public static final String LOAD_CNTL_TARGET_NAME = "loadtgt.ctl";
    
    // FTP control files
    public static final String FTP_CNTL_NETRC_NAME = "netrc.ctl";
    
}
