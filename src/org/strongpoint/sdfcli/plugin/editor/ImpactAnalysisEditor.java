package org.strongpoint.sdfcli.plugin.editor;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;

public class ImpactAnalysisEditor implements IEditorLauncher{

	@Override
	public void open(IPath arg0) {
		File file = arg0.toFile();
		
	}

}
