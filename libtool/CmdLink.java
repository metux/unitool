//
// FIXME: should handle the export-symbols-regex linker parameters
//        by giving it to the actual tools
//
 
package org.de.metux.unitool.libtool;

import org.de.metux.util.Environment;
import org.de.metux.util.StrReplace;
import org.de.metux.util.FileOps;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StrSplit;
import org.de.metux.util.StrUtil;
import org.de.metux.util.UniqueValues;

import org.de.metux.propertylist.IPropertylist;

import org.de.metux.unitool.base.ToolParam;
import org.de.metux.unitool.base.LinkerParam;
import org.de.metux.unitool.base.EUnitoolError;
import org.de.metux.unitool.base.LibraryInfo;
import org.de.metux.unitool.base.ObjectInfo;
import org.de.metux.unitool.base.ToolConfig;

import org.de.metux.unitool.db.LoadLibtoolArchive;
import org.de.metux.unitool.db.LoadLibtoolObject;
import org.de.metux.unitool.db.StoreLibtoolArchive;

import org.de.metux.unitool.tools.LinkSharedLibrary;
import org.de.metux.unitool.tools.LinkExecutable;
import org.de.metux.unitool.tools.LinkStaticLibrary;

import java.io.IOException;
import java.util.Enumeration;

public class CmdLink
{
    /* link libraries from within our sourcetree (aka addressed via 
       relative path) by directly passing library filename */
    boolean ltmagic_intree_link_direct = true;
    
    String sysroot;
    String release;
    LinkerParam param;

    boolean link_uninstalled_static = false;
    boolean link_ar_recursive       = Environment.getenv_bool("LT_UNITOOL_LINK_AR_RECURSIVE",false);

    ToolConfig config;
    
    String [] my_args;
    
    CmdLink(String[] argv, ToolConfig cf) throws Exception, EUnitoolError
    {
	my_args = argv;
	config = cf;
    }

    void processLibraryImport(String libs[], LinkerParam par, boolean uninstalled_static)
	throws IOException
    {
	for (int x=0; x<libs.length; x++)
	    processLibraryImport(libs[x], par, uninstalled_static);
    }

    // FIXME: move this to LinkerParam
    void processLibraryImport(String current, LinkerParam par, boolean uninstalled_static)
	throws IOException
    {
	if (StrUtil.isEmpty(current))
	    return;

	// append $sysroot to absolute pathnames
	String add_la_file = par.normalizer.enc_sysroot(current);
	String la_prefix   = 
	    PathNormalizer.normalize_dir(
		PathNormalizer.dirname(current
	));

	System.out.println("processLibraryImport: loading file: "+add_la_file+" with prefix="+la_prefix+" current="+current);

	LibraryInfo libinf = LoadLibtoolArchive.load_archive(
	    add_la_file,
	    sysroot,
	    la_prefix,
	    uninstalled_static
	);

	par.addLibraryImport(libinf);
    }

    public void run()
	throws EUnitoolError, IOException
    {
	// FIXME: 
	param = new LinkerParam(config);
	LinkerParam param_ar = new LinkerParam(config);
	
	String _output = null;
	
	if ((sysroot=Environment.getenv("SYSROOT"))==null)
	    sysroot = "";
	
	String env_link_uninstalled_static = Environment.getenv("LT_UNITOOL_LINK_UNINSTALLED_STATIC");
	if ((env_link_uninstalled_static!=null)&&
	    (env_link_uninstalled_static.equals("YES")))
	{
	    System.out.println("[INFO] Linking uninstalled libs statically");
	    link_uninstalled_static = true;
	}
	else
	{
	    System.out.println("[INFO] uninstalled libs are linked dynamically (default)");
	    link_uninstalled_static = false;
	}
	
	param.setSysroot(sysroot);
	param.normalizer.addSkip(Environment.getenv("SRCDIR"));

	if (my_args.length<2)
	    throw new RuntimeException("missing compiler command");

	param.setLinkerCommand(my_args[1]);
	param.setVersionInfo("0:0:0");
	
	for (int x=2; x<my_args.length; x++)
	{
	    String current = my_args[x];
	    System.out.println("CmdLink -> Parameter["+x+"]: "+current);
	    
	    if (current.equals("-Wall") 		||
	        current.equals("-Wpointer-arith") 	||
		current.equals("-Wmissing-prototypes")
	    ) 
		System.err.println("NOTICE: ignoring useless parameter: "+current);
	    else if (current.equals("-include"))
		System.err.println("NOTICE: ignoring useless parameter: -include "+my_args[++x]);
	    else if (current.startsWith("-I"))
		System.err.println("NOTICE: ignoring useless parameter: "+current);
	    else if (current.equals("-g")			||
		current.equals("-O2")			||
		current.startsWith("-march=")		||
		current.startsWith("-mcpu=")		||
		current.equals("-export-dynamic")	||
		current.startsWith("-W")		||
		current.startsWith("-f")
	    )
	    {
		System.err.println("FIXME: adding verbatim: "+current);
		param.addVerbatim(current);
	    }
	    else if (current.equals("-o"))
	    {
		_output = my_args[++x];
		param.setOutputFile(_output);
	    }
	    // FIXME !
	    else if (current.equals("-export-symbols"))
		param.addExportSymbols(my_args[++x]);
	    else if (current.equals("-export-symbols-regex"))
		param.addExportSymbolsRegex(my_args[++x]);
	    else if (current.equals("-no-undefined"))
		param.addLinkFlag("no-undefined");
	    else if (current.equals("-version-info")||current.equals("-version-number"))
		param.setVersionInfo(my_args[++x]);
	    else if (current.equals("-pthread"))
		param.addSharedLink("pthread");
	    else if ((current.equals("-rpath"))||(current.equals("-R")))
		param.addRuntimeLibraryPath(my_args[++x]);
	    else if (current.equals("-release"))
		release = my_args[++x];
	    else if (current.startsWith("-D"))
		;	// param.addDefine(current.substring(2));
	    else if (current.startsWith("-L"))
		param.addLibraryPath_enc_sysroot(current.substring(2));
	    else if (current.startsWith("-l"))
		param.addSharedLink(current.substring(2));
	    else if (current.endsWith(".lo"))
	    {
		ObjectInfo objinf = LoadLibtoolObject.load_lo(current);
		// FIXME: we're currently only using PIC code
		param.addObjectLink(objinf.object_pic);
	    }
	    else if (current.endsWith(".o"))
		param.addObjectLink(current);
	    else if (current.endsWith(".la"))
		param.addLibraryImport(current);
	    else if (current.endsWith(".a"))
		param.addStaticLink(current);
	    else
//		throw new RuntimeException("UNHANDLED PARAM: "+current);
	    {
		System.err.println("WARN: adding verbatim parameter:"+current);
		param.addVerbatim(current);
	    }
	}

	/* process output file option */
	if (_output == null)
	    throw new RuntimeException("no output given");

	if (_output.endsWith(".la"))
	    target_type_la();
	else if (_output.endsWith(".a"))
	    throw new RuntimeException("linking directly to .a files not supported");
	else if (_output.endsWith(".so"))
	    throw new RuntimeException("linking directly to .so files not supported");
	else if (_output.endsWith(".lo"))
	    throw new RuntimeException("linking to .lo files not supported");
	else if (_output.endsWith(".o"))
	    throw new RuntimeException("linking to .o files not supported");
	else
	    target_type_executable();
    }

    // create a shared library as .la file
    
    // library_name is w/ lib*, but module_name is without (ie. for -l)
    public void target_type_la()
	throws IOException
    {
	String output_la_file  = param.getOutputFile();
	String output_basename = PathNormalizer.basename(output_la_file);
	String output_dirname  = PathNormalizer.dirname_slash(output_la_file);
	String module_name;

	// library name without "lib" and ".so", just "foo"
	String library_name = output_basename.substring(0,output_basename.lastIndexOf(".la"));
	if (library_name.startsWith("lib"))
	    module_name = library_name.substring(3);
	else
	{
	    module_name = library_name;
	    System.err.println("CmdLink::target_type_la(): WARNING: output filename should start with \"lib\"");
	}
	
	if (!StrUtil.isEmpty(release))
	    library_name += "-"+release;

	System.out.println("LIBRARY: library_name="+library_name+" module="+module_name);
	    
    	// libfoo.so + libfoo.a
	String shared_library_short = library_name+".so";
	String static_archive_file  = library_name+".a";	

	FileOps.mkdir(LoadLibtoolArchive.tmp_libdir);
	FileOps.mkdir(output_dirname+LoadLibtoolArchive.tmp_libdir);

	/* -- fetch out parameters -- */
	String par_objectlinks[]    = param.getObjectLinks();
	String par_staticlinks[]    = param.getStaticLinks();
	String par_verbatims[]      = param.getVerbatims();
	String par_exportsymregex[] = param.getExportSymbolsRegex();
	String par_exportsymbols[]  = param.getExportSymbols();
	String par_imports[]        = param.getLibraryImports();
	String par_linkflag         = param.getLinkFlag();
	String par_sysroot          = param.getSysroot();

	String rpath[] = param.getRuntimeLibraryPathes();
	if ((rpath==null)||(rpath.length==0))
	    throw new RuntimeException("missing rpath");
	    
	if (rpath.length>1)
    	    throw new RuntimeException("more than one rpath given");

	LibraryInfo[] imports = new LibraryInfo[par_imports.length];
	for (int x=0; x<par_imports.length; x++)
	{
	    if (!StrUtil.isEmpty(par_imports[x]))
	    {
	    	if (!par_imports[x].endsWith(".la"))
			throw new RuntimeException("unhandled import type: "+par_imports[x]);

		System.out.println("Loading imported library info: "+par_imports[x]);
		imports[x] = LoadLibtoolArchive.load_archive(
		    par_imports[x],
		    sysroot, 
	    	    null, 
		    link_uninstalled_static 
		);
	    }
	}
	
	/* -- create static library output (.a) -- */
	{
	    LinkerParam param_ar = new LinkerParam(config);
	    param_ar.setSysroot(par_sysroot);
	    param_ar.setOutputFile(output_dirname+LoadLibtoolArchive.tmp_libdir+static_archive_file);
	    param_ar.setLibraryName(library_name);
	    param_ar.addObjectLink(par_objectlinks);
	    param_ar.addStaticLink(par_staticlinks);
	    param_ar.addExportSymbolsRegex(par_exportsymregex);
	    param_ar.addExportSymbols(par_exportsymbols);
	    param_ar.addLinkFlag(par_linkflag);

	    if (link_ar_recursive)
		processLibraryImport(par_imports,param_ar,link_uninstalled_static);
	    else
		System.err.println("LinkStatic: we do not recursivly import .a into another .a");
	    new LinkStaticLibrary(param_ar).run();
	}
	
	/* -- create shared library output (.so) -- */
	{
	    LinkerParam param_so = new LinkerParam(config);
	    param_so.normalizer = param.normalizer;
	    param_so.setSysroot(par_sysroot);
	    param_so.setOutputFile(output_dirname+LoadLibtoolArchive.tmp_libdir+shared_library_short);
	    param_so.setLinktypeShared();
	    param_so.setLinkerCommand(param.getLinkerCommand());
	    param_so.setLibraryName(library_name);
	    param_so.setModuleName(module_name);
	    param_so.addVerbatim(par_verbatims);
	    param_so.addSharedLink(param.getSharedLinks());
	    param_so.addObjectLink(param.getObjectLinks());
	    param_so.addLibraryPath(param.getLibraryPathes());
	    param_so.addRuntimeLibraryPath(param.getRuntimeLibraryPathes());
	    param_so.addLinkFlag(par_linkflag);
	    param_so.addStaticLink(par_staticlinks);
	    param_so.addExportSymbolsRegex(par_exportsymregex);
	    param_so.addExportSymbols(par_exportsymbols);
	    processLibraryImport(par_imports,param_so,link_uninstalled_static);
	    new LinkSharedLibrary(param_so).run();
	}

	/* -- create .la file -- */
	{
	    LibraryInfo libinf = new LibraryInfo();
	
	    System.out.println("==> Creating libtool library: "+output_la_file);
	    System.out.println("    module_name="+module_name);
	    System.out.println("    library_name="+library_name);
	    System.out.println("    arname="+static_archive_file);
	    System.out.println("    release="+release);

	    // generate libtool information for uninstalled library	
	    libinf.prefix           = "";
	    libinf.libdir           = rpath[0];
	    libinf.installed        = false;
	    libinf.library_name     = library_name;
	    libinf.module_name      = module_name;
	    libinf.version_current  = param.getVersionCurrent_i();
	    libinf.version_age      = param.getVersionAge_i();
    	    libinf.version_revision = param.getVersionRevision_i();
	    libinf.arname           = static_archive_file;
	    libinf.release          = release;
	    libinf.search_pathes    = param.getLibraryPathes();
	    
	    if (libinf.search_pathes != null)
		for (int x=0; x<libinf.search_pathes.length; x++)
		    libinf.search_pathes[x] = 
			param.normalizer.strip_sysroot(libinf.search_pathes[x]);

	    String deps = "";

	    for (int x=0; x<imports.length; x++)
	    {
		if (imports[x] != null)
		{
		    System.out.println("    processing library import #"+x);

		    /* we skip dependencies to statically linked-in libs, 
		       otherwise we sometimes would link these objects twice */
	       
		    if (imports[x].link_static)
			System.out.println(" ==> imported lib ("+imports[x].library_name+") is linked statically - skipping it from .la's dependency list" );
		    else
	    	        deps += " "+par_imports[x];
		}
	    }
		
	    for (int x=0; x<libinf.search_pathes.length; x++)
		if (!StrUtil.isEmpty(libinf.search_pathes[x]))
		    deps += " -L"+param.normalizer.strip_sysroot(libinf.search_pathes[x]);
		
	    String dynlibs[] = param.getSharedLinks();
	    for (int x=0; x<dynlibs.length; x++)
		if (!StrUtil.isEmpty(dynlibs[x]))
		    deps += " -l"+dynlibs[x];

	    libinf.dependency_names = UniqueValues.unique(StrSplit.split(deps));
	    libinf.libdir = param.normalizer.strip_sysroot(libinf.libdir);

	    StoreLibtoolArchive.store(libinf,output_la_file);
	}

	run_postcheck();
    }

    // create a shared library as .la file
    public void target_type_executable()
	throws IOException
    {
	String output   = param.getOutputFile();
	String basename = PathNormalizer.basename(output);
	String dirname  = PathNormalizer.dirname_slash(output);

	String par_verbatims[]   = param.getVerbatims();
	String par_runtimepath[] = param.getRuntimeLibraryPathes();
	String par_libpath[]     = param.getLibraryPathes();
	String par_sharedlinks[] = param.getSharedLinks();
	String par_objlinks[]    = param.getObjectLinks();
	String par_linkflag[]    = param.getLinkFlags();

	if (!StrUtil.isEmpty(release))
	    output += "-"+release;

	FileOps.mkdir(LoadLibtoolArchive.tmp_libdir);

	LinkerParam param_exe = new LinkerParam(config);
	param_exe.normalizer = param.normalizer;
	param_exe.setSysroot(param.getSysroot());
	param_exe.setOutputFile(dirname+LoadLibtoolArchive.tmp_libdir+basename);
	param_exe.setLinkerCommand(param.getLinkerCommand());
	param_exe.addVerbatim(par_verbatims);
	param_exe.addSharedLink(par_sharedlinks);
	param_exe.addObjectLink(par_objlinks);
	param_exe.addLibraryPath(par_libpath);
	param_exe.addRuntimeLibraryPath(par_runtimepath);
	param_exe.addLinkFlag(par_linkflag);
	param_exe.addStaticLink(param.getStaticLinks());
	processLibraryImport(param.getLibraryImports(),param_exe,link_uninstalled_static);

	new LinkExecutable(param_exe).run();
	
	String symlink_target = dirname+LoadLibtoolArchive.tmp_libdir+basename;
	
	/* an special hack for outputs in subdirs ... fixme ... */
	String output_dirname = PathNormalizer.dirname_slash(output);
	System.err.println("output_dirname="+output_dirname);
	if (symlink_target.startsWith(output_dirname))
	{
	    System.err.println("Output in subdir ... fixing filenames");
	    int sz = output_dirname.length();
	    symlink_target = symlink_target.substring(sz);
	    
	    System.err.println("output="+output);
    	    System.err.println("symlink_target="+symlink_target);
	}

	FileOps.symlink(symlink_target,output);

	run_postcheck();
    }

    void run_postcheck()
    {
	// clear all used parameters and check if somethhing remains
	
	param.clearSysroot();
	param.clearOutputFile();
	param.clearLibraryName();
	param.clearLinkerCommand();
	param.clearRuntimeLibraryPath();
	param.clearLibraryPath();
	param.clearSharedLink();
	param.clearObjectLink();
	param.clearLinktype();
	param.clearVersionInfo();
	param.clearVerbatim();
	param.clearExportSymbolsRegex();
	param.clearExportSymbols();
	param.clearStaticLink();
	param.clearLinkFlag();
	param.clearLibraryImport();
	
	for (Enumeration e = param.propertyNames(); e.hasMoreElements(); )
	{
	    String key = (String) e.nextElement();
	    throw new RuntimeException("unhandled parameter: "+key+"=>\""+param.get(key)+"\"");
	}
    }
}
