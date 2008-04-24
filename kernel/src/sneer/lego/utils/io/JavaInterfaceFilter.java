package sneer.lego.utils.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import sneer.lego.utils.asm.MetaClass;


public class JavaInterfaceFilter extends JavaFilter {
	
	public JavaInterfaceFilter(File root) {
		super(root);
	}

	@SuppressWarnings("unused")
	@Override
	protected void handleClass(MetaClass metaClass, int depth, Collection<MetaClass> results) throws IOException {
		if(metaClass.isInterface())
			results.add(metaClass);
	}
}