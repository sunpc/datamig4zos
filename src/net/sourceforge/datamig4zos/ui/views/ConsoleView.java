/**
 *  Class: panels.ConsoleView
 *  Description: This is the console view.
 * 
 * 	Author: Peng Cheng Sun
 * 
 *  Program originally from 
 *  http://waynebeaton.wordpress.com/2006/10/19/displaying-the-console-in-your-rcp-application/
 * 
 *  Modification History
 *  01. 04/10/2008: Copy from http://blog.sina.com.cn/s/blog_4d15ab8001008v2c.html
 *  02. 11/01/2008: Initial Version for DataMig V2.1
 *  03. 02/21/2009: Migrated to DataMig4zOS by integrated LoopedStreams class. (V5.0)
 *  04. 06/18/2010: Re-written entire code to cater for Eclipse RCP. (V6.0 phase 1)
 *  05. 06/22/2010: Override flush() to handle multiple thread prints. (V6.0 phase 1)
 *  06. 04/03/2011: Check the text limit while appending the new text. (V6.0 beta 1)
 *  07. 04/04/2011: Performance enhancement: Only start one thread instead of start a new thread in each flush. (V6.0 beta 1)
 */
package net.sourceforge.datamig4zos.ui.views;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ConsoleView extends ViewPart {
	private Text text;

	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);

		OutputStream out = new OutputStream() {
			private StringBuffer buffer = new StringBuffer();
			private final Object obj = new Object();
			
			private Thread thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								String newText = buffer.toString();
								
								if (!newText.equals("")) {
									// check if reaches the Text Limit - v6.0.beta1
									while (text.getText().length() + newText.length()
											> text.getTextLimit()) {
										text.setText(text.getText().substring(text.getText().indexOf("\n") + 1));
									}

									// append the new text
									text.append(newText);
								}
								
								buffer = new StringBuffer();
							}
						});								
					}
				}
			});

			@Override
			public void write(final int b) throws IOException {
				synchronized (obj) {
					if (text.isDisposed())
						return;
					
					buffer.append((char) b);
				}
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				super.write(b, off, len);
				flush();
			}

			@Override
			public void flush() throws IOException {
				synchronized (obj) {
					// start the thread if it's not alive - v6.0 beta1
					if(!thread.isAlive()) {
						thread.start();
					}
				}
			}
		};

		final PrintStream oldOut = System.out;
		final PrintStream oldErr = System.err;

		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(out));

		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				System.setOut(oldOut);
				System.setErr(oldErr);
			}
		});
	}

	public void setFocus() {
		text.setFocus();
	}
}