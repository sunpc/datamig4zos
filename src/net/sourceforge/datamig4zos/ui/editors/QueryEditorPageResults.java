/**
 *  Class: ui.editors.QueryEditorPageResults
 *  Description: This is the Query Editor (Page Results).
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Modification History
 *  01. 11/05/2012: Code baseline. (V6.1)
 *  02. 08/08/2013: Change form title. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.editors;

import net.sourceforge.datamig4zos.actions.SaveQueryResultsAction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
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


public class QueryEditorPageResults extends EditorPart {

	private QueryEditor parent_editor;

	private FormToolkit toolkit;
	private ScrolledForm form;
	
	private Text txt_results;
	
	public QueryEditorPageResults(QueryEditor parentEditor) {
		super();
		parent_editor = parentEditor;
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

		// Section 1: Results
		Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Query Results");
		section.setDescription("This section describes the query results.");
		Composite sectionClient = toolkit.createComposite(section);
		
		GridLayout gl = new GridLayout();
		sectionClient.setLayout(gl);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 300;
		
		// results text box
		txt_results = toolkit.createText(sectionClient, "", SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		txt_results.setLayoutData(gd);
		txt_results.setEditable(false);		
		txt_results.setFont(new Font(null, "Courier New", 10, SWT.NONE));

		section.setClient(sectionClient);

		// Add the tool bar
		form.getToolBarManager().add(new SaveQueryResultsAction(txt_results));
		form.getToolBarManager().update(true);
		
		// always call to let toolkit to decide the "look"
		toolkit.paintBordersFor(parent);
	}
	
	@Override
	public void setFocus() {
		txt_results.setFocus();
		txt_results.setText(((QueryEditorInput) parent_editor.getEditorInput()).getQueryResults());
	}
	
}
