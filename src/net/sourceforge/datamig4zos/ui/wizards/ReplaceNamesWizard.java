/**
 *  Class: wizards.ReplaceNamesWizard
 *  Description: A Wizard to replace the table or dataset names
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/07/2012: Code baseline. (V6.0.0)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author SunPC
 *
 */
public class ReplaceNamesWizard extends Wizard {
	
	private WizardPage page;
	
	private String old_str = "";
	private String new_str = "";
	private String search_type = "";
	private boolean source_checked = false;
	private boolean target_checked = false;
	
	public ReplaceNamesWizard(String searchType) {
		super();		
		search_type = searchType;		
		setNeedsProgressMonitor(false);
		setWindowTitle("Replace " + search_type + " names");
	}
	
	@Override
	public void addPages() {
		page = new WizardPage("Replace " + search_type + " names") {
			
			private Text txt_old_str;
			private Text txt_new_str;
			private Button chk_source;
			private Button chk_target;
			
			// final listener method
			final private void listen() {
				if (!txt_old_str.getText().trim().isEmpty()
						&& !txt_new_str.getText().trim().isEmpty()
						&& (chk_source.getSelection() || chk_target.getSelection())) {
					old_str = txt_old_str.getText().trim();
					new_str = txt_new_str.getText().trim();
					source_checked = chk_source.getSelection();
					target_checked = chk_target.getSelection();
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
			
			@Override
			public void createControl(Composite parent) {
				setTitle("Replace " + search_type + " names");
				setDescription("Find and replace the " + search_type.toLowerCase() + " names. ");
				
				// grid data
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				
				// create container
				Composite container = new Composite(parent, SWT.NULL);
				GridLayout layout = new GridLayout();
				container.setLayout(layout);
				container.setLayoutData(gd);
				layout.numColumns = 1;
				
				// text boxes
				Composite textboxes = new Composite(container, SWT.NULL);
				GridLayout glaytxt = new GridLayout(); 
				glaytxt.numColumns = 2;
				textboxes.setLayout(glaytxt);
				textboxes.setLayoutData(gd);
				
				// txt_old_str
				Label label1 = new Label(textboxes, SWT.NULL);
				label1.setText("Find: ");
				
				txt_old_str = new Text(textboxes, SWT.BORDER | SWT.SINGLE);
				txt_old_str.setText("");
				txt_old_str.setLayoutData(gd);
				txt_old_str.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_old_str.addVerifyListener(new VerifyListener(){
					@Override
			        public void verifyText(VerifyEvent e) {                
						char c = e.character;
						if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
								|| c == '.' || c == '@' || c == '$' || c == '#' || c == '_') {
							e.doit = true;
							String s = e.text;
				          	e.text = s.toUpperCase();
						} else {
							e.doit = false;
						}
					}	            
				});
				
				// txt_job_batch_id
				Label label2 = new Label(textboxes, SWT.NULL);
				label2.setText("Replace with: ");
				
				txt_new_str = new Text(textboxes, SWT.BORDER | SWT.SINGLE);
				txt_new_str.setText("");
				txt_new_str.setLayoutData(gd);
				txt_new_str.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_new_str.addVerifyListener(new VerifyListener(){
					@Override
			        public void verifyText(VerifyEvent e) {                
						char c = e.character;
						if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
								|| c == '.' || c == '@' || c == '$' || c == '#' || c == '_') {
							e.doit = true;
							String s = e.text;
				          	e.text = s.toUpperCase();
						} else {
							e.doit = false;
						}
					}	            
				});
				
				// check buttons
				Composite buttons = new Composite(container, SWT.NULL);
				GridLayout glaybtn = new GridLayout(); 
				glaybtn.numColumns = 4;
				buttons.setLayout(glaybtn);
				
				// chk_source
				chk_source = new Button(buttons, SWT.CHECK);
				chk_source.setSelection(false);
				chk_source.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						listen();
					}
				});
				Label lbl_source = new Label(buttons, SWT.NONE);
				lbl_source.setText("Source " + search_type);

				// chk_target
				chk_target = new Button(buttons, SWT.CHECK);
				chk_target.setSelection(false);
				chk_target.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						listen();
					}
				});
				Label lbl_target = new Label(buttons, SWT.NONE);
				lbl_target.setText("Target " + search_type);
				
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
	
	public String getOldStr() {
		return old_str;
	}

	public String getNewStr() {
		return new_str;
	}

	public boolean isSourceChecked() {
		return source_checked;
	}

	public boolean isTargetChecked() {
		return target_checked;
	}

}
