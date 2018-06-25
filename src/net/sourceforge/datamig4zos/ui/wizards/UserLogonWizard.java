/**
 *  Class: wizards.UserLogonWizard
 *  Description: A Wizard to log on the user when core fails due to incorrect password.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 03/09/2012: Code baseline. (V6.0.0)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import net.sourceforge.datamig4zos.objects.MFServer;

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
public class UserLogonWizard extends Wizard {
	
	private WizardPage page;
	
	private MFServer server;
	private String str_user = "";
	private String str_pwd = "";
	
	private boolean is_finished = false;
	
	public UserLogonWizard(MFServer server) {
		super();
		this.server = server;
		this.str_user = server.getLogonUser();
		this.str_pwd = server.getLogonPwd();
		setNeedsProgressMonitor(false);
		setWindowTitle("Logon");
	}
	
	@Override
	public void addPages() {
		page = new WizardPage("Logon") {
			
			private Text txt_user;
			private Text txt_pwd;
			
			// final listener method
			final private void listen() {
				if (!txt_user.getText().trim().isEmpty()
						&& !txt_pwd.getText().trim().isEmpty()) {
					str_user = txt_user.getText().trim();
					str_pwd = txt_pwd.getText().trim();
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
			
			@Override
			public void createControl(Composite parent) {
				setTitle("Logon");
				setDescription("Enter user name and password to logon.");
				
				// grid data
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				
				// create container
				Composite container = new Composite(parent, SWT.NULL);
				GridLayout layout = new GridLayout();
				container.setLayout(layout);
				container.setLayoutData(gd);
				layout.numColumns = 2;
				
				// server name
				Label label1a = new Label(container, SWT.NULL);
				label1a.setText("Server Name: ");
				Label label1b = new Label(container, SWT.NULL);
				label1b.setText(server.getServerName());
				
				// server name
				Label label2a = new Label(container, SWT.NULL);
				label2a.setText("Host IP: ");
				Label label2b = new Label(container, SWT.NULL);
				label2b.setText(server.getHostIp());
				
				// logon user
				Label label3 = new Label(container, SWT.NULL);
				label3.setText("Logon User: ");
				
				txt_user = new Text(container, SWT.BORDER | SWT.SINGLE);
				txt_user.setText(str_user);
				txt_user.setTextLimit(8);
				txt_user.setLayoutData(gd);
				txt_user.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				txt_user.addVerifyListener(new VerifyListener(){
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
				
				// logon pwd
				Label label4 = new Label(container, SWT.NULL);
				label4.setText("Logon Password: ");
				
				txt_pwd = new Text(container, SWT.BORDER | SWT.PASSWORD);
				txt_pwd.setText(str_pwd);
				txt_pwd.setTextLimit(8);
				txt_pwd.setLayoutData(gd);
				txt_pwd.addKeyListener(new KeyListener() {
					
					@Override
					public void keyPressed(KeyEvent e) {
					}

					@Override
					public void keyReleased(KeyEvent e) {
						listen();
					}
					
				});
				
				// Required to avoid an error in the system
				setControl(container);
				setPageComplete(true);
			}
			
		};
		
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		is_finished = true;
		return is_finished;
	}
	
	public String getUser() {
		return str_user;
	}

	public String getPwd() {
		return str_pwd;
	}
	
	public boolean isFinished() {
		return is_finished;
	}

}
