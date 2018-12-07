/**
 *  Class: ui.editors.ServerEditorPageOverview
 *  Description: This is the Server Editor (Page Overview).
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 06/18/2010: Code baseline. (V6.0 phase 1)
 *  02. 03/11/2011: Functional enhancements. (V6.0 phase 1)
 *  03. 03/12/2011: Form design enhancements. (V6.0 phase 2)
 *  	1) Change the form title from Server Editor to Server.
 *  	2) Add an expandable help section.
 *  	3) Adjust the table size.
 *  04. 03/18/2011: Form design enhancements. (V6.0 phase 2)
 *  	1) Re-design the layout of the Server section.
 *  	2) Add SWT check and full selection in the table lists.
 *  	3) Add link to remove multiple tables from the list.
 *  05. 03/20/2011: Server save and Table unload actions enhancements. (V6.0 phase 3)
 *  	1) Use deleteServer() and addNewServer() to replace saveServer() to cater for server name and type changes. 
 *  	2) Reset the form after the server is saved to cater for server name and type changes. 
 *  	3) Reset the server after the table unloaded as some attributes may get changed during the unload.
 *  06. 03/21/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  07. 03/22/2011: Split the Server Overview part from the original ServerEditor.class. (V6.0 phase 4)
 *  08. 03/23/2011: Add listeners on all text fields to refresh the MFServer object on every change. (V6.0 phase 4)
 *  09. 04/03/2011: Call the new TestServerConnection.class. (V6.0 beta 1)
 *  10. 03/01/2012: Functional enhancements (V6.0.0)
 *  	1) Add updateSchemaTable() function.
 *  	2) Enhance the text boxes. 
 *  11. 03/09/2012: Functional enhancements (V6.0.0)
 *  	1) Remove the error message when server testing failed. 
 *  	2) Remove the unnecessary server set function calls.
 *  	3) Implement setFocus() function.
 *  12. 11/02/2012: Collateral damages (V6.0.1)
 *  	1) Handle exceptions for schema.getJobBatchId() and schema.getDatasetPrefix().
 *  	2) Change "Logon User" to "Logon User ID" on the panel.
 *  13. 11/07/2012: Add JCL overrides. (V6.1.1) 
 *  14. 11/15/2012: Fix a bug on JCL overridden. (V6.1.3)
 *  15. 08/08/2013: Form design enhancements (V6.3)
 *  	1) Change the form title.
 *  	2) Change the JCL font.
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.actions.SaveServerChangesAction;
import net.sourceforge.datamig4zos.core.TestServerConnection;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.runtime.IJclDefaults;
import net.sourceforge.datamig4zos.ui.wizards.NewSchemaWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;


public class ServerEditorPageOverview extends EditorPart {

	private ServerEditor parent_editor;

	private FormToolkit toolkit;
	private ScrolledForm form;
	private Table table;
	
	private Text txt_name;
	private Text txt_host;
	private Text txt_user;
	private Text txt_pwd;
	private Text txt_ssid;
	private Text txt_jcl;
	
	private Text txt_port;		// v7.0.0
	private CCombo cmb_protocol;	// v7.0.0
	
	private Text txt_db2load;	// v6.1.1
	private Text txt_dsnload;	// v6.1.1
	private Text txt_sortlib;	// v6.1.1
	private Text txt_space;		// v6.1.1
	private Text txt_unit;		// v6.1.1
	
	private TableEditor table_editor;

	private MFServer server;

	public ServerEditorPageOverview(ServerEditor parentEditor) {
		super();
		parent_editor = parentEditor;
	}
	
	public void updateSchemaTable(String schemaName, String newValue, int colIndex) {
		// go thru all checked items and perform the removal
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			if(item.getText(0).equals(schemaName)) {
				item.setText(colIndex, newValue);
				break;
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		server = ((ServerEditorInput) input).getServer();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(final Composite parent) {
		// Create the form
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm()); 			// v6.0.phase4
		setFormText();		// v6.3

		// Add the tool bar - V6.0.phase4
		form.getToolBarManager().add(new SaveServerChangesAction(parent_editor, server));
		form.getToolBarManager().update(true);

		// Create the layout
		ColumnLayout layout = new ColumnLayout();
		layout.maxNumColumns = 2;
		form.getBody().setLayout(layout);

		// Section 1 : Server info
		Section section1 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		// td = new TableWrapData(TableWrapData.FILL_GRAB);
		// td.colspan = 2;
		// section.setLayoutData(td);
		section1.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section1.setText("General Server Information");
		section1.setDescription("This section describes general information about this server.");
		Composite sectionClient = toolkit.createComposite(section1);
		
		GridLayout gl1 = new GridLayout();
		gl1.numColumns = 4; 			// v6.0.phase4	// v7.0.0
		gl1.horizontalSpacing = 30; 	// v6.0.phase2
		sectionClient.setLayout(gl1);
		GridData gd1 = new GridData();
		gd1.widthHint = 180; 			// v6.0.phase2

		// server detail form
		toolkit.createLabel(sectionClient, "Server name: ");
		txt_name = toolkit.createText(sectionClient, server.getServerName());
		txt_name.setTextLimit(8);		// v6.0.0
		txt_name.setLayoutData(gd1);
		txt_name.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setServerName(txt_name.getText().trim());
			}
			
		});
		txt_name.addVerifyListener(new VerifyListener(){	// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)) {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		
		// filler - v7.0.0
		toolkit.createLabel(sectionClient, "");
		toolkit.createLabel(sectionClient, "");
		
		// host name
		toolkit.createLabel(sectionClient, "Host name: ");
		txt_host = toolkit.createText(sectionClient, server.getHostIp());
		txt_host.setLayoutData(gd1);
		txt_host.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setHostIp(txt_host.getText().trim());
			}
			
		});
		txt_host.addVerifyListener(new VerifyListener() { // v6.0.0
			@Override
			public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c) || c == '.') {
					e.doit = true;
				} else {
					e.doit = false;
				}
			}
		});
		
		// port - v7.0.0
		toolkit.createLabel(sectionClient, "Port number: ");
		txt_port = toolkit.createText(sectionClient, String.valueOf(server.getPortNumber()));
		txt_port.setLayoutData(gd1);
		txt_port.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setPortNumber(Integer.parseInt(txt_port.getText().trim()));
			}
			
		});
		txt_port.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isDigit(c)) {
					e.doit = true;
				} else {
					e.doit = false;
				}
			}
		});

		// user name
		toolkit.createLabel(sectionClient, "User name: ");			// v6.0.1
		txt_user = toolkit.createText(sectionClient, server.getLogonUser());
		txt_user.setTextLimit(8);		// v6.0.0
		txt_user.setLayoutData(gd1);
		txt_user.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setLogonUser(txt_user.getText().trim());
			}
			
		});
		txt_user.addVerifyListener(new VerifyListener(){	// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)) {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}	            
		});

		// password
		toolkit.createLabel(sectionClient, "Password: ");
		txt_pwd = toolkit.createText(sectionClient, server.getLogonPwd(), SWT.PASSWORD);
		txt_pwd.setTextLimit(8);		// v6.0.0
		txt_pwd.setLayoutData(gd1);
		txt_pwd.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setLogonPwd(txt_pwd.getText().trim());
			}
			
		});
		
		// file protocol - v7.0.0
		toolkit.createLabel(sectionClient, "File protocol: ");
		cmb_protocol = new CCombo(sectionClient, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		cmb_protocol.setLayoutData(gd1);
		cmb_protocol.add("FTP", 0);
		cmb_protocol.add("FTP TLS/SSL encryption", 1);
		
		cmb_protocol.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cmb_protocol.getSelectionIndex() == 0) {
					server.setFileProtocol("FTP");
					server.setPortNumber(21);
					txt_port.setText("21");
				} else if (cmb_protocol.getSelectionIndex() == 1) {
					server.setFileProtocol("FTPS");
					server.setPortNumber(990);
					txt_port.setText("990");
				}				
			}
			
		});
		
		if (server.getFileProtocol().equals("FTP")) {
			cmb_protocol.select(0);
		} else if (server.getFileProtocol().equals("FTPS")) {
			cmb_protocol.select(1);
		} else {
			cmb_protocol.select(0);
			server.setFileProtocol("FTP");
			server.setPortNumber(21);
			txt_port.setText("21");
		}

		// db2 ssid
		toolkit.createLabel(sectionClient, "DB2 SSID: ");
		txt_ssid = toolkit.createText(sectionClient, server.getDbSsid());
		txt_ssid.setLayoutData(gd1);
		txt_ssid.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setDbSsid(txt_ssid.getText().trim());
			}
			
		});
		txt_ssid.addVerifyListener(new VerifyListener(){	// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)) {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}	            
		});

		// link to test server connection
		Hyperlink link_test = toolkit.createHyperlink(sectionClient,
				"Test server connection", SWT.WRAP);
		link_test.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// server.setHostIp(txt_host.getText()); 		// v6.0.0 removed
				// server.setLogonUser(txt_user.getText());		// v6.0.0 removed
				// server.setLogonPwd(txt_pwd.getText());		// v6.0.0 removed

				// schedule the job - v6.0.beta1
				TestServerConnection job = new TestServerConnection(server);

				job.addJobChangeListener(new JobChangeAdapter() {
					public void done(final IJobChangeEvent event) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (event.getResult().isOK()) {
									MessageDialog.openInformation(
											Display.getDefault().getActiveShell(),
											"Success", "Server connection test to " 
											+ server.getServerName() + " passed!");
								} /*else { -- commented out in V6.0.0
									MessageDialog.openError(Display
											.getDefault().getActiveShell(),
											"Error",
											"Server connection test to " 
											+ server.getServerName() + " failed! "
													+ event.getResult().getMessage());
								}*/
								setFocus();		// v6.0.0
							}
						});
					}
				});

				job.setUser(true);
				job.schedule();

			}
		});

		section1.setClient(sectionClient);
		
		// Section 2 : Schema info - V6.0.phase3
		Section section3 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		// td = new TableWrapData(TableWrapData.FILL_GRAB);
		// td.colspan = 2;
		// section.setLayoutData(td);
		section3.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section3.setText("Schemas");
		section3.setDescription("This section describes the schemas added in this server.");
		sectionClient = toolkit.createComposite(section3);

		GridLayout gl3 = new GridLayout();
		sectionClient.setLayout(gl3);
		GridData gd3 = new GridData(GridData.FILL_BOTH);
		gd3.heightHint = 100;		// v6.1.1

		// schema detail table
		table = toolkit.createTable(sectionClient, SWT.BORDER | SWT.MULTI
				| SWT.CHECK | SWT.FULL_SELECTION);
		table.setLayoutData(gd3);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// define table head
		TableColumn col_schema_name = new TableColumn(table, SWT.NONE);
		col_schema_name.setWidth(150);
		col_schema_name.setText("Schema Name");

		TableColumn col_batch_id = new TableColumn(table, SWT.NONE);
		col_batch_id.setWidth(150);
		col_batch_id.setText("Job Batch ID");

		TableColumn col_ds_prefix = new TableColumn(table, SWT.NONE);
		col_ds_prefix.setWidth(150);
		col_ds_prefix.setText("Dataset Prefix");

		// Create an editor object to use for text editing
		table_editor = new TableEditor(table);
		table_editor.horizontalAlignment = SWT.LEFT;
		table_editor.grabHorizontal = true;

		// add the rows (items)
		for (int i = 0; i < server.getSchemas().length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			DB2Schema schema = server.getSchemas()[i];
			try { // v6.0.1
				item.setText(new String[] { schema.getSchemaName(),
						schema.getJobBatchId(), schema.getDatasetPrefix() });
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// create hyperlink composite
		Composite hyperlinks = toolkit.createComposite(sectionClient);
		GridLayout glaylnk = new GridLayout();
		glaylnk.numColumns = 3;
		hyperlinks.setLayout(glaylnk);

		// link to add a new schema
		Hyperlink link_add = toolkit.createHyperlink(hyperlinks,
				"Add a new schema", SWT.WRAP);
		link_add.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// run the wizard
				NewSchemaWizard wizard = new NewSchemaWizard();
				WizardDialog dialog = new WizardDialog(null, wizard);
				dialog.open();

				// add the row
				if (!wizard.getSchemaName().trim().isEmpty()) {
					try {
						// add the new schema into server object
						DB2Schema schema = new DB2Schema(
								wizard.getSchemaName(), wizard.getJobBatchId(),
								wizard.getDatasetPrefix(), server);
						server.addSchema(schema);

						// add the new schema into the table
						TableItem item = new TableItem(table, SWT.NONE);
						item.setText(new String[] { wizard.getSchemaName(),
								wizard.getJobBatchId(),
								wizard.getDatasetPrefix() });

						// add a new page on the editor
						parent_editor.createSchemaPage(schema);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		// link to delete a schema
		Hyperlink link_delete = toolkit.createHyperlink(hyperlinks,
				"Remove checked schemas", SWT.WRAP);
		link_delete.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// check if any checked item
				int i = 0;
				for (i = 0; i < table.getItemCount(); i++) {
					final TableItem item = table.getItem(i);
					if (item.getChecked()) {
						break;
					}
				}

				if (i == table.getItemCount()) {
					MessageDialog.openWarning(Display.getDefault()
							.getActiveShell(), "Warning",
							"Please check a schema first.");
					return;
				}

				// confirm removal
				if (MessageDialog.openQuestion(Display.getDefault()
						.getActiveShell(), "Confirm Remove",
						"Are you sure you want to remove the checked schemas?")) {
					// initialize the index
					int index = 0;
					int[] indices = new int[table.getItemCount()];

					// go thru all checked items and perform the removal
					for (i = 0; i < table.getItemCount(); i++) {
						final TableItem item = table.getItem(i);
						if (item.getChecked()) {
							try {
								// remove the schema from the server
								DB2Schema schema = new DB2Schema(item
										.getText(0), item.getText(1), item
										.getText(2), server);
								server.removeSchema(schema);

								// remove the page from the editor
								parent_editor.removeSchemaPage(schema);

							} catch (Exception ex) {
								ex.printStackTrace();
							}
							indices[index] = i;
							index++;
						}
					}

					// reset the removal indices - V6.0.phase4
					int[] removal_indices = new int[index];
					for (i = 0; i < index; i++) {
						removal_indices[i] = indices[i];
					}

					// remove the items from the table
					table.remove(removal_indices);
				}
			}
		});

		section3.setClient(sectionClient);

		// Section 3 : JCL Header
		Section section5 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		// td = new TableWrapData(TableWrapData.FILL_GRAB);
		// td.colspan = 2;
		// section.setLayoutData(td);
		section5.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section5.setText("JCL Header");
		section5.setDescription("This section describes the JCL header to be used in the batch jobs on this server.");
		sectionClient = toolkit.createComposite(section5);
		
		GridLayout gl5 = new GridLayout();
		sectionClient.setLayout(gl5);
		GridData gd5 = new GridData(GridData.FILL_BOTH);
		gd5.widthHint = 250;
		gd5.heightHint = 119;

		// database detail table
		txt_jcl = toolkit.createText(sectionClient, server.getJclHeaderDisplay(), 
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		if (txt_jcl.getText().trim().length() == 0) {					// v6.0.0
			server.setJclHeader(IJclDefaults.DEFAULT_JCL_HEADER);
			txt_jcl.setText(IJclDefaults.DEFAULT_JCL_HEADER);
		}
		txt_jcl.setLayoutData(gd5);
		txt_jcl.setFont(new Font(null, "Courier New", 10, SWT.NONE));	// v6.3
		txt_jcl.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setJclHeader(txt_jcl.getText().trim());
			}
			
		});
		txt_jcl.addVerifyListener(new VerifyListener(){		// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				String s = e.text;
	          	e.text = s.toUpperCase();
			}	            
		});

		Hyperlink link_jcl = toolkit.createHyperlink(sectionClient,
				"Reset to default JCL template", SWT.WRAP);
		link_jcl.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				txt_jcl.setText(IJclDefaults.DEFAULT_JCL_HEADER);		// v6.0.0
				server.setJclHeader(IJclDefaults.DEFAULT_JCL_HEADER);	// v6.0.0
			}
		});

		section5.setClient(sectionClient);

		// Section 4 : JCL overrides - v6.1.1
		Section section7 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section7.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section7.setText("JCL Overrides");
		section7.setDescription("This section describes the JCL overrides.");
		sectionClient = toolkit.createComposite(section7);
		
		GridLayout gl7 = new GridLayout();
		gl7.numColumns = 3;
		gl7.horizontalSpacing = 10;
		sectionClient.setLayout(gl7);
		GridData gd7 = new GridData();
		gd7.widthHint = 150;

		// build form
		toolkit.createLabel(sectionClient, "DB2 Load Library: ");
		
		String default_db2load = "";
		try {
			default_db2load = server.getParamDb2load();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_db2load.isEmpty()) {
			default_db2load = "<DEFAULT>";
			server.setParamDb2load(default_db2load);
		}
		txt_db2load = toolkit.createText(sectionClient, default_db2load);
		txt_db2load.setLayoutData(gd7);
		txt_db2load.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setParamDb2load((txt_db2load.getText().trim()));
			}
			
		});
		txt_db2load.addVerifyListener(new VerifyListener(){
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '@' || c == '$' || c == '#'
						|| c== '<' || c== '>') {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		toolkit.createLabel(sectionClient, "* Use <DEFAULT> to represent Default DB2 Load Library");

		toolkit.createLabel(sectionClient, "DSN Load Library: ");
		String default_dsnload = "";
		try {
			default_dsnload = server.getParamDsnload();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_dsnload.isEmpty()) {
			default_dsnload = "<DEFAULT>";
			server.setParamDsnload(default_dsnload);
		}
		txt_dsnload = toolkit.createText(sectionClient, default_dsnload);
		txt_dsnload.setLayoutData(gd7);
		txt_dsnload.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setParamDsnload((txt_dsnload.getText().trim()));
			}
			
		});
		txt_dsnload.addVerifyListener(new VerifyListener(){
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '@' || c == '$' || c == '#'
						|| c== '<' || c== '>') {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		toolkit.createLabel(sectionClient, "* Use <DEFAULT> to represent Default DSN Load Library");

		toolkit.createLabel(sectionClient, "Sort Library: ");
		String default_sortlib = "";
		try {
			default_sortlib = server.getParamSortlib();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_sortlib.isEmpty()) {
			default_sortlib = "<DEFAULT>";
			server.setParamSortlib(default_sortlib);
		}
		txt_sortlib = toolkit.createText(sectionClient, default_sortlib);
		txt_sortlib.setLayoutData(gd7);
		txt_sortlib.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setParamSortlib((txt_sortlib.getText().trim()));
			}
			
		});
		txt_sortlib.addVerifyListener(new VerifyListener(){
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '@' || c == '$' || c == '#'
						|| c== '<' || c== '>') {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		toolkit.createLabel(sectionClient, "* Use <DEFAULT> to represent Default Sort Library");

		toolkit.createLabel(sectionClient, "Default Space Allocation: ");
		String default_space = "";
		try {
			default_space = server.getParamSpace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_space.isEmpty()) {
			default_space = "<DEFAULT>";
			server.setParamSpace(default_space);
		}
		txt_space = toolkit.createText(sectionClient, default_space);
		txt_space.setLayoutData(gd7);
		txt_space.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setParamSpace((txt_space.getText().trim()));
			}
			
		});
		txt_space.addVerifyListener(new VerifyListener(){
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '(' || c == ')' || c == ','		// v6.1.3
						|| c== '<' || c== '>') {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		toolkit.createLabel(sectionClient, "* Use <DEFAULT> to represent Default Space Allocation");

		toolkit.createLabel(sectionClient, "Default DASD unit: ");
		String default_unit = "";
		try {
			default_unit = server.getParamUnit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_unit.isEmpty()) {
			default_unit = "<DEFAULT>";
			server.setParamUnit(default_unit);
		}
		txt_unit = toolkit.createText(sectionClient, default_unit);
		txt_unit.setLayoutData(gd7);
		txt_unit.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				server.setParamUnit((txt_unit.getText().trim()));
			}
			
		});
		txt_unit.addVerifyListener(new VerifyListener(){
			@Override
	        public void verifyText(VerifyEvent e) {
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '(' || c == ')' || c == ','		// v6.1.3
						|| c== '<' || c== '>') {
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}
		});
		toolkit.createLabel(sectionClient, "* Use <DEFAULT> to represent Default DASD Unit");

		section7.setClient(sectionClient);
		
		// set vertical spacings - V6.0.phase4
		section5.descriptionVerticalSpacing = section7.getTextClientHeightDifference();
		section3.descriptionVerticalSpacing = section7.getTextClientHeightDifference();

		// always call to let toolkit to decide the "look"
		toolkit.paintBordersFor(parent);
	}

	@Override
	public void setFocus() {
		// set the user and pwd text boxes - v6.0.0
		if (!txt_user.getText().equals(server.getLogonUser())) {
			txt_user.setText(server.getLogonUser());
		}

		if (!txt_pwd.getText().equals(server.getLogonPwd())) {
			txt_pwd.setText(server.getLogonPwd());
		}
	}
	
	public void setFormText() {
		// set the form text - v6.3
		form.setText(server.getServerName() + " Overview");
	}
	
}
