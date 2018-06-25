/**
 *  Class: ui.editors.QueryEditorPageForm
 *  Description: This is the Query Editor (Page Form).
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 11/05/2012: Code baseline. (V6.1)
 *  02. 11/07/2012: Add blank schema. (V6.1.1)
 *  03. 08/08/2013: Change form title. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.actions.RunDatabaseQueryAction;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;


public class QueryEditorPageForm extends EditorPart {

	private QueryEditor parent_editor;
	private QueryEditorInput editor_input;

	private FormToolkit toolkit;
	private ScrolledForm form;
	
	private CCombo cmb_server;
	private CCombo cmb_schema;
	
	private Text txt_sql;
	
	public QueryEditorPageForm(QueryEditor parentEditor) {
		super();
		parent_editor = parentEditor;
		editor_input = (QueryEditorInput) parent_editor.getEditorInput();
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
		form.setText("Database Query");		// v6.3
		toolkit.decorateFormHeading(form.getForm());

		// Create the layout
		ColumnLayout layout = new ColumnLayout();
		layout.maxNumColumns = 1;
		form.getBody().setLayout(layout);

		// Section 1 : Server info
		Section section1 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section1.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section1.setText("Server and Schema");
		section1.setDescription("This section describes the server and schema.");
		Composite sectionClient = toolkit.createComposite(section1);
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		sectionClient.setLayout(gl);
		GridData gd = new GridData();
		gd.widthHint = 80;

		// combo - server
		toolkit.createLabel(sectionClient, "Server:");
		cmb_server = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_server.setLayoutData(gd);
		
		try {
			// get all servers
			MFServer[] source_servers = MFServer.getAllServers();
			
			for (int i=0; i<source_servers.length; i++) {
				cmb_server.add(source_servers[i].getServerName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		cmb_server.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (cmb_server.getSelectionIndex() == -1)
						return;
					
					cmb_schema.removeAll();
					
					MFServer server = new MFServer(cmb_server.getItem(cmb_server.getSelectionIndex()));
					server.setSchemas();
					
					cmb_schema.add("");		// v6.1.1
					
					DB2Schema[] schemas = server.getSchemas();
					for (int i=0; i<schemas.length; i++) {
						cmb_schema.add(schemas[i].getSchemaName());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		if (editor_input.getSchema() instanceof DB2Schema) {
			for(int i=0; i<cmb_server.getItemCount(); i++) {
				if(cmb_server.getItem(i).equals(editor_input.getSchema().getServer().getServerName())) {
					cmb_server.select(i);
					break;
				}
			}
		}
		
		// combo - schema
		toolkit.createLabel(sectionClient, "Schema:");
		cmb_schema = new CCombo(sectionClient, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cmb_schema.setLayoutData(gd);
		
		try {
			// get all schemas
			if (cmb_server.getSelectionIndex() != -1) {
				MFServer server = new MFServer(cmb_server.getItem(cmb_server.getSelectionIndex()));
				server.setSchemas();
				
				cmb_schema.add("");		// v6.1.1
				
				DB2Schema[] schemas = server.getSchemas();
				for (int i=0; i<schemas.length; i++) {
					cmb_schema.add(schemas[i].getSchemaName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (editor_input.getSchema() instanceof DB2Schema) {
			for(int i=0; i<cmb_schema.getItemCount(); i++) {
				if(cmb_schema.getItem(i).equals(editor_input.getSchema().getSchemaName())) {
					cmb_schema.select(i);
					break;
				}
			}
		}
		
		section1.setClient(sectionClient);

		// Section 2 : SQL statements
		Section section2 = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section2.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section2.setText("SQL Statements");
		section2.setDescription("This section describes the SQL statements to be used in the query.");
		sectionClient = toolkit.createComposite(section2);
		
		GridLayout gl2 = new GridLayout();
		sectionClient.setLayout(gl2);
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd2.heightHint = 220;

		// sql text box
		txt_sql = toolkit.createText(sectionClient, "", SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		txt_sql.setLayoutData(gd2);		
		txt_sql.setFont(new Font(null, "Courier New", 10, SWT.NONE));
		txt_sql.setText(editor_input.getSqlStmts());

		section2.setClient(sectionClient);
		
		// set vertical spacings
		section2.descriptionVerticalSpacing = section1.getTextClientHeightDifference();

		// Add the tool bar
		form.getToolBarManager().add(new RunDatabaseQueryAction(cmb_server, cmb_schema, txt_sql, parent_editor));
		form.getToolBarManager().update(true);

		// always call to let toolkit to decide the "look"
		toolkit.paintBordersFor(parent);
	}

	@Override
	public void setFocus() {
		txt_sql.setFocus();
	}
	
}
