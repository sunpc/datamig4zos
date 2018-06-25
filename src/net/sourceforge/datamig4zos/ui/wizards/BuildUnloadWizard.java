/**
 *  Class: wizards.BuildUnloadWizard
 *  Description: A Wizard to build the unload criteria
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/08/2012: Code baseline. (V6.0.0)
 *  02. 11/06/2012: Use formatSql() instead of replaceStr(). (V6.1)
 *  03. 11/12/2012: Restore the code back to V6.0.0 version. (V6.1.2)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import net.sourceforge.datamig4zos.util.TextProcessor;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author SunPC
 *
 */
public class BuildUnloadWizard extends Wizard {
	
	private WizardPage page;
	
	private Table tbl_tables_editor;
	
	private String [] orig_sql_list = {};
	private String unload_sql = "";
	
	public BuildUnloadWizard(Table tblTables) {
		super();
		setNeedsProgressMonitor(false);
		setWindowTitle("Build unload criteria");
		
		tbl_tables_editor = tblTables;
		orig_sql_list = new String [tbl_tables_editor.getItemCount()];
		
		for (int i = 0; i < tbl_tables_editor.getItemCount(); i++) {
			orig_sql_list[i] = tbl_tables_editor.getItem(i).getText(4);
		}		
	}
	
	@Override
	public void addPages() {
		page = new WizardPage("Build unload criteria") {
			
			private Text txt_unload_sql;
			private Table tbl_tables;
			
			// final listener method
			final private void listen() {
				boolean table_checked = false; 
				
				try {
					unload_sql = TextProcessor.replaceStr(txt_unload_sql.getText().trim(), "\r\n", " ");
					unload_sql = TextProcessor.replaceStr(unload_sql, "\n", "");
					unload_sql = TextProcessor.replaceStr(unload_sql, "\t", "");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				for (int i = 0; i < tbl_tables.getItemCount(); i++) {
					if (tbl_tables.getItem(i).getChecked()) {
						table_checked = true;
						if (!unload_sql.trim().isEmpty())
							tbl_tables_editor.getItem(i).setText(4, unload_sql);
					} else {
						tbl_tables_editor.getItem(i).setText(4, orig_sql_list[i]);
					}
				}
				
				if (!txt_unload_sql.getText().trim().isEmpty() && table_checked) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
			
			@Override
			public void createControl(Composite parent) {
				setTitle("Build unload criteria");
				setDescription("Build the unload criteria for selected tables.");
				
				// grid data
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				
				// create container
				Composite container = new Composite(parent, SWT.NULL);
				GridLayout layout = new GridLayout();
				container.setLayout(layout);
				container.setLayoutData(gd);
				layout.numColumns = 1;
				
				// label
				Label label1 = new Label(container, SWT.NULL);
				label1.setText("Critera: ");
				
				// define grid data for text box
				GridData gd_txt = new GridData(GridData.HORIZONTAL_ALIGN_FILL
						| GridData.GRAB_HORIZONTAL);
				gd_txt.heightHint = 100;
				
				// define the text area
				txt_unload_sql = new Text(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
				txt_unload_sql.setLayoutData(gd_txt);
				txt_unload_sql.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}
					
					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
				});
				
				// buttons
				Composite buttons = new Composite(container, SWT.NULL);
				GridLayout glaybtn = new GridLayout(); 
				glaybtn.numColumns = 12;
				buttons.setLayout(glaybtn);
				
				// define the buttons
				String [] str_buttons = {"(", ")", "=", "<>", "AND", "OR", "<", "<=",
						">", ">=", "NOT"}; 
				for (int i = 0; i < str_buttons.length; i++) {
					final Button btn = new Button(buttons, SWT.PUSH);
					btn.setText(str_buttons[i]);
					btn.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							txt_unload_sql.insert(" " + btn.getText() + " ");
							txt_unload_sql.setFocus();
							listen();
						}
					});
				}
				
				// label
				Label label2 = new Label(container, SWT.NULL);
				label2.setText("Apply to: ");
				
				// define grid data for table list
				GridData gd_tbl = new GridData(GridData.HORIZONTAL_ALIGN_FILL
						| GridData.GRAB_HORIZONTAL);
				gd_tbl.heightHint = 100;
				
				// table list
				tbl_tables = new Table(container, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
				tbl_tables.setLayoutData(gd_tbl);
				tbl_tables.setLinesVisible(true);
				tbl_tables.setHeaderVisible(true);
				
				// define table head
				TableColumn col_stname = new TableColumn(tbl_tables, SWT.NONE);
				col_stname.setWidth(200);
				col_stname.setText("Source Table Name");

				TableColumn col_ttname = new TableColumn(tbl_tables, SWT.NONE);
				col_ttname.setWidth(200);
				col_ttname.setText("Target Table Name");

				// define table items
				for(int i = 0; i < tbl_tables_editor.getItemCount(); i++) {
					if(!tbl_tables_editor.getItem(i).getText(2).trim().isEmpty()) {
						TableItem item = new TableItem(tbl_tables, SWT.NONE);
						item.setText(new String[] { tbl_tables_editor.getItem(i).getText(2).trim(), 
								tbl_tables_editor.getItem(i).getText(3).trim() });
					}
				}
				
				// add listener on mouse down
				tbl_tables.addListener(SWT.MouseDown, new Listener() {

					@Override
					public void handleEvent(Event event) {
						listen();
					}
					
				});
				
				// Required to avoid an error in the system
				setControl(container);
				setPageComplete(false);
			}
			
		};
		
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getUnloadSql() {
		return unload_sql;
	}

}
