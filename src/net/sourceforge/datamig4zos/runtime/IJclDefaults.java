package net.sourceforge.datamig4zos.runtime;

public interface IJclDefaults {
	
	public static final String JCL_TEMPLATE_VERSION = "template.v62";
	
    public static final String JOBNAME_PREFIX_UNLOAD = "UL";
    public static final String JOBNAME_PREFIX_FTP = "FT";
    public static final String JOBNAME_PREFIX_LOAD = "LD";
    public static final String JOBNAME_PREFIX_QUERY = "QR";
    
    public static final String DEFAULT_JCL_HEADER = 
    	  "//#JOB_NAME# JOB (@DMG,ZOS6),#JOB_NAME#,      \n"
		+ "//        USER=#BATCH_ID#,MSGCLASS=V,TIME=1440";
    
    public static final String JCL_PARAMETER_DB2LOAD = "SYS1.DB2LOAD";
    public static final String JCL_PARAMETER_DSNLOAD = "SYS1.DSNLOAD";
    public static final String JCL_PARAMETER_SORTLIB = "SYS1.SORTLIB";
    public static final String JCL_PARAMETER_SPACE = "(CYL,(150,150),RLSE)";
    public static final String JCL_PARAMETER_UNIT = "(3390,59)";
    
}
