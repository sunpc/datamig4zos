/**
 *  Class: core.base.CoreJob
 *  Description: This is the base of a core job.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 04/03/2011: Code baseline. (V6.0 beta 1)
 *  Major code copied from the original ProcessDataMigration.class.
 *  2. 04/05/2011: Add smart process lookup logic. (V6.0 beta 1)
 *  3. 04/15/2011: Handle RC=8 and set status for New. (V6.0 beta 1)
 *  4. 03/02/2012: Defect fixes (V6.0.0)
 *  	1) Fix a bug that the program shall handle blank process reference.
 *  	2) Fix a bug that on handling rc=8 to avoid array overflow.
 *  	3) Fix the finish line from "Completed Successfully" to "Completed".
 *  5. 03/09/2012: Fix the cancel function to reflect the correct step. (V6.0.0)
 *  6. 03/09/2012: Call to User Logon Wizard when log on is failed. (V6.0.0)
 *  7. 10/30/2012: Call to User Logon Wizard on any connection error. (V6.0.1)
 *  8. 11/14/2014: Sleep 3 seconds instead of 10. (V6.3.1)
 */
package net.sourceforge.datamig4zos.core.base;


import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.objects.DMGProcess;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.ui.wizards.UserLogonWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.enterprisedt.util.debug.Level;
import com.enterprisedt.util.debug.Logger;

/**
 * @author SunPC
 *
 */
public class CoreJob extends Job {
	
	private Logger log = Logger.getLogger(CoreJob.class);
	
	private String proc_name = "";

	private MFServer[] servers = new MFServer[0];
	private DMGProcess[] procs = new DMGProcess[0];
	private int[] proc_returns = new int[0];
	
	private Table[] tables = new Table[0];
	private String[] table_ids = new String[0];
	private int[] table_counts = new int[0];
	
	private String[] restart_rules = new String[0];
	
	private int i = 0, i_save = -1;
	
	private boolean server_connected = false;
	private boolean logon_retry_wanted = true;
	private String logon_error_msg = "";

	public CoreJob(String jobName) {
		this(jobName, jobName);
	}

	public CoreJob(String jobName, String procName) {
		super(jobName);
		this.proc_name = procName;

		Logger.setLevel(Level.INFO);
	}
	
	public String getProcName() {
		return proc_name;
	}
	
	public Logger getLogger() {
		return log;
	}
	
	public void setLoggerClass(Class<?> c) {
		this.log = Logger.getLogger(c);
	}

	public void setServers(MFServer[] servers) {
		this.servers = servers;
	}
	
	public DMGProcess[] getProcs() {
		return procs;
	}
	
	public void setProcs(DMGProcess[] procs) {
		this.procs = procs;
		
		proc_returns = new int[procs.length];
		
		for(int n = 0; n < proc_returns.length; n++) {
			proc_returns[n] = -1;
		}
	}

	public void setProcs(DMGProcess[] procs, Table[] tables, String[] tableIds) {
		setProcs(procs);
		this.tables = tables;
		this.table_ids = tableIds;
	}

	public void setRestartRules(String[] restartRules) {
		this.restart_rules = restartRules;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		log.info(proc_name + " Started");
		monitor.beginTask(proc_name + " Started", 100);
		
		try {
			// initialize the DMGProcess array
			Display.getDefault().syncExec(new Runnable() {
	               public void run() {
	            	   table_counts = new int[tables.length];

	            	   for (int k = 0; k < table_counts.length; k++) {
	            		   table_counts[k] = tables[k].getItemCount();
	            	   }
	               }
			});

			// if no server, return ok status
			if (servers.length == 0) {
				monitor.done();
				return Status.OK_STATUS;
			}
			
			// process start
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
			// connect to the servers
			int avg_conn_perc = (procs.length > 0 ? 20 : 100) / servers.length;
			
			for (int k = 0; k < servers.length; k++) {
				// sub task
				monitor.subTask("Connecting to the server " + servers[k].getServerName());
				
				// connect to server - v6.0.0
				final MFServer tmp_server = servers[k];
				
				server_connected = false;
				logon_retry_wanted = true;
				logon_error_msg = "";
				
				while (!server_connected && logon_retry_wanted) {
					try {
						tmp_server.connectServer();
						server_connected = true;
					} catch (Exception ex) {
						logon_error_msg = ex.getMessage();

						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								// ask if retry
								logon_retry_wanted = MessageDialog.openQuestion(
												Display.getDefault().getActiveShell(),
												"Connecting to " + tmp_server.getServerName(),
												"Could not connect to "
														+ tmp_server.getServerName()
														+ "; the exact message was: \n\n"
														+ logon_error_msg
														+ "\n\n"
														+ "Do you want to try again?");

								// call the wizard on any connection error - v6.0.1
								if (logon_retry_wanted) {
									// run the wizard
									UserLogonWizard wizard = new UserLogonWizard(tmp_server);
									WizardDialog dialog = new WizardDialog(null, wizard);
									dialog.open();

									// update the user details
									if (wizard.isFinished()) {
										try {
											tmp_server.resetUserPwd(wizard.getUser().trim(), 
													wizard.getPwd().trim());
										} catch (Exception ex1) {
											logon_retry_wanted = false;
											ex1.printStackTrace();
										}
									} else {
										logon_retry_wanted = false;
									}
								}

								// close connection
								try {
									tmp_server.closeConnection();
								} catch (Exception ex2) {
									logon_retry_wanted = false;
									ex2.printStackTrace();
								}
							}
						});

					}
				}
				
				// if server is failed on connection - v6.0.0
				if (!server_connected) {
					throw new Exception(logon_error_msg);
				}
				
				// handle is canceled
				if (monitor.isCanceled()) {
					tmp_server.closeConnection();
					throw new InterruptedException();
				}
				
				//avg_conn_perc%
				monitor.worked(avg_conn_perc);
			}
			
			// if no process, return ok status
			if (procs.length == 0) {
				// close the server and complete the migration
				for (int k = 0; k < servers.length; k++) {
					servers[k].closeConnection();
				}
				
				// process done
				monitor.done();
				log.info(proc_name + " Completed Successfully");
				return Status.OK_STATUS;
			}
			
			// define the average completion %
			int total_completed_perc = 20;
			int avg_completed_perc = 80 / procs.length;
			
			// set all status - first loop
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					for (int k = 0; k < table_counts.length; k++) {
						for (int t = 0; t < table_counts[k]; t++) {
							for (int p = 0; p < procs.length; p++) {
								if (procs[p].getProcessRef().equals(table_ids[k] + "." + t)) {
									procs[p].setProcessStatus(tables[k].getItem(t).getText(1));
								}
							}
						}
					}
				}
			});

			// go thru all processes
			log.info("Started to launch all processes");
			
			// Get a Runtime object for Java Garbage Collection - v6.0.beta1
			Runtime r = Runtime.getRuntime();
			r.gc();
			
			while (true) {
				// see how much memory we have now
				r.gc();
				log.info("Available memory: " + r.freeMemory() + " bytes");
				
				// retain the connections to server - solution from V5.0 RC2
				for (int k = 0; k < servers.length; k++) {
					servers[k].listFile(null);
				}
				
				// sync DMGProcess array from user tables - get status
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						for (int k = 0; k < table_counts.length; k++) {
							for (int t = 0; t < table_counts[k]; t++) {
								for (int p = 0; p < procs.length; p++) {
									if (procs[p].getProcessRef().equals(table_ids[k] + "." + t)
											&& (tables[k].getItem(t).getText(1).startsWith("Canceled") 
													|| tables[k].getItem(t).getText(1).startsWith("Restarted"))) {
										procs[p].setProcessStatus(tables[k].getItem(t).getText(1));
									}
								}

								// set initial status - v6.0 beta 1
								if (tables[k].getItem(t).getText(1).startsWith("New")) {
									tables[k].getItem(t).setText(1, "Starting");
								} else if (tables[k].getItem(t).getText(1).startsWith("Restarted")) {
									tables[k].getItem(t).setText(1, "Restarting");
								}
							}
						}
					}
				});
				
				// look up the next available process, where return code is -1
				// or in restarted status.
				log.info("Looking for the next available process");
				i = 0;
				for (; i < procs.length; i++) {
					System.out.println(procs[i].getProcessId() + " # " + i 
							+ ": rc = " + procs[i].getReturnCode()
							+ "; status = " + procs[i].getProcessStatus());
					
					if ((procs[i].getReturnCode() == -1
							&& !procs[i].getProcessStatus().startsWith("Completed")
							&& !procs[i].getProcessStatus().startsWith("Canceled")
							&& !procs[i].getProcessStatus().startsWith("Failed"))
							|| procs[i].getProcessStatus().startsWith("Restarted")) {

						// if restarted, run only the steps after the restarted step
						if (procs[i].getProcessStatus().startsWith("Restarted")) {
							String status = procs[i].getProcessStatus();
							String name = procs[i].getProcessName();
							
							// always run init and finish
							if (name.equals("Init") || name.equals("Finish")) {
								break;
							}
							
							// go thru restart rules
							boolean is_restart_allowed = false;
							
							for (int x = 0; x < restart_rules.length; x++) {
								if (status.equals("Restarted from " + restart_rules[x])) {
									for (int y = x; y < restart_rules.length; y++) {
										if (name.equals(restart_rules[y])) {
											is_restart_allowed = true;
										}
									}
								}
							}
							
							// if restart is allowed according to the restart rules,
							// break and process it;
							// else, set the status as Completed. - v6.0 beta 1
							if (is_restart_allowed) {
								break;
							} else {
								procs[i].setProcessStatus("Completed");
							}
							
						} else {
							// if not a restarted process, run or skip it.
							// smart process lookup - v6.0 beta 1
							if (i < i_save) {								
								// if the original index is less than the last one
								// move the index to the last one
								i = i_save - 1;
							} else if (i == i_save) {
								// if the original index equals to the last one 
								// skip it and try to process from the last one's next proc id
								for (i = i_save + 1; i < procs.length; i++) {
									if (!procs[i].getProcessId().equals(procs[i_save].getProcessId())) {
										i--;
										break;
									}
								}
								
								// if cannot skip, wait and go back to the top
								if (i == procs.length) {
									// reset i and i_save
									i = -1;
									i_save = -1;
									
									// compare and record the return code
									boolean is_any_diff = false;
									for (int n = 0; n < proc_returns.length; n++) {
										if (proc_returns[n] != procs[n].getReturnCode()) {
											proc_returns[n] = procs[n].getReturnCode();
											is_any_diff = true;
										}
									}
									
									// wait for 3 seconds if no diff return found
									if (!is_any_diff) {
										log.info("Waiting for 3 seconds");				// v6.3.1
										Thread.sleep(3000);	// sleep 3 seconds			// v6.3.1
									}
								}
							} else {
								// if the original index is larger than the last one
								i_save = i;

								// process the current i
								break;
							}
						}
					}
				}

				// check if there is any unfinished process
				boolean is_all_done = true;
				for (int j = 0; j < procs.length; j++) {
					if ((procs[j].getReturnCode() == -1
							&& !procs[j].getProcessStatus().startsWith("Completed")
							&& !procs[j].getProcessStatus().startsWith("Canceled")
							&& !procs[j].getProcessStatus().startsWith("Failed"))
							|| procs[j].getProcessStatus().startsWith("Restarted"))  {
						is_all_done = false;
					}
				}
				
				// if the index moves to the last and all process completed
				// then break, else continue
				if (i == procs.length) {
					i_save = -1;
					if (is_all_done)
						break;
					else
						continue;
				}
				
				// determine if the monitor is canceled.
				if (monitor.isCanceled() && !procs[i].getProcessName().equals("Finish")) {
					log.info("Monitor canceled by user");
					for (; i < procs.length; i++) {
						if (!procs[i].getProcessName().equals("Finish")
								&& procs[i].getReturnCode() == -1) {		// v6.0.0
							procs[i].setProcessStatus("Canceled in " + procs[i].getProcessName());
						}
					}
					
					continue;
				}
				
				// set the monitor task and subtask names
				monitor.setTaskName(procs[i].getTaskDesc());
				monitor.subTask(procs[i].getProcessDesc());
				
				// run the process.
				procs[i].run();
				
				// review the return code. 
				// if the return code is 8, set the status of all processes 
				// with same proc id and return code=-1 as failed with return code 8, 
				// except the finish one.
				if (procs[i].getReturnCode() != -1) {
					log.info("Finished process " + procs[i].getProcessId() + ", rc = " + procs[i].getReturnCode());
				}
				
				// handle rc 8 - V6.0 beta 1
				if (procs[i].getReturnCode() == 8) {
					String proc_id = procs[i].getProcessId();
					String proc_status = procs[i].getProcessStatus();
					for (int j = i; j < procs.length; j++) {	// v6.0.0
						if (procs[j].getProcessId().equals(proc_id)
								&& !procs[j].getProcessName().equals("Finish")) {
							procs[j].setProcessStatus(proc_status);
						}
					}
				}

				// sync DMGProcess array from user tables - get status
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						for (int k = 0; k < table_counts.length; k++) {
							for (int t = 0; t < table_counts[k]; t++) {
								for (int p = 0; p < procs.length; p++) {
									if (procs[p].getProcessRef().equals(table_ids[k] + "." + t)
											&& (tables[k].getItem(t).getText(1).startsWith("Canceled") 
													|| tables[k].getItem(t).getText(1).startsWith("Restarted"))) {
										procs[p].setProcessStatus(tables[k].getItem(t).getText(1));
									}
								}
							}
						}
					}
				});
				
				// sync DMGProcess array to user tables - set status
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// get the row index
						if (procs[i].getProcessRef().length() >= 3) {	// v6.0.0
							int row_idx = Integer.parseInt(procs[i].getProcessRef().substring(2));
							
							// determine the status - take the first canceled or failed if there is one
							for (int s = 0; s < procs.length; s++) {
								if (procs[s].getProcessRef().equals(procs[i].getProcessRef())
										&& (procs[s].getProcessStatus().startsWith("Canceled")
												|| procs[s].getProcessStatus().startsWith("Failed"))) {
									procs[i].setProcessStatus(procs[s].getProcessStatus());
									break;
								}
							}
							
							// set the status
							for (int t = 0; t < table_counts.length; t++) {
								if (procs[i].getProcessRef().startsWith(table_ids[t])) {
									tables[t].getItem(row_idx).setText(1, procs[i].getProcessStatus());
								}
							}
						}
					}
				});
				
				// set the completion %
				int expected_completed_perc = 20;
				for (int k = 0; k < procs.length; k++) {
					if (procs[k].getReturnCode() != -1
							&& !procs[k].getProcessStatus().startsWith("Restarted")) {
						expected_completed_perc += avg_completed_perc;
					}
					
					int work_completed_perc = expected_completed_perc - total_completed_perc;
					
					if (work_completed_perc > 0) {
						monitor.worked(work_completed_perc);
						total_completed_perc = expected_completed_perc;
					}
				}
				
				log.info(expected_completed_perc + "% completed");
			}

			// close the server and complete the migration
			for (int k = 0; k < servers.length; k++) {
				servers[k].closeConnection();
			}
			
			// handle cancel - last loop
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
		} catch (InterruptedException ex) {
			log.error(proc_name + " Canceled");
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(proc_name + " Failed");
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 8, ex.getMessage(), ex);
		}
		
		monitor.done();		
		log.info(proc_name + " Completed");		// v6.0.0
		return Status.OK_STATUS;
	}

}
