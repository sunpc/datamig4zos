/**
 *  Class: ui.editors.ServerEditorPageSchema
 *  Description: This is the Server Editor (Page Schema).
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
 *  07. 03/22/2011: Split the Table setup part from the original ServerEditor.class. (V6.0 phase 4)
 *  08. 04/03/2011: Call the new UnloadTableList.class. (V6.0 beta 1)
 *  09. 03/01/2012: Functional enhancements (V6.0.0)
 *  	1) Add JCL Settings section.
 *  	2) Enhance the text boxes. 
 *  10. 03/09/2012: Remove the message dialog on table list unload. (V6.0.0)
 *  11. 11/01/2012: Allow <USERID> in Job Batch ID and Data set Prefix. (V6.0.1)
 *  12. 11/07/2012: Set the schema to <USERID> when blank. (V6.1.1)
 *  13. 08/06/2013: Fix DB2Table constructor. (V6.3)
 *  14. 08/08/2013: Form design enhancements (V6.3)
 *  	1) Change the form title.
 *  	2) Change the JCL font.
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.actions.SaveServerChangesAction;
import net.sourceforge.datamig4zos.core.UnloadTableList;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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


public class ServerEditorPageSchema extends EditorPart {
	
	private ServerEditor parent_editor;
	
	private FormToolkit toolkit;
	private ScrolledForm form;

	private DB2Schema schema;

	public ServerEditorPageSchema(ServerEditor parentEditor, DB2Schema schema) {
		super();
		parent_editor = parentEditor;
		this.schema = schema;
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
		toolkit.decorateFormHeading(form.getForm()); // v6.0.phase4
		setFormText();		// v6.3

		// Add the tool bar - V6.0.phase4
		form.getToolBarManager().add(new SaveServerChangesAction(parent_editor, schema.getServer()));
		form.getToolBarManager().update(true);

		// Create the layout
		ColumnLayout layout = new ColumnLayout();
		layout.maxNumColumns = 2;
		form.getBody().setLayout(layout);
		
		// Section 1 : Schema info - v6.0.0
		Section section1 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section1.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section1.setText("General Schema Information");
		section1.setDescription("This section describes general information about this schema.");
		Composite sectionClient = toolkit.createComposite(section1);
		
		GridLayout gl1 = new GridLayout();
		gl1.numColumns = 3;				// v6.0.1
		gl1.horizontalSpacing = 10;
		sectionClient.setLayout(gl1);
		GridData gd1 = new GridData();
		gd1.widthHint = 150;

		// schema detail form
		toolkit.createLabel(sectionClient, "Schema Name: ");
		final Text txt_name = toolkit.createText(sectionClient, schema.getSchemaName());
		toolkit.createLabel(sectionClient, "");		// v6.0.1
		txt_name.setLayoutData(gd1);
		txt_name.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String schema_name = txt_name.getText().trim();				
				
				// update overview page
				parent_editor.updateSchemaTable(schema.getSchemaName(), schema_name, 0);
				
				// update schema
				schema.setSchemaName(schema_name);
				parent_editor.setActiveSchemaPageText(schema_name);
				// form.setText(schema.getServer().getServerName() + " - " + schema_name);	// v6.3
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

		toolkit.createLabel(sectionClient, "Job Batch ID: ");
		String default_batch_id = "";														// v6.0.1
		try {
			default_batch_id = schema.getJobBatchId();
		} catch (Exception ex) {
			ex.printStackTrace();
		}										
		if (default_batch_id.isEmpty()) {													// v6.0.1
			default_batch_id = "<USERID>";													// v6.0.1
			schema.setJobBatchId(default_batch_id);											// v6.1.1
		}
		final Text txt_batch_id = toolkit.createText(sectionClient, default_batch_id); 		// v6.0.1
		toolkit.createLabel(sectionClient, "* Use <USERID> to represent Logon User ID");	// v6.0.1
		txt_batch_id.setTextLimit(8);														// v6.0.0
		txt_batch_id.setLayoutData(gd1);
		txt_batch_id.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String batch_id = txt_batch_id.getText().trim();				
				
				// update overview page
				parent_editor.updateSchemaTable(schema.getSchemaName(), batch_id, 1);
				
				// update schema
				schema.setJobBatchId(batch_id);
			}
			
		});
		txt_batch_id.addVerifyListener(new VerifyListener(){	// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c== '<' || c== '>') {				// v6.0.1
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}	            
		});
		

		toolkit.createLabel(sectionClient, "Dataset Prefix: ");
		String default_ds_prefix = "";														// v6.0.1
		try {
			default_ds_prefix = schema.getDatasetPrefix();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		if (default_ds_prefix.isEmpty()) {													// v6.0.1
			default_ds_prefix = "<USERID>";													// v6.0.1
			schema.setDatasetPrefix(default_ds_prefix);										// v6.1.1
		}
		final Text txt_ds_prefix = toolkit.createText(sectionClient, default_ds_prefix);	// v6.0.1
		toolkit.createLabel(sectionClient, "* Use <USERID> to represent Logon User ID");	// v6.0.1
		txt_ds_prefix.setTextLimit(25);														// v6.0.0
		txt_ds_prefix.setLayoutData(gd1);
		txt_ds_prefix.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String ds_prefix = txt_ds_prefix.getText().trim();				
				
				// update overview page
				parent_editor.updateSchemaTable(schema.getSchemaName(), ds_prefix, 2);
				
				// update schema
				schema.setDatasetPrefix(ds_prefix);
			}
			
		});
		txt_ds_prefix.addVerifyListener(new VerifyListener(){	// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				char c = e.character;
				if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
						|| c == '.' || c == '@' || c == '$' || c == '#'
						|| c== '<' || c== '>') {				// v6.0.1
					e.doit = true;
					String s = e.text;
		          	e.text = s.toUpperCase();
				} else {
					e.doit = false;
				}
			}	            
		});

		section1.setClient(sectionClient);
		
		// Section 2 : JCL Settings - v6.0.0
		Section section2 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section2.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section2.setText("JCL Settings");
		section2.setDescription("This section describes the additional JCL settings to be used in the batch jobs on this schema.");
		sectionClient = toolkit.createComposite(section2);
		
		GridLayout gl2 = new GridLayout();
		sectionClient.setLayout(gl2);
		GridData gd2 = new GridData(GridData.FILL_BOTH);
		gd2.widthHint = 250;
		gd2.heightHint = 173;

		// database detail table
		final Text txt_jcl = toolkit.createText(sectionClient, schema.getJclSettingsDisplay(), 
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		txt_jcl.setLayoutData(gd2);
		txt_jcl.setFont(new Font(null, "Courier New", 10, SWT.NONE));	// v6.3
		txt_jcl.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				schema.setJclSettings(txt_jcl.getText().trim());
			}
			
		});
		txt_jcl.addVerifyListener(new VerifyListener(){		// v6.0.0
			@Override
	        public void verifyText(VerifyEvent e) {                
				String s = e.text;
	          	e.text = s.toUpperCase();
			}	            
		});

		section2.setClient(sectionClient);

		// Section 3 : Server Database info
		Section section3 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section3.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section3.setText("Tables");
		section3.setDescription("This section describes the tables locate on this server and schema.");
		sectionClient = toolkit.createComposite(section3);
		
		GridLayout gl3 = new GridLayout(); 
		sectionClient.setLayout(gl3);
		GridData gd3 = new GridData(GridData.FILL_BOTH);
		gd3.heightHint = 300;
	    
	    // database detail table
		final Table table = toolkit.createTable(sectionClient, 
				SWT.BORDER | SWT.MULTI | SWT.CHECK | SWT.FULL_SELECTION);
		table.setLayoutData(gd3);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		// define table head 
		TableColumn col_tbname = new TableColumn(table, SWT.NONE);
		col_tbname.setWidth(250);
		col_tbname.setText("Table");

		TableColumn col_dbname = new TableColumn(table, SWT.NONE);
		col_dbname.setWidth(100);
		col_dbname.setText("Database");

		TableColumn col_tsname = new TableColumn(table, SWT.NONE);
		col_tsname.setWidth(100);
		col_tsname.setText("Table Space");
		
		// Create an editor object to use for text editing
	    final TableEditor table_editor = new TableEditor(table);
	    table_editor.horizontalAlignment = SWT.LEFT;
	    table_editor.grabHorizontal = true;	    
	    
	    // add the rows (items)
		for (int i = 0; i < schema.getTables().length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			DB2Table db2_table = schema.getTables()[i];
			item.setText(new String[] {db2_table.getTableName(), db2_table.getDbName(), db2_table.getTsName()});
		}
		
		// create hyperlink composite
		Composite hyperlinks = toolkit.createComposite(sectionClient);
		GridLayout glaylnk = new GridLayout(); 
		glaylnk.numColumns = 3;
		hyperlinks.setLayout(glaylnk);
		
		// link to unload table lists
		Hyperlink link_unload = toolkit.createHyperlink(hyperlinks,
				"Unload table list", SWT.WRAP);
		link_unload.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// run action to unload the table list
				// schedule the job - v6.0.beta1 rewrite
				UnloadTableList job = new UnloadTableList(schema);

				job.addJobChangeListener(new JobChangeAdapter() {
					public void done(final IJobChangeEvent event) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (event.getResult().isOK()) {
									// reset swt table from server.table_list
									table.removeAll();
									for (int i = 0; i < schema.getTables().length; i++) {
										TableItem item = new TableItem(table,
												SWT.NONE);
										DB2Table db2_table = schema.getTables()[i];
										item.setText(new String[] {
												db2_table.getTableName(),
												db2_table.getDbName(),
												db2_table.getTsName() });
									}

									/*MessageDialog		-- removed in v6.0.0
											.openInformation(Display
													.getDefault()
													.getActiveShell(),
													"Success",
													"Table list unloaded successfully from " 
													+ schema.getSchemaName() + "!");*/
								} /*else {		-- removed in v6.0.0
									MessageDialog.openError(Display
											.getDefault().getActiveShell(),
											"Error",
											"Failed to unload the table list from " 
													+ schema.getSchemaName() + "! "
													+ event.getResult().getMessage());
								}*/
							}
						});
					}
				});

				job.setUser(true);
				job.schedule();

			}
		});
		
		// link to delete a table - v6.0.phase2
		Hyperlink link_delete = toolkit.createHyperlink(hyperlinks, "Remove checked tables",
				SWT.WRAP);
		link_delete.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// check if any checked item
				int i = 0;
				for (i=0; i<table.getItemCount(); i++) {
					final TableItem item = table.getItem(i);
					if(item.getChecked()) {
						break;
					}
				}
				
				if(i==table.getItemCount()) {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(), 
							"Warning", "Please check a table first.");
					return;
				}
				
				// confirm removal
				if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
						"Confirm Remove", "Are you sure you want to remove the checked tables?")) {
					// initialize the index
					int index = 0;
					int[] indices = new int[table.getItemCount()];
					
					// go thru all checked items and perform the removal
					for (i=0; i<table.getItemCount(); i++) {
						final TableItem item = table.getItem(i);
						if(item.getChecked()) {
							try {
								schema.removeTable(new DB2Table(item.getText(0), schema));	// v6.3
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							indices[index] = i;
							index++;
						}
					}
					
					// reset the removal indices - V6.0.phase4
					int[] removal_indices = new int[index];
					for (i=0; i<index; i++) {
						removal_indices[i] = indices[i];
					}
					
					// remove the items from the table
					table.remove(removal_indices);
				}
			}
		});
		
		section3.setClient(sectionClient);

		// always call to let toolkit to decide the "look"
		toolkit.paintBordersFor(parent);
	}

	@Override
	public void setFocus() {

	}
	
	public void setFormText() {
		// set the form text - v6.3
		form.setText(schema.getServer().getServerName() + " - " + schema.getSchemaName());
	}

}
