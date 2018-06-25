/**
 *  Class: install.data.JCLTemplates
 *  Description: This contains the default JCL templates.
 * 
 * 	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 11/07/2012: Code baseline. (V6.1.1)
 *  02. 11/12/2012: Change the step names and CNTL allocation. (V6.1.2)
 *  03. 07/30/2013: Change the FTP and LOAD templates. (V6.2)
 */
package net.sourceforge.datamig4zos.install.data;

/**
 * @author SunPC
 *
 */
public class JCLTemplates {
	
	public static String TEMPLATE_CNTL_ALLOC_JCL = 
			  "//*********************************************************************\n"
			+ "//* #PROCESS_ID# : #DATE# #TIME#                                       \n"
			+ "//* THIS PROC IS TO ALLOCATE THE CNTL LIBRARY                          \n"
			+ "//*********************************************************************\n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 
			+ "//* DELETE UNLOAD DATA SETS                                            \n"
			+ "//*********************************************************************\n" 
			+ "//CTLS010 EXEC PGM=IEFBR14                                             \n"
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//DMGCNTL  DD DSN=#CNTL_LIBRARY#,                                      \n"
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n"
			+ "//*********************************************************************\n" 
			+ "//* ALLOCATE THE CNTL LIBRARY                                          \n"
			+ "//*********************************************************************\n" 
			+ "//CTLS020 EXEC PGM=IEFBR14,COND=(4,LT)                                 \n"
			+ "//DMGCNTL  DD DSN=#CNTL_LIBRARY#,                                      \n"
			+ "//            DSNTYPE=LIBRARY,                                         \n"
			+ "//            DISP=(NEW,CATLG,DELETE),                                 \n"
			+ "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE=0),                       \n"
			+ "//            SPACE=(TRK,(1,1,10))                                     \n"	// v6.1.2
			+ "//*                                                                    ";
	
	public static String TEMPLATE_CNTL_SET_JCL = 
			  "//*********************************************************************\n" 
			+ "//* SET SUCCESS CNTL FLAG                                              \n"
			+ "//*********************************************************************\n" 
			+ "//CTLS980  EXEC PGM=IEBGENER,COND=(4,LT)                               \n"
			+ "//SYSUT1   DD *                                                        \n"
			+ "MEMBER NAME : #CNTL_MEMBER#                                            \n"
			+ "LIBRARY     : #CNTL_LIBRARY#                                           \n"
			+ "PROCESS ID  : #PROCESS_ID#                                             \n"
			+ "JOB NAME    : #JOB_NAME#                                               \n"
			+ "STATUS      : JOB COMPLETED SUCCESSFULLY                               \n"
			+ "//SYSUT2   DD DSN=#CNTL_LIBRARY#(#CNTL_MEMBER#C),                      \n"
			+ "//            DISP=SHR                                                 \n"
			+ "//SYSIN    DD DUMMY                                                    \n"
			+ "//SYSPRINT DD SYSOUT=*                                                 \n"
			+ "//*********************************************************************\n" 
			+ "//* SET FAILURE CNTL FLAG                                              \n"
			+ "//*********************************************************************\n" 
			+ "//CTLS990  EXEC PGM=IEBGENER,COND=((00,EQ,CTLS980),(EVEN))             \n"
			+ "//SYSUT1   DD *                                                        \n"
			+ "MEMBER NAME : #CNTL_MEMBER#                                            \n"
			+ "LIBRARY     : #CNTL_LIBRARY#                                           \n"
			+ "PROCESS ID  : #PROCESS_ID#                                             \n"
			+ "JOB NAME    : #JOB_NAME#                                               \n"
			+ "STATUS      : JOB FAILED                                               \n"
			+ "//SYSUT2   DD DSN=#CNTL_LIBRARY#(#CNTL_MEMBER#E),                      \n"
			+ "//            DISP=SHR                                                 \n"
			+ "//SYSIN    DD DUMMY                                                    \n"
			+ "//SYSPRINT DD SYSOUT=*                                                 \n"
			+ "//*                                                                    ";
	
	public static String TEMPLATE_DATASET_FTP_JCL = 
			  "//*********************************************************************\n"
			+ "//* #PROCESS_ID# : #DATE# #TIME#                                       \n"
			+ "//* THIS PROC IS TO FTP DATA FROM #HOST_NAME#                          \n"
			+ "//*********************************************************************\n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 
			+ "//* FTP #DS_SOURCE_NAME# (DMG-V6.2)                                    \n"
			+ "//*********************************************************************\n" 
			+ "//*********************************************************************\n" 
			+ "//* DELETE TARGET DATA SETS                                            \n"
			+ "//*********************************************************************\n" 
			+ "//FTPS010  EXEC PGM=IDCAMS,COND=(4,LT)                                 \n"	// v6.2
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//SYSIN    DD  *                                                       \n"	// v6.2
			+ "  DELETE #DS_TARGET_NAME#                                              \n"	// v6.2
			+ "  IF MAXCC > 4 THEN SET MAXCC = 4                                      \n"	// v6.2
			+ "//*                                                                    \n" 
			+ "//*********************************************************************\n"
			+ "//* RUN FTP COMMAND                                                    \n"
			+ "//*********************************************************************\n"
			+ "//FTPS020  EXEC PGM=FTP,COND=(4,LT),                                   \n"
			+ "//             PARM='(TIMEOUT 200 EXIT'                                \n"	// v6.2
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//NETRC    DD  DSN=#DS_NETRC_NAME#,                                    \n"	// v6.2
			+ "//             DISP=SHR                                                \n"	// v6.2
			+ "//OUTPUT   DD  SYSOUT=*                                                \n"
			+ "//INPUT    DD  *                                                       \n"
			+ "#HOST_NAME#                                                            \n"	// v6.2
			+ "BIN                                                                    \n"
			+ "LOCSITE DATACLAS=COMPTAIL                                              \n"
			+ "LOCSITE LR=#DS_LRECL# BLK=#DS_BLKSZ# REC=#DS_RECFM#                    \n"	// v6.2
			+ "LOCSITE #DS_SPACE# PRI=#DS_PRI# SEC=#DS_SEC#                           \n"	// v6.2
			+ "LOCSITE UNIT=#DS_UNIT# UCOUNT=#DS_UCOUNT#                              \n" 	// v6.2
			+ "CD '#DS_SOURCE_PREFIX#'                                                \n" 	// v6.2
			+ "LCD '#DS_TARGET_PREFIX#'                                               \n" 	// v6.2
			+ "GET #DS_SOURCE_MEMBER# #DS_TARGET_MEMBER# (REPLACE                     \n"	// v6.2
			+ "QUIT                                                                   \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 	// v6.2
			+ "//* DELETE NETRC DATA SETS                                             \n"	// v6.2
			+ "//*********************************************************************\n" 	// v6.2
			+ "//FTPS030  EXEC PGM=IEFBR14                                            \n"  	// v6.2
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"	// v6.2
			+ "//TARGETDS DD DSN=#DS_NETRC_NAME#,                                     \n"	// v6.2
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n" 	// v6.2
			+ "//*                                                                    ";	// v6.2
	
	public static String TEMPLATE_TABLE_LOAD_JCL = 
			  "//*********************************************************************\n"
			+ "//* #PROCESS_ID# : #DATE# #TIME#                                       \n"
			+ "//* THIS PROC IS TO LOAD DATA INTO A TABLE                             \n"
			+ "//*********************************************************************\n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 
			+ "//* LOAD #TABLE_NAME#                                                  \n"
			+ "//*********************************************************************\n" 
			+ "//*********************************************************************\n"
			+ "//* DELETE WORK FILES                                                  \n"
			+ "//*********************************************************************\n"
			+ "//LODS010  EXEC PGM=IEFBR14,COND=(4,LT)                                \n"  
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//SYSDISCD DD DSN=#DATASET_SYSDISCD#,                                  \n"
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n" 
			+ "//*                                                                    \n" 
			+ "//*********************************************************************\n"
			+ "//* PUT TABLESPACE INTO UTILITY MODE                                   \n"
			+ "//*********************************************************************\n"
			+ "//LODS020 EXEC PGM=IKJEFT01,                                           \n"
			+ "//             DYNAMNBR=20,                                            \n"
			+ "//             COND=(04,LT)                                            \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSTSIN   DD *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "  -START DATABASE (#DB_NAME#) SPACENAM (#TS_NAME#) ACCESS (UT)         \n"
			+ "  -DISPLAY DATABASE (#DB_NAME#) SPACENAM (#TS_NAME#)                   \n"
			+ "END                                                                    \n"
			+ "//SYSTSPRT  DD SYSOUT=*                                                \n"
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//SYSIN     DD DUMMY                                                   \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n"
			+ "//* LOAD THE DB2 TABLE                                                 \n"
			+ "//*********************************************************************\n"
			+ "//LODS030  EXEC PGM=DSNUTILB,REGION=4096K,                             \n" 
			+ "//         PARM=('#DB_SSID#,DMGLT#TS_NAME#,'),                         \n"
			+ "//         COND=(04,LT)                                                \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSIN     DD DSN=#DATASET_SYSPUNCH#,                                 \n"
			+ "//             DISP=SHR                                                \n"
			+ "//SYSREC    DD DSN=#DATASET_SYSREC#,                                   \n"
			+ "//             DISP=SHR                                                \n"
			+ "//SYSDISC   DD DSN=#DATASET_SYSDISCD#,                                 \n"
			+ "//             DISP=(NEW,CATLG,CATLG),                                 \n"
			+ "//             UNIT=#UNIT#,SPACE=#SPACE#,                              \n"   
			+ "//             DCB=(*.SYSREC)                                          \n"
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//DSNTRACE  DD SYSOUT=*                                                \n"
			+ "//UTPRINT   DD SYSOUT=*                                                \n"
			+ "//SORTLIB   DD DSN=#SORTLIB#,DISP=SHR                                  \n" 
			+ "//SYSERR    DD UNIT=#UNIT#,SPACE=#SPACE#                               \n"
			+ "//SYSMAP    DD UNIT=#UNIT#,SPACE=#SPACE#                               \n"
			+ "//SYSUT1    DD UNIT=#UNIT#,SPACE=#SPACE#                               \n"
			+ "//SORTOUT   DD UNIT=#UNIT#,SPACE=#SPACE#                               \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n"
			+ "//* TERMINATE UTILITY IF THE ABOVE STEP ABENDS                         \n"
			+ "//*********************************************************************\n"
			+ "//LODS035 EXEC PGM=IKJEFT01,                                           \n"
			+ "//             TIME=40,                                                \n"
			+ "//             DYNAMNBR=60,                                            \n"
			+ "//             COND=ONLY                                               \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSTSPRT  DD SYSOUT=*                                                \n"
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//SYSTSIN   DD *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "  -DIS  UTIL(*)                                                        \n"
			+ "  -TERM UTIL(DMGLT#TS_NAME#)                                           \n"
			+ "  -DIS  UTIL(*)                                                        \n"
			+ "//SYSIN     DD DUMMY                                                   \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n"
			+ "//* GENERATE STATISTICS FOR TABLESPACE                                 \n"
			+ "//*********************************************************************\n"
			+ "//LODS040 EXEC PGM=DSNUTILB,REGION=4096K,                              \n" 
			+ "//             PARM=('#DB_SSID#,DMGRS#TS_NAME#,'),                     \n"
			+ "//             COND=(04,LT)                                            \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//STPRIN01  DD SYSOUT=*                                                \n"
			+ "//UTPRINT   DD SYSOUT=*                                                \n"	// v6.2
			+ "//SYSIN     DD *                                                       \n"
			+ "  RUNSTATS TABLESPACE #DB_NAME#.#TS_NAME#                              \n"
			+ "           TABLE ALL                                                   \n"
			+ "           INDEX ALL                                                   \n"
			+ "           SHRLEVEL REFERENCE                                          \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n"
			+ "//* TERMINATE UTILITY IF THE ABOVE STEP ABENDS                         \n"
			+ "//*********************************************************************\n"
			+ "//LODS045 EXEC PGM=IKJEFT01,                                           \n"
			+ "//             TIME=40,                                                \n"
			+ "//             DYNAMNBR=60,                                            \n"
			+ "//             COND=ONLY                                               \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSTSPRT  DD SYSOUT=*                                                \n"
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//SYSTSIN   DD *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "  -DIS  UTIL(*)                                                        \n"
			+ "  -TERM UTIL(DMGRS#TS_NAME#)                                           \n"
			+ "  -DIS  UTIL(*)                                                        \n"
			+ "//SYSIN     DD DUMMY                                                   \n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n"
			+ "//* PUT TABLESPACE INTO READ/WRITE MODE                                \n"
			+ "//*********************************************************************\n"
			+ "//LODS050 EXEC PGM=IKJEFT01,                                           \n"
			+ "//             DYNAMNBR=20,                                            \n"
			+ "//             COND=(04,LT)                                            \n"
			+ "//STEPLIB   DD DSN=#DB2LOAD#,DISP=SHR                                  \n" 
			+ "//SYSTSIN   DD *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "  -START DATABASE (#DB_NAME#) SPACENAM (#TS_NAME#) ACCESS (RW)         \n"
			+ "  -DISPLAY DATABASE (#DB_NAME#) SPACENAM (#TS_NAME#)                   \n"
			+ "END                                                                    \n"
			+ "//SYSTSPRT  DD SYSOUT=*                                                \n"
			+ "//SYSPRINT  DD SYSOUT=*                                                \n"
			+ "//SYSUDUMP  DD SYSOUT=*                                                \n"
			+ "//SYSIN     DD DUMMY                                                   \n"
			+ "//*                                                                    ";
	
	public static String TEMPLATE_TABLE_QUERY_JCL = 
			  "//*********************************************************************\n"
			+ "//* #PROCESS_ID# : #DATE# #TIME#                                       \n"
			+ "//* THIS PROC IS TO RUN A DB2 QUERY (DMG-V6.1)                         \n"
			+ "//*********************************************************************\n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 
			+ "//* RUN QUERY ON #DB_SSID#.#DB_SCHEMA#                                 \n"
			+ "//*********************************************************************\n" 
			+ "//*********************************************************************\n" 
			+ "//* DELETE QUERY RESULT DATA SETS                                      \n"
			+ "//*********************************************************************\n" 
			+ "//QRYS010  EXEC PGM=IEFBR14,COND=(4,LT)                                \n"  
			+ "//SYSPRINT DD SYSOUT=*                                                 \n"
			+ "//OUTPFILE DD DSN=#DATASET_OUTPUT#,                                    \n"
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n" 
			+ "//*                                                                    \n" 
			+ "//*********************************************************************\n" 
			+ "//* RUN DSNTEP2 UTILITY ON #DB_SSID#.#DB_SCHEMA#                       \n"
			+ "//*********************************************************************\n" 
			+ "//QRYS020  EXEC PGM=IKJEFT01,DYNAMNBR=20,COND=(4,LT)                   \n"
			+ "//STEPLIB  DD DSN=#DSNLOAD#,DISP=SHR                                   \n"
			+ "//         DD DSN=#DB2LOAD#,DISP=SHR                                   \n"
			+ "//SYSTSPRT DD  SYSOUT=*                                                \n"
			+ "//SYSUDUMP DD  SYSOUT=*                                                \n"
			+ "//SYSPRINT DD  DSN=#DATASET_OUTPUT#,                                   \n"
			+ "//     SPACE=#SPACE#,UNIT=#UNIT#,                                      \n"
			+ "//     DISP=(NEW,CATLG,DELETE)                                         \n"
			+ "//SYSTSIN  DD  *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "RUN PROGRAM(DSNTEP2) PLAN(DSNTEP2)                                     \n"
			+ "END                                                                    \n"
			+ "//SYSIN    DD  *                                                       \n"
			+ "SET CURRENT SQLID = '#DB_SCHEMA#';                                     \n"
			+ "#SQL_STMTS#                                                            \n"
			+ "//*                                                                    ";
	
	public static String TEMPLATE_TABLE_UNLOAD_JCL = 
			  "//*********************************************************************\n"
			+ "//* #PROCESS_ID# : #DATE# #TIME#                                       \n"
			+ "//* THIS PROC IS TO UNLOAD DATA FROM A TABLE                           \n"
			+ "//*********************************************************************\n"
			+ "//*                                                                    \n"
			+ "//*********************************************************************\n" 
			+ "//* UNLOAD #TABLE_NAME#                                                \n"
			+ "//*********************************************************************\n" 
			+ "//*********************************************************************\n" 
			+ "//* DELETE UNLOAD DATA SETS                                            \n"
			+ "//*********************************************************************\n" 
			+ "//UNLS010  EXEC PGM=IEFBR14,COND=(4,LT)                                \n"  
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//SYSPUNCH DD DSN=#DATASET_SYSPUNCH#,                                  \n"
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n" 
			+ "//SYSREC00 DD DSN=#DATASET_SYSREC#,                                    \n"
			+ "//       DISP=(MOD,DELETE,DELETE),SPACE=(TRK,(1,1))                    \n" 
			+ "//*                                                                    \n" 
			+ "//*********************************************************************\n" 
			+ "//* RUN DSNTIAUL UTILITY TO UNLOAD #TABLE_NAME#                        \n"
			+ "//*********************************************************************\n" 
			+ "//UNLS020  EXEC PGM=IKJEFT01,DYNAMNBR=20,COND=(4,LT)                   \n"  
			+ "//STEPLIB  DD  DSN=#DSNLOAD#,DISP=SHR                                  \n"  
			+ "//         DD  DSN=#DB2LOAD#,DISP=SHR                                  \n"  
			+ "//SYSTSPRT DD  SYSOUT=*                                                \n"
			+ "//SYSPRINT DD  SYSOUT=*                                                \n"
			+ "//SYSUDUMP DD  SYSOUT=*                                                \n"
			+ "//SYSTSIN  DD  *                                                       \n"
			+ "DSN SYSTEM(#DB_SSID#)                                                  \n"
			+ "RUN PROGRAM(DSNTIAUL) PARM('SQL,1,TOLWARN(YES)') PLAN(DSNTIAUL)        \n"
			+ "END                                                                    \n"
			+ "//SYSPUNCH DD  DSN=#DATASET_SYSPUNCH#,                                 \n"
			+ "//     SPACE=(TRK,(1,1),RLSE),UNIT=#UNIT#,                             \n"
			+ "//     DISP=(NEW,CATLG,DELETE)                                         \n" 
			+ "//SYSREC00 DD  DSN=#DATASET_SYSREC#,                                   \n"
			+ "//     SPACE=#SPACE#,UNIT=#UNIT#,                                      \n"
			+ "//     DISP=(NEW,CATLG,DELETE)                                         \n" 
			+ "//SYSIN    DD  *                                                       \n"
			+ "#UNLOAD_SQL#                                                           \n"
			+ "//*                                                                    "; 
}