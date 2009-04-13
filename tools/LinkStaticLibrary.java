
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.LinkerParam;
import org.de.metux.unitool.db.LoadLibtoolArchive;
import org.de.metux.util.Exec;
import org.de.metux.util.FileOps;
import java.io.File;

public class LinkStaticLibrary extends LinkerBase
{
    public LinkStaticLibrary(LinkerParam p)
    {
	super(p);
    }

    public void run()
    {
	String output = param.getOutputFile();

	if (output.length()==0) 
	    throw new RuntimeException("missing property <output-file>");

	// first step: create archive
	FileOps.mkdir(LoadLibtoolArchive.tmp_libdir);
	String cmdline_ar = "pwd && "+ar_command()+" cru "+output;

	/* -- now extract the .a files and add them to the list -- */
	/* -- MUST be done before we're handling objects -- */
	handle_static_libs();

	/* --- add all direct object links to the command list --- */
	filter_objects();

	{
	    String input[] = param.getObjectLinks();
	    if (input.length==0)
		throw new RuntimeException("missing property <source>");
	    for (int x=0; x<input.length; x++)
		cmdline_ar += " "+input[x];
	}
	
	System.out.println("==> LinkStaticLibrary: (ar) "+cmdline_ar);
	if (!(new Exec().run(cmdline_ar)))
	    throw new RuntimeException("Exec failed: "+cmdline_ar);

	// now call ranlib
	String commandline = ranlib_command()+" "+output;
	System.out.println("==> LinkStaticLibrary: (ranlib) "+commandline);
	if (!(new Exec().run(cmdline_ar)))
	    throw new RuntimeException("Exec failed: "+commandline);
    }
}
