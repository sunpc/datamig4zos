/**
 *  Class: provider.ServerExplorerLabelProvider
 *  Description: The label provider for Server Explorer.
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  01. 03/21/2011: Code baseline. (V6.0 phase 3)
 *  02. 03/22/2011: Collapse damage for new data model objects. (V6.0 phase 4) 
 */
package net.sourceforge.datamig4zos.ui.providers;

import net.sourceforge.datamig4zos.Activator;
import net.sourceforge.datamig4zos.objects.DB2Schema;
import net.sourceforge.datamig4zos.objects.DB2Table;
import net.sourceforge.datamig4zos.objects.MFServer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;


/**
 * @author SunPC
 * 
 */
public class ServerExplorerLabelProvider extends LabelProvider implements ILabelProvider,
		IDescriptionProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof MFServer) {
			return Activator.getImageDescriptor("/images/server_obj.gif").createImage();
			// return PlatformUI.getWorkbench().getSharedImages().getImage(
			//		ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof DB2Schema) {
			return Activator.getImageDescriptor("/images/schema_obj.gif").createImage();
			// return PlatformUI.getWorkbench().getSharedImages().getImage(
			//		ISharedImages.IMG_OBJ_ELEMENT);
		} else if (element instanceof DB2Table) {
			return Activator.getImageDescriptor("/images/table_obj.gif").createImage();
			// return PlatformUI.getWorkbench().getSharedImages().getImage(
			//		ISharedImages.IMG_OBJ_FILE);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof MFServer) {
			return ((MFServer) element).getServerName();
		} else if (element instanceof DB2Schema) {
			return ((DB2Schema) element).getSchemaName();
		} else if (element instanceof DB2Table) {
			return ((DB2Table) element).getTableName();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang
	 * .Object)
	 */
	@Override
	public String getDescription(Object anElement) {
		return getText(anElement);
	}

}
