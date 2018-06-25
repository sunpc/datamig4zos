/**
 *  Class: ui.editors.RequestEditor
 *  Description: This is the Request Editor.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/12/2011: Code baseline. (V6.0 phase 2)
 *  02. 03/17/2011: Add polishTable() method. (V6.0 phase 2)
 *  03. 03/19/2011: Get the wizard inputs from the editor input. (V6.0 phase 2)
 *  04. 03/20/2011: Collapse damage for new data model objects. (V6.0 phase 3)
 *  05. 03/22/2011: Add DB2Schema on the panel. (V6.0 phase 4)
 *  06. 03/22/2011: Form UI design enhancements. (V6.0 phase 4)
 *  07. 03/24/2011: Limit the size of the combos. (V6.0 phase 5)
 *  08. 03/29/2011: Process control enhancements. (V6.0 phase 6)
 *  08. 04/01/2011: Functional enhancements. (V6.0 phase 6)
 *  	1) Re-word Cancelled to Canceled.
 *		2) Not allow to delete the items while processing.
 *  	3) Rename the form from New Data Migration Request to Data Migration Request.
 *  09. 04/15/2011: Enhance the table and pass the editor input to core. (V6.0 beta 1)
 *  10. 03/07/2012: Add name replace and sql update links. (V6.0.0)
 *  11. 03/09/2012: Handle Canceled in Init. (V6.0.0)
 *  12. 11/12/2012: Adjust polishTable() method to cater for the process change. (V6.1.2)
 *  13. 08/17/2013: Change the table_items to receive filters from RequestEditorInput. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.editors;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.datamig4zos.actions.StartDataMigrationAction;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;
import net.sourceforge.datamig4zos.ui.wizards.BuildUnloadWizard;
import net.sourceforge.datamig4zos.ui.wizards.ReplaceNamesWizard;
import net.sourceforge.datamig4zos.util.TextProcessor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;


public class RequestEditor extends EditorPart {

	private RequestEditorInput input;
	private FormToolkit toolkit;
	private ScrolledForm form;
	
	private Label lbl_status;
	private Label lbl_upt_time;
	
	private CCombo cmb_source_server;
	private CCombo cmb_source_schema;
	private CCombo cmb_target_server;
	private CCombo cmb_target_schema;

	private Table tbl_tables;
	private Table tbl_datasets;
	
	private Text txt_desc;
	
	private Hyperlink lnk_add_table;
	private Hyperlink lnk_replace_tables;		// v6.0.0
	private Hyperlink lnk_build_unload;			// v6.0.0
	private Hyperlink lnk_add_dataset;
	private Hyperlink lnk_replace_datasets;		// v6.0.0

	public RequestEditor() {

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
		if (!(input instanceof RequestEditorInput)) {
			throw new RuntimeException("Unexpected request editor input");
		}
		
		setSite(site);
		setInput(input);
		this.input = (RequestEditorInput) input;
		setPartName("Data Migration Request");			// v6.0.phase6
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
		form.setText(this.getPartName());					// v6.0.phase6
		toolkit.decorateFormHeading(form.getForm());		// v6.0.phase4
		
		// Create the layout
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		TableWrapData td = new TableWrapData();

		// Status and last upt time label - v6.0.phase6
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		toolkit.createLabel(form.getBody(), "Status: ");
		lbl_status = toolkit.createLabel(form.getBody(), "DRAFT          ");
		toolkit.createLabel(form.getBody(), "Last Updated Time: ");
		lbl_upt_time = toolkit.createLabel(form.getBody(), formatter.format(cal.getTime()));
		
		layout.numColumns = 2;
		td.colspan = 2;

		// Description field
		toolkit.createLabel(form.getBody(), "Description: ");
		txt_desc = toolkit.createText(form.getBody(), input.getDesc());
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		txt_desc.setLayoutData(td);

		// Help: expandable composite - V6.0.phase4 removed
		/* ExpandableComposite ec = toolkit.createExpandableComposite(form
				.getBody(), ExpandableComposite.TREE_NODE
				| ExpandableComposite.CLIENT_INDENT);
		ec.setText("Help");
		String ctext = "We will now create a somewhat long text so that \n"
				+ "we can use it as content for the expandable composite. \n"
				+ "Expandable composite is used to hide or show the text using the \n"
				+ "toggle control.";
		Label client = toolkit.createLabel(ec, ctext, SWT.WRAP);
		ec.setClient(client);
		td = new TableWrapData();
		td.colspan = 2;
		ec.setLayoutData(td);
		ec.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		}); */

		// Section 1: Servers
		Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Servers and Schemas");
		section.setDescription("This section describes the servers and schemas to be " +
				"migrated in this request.");
		Composite sectionClient = toolkit.createComposite(section);
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		sectionClient.setLayout(gl);
		GridData gd = new GridData();
		gd.widthHint = 80;

		// combo - source server
		toolkit.createLabel(sectionClient, "Source:\tServer");
		cmb_source_server = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_source_server.setLayoutData(gd);
		
		try {
			// get all servers - V6.0.phase4
			MFServer[] source_servers = MFServer.getAllServers();
			
			for (int i=0; i<source_servers.length; i++) {
				cmb_source_server.add(source_servers[i].getServerName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		cmb_source_server.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (cmb_source_server.getSelectionIndex() == -1)
						return;
					
					cmb_source_schema.removeAll();
					
					MFServer server = new MFServer(cmb_source_server.getItem(cmb_source_server.getSelectionIndex()));
					server.setSchemas();
					
					DB2Schema[] schemas = server.getSchemas();
					for (int i=0; i<schemas.length; i++) {
						cmb_source_schema.add(schemas[i].getSchemaName());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		for(int i=0; i<cmb_source_server.getItemCount(); i++) {
			if(cmb_source_server.getItem(i).equals(input.getSourceServerName())) {
				cmb_source_server.select(i);
				break;
			}
		}
		
		// combo - source schema - V6.0.phase4
		toolkit.createLabel(sectionClient, "Schema");
		cmb_source_schema = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_source_schema.setLayoutData(gd);
		
		try {
			// get all schemas
			if (cmb_source_server.getSelectionIndex() != -1) {
				MFServer server = new MFServer(cmb_source_server.getItem(cmb_source_server.getSelectionIndex()));
				server.setSchemas();
				
				DB2Schema[] schemas = server.getSchemas();
				for (int i=0; i<schemas.length; i++) {
					cmb_source_schema.add(schemas[i].getSchemaName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for(int i=0; i<cmb_source_schema.getItemCount(); i++) {
			if(cmb_source_schema.getItem(i).equals(input.getSourceSchemaName())) {
				cmb_source_schema.select(i);
				break;
			}
		}
		
		// combo - target server
		toolkit.createLabel(sectionClient, "Target:\tServer");
		cmb_target_server = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_target_server.setLayoutData(gd);
		
		try {
			// get all servers - V6.0.phase4
			MFServer[] target_servers = MFServer.getAllServers();
			
			for (int i=0; i<target_servers.length; i++) {
				cmb_target_server.add(target_servers[i].getServerName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		cmb_target_server.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (cmb_target_server.getSelectionIndex() == -1)
						return;
					
					cmb_target_schema.removeAll();
					
					MFServer server = new MFServer(cmb_target_server.getItem(cmb_target_server.getSelectionIndex()));
					server.setSchemas();
					
					DB2Schema[] schemas = server.getSchemas();
					for (int i=0; i<schemas.length; i++) {
						cmb_target_schema.add(schemas[i].getSchemaName());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		for(int i=0; i<cmb_target_server.getItemCount(); i++) {
			if(cmb_target_server.getItem(i).equals(input.getTargetServerName())) {
				cmb_target_server.select(i);
				break;
			}
		}
		
		// combo - target schema - V6.0.phase4
		toolkit.createLabel(sectionClient, "Schema");
		cmb_target_schema = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_target_schema.setLayoutData(gd);
		
		try {
			// get all schemas
			if (cmb_target_server.getSelectionIndex() != -1) {
				MFServer server = new MFServer(cmb_target_server.getItem(cmb_target_server.getSelectionIndex()));
				server.setSchemas();
				
				DB2Schema[] schemas = server.getSchemas();
				for (int i=0; i<schemas.length; i++) {
					cmb_target_schema.add(schemas[i].getSchemaName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for(int i=0; i<cmb_target_schema.getItemCount(); i++) {
			if(cmb_target_schema.getItem(i).equals(input.getTargetSchemaName())) {
				cmb_target_schema.select(i);
				break;
			}
		}

		section.setClient(sectionClient);

		// Section 2: Tables
		section = toolkit.createSection(form.getBody(), Section.DESCRIPTION
				| Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Tables");
		section.setDescription("This section describes the tables to be migrated " +
				"in this request.");
		sectionClient = toolkit.createComposite(section);
		gl = new GridLayout();
		// gl.numColumns = 2;
		sectionClient.setLayout(gl);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		// gd.widthHint = 500;
		gd.heightHint = 100;

		// table
		tbl_tables = toolkit.createTable(sectionClient, 
				SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		tbl_tables.setLayoutData(gd);
		tbl_tables.setLinesVisible(true);
		tbl_tables.setHeaderVisible(true);

		// define table head
		TableColumn col_action_t = new TableColumn(tbl_tables, SWT.NONE);
		col_action_t.setWidth(100);
		col_action_t.setText("Actions");
		col_action_t.setResizable(false);

		TableColumn col_status_t = new TableColumn(tbl_tables, SWT.NONE);
		col_status_t.setWidth(120);
		col_status_t.setText("Status");
		col_status_t.setResizable(false);

		TableColumn col_stname = new TableColumn(tbl_tables, SWT.NONE);
		col_stname.setWidth(200);
		col_stname.setText("Source Table Name");

		TableColumn col_ttname = new TableColumn(tbl_tables, SWT.NONE);
		col_ttname.setWidth(200);
		col_ttname.setText("Target Table Name");

		TableColumn col_unsql = new TableColumn(tbl_tables, SWT.NONE);
		col_unsql.setWidth(400);
		col_unsql.setText("Unload SQL");
		
		// define table items
		String[][] table_items = input.getTableItems();
		for (int i = 0; i < table_items.length; i++) {
			if(!table_items[i][0].trim().isEmpty()) {
				TableItem item = new TableItem(tbl_tables, SWT.NONE);
				item.setText(new String[] { "Click for Actions", "New", 
						table_items[i][0].trim(), table_items[i][1].trim(),
						table_items[i][2].trim()});		// v6.3
			}
		}

		// define table editor etc
		polishTable(tbl_tables, true);
		
		// create hyperlink composite - v6.0.0
		Composite hyperlinks1 = toolkit.createComposite(sectionClient);
		GridLayout glaylnk1 = new GridLayout(); 
		glaylnk1.numColumns = 6;
		hyperlinks1.setLayout(glaylnk1);

		// link to add a new table item
		lnk_add_table = toolkit.createHyperlink(hyperlinks1,
				"Add another table", SWT.WRAP);
		lnk_add_table.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				TableItem item = new TableItem(tbl_tables, SWT.NONE);
				item.setText(new String[] { "Click for Actions", "New" });
			}
		});
		
		// link to replace dataset names - v6.0.0
		lnk_replace_datasets = toolkit.createHyperlink(hyperlinks1, "Replace table names",
				SWT.WRAP);
		lnk_replace_datasets.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// run the wizard
				ReplaceNamesWizard wizard = new ReplaceNamesWizard("Table");
		   		WizardDialog dialog = new WizardDialog(null, wizard);
		   		dialog.open();
		   		
		   		// replace the names
		   		if (!wizard.getOldStr().isEmpty()) {
					try {
						for (int i = 0; i < tbl_tables.getItemCount(); i++) {
							if (wizard.isSourceChecked()) {
								String old_name = tbl_tables.getItem(i).getText(2).trim().toUpperCase();
								if (old_name.indexOf(wizard.getOldStr().toUpperCase()) != -1) {
									String new_name = TextProcessor.replaceStr(old_name, wizard.getOldStr().toUpperCase(), wizard.getNewStr().toUpperCase());
									tbl_tables.getItem(i).setText(2, new_name);
								}
							}
							
							if (wizard.isTargetChecked()) {
								String old_name = tbl_tables.getItem(i).getText(3).trim().toUpperCase();
								if (old_name.indexOf(wizard.getOldStr().toUpperCase()) != -1) {
									String new_name = TextProcessor.replaceStr(old_name, wizard.getOldStr().toUpperCase(), wizard.getNewStr().toUpperCase());
									tbl_tables.getItem(i).setText(3, new_name);
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
		   		}
			}
		});
		
		// link to build the unload critera - v6.0.0
		lnk_build_unload = toolkit.createHyperlink(hyperlinks1, "Build unload criteria",
				SWT.WRAP);
		lnk_build_unload.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// run the wizard
				BuildUnloadWizard wizard = new BuildUnloadWizard(tbl_tables);
		   		WizardDialog dialog = new WizardDialog(null, wizard);
		   		dialog.open();
			}
		});

		section.setClient(sectionClient);

		// Section 3: Datasets
		section = toolkit.createSection(form.getBody(), Section.DESCRIPTION
				| Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Datasets");
		section.setDescription("This section describes the datasets to be migrated " +
				"in this request.");
		sectionClient = toolkit.createComposite(section);
		gl = new GridLayout();
		// gl.numColumns = 2;
		sectionClient.setLayout(gl);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		// gd.widthHint = 500;
		gd.heightHint = 100;

		// table
		tbl_datasets = toolkit.createTable(sectionClient,
				SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		tbl_datasets.setLayoutData(gd);
		tbl_datasets.setLinesVisible(true);
		tbl_datasets.setHeaderVisible(true);

		// define table head
		TableColumn col_action_d = new TableColumn(tbl_datasets, SWT.NONE);
		col_action_d.setWidth(100);
		col_action_d.setText("Actions");
		col_action_d.setResizable(false);

		TableColumn col_status_d = new TableColumn(tbl_datasets, SWT.NONE);
		col_status_d.setWidth(120);
		col_status_d.setText("Status");
		col_status_d.setResizable(false);

		TableColumn col_sdname = new TableColumn(tbl_datasets, SWT.NONE);
		col_sdname.setWidth(300);
		col_sdname.setText("Source Dataset Name");

		TableColumn col_tdname = new TableColumn(tbl_datasets, SWT.NONE);
		col_tdname.setWidth(300);
		col_tdname.setText("Target Dataset Name");
		
		// define dataset items
		String[][] dataset_items = input.getDatasetItems();
		for (int i = 0; i < dataset_items.length; i++) {
			if(!dataset_items[i][0].trim().isEmpty()) {
				TableItem item = new TableItem(tbl_datasets, SWT.NONE);
				item.setText(new String[] { "Click for Actions", "New", 
						dataset_items[i][0].trim(), dataset_items[i][1].trim() });
			}
		}

		// add table editor etc.
		polishTable(tbl_datasets, false);
		
		// create hyperlink composite - v6.0.0
		Composite hyperlinks2 = toolkit.createComposite(sectionClient);
		GridLayout glaylnk2 = new GridLayout(); 
		glaylnk2.numColumns = 6;
		hyperlinks2.setLayout(glaylnk2);

		// link to add a new table item
		lnk_add_dataset = toolkit.createHyperlink(hyperlinks2,
				"Add another dataset", SWT.WRAP);
		lnk_add_dataset.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				TableItem item = new TableItem(tbl_datasets, SWT.NONE);
				item.setText(new String[] { "Click for Actions", "New" });
			}
		});
		
		// link to replace dataset names - v6.0.0
		lnk_replace_tables = toolkit.createHyperlink(hyperlinks2, "Replace dataset names",
				SWT.WRAP);
		lnk_replace_tables.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(final HyperlinkEvent e) {
				// run the wizard
				ReplaceNamesWizard wizard = new ReplaceNamesWizard("Dataset");
		   		WizardDialog dialog = new WizardDialog(null, wizard);
		   		dialog.open();
		   		
		   		// replace the names
		   		if (!wizard.getOldStr().isEmpty()) {
					try {
						for (int i = 0; i < tbl_datasets.getItemCount(); i++) {
							if (wizard.isSourceChecked()) {
								String old_name = tbl_datasets.getItem(i).getText(2).trim().toUpperCase();
								if (old_name.indexOf(wizard.getOldStr().toUpperCase()) != -1) {
									String new_name = TextProcessor.replaceStr(old_name, wizard.getOldStr().toUpperCase(), wizard.getNewStr().toUpperCase());
									tbl_datasets.getItem(i).setText(2, new_name);
								}
							}
							
							if (wizard.isTargetChecked()) {
								String old_name = tbl_datasets.getItem(i).getText(3).trim().toUpperCase();
								if (old_name.indexOf(wizard.getOldStr().toUpperCase()) != -1) {
									String new_name = TextProcessor.replaceStr(old_name, wizard.getOldStr().toUpperCase(), wizard.getNewStr().toUpperCase());
									tbl_datasets.getItem(i).setText(3, new_name);
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
		   		}
			}
		});

		section.setClient(sectionClient);

		// Add the tool bar - V6.0.phase4
		form.getToolBarManager().add(new StartDataMigrationAction(lbl_status, lbl_upt_time, 
				cmb_source_server, cmb_source_schema, cmb_target_server, cmb_target_schema, 
				tbl_tables, tbl_datasets, txt_desc, lnk_add_table, lnk_replace_tables, 
				lnk_build_unload, lnk_add_dataset, lnk_replace_datasets, input));
		form.getToolBarManager().update(true);
		
		// always call to let toolkit to decide the "look"
		toolkit.paintBordersFor(parent);
	}

	@Override
	public void setFocus() {

	}

	// add table editor to a table
	private void polishTable(final Table table, final boolean unload_load_flag) {
		// define table editor
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		
		// dispose any existing editor
        Control old_editor = editor.getEditor();
        if (old_editor != null) old_editor.dispose();
               
		// add listener on mouse down
		table.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(final Event event) {
				Rectangle clientArea = table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();

				// process each table item
				while (index < table.getItemCount()) {
					boolean visible = false;
					final TableItem item = table.getItem(index);
					final int row = index;

					// for each column from 0 to end, set it as below
					for (int i = 0; i < table.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final int column = i;

							// add a drop down to the first column
							if (column == 0) {
								// Create the drop down data based on the value of Status
								/*
									New  - Delete
	
									Starting - Cancel		// v6.0 beta 1
									Restarting - Cancel		// v6.0 beta 1
									Unloading - Cancel
									Ftping - Cancel
									Preloading - Cancel
									Loading - Cancel
									
									Completed - Delete, Re-Run, Re-Ftp, Re-Load		// v6.1.2
									
									Failed to Init - Delete, Re-Run					// v6.0 beta 1
									Failed to Unload - Delete, Re-Run
									Failed to Ftp - Delete, Re-Run, Re-Ftp
									Failed to Preload - Delete, Re-Run, Re-Load		// v6.1.2
									Failed to Load - Delete, Re-Run, Re-Load		// v6.1.2
									
									Restarted from Unload - Cancel
									Restarted from Ftp - Cancel
									Restarted from Preload - Cancel
									Restarted from Load - Cancel
									
									Canceled in Init - Delete, Re-Run				// v6.0.0
									Canceled in Unload - Delete, Re-Run
									Canceled in Ftp - Delete, Re-Run, Re-Ftp
									Canceled in Preload - Delete, Re-Run, Re-Load	// v6.1.2
									Canceled in Load - Delete, Re-Run, Re-Load		// v6.1.2
								*/

								String[] options = { "No action" };
								String status = item.getText(column+1);
								
								if(status.equals("New")) {
									options = new String[] { "Delete" };
								}
								else if(status.equals("Starting")		// v6.0 beta 1
										|| status.equals("Restarting")	// v6.0 beta 1
										|| status.equals("Unloading")
										|| status.equals("Ftping")
										|| status.equals("Preloading")
										|| status.equals("Loading")
										|| status.equals("Restarted from Unload")
										|| status.equals("Restarted from Ftp")
										|| status.equals("Restarted from Preload")
										|| status.equals("Restarted from Load")) {
									options = new String[] { "Cancel" };
								}
								else if(status.equals("Failed to Init")			// v6.0 beta 1
										|| status.equals("Failed to Unload")
										|| status.equals("Canceled in Init")	// v6.0.0
										|| status.equals("Canceled in Unload")) {
									options = new String[] { "Delete", "Re-Run" };
								}
								else if(status.equals("Failed to Ftp")
										|| status.equals("Canceled in Ftp")) {
									options = new String[] { "Delete", "Re-Ftp"};
								}
								else if(status.equals("Failed to Preload")
										|| status.equals("Canceled in Preload")
										|| status.equals("Failed to Load")
										|| status.equals("Canceled in Load")) {
									options = new String[] { "Delete", "Re-Run", "Re-Load" };
								}
								else if(status.equals("Completed")) {
									if(unload_load_flag)
										options = new String[] { "Delete", "Re-Run", "Re-Load" };
									else
										options = new String[] { "Delete", "Re-Ftp"};
								}
								
								// create the drop down
								final CCombo combo = new CCombo(table, SWT.READ_ONLY);
								for (int j = 0, n = options.length; j < n; j++) {
									// no Delete allowed while PROCESSING - v6.0.phase6
									if (!lbl_status.getText().trim().equals("PROCESSING")
											|| !options[j].equals("Delete")) {
										combo.add(options[j]);
									}
								}

								// Select the previously selected item from the cell
								combo.select(combo.indexOf(item.getText(column)));

								// Compute the width for the editor
								// Also, compute the column width, so that the dropdown fits
								editor.minimumWidth = combo.computeSize(
										SWT.DEFAULT, SWT.DEFAULT).x;
								if(table.getColumn(column).getWidth() < editor.minimumWidth)
									table.getColumn(column).setWidth(editor.minimumWidth);

								// Set the focus on the drop down and set into the editor
								combo.setFocus();
								editor.setEditor(combo, item, column);

								// Add a listener to set the cell and status
								final int col = column;
								combo.addSelectionListener(new SelectionListener() {
									public void widgetSelected(SelectionEvent event) {
										// set the text back to the cell
										item.setText(col, combo.getText());
										
										// take action according to the selection
										if(combo.getText().equals("Delete")) {
											if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
													"Confirm Delete", "Are you sure you want to delete this item?"))
												table.remove(row);
											else
												item.setText(col, "Click for Actions");
										}
										else if(combo.getText().equals("Re-Run")) {
											if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
													"Confirm Re-run", "Are you sure you want to re-run this item?"))
												item.setText(col+1, "Restarted from Unload");

											item.setText(col, "Click for Actions");
										}
										else if(combo.getText().equals("Re-Ftp")) {
											if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
													"Confirm Re-ftp", "Are you sure you want to re-ftp this item?"))
												item.setText(col+1, "Restarted from Ftp");
											
											item.setText(col, "Click for Actions");
										}
										else if(combo.getText().equals("Re-Load")) {
											if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
													"Confirm Re-load", "Are you sure you want to re-load this item?"))
												item.setText(col+1, "Restarted from Preload");
												
											item.setText(col, "Click for Actions");
										}
										else if(combo.getText().equals("Cancel")) {
											if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
													"Confirm Cancel", "Are you sure you want to cancel this item?")) {
												String stat = item.getText(col+1);
												if(stat.indexOf("Unload") != -1)
													item.setText(col+1, "Canceled in Unload");
												else if(stat.indexOf("Ftp") != -1)
													item.setText(col+1, "Canceled in Ftp");
												else if(stat.indexOf("Preload") != -1)
													item.setText(col+1, "Canceled in Preload");
												else if(stat.indexOf("Load") != -1)
													item.setText(col+1, "Canceled in Load");
												else		// v6.0.0
													item.setText(col+1, "Canceled in Init");
											} 
											
											item.setText(col, "Click for Actions");
										}
										
										// They selected an item; end the editing session
										combo.dispose();
									}

									public void widgetDefaultSelected(SelectionEvent e) {
										// no default selection
									}
								});
								
								// add a focusout listener to dispose the combo
								combo.addListener(SWT.FocusOut, new Listener() {
									public void handleEvent(final Event e) {
										combo.dispose();
									}
								});
							}

							// keep the second column as non-editable
							// and set the other columns as editable
							// when the status is not PROCESSING - v6.0.phase6
							else if (column >= 2
									&& !lbl_status.getText().trim().equals("PROCESSING")) {	// v6.0.phase6
								final Text text = new Text(table, SWT.NONE);
								Listener textListener = new Listener() {
									public void handleEvent(final Event event) {
										switch (event.type) {
										case SWT.FocusOut:
											// v6.0 beta 1
											if (!item.getText(column).equals(text.getText())) {
												item.setText(column, text.getText());
												item.setText(1, "New");
											}
											text.dispose();
											break;
										case SWT.Traverse:
											switch (event.detail) {
											case SWT.TRAVERSE_RETURN:
												// v6.0 beta 1
												if (!item.getText(column).equals(text.getText())) {
													item.setText(column, text.getText());
													item.setText(1, "New");
												}
												// FALL THROUGH
											case SWT.TRAVERSE_ESCAPE:
												text.dispose();
												event.doit = false;
											}
											break;
										}
									}
								};
								text.addListener(SWT.FocusOut, textListener);
								text.addListener(SWT.Traverse, textListener);
								editor.setEditor(text, item, i);
								text.setText(item.getText(i));
								text.selectAll();
								text.setFocus();
							}
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
	}

}
