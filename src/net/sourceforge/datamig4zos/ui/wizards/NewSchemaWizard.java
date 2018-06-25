/**
 *  Class: wizards.NewSchemaWizard
 *  Description: The New Schema Wizard
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/22/2011: Code baseline. (V6.0 phase 4)
 *  02. 03/02/2012: Enhance the text boxes. (V6.0.0)
 *  03. 11/02/2012: Allow <USERID> in Job Batch ID and Data set Prefix. (V6.0.1) 
 */
package net.sourceforge.datamig4zos.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author SunPC
 *
 */
public class NewSchemaWizard extends Wizard {
	
	private WizardPage page;
	
	private String schema_name = "";
	private String job_batch_id = "";
	private String dataset_prefix = "";
	
	public NewSchemaWizard() {
		super();
		setNeedsProgressMonitor(false);
		setWindowTitle("New DB2 Schema");
	}
	
	@Override
	public void addPages() {
		page = new WizardPage("New DB2 Schema") {
			
			private Text txt_schema_name;
			private Text txt_job_batch_id;
			private Text txt_dataset_prefix;
			
			// final listener method
			final private void listen() {
				if (!txt_schema_name.getText().trim().isEmpty()
						&& !txt_job_batch_id.getText().trim().isEmpty()
						&& !txt_dataset_prefix.getText().trim().isEmpty()) {
					schema_name = txt_schema_name.getText().trim();
					job_batch_id = txt_job_batch_id.getText().trim();
					dataset_prefix = txt_dataset_prefix.getText().trim();
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
			
			@Override
			public void createControl(Composite parent) {
				setTitle("New DB2 Schema");
				setDescription("Specify a valid DB2 schema. ");
				
				// create container
				Composite container = new Composite(parent, SWT.NULL);
				GridLayout layout = new GridLayout();
				container.setLayout(layout);
				layout.numColumns = 2;
				
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				
				// txt_schema_name
				Label label1 = new Label(container, SWT.NULL);
				label1.setText("Schema Name: ");
				
				txt_schema_name = new Text(container, SWT.BORDER | SWT.SINGLE);
				txt_schema_name.setText("");
				txt_schema_name.setLayoutData(gd);
				txt_schema_name.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_schema_name.addVerifyListener(new VerifyListener(){	// v6.0.0
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
				
				// txt_job_batch_id
				Label label2 = new Label(container, SWT.NULL);
				label2.setText("Job Batch ID: ");
				
				txt_job_batch_id = new Text(container, SWT.BORDER | SWT.SINGLE);
				txt_job_batch_id.setText("<USERID>");	// v6.0.1
				txt_job_batch_id.setTextLimit(8);		// v6.0.0
				txt_job_batch_id.setLayoutData(gd);
				txt_job_batch_id.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_job_batch_id.addVerifyListener(new VerifyListener(){	// v6.0.0
					@Override
			        public void verifyText(VerifyEvent e) {                
						char c = e.character;
						if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
								|| c == '<' || c == '>') {					// v6.0.1
							e.doit = true;
							String s = e.text;
				          	e.text = s.toUpperCase();
						} else {
							e.doit = false;
						}
					}	            
				});
				
				// txt_dataset_prefix
				Label label3 = new Label(container, SWT.NULL);
				label3.setText("Dataset Prefix: ");
				
				txt_dataset_prefix = new Text(container, SWT.BORDER | SWT.SINGLE);
				txt_dataset_prefix.setText("<USERID>");		// v6.0.1
				txt_dataset_prefix.setTextLimit(25);		// v6.0.0
				txt_dataset_prefix.setLayoutData(gd);
				txt_dataset_prefix.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_dataset_prefix.addVerifyListener(new VerifyListener(){	// v6.0.0
					@Override
			        public void verifyText(VerifyEvent e) {                
						char c = e.character;
						if (Character.isISOControl(c) || Character.isLetterOrDigit(c)
								|| c == '.' || c == '@' || c == '$' || c == '#'
								|| c == '<' || c == '>') {					// v6.0.1
							e.doit = true;
							String s = e.text;
				          	e.text = s.toUpperCase();
						} else {
							e.doit = false;
						}
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

	public String getSchemaName() {
		return schema_name;
	}

	public String getJobBatchId() {
		return job_batch_id;
	}

	public String getDatasetPrefix() {
		return dataset_prefix;
	}

}
