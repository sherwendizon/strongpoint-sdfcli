package org.strongpoint.sdfcli.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;

public class SdfcliImpactAnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(
				window.getShell(),
				"Strongpoint Impact Analysis",
				"No significant impact to show!safe: [\n" + 
				"		{scriptid: 'customscript_flo_trigger', name: 'Strongpoint Trigger Script'},\n" + 
				"		{scriptid: 'customsearch_flo_unused', name: 'Strongpoint Unused Search'},\n" + 
				"		{scriptid: 'customscript123', name: 'Test Script'},\n" + 
				"		{scriptid: 'customsearch1122', name: 'Test Search'}\n" + 
				"	],\n" + 
				"	notSafe: [\n" + 
				"		{scriptid: 'customscript_flo_notsafetrigger', name: 'Strongpoint Not Safe Trigger Script', warning: 'RECENTLY USED', impacted: [{scriptid: 'customrecord1', name: 'Record 1'}, {scriptid: 'customrecord2', name: 'Record 2'}, {scriptid: 'customrecord3', name: 'Record 3'}]},\n" + 
				"		{scriptid: 'customsearch_flo_testsearch', name: 'Strongpoint Test Search', warning: 'RECENTLY USED', impacted: [{scriptid: 'customrecord1', name: 'Record 1'}, {scriptid: 'customrecord2', name: 'Record 2'}, {scriptid: 'customrecord3', name: 'Record 3'}]}\n" + 
				"	],\n" + 
				"	notActive: [\n" + 
				"		{scriptid: 'customsearch12345', name: 'Test 12345'}\n" + 
				"	]");
		return null;
	}

}
