
package org.de.metux.unitool.base;

import org.de.metux.util.PathNormalizer;
import org.de.metux.util.UniqueValues;
import org.de.metux.util.StrUtil;
import org.de.metux.util.StrReplace;
import org.de.metux.propertylist.IPropertylist;

public class LinkerParam extends ToolParam
{
    // FIXME !!!
    public static final boolean hack_link_intree_so_direct = false;

    public static final String cf_linktype             = "link-type";
    public static final String cf_linktype_shared      = "shared";
    public static final String cf_linktype_executable  = "executable";
    public static final String cf_runtime_library_path = "runtime-library-path";
    public static final String cf_library_path         = "library-path";
    public static final String cf_object_links         = "link-object";
    public static final String cf_static_links         = "link-static";
    public static final String cf_shared_links         = "link-shared";
    public static final String cf_library_name         = "library-name";
    public static final String cf_library_dlname       = "library-dlname";
    public static final String cf_module_name          = "module-name";
    public static final String cf_linker_command       = "linker-command";
    public static final String cf_linker_flag          = "linker-flag";
    public static final String cf_output_file          = "output-file";
    public static final String cf_version_info         = "version-info";
    public static final String cf_version_current      = "version-current";
    public static final String cf_version_age          = "version-age";
    public static final String cf_version_revision     = "version-revision";
    public static final String cf_export_symbols_regex = "export-symbols-regex";
    public static final String cf_export_symbols       = "export-symbols";
    public static final String cf_import_libtool       = "import-libtool";
    public static final String cf_linker_dll_command   = "tools/linker/link-dll/linker-command";
    public static final String cf_linker_executable_command = "tools/linker/link-executable/linker-command";    

    public LinkerParam(ToolConfig cf)
    {
	super(cf);
    }

    public String[] getRuntimeLibraryPathes()
    {
	return get_path_list_strip_sysroot(cf_runtime_library_path);
    }

    public void addRuntimeLibraryPath(String path[])
    {
	if (path!=null)
	    for (int x=0; x<path.length; x++)
		addRuntimeLibraryPath(path[x]);
    }
    
    public void clearRuntimeLibraryPath()
    {
	remove(cf_runtime_library_path);
    }
    
    public String[] getObjectLinks_add_sysroot()
    {
	String[] pathes = getObjectLinks();
	for (int x=0; x<pathes.length; x++)
	    pathes[x] = normalizer.dec_sysroot(pathes[x]);
	return pathes;
    }

    public String[] getObjectLinks()
    {
	return get_str_list(cf_object_links);
    }

    public String[] getStaticLinks()
    {
	return get_str_list(cf_static_links);
    }

    public void setLinktypeShared()
    {
	set(cf_linktype, cf_linktype_shared);
    }
    
    public void clearLinktype()
    {
	remove(cf_linktype);
    }
        
    public boolean isLinktypeShared()
    {
	return (get_str_def(cf_linktype,cf_linktype_executable).equals(cf_linktype_shared));
    }
    
    public void setModuleName(String name)
    {
	set(cf_module_name, name);
    }

    public String getModuleName()
    {
	return get(cf_module_name);
    }
    
    public void setLibraryName(String name)
    {
	set(cf_library_name, name);
    }
    
    public String getLibraryName()
    {
	return get(cf_library_name);
    }

    // dlname == soname
    public void setDLName(String name)
    {
	set(cf_library_dlname,name);
    }
    
    public String getDLName()
    {
	return get(cf_library_dlname);
    }

    public void clearLibraryName()
    {
	remove(cf_library_name);
    }
    
    public void clearLinkerCommand()
    {
	remove(cf_linker_command);
    }
    
    public void setLinkerCommand(String cmd)
    {
	set(cf_linker_command, cmd);
    }

    public String getLinkerCommand()
    {
	return get(cf_linker_command);
    }

    public void addObjectLink(String obj)
    {
	add(cf_object_links, obj);
    }
    
    public void addObjectLink(String objs[])
    {
	if (objs!=null)
	    for (int x=0; x<objs.length; x++)
		addObjectLink(objs[x]);
    }
    
    public void addSharedLink(String libname)
    {
	if (!StrUtil.isEmpty(libname))
	    add(cf_shared_links, libname);
    }

    public void addSharedLink(String libs[])
    {
	if (libs!=null)
	    for (int x=0; x<libs.length; x++)
		addSharedLink(libs[x]);
    }
    
    public void addStaticLink(String libfile)
    {
	add(cf_static_links, libfile);
    }

    public void addStaticLink(String libs[])
    {
	add(cf_static_links, libs);
    }
    
    // fixme: we also should kick off sysroot-prefixes here
    // we assume its already in sysroot'ed notation !
    // aehm, ... no, it always should be ABSOLUTE notation
    public void addRuntimeLibraryPath(String path)
    {
	add(cf_runtime_library_path, PathNormalizer.normalize(path));
    }
    
    public void addLinkFlag(String flag)
    {
	add(cf_linker_flag, flag);
    }

    public void addLinkFlag(String flag[])
    {
	add(cf_linker_flag, flag);
    }
    
    public String getLinkFlag()
    {
	return get(cf_linker_flag);
    }

    public String[] getLinkFlags()
    {
	return get_str_list(cf_linker_flag);
    }
    
    public void clearLinkFlag()
    {
	remove(cf_linker_flag);
    }

    public String[] getSharedLinks()
    {
	return UniqueValues.unique(get_str_list(cf_shared_links));
    }

    // fixme: we also should kick off sysroot-prefixes here
    public void addLibraryPath(String path)
    {
	add(cf_library_path, PathNormalizer.normalize(path));
    }

    public void addLibraryPath_enc_sysroot(String[] path)
    {
	if (path!=null)
	    for (int x=0; x<path.length; x++)
	    {
		String px = normalizer.enc_sysroot(path[x]);
		add(cf_library_path, px);
	    }
    }
    
    public void addLibraryPath_enc_sysroot(String path)
    {
	add(cf_library_path, normalizer.enc_sysroot(path));
    }

    public void addLibraryPath(String path[])
    {
	if (path!=null)
	    for (int x=0; x<path.length; x++)
		addLibraryPath(path[x]);
    }

    public void clearLibraryPath()
    {
	remove(cf_library_path);
    }
    
    public String[] getLibraryPathes()
    {
	return get_path_list(cf_library_path);
    }

    public String[] getLibraryPathes_dec_sysroot()
    {
	String[] pathes = getLibraryPathes();
	for (int x=0; x<pathes.length; x++)
	    pathes[x] = normalizer.dec_sysroot(pathes[x]);
	pathes = UniqueValues.unique(pathes);
	return pathes;
    }
    
    public void clearObjectLink()
    {
	remove(cf_object_links);
    }
    
    public void clearSharedLink()
    {
	remove(cf_shared_links);
    }
    
    public void clearStaticLink()
    {
	remove(cf_static_links);
    }
    
    public void setOutputFile(String fn)
    {
	set(cf_output_file,fn);
    }
    
    public String getOutputFile()
    {
	return get(cf_output_file);
    }
    
    public void clearOutputFile()
    {
	remove(cf_output_file);
    }

    public void setVersionCurrent(int c)
    {
	set(cf_version_current,c);
    }
    public void setVersionAge(int c)
    {
	set(cf_version_age,c);
    }

    public void setVersionRevision(int c)
    {
	set(cf_version_revision,c);
    }
    public int getVersionRevision_i()
    {
	return get_int(cf_version_revision);
    }
    public int getVersionCurrent_i()
    {
	return get_int(cf_version_current);
    }
    public int getVersionAge_i()
    {
	return get_int(cf_version_age);
    }    

    public void clearVersionInfo()
    {
	remove(cf_version_current);
	remove(cf_version_age);
	remove(cf_version_revision);
	remove(cf_version_info);
    }

    public String getVersionInfo()
    {
	return get(cf_version_info);
    }
    
    public void setVersionInfo(String version)
	throws EVersionSyntaxError
    {
	int x;
	
	version = StrReplace.replace(".",":",version).trim();
	set(cf_version_info, version);
	
	if ((x=version.indexOf(":"))<1)
	    throw new EVersionSyntaxError(version);

	set(cf_version_current, version.substring(0,x));
	version = version.substring(x+1);
	
	if ((x=version.indexOf(":"))<1)
	    throw new EVersionSyntaxError(version);

	set(cf_version_age, version.substring(0,x));
	version = version.substring(x+1);
	
	set(cf_version_revision, version);
    }

    public void addExportSymbols(String fn)
    {
	add(cf_export_symbols,fn);
    }
    
    public void addExportSymbols(String fn[])
    {
	add(cf_export_symbols,fn);
    }

    public void addExportSymbolsRegex(String re)
    {
	add(cf_export_symbols_regex, re);
    }

    public void addExportSymbolsRegex(String[] re)
    {
	add(cf_export_symbols_regex, re);
    }

    public String[] getExportSymbols()
    {
	return get_str_list(cf_export_symbols);
    }
        
    public String[] getExportSymbolsRegex()
    {
	return get_str_list(cf_export_symbols_regex);
    }

    public void clearExportSymbols()
    {
	remove(cf_export_symbols);
    }

    public void clearExportSymbolsRegex()
    {
	remove(cf_export_symbols_regex);
    }

    public void clearLibraryImport()
    {
	remove(cf_import_libtool);
    }

    public void addLibraryImport(String lib)
    {
	if (lib.endsWith(".la"))
	    add(cf_import_libtool,PathNormalizer.normalize(lib));
	else 
	    throw new RuntimeException("unhandled library import: "+lib);
    }

    public void addLibraryImport(String lib[])
    {
	for (int x=0; x<lib.length; x++)
	    addLibraryImport(lib[x]);
    }

    public String[] getLibraryImports()
    {
	return get_str_list(cf_import_libtool);
    }

    public void addLibraryImport(PackageInfo libinf[])
    {
	if (libinf!=null)
	    for (int x=0; x<libinf.length; x++)
		addLibraryImport(libinf[x]);
    }
	
    public void addLibraryImport(PackageInfo libinf)
    {
	System.err.println("addLibraryImport(): "+libinf.name);

	// FIXME: perhaps not finished yet
	addLibraryPath_enc_sysroot(libinf.library_pathes.getNames());
	addSharedLink(libinf.libraries.getNames());
	
	if (!StrUtil.isEmpty(libinf.ldflags))
	    throw new RuntimeException("verbatim ldflags not supported yet");
	if (!StrUtil.isEmpty(libinf.ldflags_private))
	    throw new RuntimeException("verbatim ldflags_private not supported yet");
	if (!StrUtil.isEmpty(libinf.requires))
	    throw new RuntimeException("requires in pkginf not supported yet");
	if (!StrUtil.isEmpty(libinf.requires_private))
	    throw new RuntimeException("require_private in pkginf not supprted yet");
    }

    public void addLibraryImport(LibraryInfo libinf)
    {
	// FIXME we probably should compare the sysroot w/ the LinkerParam
	if ((!StrUtil.isEmpty(libinf.sysroot))	&&
	   (!PathNormalizer.normalize_dir(libinf.sysroot).equals
	    (PathNormalizer.normalize_dir(getSysroot()))))
	   throw new RuntimeException("mismatching sysroot inf->"+libinf.sysroot+" self->"+getSysroot());

	if (libinf.link_static)
	{
	    System.out.println("LinkerParam::addLibraryImport() linking static: "+libinf.library_name);

	    if (StrUtil.isEmpty(libinf.arname))
		throw new RuntimeException("missing arname for static linking");

	    if (libinf.installed)
		throw new RuntimeException("linking statically against installed libs not supported yet");
	    else
	    {
		String fullpath = libinf.prefix+libinf.uninstalled_libdir+libinf.arname;
		System.out.println("addLibraryImport(): linking static against noninstalled: "+fullpath+" (arname="+libinf.arname+")");
		addStaticLink(fullpath);
	    }
	}
	else
	{

	    if (libinf.installed)
	    {
		System.out.println("addLibraryImport(): linking dynamic against installed: "+libinf.module_name+" / "+libinf.library_name);
		addLibraryPath_enc_sysroot(libinf.libdir);
	    }
	    else
	    {
		System.out.println("addLibraryImport(): linking dynamically against an uninstalled so "+libinf.library_name+" "+libinf.cf+" "+libinf.dlname);	
		addLibraryPath_enc_sysroot(libinf.prefix+libinf.uninstalled_libdir);
	    }
	    
	    if (StrUtil.isEmpty(libinf.module_name))
		throw new RuntimeException("missing dynamic module name");
	
	    if (StrUtil.isEmpty(libinf.library_name))
		throw new RuntimeException("missing dynamic library name");
	
	    if ((libinf.cf!=null)&& 
	         libinf.cf.startsWith("../") &&
		 hack_link_intree_so_direct)
	    {
		String objname = libinf.prefix+libinf.uninstalled_libdir+libinf.library_name+".so";

		System.out.println("addLibraryImport(): enabled hack_link_intree_direct");
		System.out.println("                    dlname="+libinf.dlname);
		System.out.println("                    library_name="+libinf.library_name);
		System.out.println("                    uninstalled_libdir="+libinf.uninstalled_libdir);
		System.out.println("                    prefix="+libinf.prefix);
		System.out.println(" --> objname="+objname);
		
		addObjectLink(objname);
	    }
	    else
		addSharedLink(libinf.module_name);
	}

	if (libinf.search_pathes!=null)
	    for (int x=0; x<libinf.search_pathes.length; x++)
		addLibraryPath_enc_sysroot(libinf.search_pathes[x]);

	System.out.println("addLibraryImport["+libinf.library_name+"] processing imports");
	if (libinf.dependencies!=null)
	    for (int x=0; x<libinf.dependencies.length; x++)
		if (libinf.dependencies[x]!=null)
		    addLibraryImport(libinf.dependencies[x]);
	System.out.println("addLibraryImport["+libinf.library_name+"] finished imports");

	try
	{
	    setVersionInfo(
		libinf.version_current+"."+
	        libinf.version_age+"."+
	        libinf.version_revision
	    );
	}
	catch (Exception e)		// FIXME !!!
	{
	    throw new RuntimeException(e);
	}
	
//	setVersionCurrent(libinf.version_current);
//	setVersionAge(libinf.version_age);
//	setVersionRevision(libinf.version_revision);
    }
}
