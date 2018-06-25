/**
 *  Class: provider.ServerExplorerContentProvider
 *  Description: The content provider for Server Explorer.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/21/2011: Code baseline. (V6.0 phase 3)
 *  02. 03/22/2011: Collapse damage for new data model objects. (V6.0 phase 4) 
 */
package net.sourceforge.datamig4zos.ui.providers;

import net.sourceforge.datamig4zos.objects.CNFRoot;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


/**
 * @author SunPC
 * 
 */
public class ServerExplorerContentProvider implements ITreeContentProvider {
	
	private MFServer[] servers;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		
		if (parentElement instanceof CNFRoot) {
			try {
				servers = MFServer.getAllServers((CNFRoot) parentElement);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return servers;
		} else if (parentElement instanceof MFServer) {
			try {
				((MFServer) parentElement).setSchemas();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return ((MFServer) parentElement).getSchemas();
		} else if (parentElement instanceof DB2Schema) {
			try {
				((DB2Schema) parentElement).setTables();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return ((DB2Schema) parentElement).getTables();
		} else if (parentElement instanceof DB2Table) {
			return new Object[0];
		} else {
			return new Object[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof DB2Table) {
			return ((DB2Table) element).getSchema();
		} else if (element instanceof DB2Schema) {
			return ((DB2Schema) element).getServer();
		} else if (element instanceof MFServer) {
			return ((MFServer) element).getRoot();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof CNFRoot || element instanceof MFServer || element instanceof DB2Schema);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		this.servers = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
