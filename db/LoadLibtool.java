
package org.de.metux.unitool.db;

import org.de.metux.util.ShellVariableDef;
import org.de.metux.util.StrSplit;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalize;
import org.de.metux.util.PathNormalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;

import org.de.metux.unitool.base.LibraryInfo;

public class LoadLibtool
{
    public final static String tmp_libdir = ".libs/";

    // for -l direct imports
    static public LibraryInfo directImport(String name, String sysroot)
    {
	LibraryInfo inf = new LibraryInfo();
	inf.module_name = name;
	inf.sysroot     = sysroot;
	return inf;
    }

    static public LibraryInfo load_archive(String fn, String sysroot)
	throws FileNotFoundException, IOException
    {
	return load_archive(fn,sysroot,"");
    }

    static public ObjectInfo load_lo(String fn)
	throws FileNotFoundException, IOException
    {
	BufferedReader in =
	    new BufferedReader(new FileReader(fn));
	
	String line;
	ObjectInfo inf = new ObjectInfo();
	inf.lo_file = fn;
	
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

	    if (shvar.name.equals("pic_object"))
		inf.object_pic = shvar.value;
	    else if (shvar.name.equals("non_pic_object"))
		inf.object_nonpic = shvar.value;
	    else
		throw new RuntimeException
		(
		    "Unhandled variable: \""+
		    shvar.name+"\" => \""+
		    shvar.value+"\""
		);
	}

	return inf;
    }
    
    static public LibraryInfo load_archive(
	String fn,
	String sysroot,
	String prefix
    )
	throws FileNotFoundException, IOException
    {
	PathNormalizer norm = new PathNormalizer();
	norm.setSysroot(sysroot);

	sysroot = PathNormalizer.normalize_dir(sysroot);

	if (prefix==null)
	    prefix="";
	else if (prefix.length()==0);
	else 
	    prefix+="/";

	System.out.println("Loading libtool library: "+fn+" (sysroot="+sysroot+" prefix="+prefix+")");

	String search_path = "";
	
	BufferedReader in =
	    new BufferedReader(new FileReader(fn));
	
	String line;
	LibraryInfo inf = new LibraryInfo();
	inf.sysroot = sysroot;
	
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
//		inf.libdir = PathNormalizer.normalize_dir(prefix+shvar.value);
		inf.libdir = norm.enc_sysroot(prefix+shvar.value);
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
			    System.out.println("LoadLibtool: direct import: "+dep);
			    inf.dependencies[y] = directImport(dep.substring(2),sysroot);
			}
			else if (dep.startsWith("-L"))
			{
			    String dirname = PathNormalizer.normalize_dir(dep.substring(2));
//	-- we currently dont use it, instead issue a warning
//			    if (dirname.startsWith(sysroot))
//				dirname = PathNormalize.normalize_strip_sysroot(dirname,sysroot);
//			    dirname = PathNormalizer.normalize()
			    if (dirname.startsWith(sysroot))
				throw new RuntimeException("-L: path starts with sysroot - is this correct ?!");

			    dirname = norm.enc_sysroot(dirname);

//			    System.out.println("["+inf.dlname+"] --> library search path: "+dirname);
			    search_path += " "+dirname;
			}
			else if (dep.endsWith(".la"))
			{
			    dep = new File(dep).getCanonicalPath();
			    System.out.println("Canonical path: "+dep);
			    if (dep.startsWith("/"))
				inf.dependencies[y] = load_archive(sysroot+dep, sysroot, prefix);
			    else
				inf.dependencies[y] = load_archive(dep, sysroot, prefix);
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

	inf.prefix        = prefix;
	inf.search_pathes = StrSplit.split(search_path);

	// get the library name 
	// FIXME!
	
	if (inf.module_name == null)
	{
	    String basename = new File(fn).getName();

	    if (basename.startsWith("lib"))
		basename = basename.substring(3);
	    else
		System.err.println("guess_libname: libname does not start with .la - guessing probably wrong: "+basename);

	    if (!basename.endsWith(".la"))
		throw new RuntimeException("filename does not end with .la - cannot guess libname:" +basename);
	    inf.module_name = basename.substring(0,basename.length()-3);
	}
	    	
	// we have to do some fixups ...
	if (inf.installed)
	{
	    throw new RuntimeException("loading of installed libs not handled yet: "+fn);
	}
	else 
	{
	    // we currently enfoce static linking against non-installed libs
	    // i don't see any way out of this now -- FIXME !!!
	    // at least problematic packages like SDL (which links several
	    // .a's together to one .so) should work this way.
	    if (inf.arname.length()!=0)
	        inf.arname = LoadLibtool.tmp_libdir+inf.arname;
	
	    inf.link_static = true;
	    
	    if ((inf.dynamic_libnames!=null)&&(inf.dynamic_libnames.length!=0))
		for (int x=0; x<inf.dynamic_libnames.length; x++)
		    if ((inf.dynamic_libnames[x] != null) && 
			(inf.dynamic_libnames[x].length()!=0))
			inf.dynamic_libnames[x] = LoadLibtool.tmp_libdir+inf.dynamic_libnames[x];
	}

	return inf;
    }
}
