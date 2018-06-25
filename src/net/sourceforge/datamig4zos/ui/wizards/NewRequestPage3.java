/**
 *  Class: wizards.NewRequestPage3
 *  Description: The New Request Wizard - Page 3
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Modification History
 *  01. 08/17/2013: Code baseline. (V6.3)
 */
package net.sourceforge.datamig4zos.ui.wizards;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sourceforge.datamig4zos.objects.DB2Filter;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class NewRequestPage3 extends WizardPage {
	
	private Combo cmb_filter_name;
	
	private Table tbl_filters;
	
	private Composite container;

	public NewRequestPage3() {
		super("Table Filters");
		setTitle("Table Filters");
		setDescription("Define the data filters on tables.");
	}

	@Override
	public void createControl(Composite parent) {
		// create container
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		// define filter name
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String filter_name = "TF" + formatter.format(cal.getTime());
		
		// combo filter name
		cmb_filter_name = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
		cmb_filter_name.setLayoutData(new GridData(120, 0));
		cmb_filter_name.setTextLimit(16);
		cmb_filter_name.setText(filter_name);
		
		try {
			// get all filter names
			String[] array_filters = DB2Filter.getAllFilterNames();
			
			for (int i = 0; i < array_filters.length; i++) {
				cmb_filter_name.add(array_filters[i]);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		cmb_filter_name.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					DB2Filter filter = new DB2Filter(cmb_filter_name.getText());
					
					String[][] data = filter.getFilterData();
					
					// process each table item
					for (int i = 0; i < tbl_filters.getItemCount(); i++) {
						for (int j = 0; j < 3; j++) {
							if (data[i][j] != null)
								tbl_filters.getItem(i).setText(j, data[i][j]);
							else
								tbl_filters.getItem(i).setText(j, "");
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		});
		
		// table filters
		tbl_filters = new Table(container, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		tbl_filters.setLayoutData(new GridData(480, 180));
		tbl_filters.setLinesVisible(true);
		tbl_filters.setHeaderVisible(true);
		
		// define table head
		TableColumn col_field = new TableColumn(tbl_filters, SWT.NONE);
		col_field.setWidth(100);
		col_field.setText("Field");

		TableColumn col_oper = new TableColumn(tbl_filters, SWT.NONE);
		col_oper.setWidth(80);
		col_oper.setText("Operator");

		TableColumn col_data = new TableColumn(tbl_filters, SWT.NONE);
		col_data.setWidth(300);
		col_data.setText("Data");
		
		// define initial table items
		for (int i = 0; i < 20; i++) {
			TableItem item = new TableItem(tbl_filters, SWT.NONE);
			item.setText(new String[] { "", "", "" });
		}
		
		// define table editor
		final TableEditor editor_filters = new TableEditor(tbl_filters);
		editor_filters.horizontalAlignment = SWT.LEFT;
		editor_filters.grabHorizontal = true;
		
		// add listener on mouse down
		tbl_filters.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(final Event event) {
				Rectangle clientArea = tbl_filters.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = tbl_filters.getTopIndex();

				// process each table item
				while (index < tbl_filters.getItemCount()) {
					boolean visible = false;
					final TableItem item = tbl_filters.getItem(index);

					// for each column from 0 to end, set it as below
					for (int i = 0; i < tbl_filters.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final int column = i;

							// add a drop down to the second column (operator column)
							if (column == 1) {
								// create the drop down
								final CCombo combo = new CCombo(tbl_filters, SWT.READ_ONLY);
								combo.add("=");
								combo.add("<>");
								combo.add("<");
								combo.add("<=");
								combo.add(">");
								combo.add(">=");
								combo.add("IN");
								combo.add("NOT IN");

								// Select the previously selected item from the cell
								combo.select(combo.indexOf(item.getText(column)));

								// Set the focus on the drop down and set into the editor
								combo.setFocus();
								editor_filters.setEditor(combo, item, column);

								// Add a listener to set the cell and status
								final int col = column;
								combo.addSelectionListener(new SelectionListener() {
									public void widgetSelected(SelectionEvent event) {
										// set the text back to the cell
										item.setText(col, combo.getText());

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

							// set the other columns as edit-able
							else {
								final Text text = new Text(tbl_filters, SWT.NONE);
								Listener textListener = new Listener() {
									public void handleEvent(final Event event) {
										switch (event.type) {
										case SWT.FocusOut:
											if (!item.getText(column).equals(text.getText())) {
												item.setText(column, text.getText());
											}
											text.dispose();
											break;
										case SWT.Traverse:
											switch (event.detail) {
											case SWT.TRAVERSE_RETURN:
												if (!item.getText(column).equals(text.getText())) {
													item.setText(column,text.getText());
												}
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
								editor_filters.setEditor(text, item, i);
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
		
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(true);
	}

	// get table filter name
	public String getTableFilterName() {
		return cmb_filter_name.getText();
	}

	// get table filter data
	public String[][] getTableFilterData() {
		String[][] array_data = new String[20][3];
		
		int index = tbl_filters.getTopIndex();

		// process each table item
		while (index < tbl_filters.getItemCount()) {
			array_data[index][0] = tbl_filters.getItem(index).getText(0);
			array_data[index][1] = tbl_filters.getItem(index).getText(1);
			array_data[index][2] = tbl_filters.getItem(index).getText(2);
			index++;
		}
		
		return array_data;
	}
	
}