
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.LinkerParam;
import org.de.metux.util.Exec;
import org.de.metux.util.Environment;
import org.de.metux.util.StrUtil;
import org.de.metux.util.StrReplace;

public class LinkSharedLibrary extends LinkerBase
{
    // FIXME: put this into the unitool config file
    public String dl_filename_mask_noversion = "lib{MODULE_NAME}.so";
    public String dl_filename_mask_version   = "lib{MODULE_NAME}.so.{VERSION_INFO}";
    
    public LinkSharedLibrary(LinkerParam p)
    {
	super(p);
    }
    
    public void run()
    {
	LD_cmdline ld;
	
	// we do not use the "linker-command" property anylonger
	// those things are now completely encapsulated behind unitool
	String linker_command = 
	    param.config.getConfigStr(
		LinkerParam.cf_linker_dll_command,
		Environment.getenv("LD"));

	// FIXME !!!
	param.addRuntimeLibraryPath("/usr/lib");
	
	// fetch linker command
	if (StrUtil.isEmpty(linker_command))
	    throw new RuntimeException("missing propery <linker-command>");

	// first we have to get all static links handled
	handle_static_libs();
	
	// filter out duplicate objects
	filter_objects();

	ld = new LD_cmdline(linker_command,param.normalizer);

//	boolean flag_strip_debug = false;
	
	String flags[] = param.getLinkFlags();
//	if (flags!=null)
//	{
//	    for (int x=0; x<flags.length; x++)
//		if (flags[x]!=null)
//		{
//		    if (flags[x].equals("strip-debug")
//			flag_strip_debug = true;
//		}
//	}
	
	ld.linker_flag(flags);
	ld.verbatims(param.getVerbatims());
	ld.libpath(param.getLibraryPathes_dec_sysroot());
	ld.rpath(param.getRuntimeLibraryPathes());
	ld.dynlink(param.getSharedLinks());
	ld.staticlink(param.getStaticLinks());
	ld.objlink(param.getObjectLinks_add_sysroot());
	ld.dlname(param.getDLName());
	ld.exportSymbolsRegex(param.getExportSymbolsRegex());
	ld.exportSymbols(param.getExportSymbols());

	// do we probably have to render the output filename ?
	// this is ie. necessary if we just have the module name
	// FIXME !!!
	String output_filename = param.getOutputFile();
	if (StrUtil.isEmpty(output_filename))
	{
	    String version_info = param.getVersionInfo();
	    String module_name  = param.getModuleName();

	    if (StrUtil.isEmpty(module_name))
		throw new RuntimeException("uuhg! no module name given. cannot construct output_filename");

	    output_filename = 
		StrReplace.replace("{MODULE_NAME}", module_name,
		StrReplace.replace("{VERSION_INFO}", version_info,
		(StrUtil.isEmpty(version_info)
		    ? dl_filename_mask_noversion : dl_filename_mask_version)
		));
	}
	ld.output_filename(output_filename);

	String l[] = param.getStaticLinks();

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

	// add misc LD dependend stuff -- fixme!
	String cmdline = ld.toString();

	System.err.println("==> SharedLibraryLinker: "+cmdline);
	Exec exec = new Exec();

	if (!exec.run(cmdline))
	    throw new RuntimeException("Exec failed: "+cmdline);
    }
}
