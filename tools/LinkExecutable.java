
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.LinkerParam;
import org.de.metux.util.Exec;
import org.de.metux.util.Environment;

public class LinkExecutable extends LinkerBase
{
    public LinkExecutable(LinkerParam p)
    {
	super(p);
    }

    public void run()
    {
	Gcc_cmdline gcc;

	String linker_command =
	    param.config.getConfigStr(
		LinkerParam.cf_linker_executable_command,
		Environment.getenv("CC"));
    
	handle_static_libs();
	filter_objects();
	
	gcc = new Gcc_cmdline(linker_command);
	gcc.setNormalizer(param.normalizer);
	gcc.shared(param.isLinktypeShared());
	gcc.verbatims(param.getVerbatims());
	gcc.output_filename(param.getOutputFile());

	gcc.libpath(param.getLibraryPathes_dec_sysroot());
	gcc.rpath(param.getRuntimeLibraryPathes());
	gcc.dynlink(param.getSharedLinks());

	gcc.staticlink(param.getStaticLinks());
	gcc.objlink(param.getObjectLinks_add_sysroot());

	// clear already processed stuff from parameter set
	// evrything's remaining is junk. warn about that
	param.clearVerbatim();
	param.clearOutputFile();
	param.clearLinktype();
	param.clearLibraryPath();
	param.clearRuntimeLibraryPath();
	param.clearStaticLink();
	param.clearSharedLink();
	param.clearObjectLink();

	// add misc GCC dependend stuff -- fixme!
	String cmdline = gcc.toString();

	System.out.println("==> ExecutableLinker: "+cmdline);
	Exec exec = new Exec();

	if (!exec.run(cmdline))
	    throw new RuntimeException("Exec failed: "+cmdline);
    }
}

