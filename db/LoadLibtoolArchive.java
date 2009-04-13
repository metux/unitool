
package org.de.metux.unitool.db;

import org.de.metux.util.ShellVariableDef;
import org.de.metux.util.StrSplit;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;

import org.de.metux.unitool.base.LibraryInfo;

public class LoadLibtoolArchive
{
    public final static String tmp_libdir = ".libs/";

    // for -l direct imports
    static public LibraryInfo directImport(String name, String sysroot, String prefix)
    {
	LibraryInfo inf = new LibraryInfo();
	inf.prefix  = prefix;
	if (name.startsWith("lib"))
	{
	    inf.library_name = name;
	    inf.module_name  = name.substring(3);
	}
	else
	{
	    inf.library_name = "lib"+name;
	    inf.module_name  = name;
	}
	    
	inf.sysroot = sysroot;
	return inf;
    }

    static public LibraryInfo load_archive(
	String fn,
	String sysroot,
	String prefix,
	boolean link_uninstalled_static
    )
	throws FileNotFoundException, IOException
    {
	PathNormalizer norm = new PathNormalizer();
	
	norm.setSysroot(sysroot);

	sysroot = PathNormalizer.normalize_dir(sysroot);

	if (prefix==null)
	    prefix=PathNormalizer.dirname(fn);
	
	prefix = PathNormalizer.normalize_dir(prefix);

	System.out.println("Loading libtool library: "+fn+" (sysroot="+sysroot+" prefix="+prefix+")");

	String search_path = "";
	String line = "";
	
	BufferedReader in = new BufferedReader(new FileReader(fn));
	LibraryInfo inf = new LibraryInfo();

	inf.sysroot            = sysroot;
	inf.uninstalled_libdir = tmp_libdir;
	inf.prefix             = prefix;
	inf.cf                 = fn;
	
	while ((line=in.readLine())!=null)
	{
	    ShellVariableDef shvar;

	    try
	    {
		shvar = new ShellVariableDef(line);
	    }
	    catch (ShellVariableDef.XEmpty e) 
	    {
		continue;
	    }
	    catch (ShellVariableDef.XParseFailed e)
	    {
		throw new RuntimeException("parse error in "+fn+": "+e);
	    }
	    
	    if (shvar.value.length()==0)
		continue;

	    if (shvar.name.equals("dlname"))
		inf.dlname           = shvar.value;
	    else if (shvar.name.equals("library_names"))
		inf.dynamic_libnames = StrSplit.split(shvar.value);
	    else if (shvar.name.equals("old_library"))
	        inf.arname           = shvar.value;
	    else if (shvar.name.equals("release"))
		inf.release          = shvar.value;
	    else if (shvar.name.equals("age"))
	        inf.version_age      = shvar.getInt(0);
	    else if (shvar.name.equals("revision"))
		inf.version_revision = shvar.getInt(0);
	    else if (shvar.name.equals("current"))
		inf.version_current  = shvar.getInt(0);
	    else if (shvar.name.equals("installed"))
		inf.installed = shvar.getBoolean();
	    else if (shvar.name.equals("shouldnotlink"))
		inf.should_not_link = shvar.getBoolean();
	    else if (shvar.name.equals("dlopen"))
		inf.param_dlopen = shvar.value;
	    else if (shvar.name.equals("dlpreopen"))
	        inf.param_dlpreopen = shvar.value;
	    else if (shvar.name.equals("libdir"))
		inf.libdir = norm.strip_sysroot(shvar.value);
	    else if (shvar.name.equals("dependency_libs"))
	    {
		inf.dependency_names = StrSplit.split(shvar.value);
		if (inf.dependency_names.length>0)
		{
		    inf.dependencies = new LibraryInfo[inf.dependency_names.length];
		    for (int y=0; y<inf.dependency_names.length; y++)
	    	    {
			String dep = inf.dependency_names[y];
			if (dep.startsWith("-l"))
			{
			    System.out.println("LoadLibtool ["+fn+"]: direct import: "+dep);
			    inf.dependencies[y] = directImport(dep.substring(2),sysroot,prefix);
			}
			else if (dep.startsWith("-L"))
			{
			    String dirname = PathNormalizer.normalize_dir(dep.substring(2));
			    if (dirname.startsWith(sysroot))
				throw new RuntimeException("-L: path starts with sysroot - is this correct ?!");

			    dirname = norm.enc_sysroot(dirname);
			    search_path += " "+dirname;
			}
			else if (dep.endsWith(".la"))
			{
			    System.out.println("LoadLibtool ["+fn+"]: la file "+dep);
			    if (dep.startsWith("/")) /* absolute path */
				inf.dependencies[y] = load_archive(norm.dec_sysroot(dep), sysroot, prefix, link_uninstalled_static);
			    else
			    {
				String dep_dirname = PathNormalizer.dirname(dep);

				inf.dependencies[y] = 
				    load_archive(
					prefix+dep, 
					sysroot, 
					PathNormalizer.normalize_dir(prefix+dep_dirname), 
					link_uninstalled_static
				    );
			    }				
			}
			else
			    throw new RuntimeException("["+inf.dlname+"] --> unhandled depedency type: "+dep);
		    }
		}
	    }
	    else
		throw new RuntimeException
		(
		    "Unhandled variable: \""+
		    shvar.name+"\" => \""+
		    shvar.value+"\""
		);
	}

	inf.search_pathes = StrSplit.split(search_path);

	// get the library name 
	// FIXME!
	
	if (StrUtil.isEmpty(inf.module_name))
	{
	    String basename = new File(fn).getName();

	    if (!basename.endsWith(".la"))
		throw new RuntimeException("filename does not end with .la - cannot guess libname:" +basename);

	    inf.library_name = basename.substring(0,basename.length()-3);
	    if (inf.library_name.startsWith("lib"))
		inf.module_name = inf.library_name.substring(3);
	    else
		inf.module_name = inf.library_name;
	}
	
	if (StrUtil.isEmpty(inf.libdir))
	{
	    System.out.println("[INFO] "+fn+" has no (dynamic) libdir. linking statically");
	    link_uninstalled_static = true;
	    inf.link_static = true;
	}
	    	
	// we have to do some fixups ...
	if (inf.installed)
	{
	    throw new RuntimeException("loading of installed libs not handled yet: "+fn);
	}
	else 
	{
	    // we can pass via parameter how to link uninstalled libs
	    // the libtool frontend gets this from $LT_UNITOOL_LINK_UNINSTALLED_STATIC
	    // at least problematic packages like SDL (which links several
	    // .a's together to one .so) should work this way.

	    // FIXME: is this really correct ?
	    inf.link_static = link_uninstalled_static;
	    
	    if (inf.dynamic_libnames!=null)
		for (int x=0; x<inf.dynamic_libnames.length; x++)
		    if (!StrUtil.isEmpty(inf.dynamic_libnames[x]))
			inf.dynamic_libnames[x] = 	
			    inf.prefix+inf.uninstalled_libdir+inf.dynamic_libnames[x];
	}

	return inf;
    }
}
