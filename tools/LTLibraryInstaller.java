
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.LibraryInfo;
import org.de.metux.unitool.base.ToolParam;
import org.de.metux.unitool.base.InstallerParam;
import org.de.metux.unitool.base.EParameterMissing;
import org.de.metux.unitool.base.EParameterInvalid;
import org.de.metux.unitool.db.StoreLibtoolArchive;
import org.de.metux.unitool.db.LoadLibtoolArchive;

import org.de.metux.util.Exec;
import org.de.metux.util.Environment;
import org.de.metux.util.StrUtil;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.FileOps;
import org.de.metux.util.StrSplit;
import org.de.metux.util.UniqueValues;

import java.io.IOException;
import java.io.File;

public class LTLibraryInstaller
{
    void __check_arname(LibraryInfo libinf)
    {
	String str = libinf.library_name+
	    (StrUtil.isEmpty(libinf.release) ? "" : "-"+libinf.release)+
	    ".a";

	if (!libinf.arname.equals(str))
	    System.err.println("arname may be broken: arname="+libinf.arname+" modname="+libinf.module_name+" libname="+libinf.library_name+" should be="+str);
    }

    String __libdirs(LibraryInfo libinf, PathNormalizer norm)
    {
	String str = "";

	if (!StrUtil.isEmpty(libinf.libdir))
	    str += " -L"+libinf.libdir;
	
	if (libinf.search_pathes!=null)
	    for (int x=0; x<libinf.search_pathes.length; x++)
		if (!StrUtil.isEmpty(libinf.search_pathes[x]))
		    str += " -L"+norm.strip_sysroot(libinf.search_pathes[x]);

	return str;
    }
    
    String __dependency(LibraryInfo libinf, LibraryInfo cur, PathNormalizer norm)
    {
	if (cur==null) return "";

	if (cur.library_name==null)
	    throw new NullPointerException("cur.library_name is null");
	if (cur.module_name==null)
	    throw new NullPointerException("cur.module_name is null");
	if (cur.prefix==null)
	    throw new NullPointerException("cur.prefix is null");
	if (cur.libdir==null)
	    throw new NullPointerException("cur.libdir is null");
	if (cur.uninstalled_libdir==null)
	    throw new NullPointerException("cur.uninstalled_libdir is null");

	System.out.println(
		"LTLibraryInstaller: Processing dependency\n"+
		"    module_name="+cur.module_name+"\n"+
		"    library_name="+cur.library_name+"\n"+
		"    prefix="+cur.prefix+"\n"+
		"    libdir="+cur.libdir+"\n"+
		"    uninst="+cur.uninstalled_libdir);

	String add_dep = "";
	    
	if (cur.link_static)
	{
	    System.out.println("    ==> selected to be linked statically. no dependency filed.");
	    if (!StrUtil.isEmpty(cur.libdir))
		throw new RuntimeException("uuh, why is libdir set ?");
	}
	else
	{
	    System.out.println("    ==> selected to be linked dynamically "+cur.library_name+" / "+cur.module_name);

	    // we have no libtool file for it.
	    if (StrUtil.isEmpty(cur.cf))
	    {
		add_dep = __libdirs(cur, norm) + " -l"+cur.module_name;
	    }
	    // libtool'ed library
	    else
	    {
		if (StrUtil.isEmpty(cur.libdir))
		    throw new RuntimeException("uuh, why is libdir empty ?!");
		add_dep = cur.libdir+"/"+PathNormalizer.basename(cur.cf);
	    }
	}
	
	System.out.println("    ==> adding: "+add_dep);
	return add_dep;
    }

    public void run(InstallerParam param)
	throws EParameterMissing, EParameterInvalid
    {
	LibraryInfo libinf;
	String installer_cmd = param.getInstallerCommand();
	String la_source = param.getInstallSource();
	String la_target = param.getInstallTarget();

	// FIXME !
	if (!la_source.endsWith(".la"))
	    throw new RuntimeException("*.la as source expected");
	    
	if (!la_target.endsWith(".la"))
	    throw new RuntimeException("*.la as target expected");
	
	String parent = new File(la_target).getParent();
	if (parent==null)
	    throw new RuntimeException("parent is null ! ("+la_target+")");

	String dirname=PathNormalizer.dirname(la_source);
	try 
	{
	    libinf = LoadLibtoolArchive.load_archive(
		la_source, param.getSysroot(), dirname, false);
	}
	catch (IOException e)
	{
	    throw new RuntimeException("loading .la archive failed"+e,e);
	}

	if (!StrUtil.isEmpty(libinf.dlname))
	    if (libinf.dlname.equals("null"))
		throw new RuntimeException("someone messed up dlname");
	    else
	        throw new RuntimeException("why isn't dlname null ?: "+libinf.dlname);

	libinf.dlname = libinf.library_name+".so."+libinf.version_current+"."+libinf.version_age+"."+libinf.version_revision;

	__check_arname(libinf);

	// libinf.dlname has the form libfoo.so.1
	String so_source = 
	    (StrUtil.isEmpty(libinf.prefix) ? "" : libinf.prefix+"/")+
	    libinf.uninstalled_libdir+
	    libinf.library_name+
	    (StrUtil.isEmpty(libinf.release) ? "" : "-"+libinf.release)+
	    ".so";
	
	String so_link2  = libinf.library_name+".so";
	String so_link1  = libinf.library_name+".so."+libinf.version_current;
	String so_name   = libinf.library_name+".so."+libinf.version_current+"."+libinf.version_age+"."+libinf.version_revision;
	String so_target = parent+"/"+libinf.dlname;

	// do some fixes in the .la file
	libinf.installed           = true;
	libinf.dlname              = so_link1;
	libinf.dynamic_libnames    = new String[3];
	libinf.dynamic_libnames[0] = so_name;
	libinf.dynamic_libnames[1] = so_link1;
	libinf.dynamic_libnames[2] = so_link2;

	System.out.println(
	    "LTLibraryInstaller: processing .la file: "+la_source+"\n"+
	    "    parent="+parent+"\n"+
	    "    so_target="+so_target+"\n"+
	    "    so_source="+so_source+"\n"+
	    "    libdir="+libinf.libdir
	);
	if (libinf.search_pathes == null)
	    System.out.println("    No search pathes");
	else
	    for (int x=0; x<libinf.search_pathes.length; x++)
		System.out.println("    search_path="+libinf.search_pathes[x]);

	// probably this should be moved to StoreLibtoolArchive
	String deps = __libdirs(libinf, param.normalizer)+" ";
	if (libinf.dependencies!=null)
	    for (int x=0; x<libinf.dependencies.length; x++)
		deps += " "+__dependency(libinf, libinf.dependencies[x], param.normalizer);

	libinf.dependency_names = UniqueValues.unique(StrSplit.split(deps));
	
	StoreLibtoolArchive.store(libinf,la_target);

	System.out.println("LTLibraryInstaller: copying so to: "+so_target);
	
	Exec exec = new Exec();

	FileOps.mkdir(parent);	
	FileOps.rm(parent+"/"+so_link1);
	FileOps.rm(parent+"/"+so_link2);

	exec.run("cp --preserve "+so_source+" "+so_target);
	exec.run("cd "+parent+" && ln -s "+so_name+" "+so_link1);
	exec.run("cd "+parent+" && ln -s "+so_name+" "+so_link2);

	if (param.getInstallStrip())
	{
	    // FIXME !
	    String strip_cmd = Environment.getenv("STRIP");
	    if ((strip_cmd==null)||(strip_cmd.equals("")))
		throw new EParameterMissing("missing STRIP command");
	    System.out.println("LTLibraryInstaller: stripping: "+so_target);
	    System.out.println(strip_cmd+" --strip-unneeded "+so_target);
	    exec.run(strip_cmd+" --strip-debug "+so_target);
	}
	else
	    System.out.println("LTLibraryInstaller: not stripping");

	System.out.println("LTLibraryInstaller: done.");
    }
}

//	    /* 
//		handling for imported libtool libraries.
//		since we now natively support .la files in our toolchain,
//		we want to use them in the dependencies, instead of 
//		their content (-l*,-L*).
//
//		we have two situations here: 
//		a) the library has an installed_libdir attribute and thus 
//		   is linked dynamically. in this case we have to add 
//		   the libtool filename under $installed_libdir
//		b) the library has *no* installed_libdir, so is linked
//		   statically and no more dependencies are needed.
//	    */
