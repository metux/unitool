
package org.de.metux.unitool.db;

import org.de.metux.unitool.base.LinkerParam;

public class LibraryInfo
{
    public String        sysroot=null;  /* system root prefix */
    public String        arname=null;	/* name of the library archive (.a) file */
    public String        name=null;     /* library name (for -l) */
    public String 	 dlname=null;	/* dynamic library filename (w/ version) */
    public String        prefix=null;   /* prefix / subdir for filenames */
    
    // take care that this arrays may also continue NULLs !
    public String[]      dynamic_libnames=null;
    public LibraryInfo[] dependencies=null;
    public String[]      dependency_names=null;
    public String[]      search_pathes=null;
    
    public int version_current  = 0;
    public int version_age 	= 0;
    public int version_revision = 0; 

    public String release = null;
    
    public boolean installed       = false;
    public boolean should_not_link = false;
    public boolean link_static     = false;
    
    public String param_dlopen     = null;
    public String param_dlpreopen  = null;
    public String libdir           = null;

    // FIXME: only supports dynamic libraries

    public void store(LinkerParam param)
    {
	// FIXME we probably should compare the sysroot w/ the LinkerParam
	if ((sysroot!=null) && (!sysroot.equals("")))
	    param.setSysroot(sysroot);

	if (libdir!=null)
	{
	    if (sysroot==null)
		throw new RuntimeException("uuuh, no sysroot ?!");

	    param.addLibraryPath_enc_sysroot(libdir);
	}

	if (link_static)
	{
	    // we enforce static linking
	    param.addStaticLink(prefix+arname);
	    System.out.println("LibraryInfo.store(): linking static against: "+prefix+arname);
	}
	else
	{
	    System.out.println("LibraryInfo.store(): linking dynamic against: "+name);

	    if (name!=null)
		param.addSharedLink(name);    
	    else if (dlname!=null)
	    {
		if (libdir==null)
		    param.addObjectLink(dlname);
	        else
	    	    param.addObjectLink(libdir+"/"+dlname);
	    }
	    else if (arname!=null)	
	    {
		if (libdir==null)
		    param.addObjectLink(arname);
	        else
	    	    param.addObjectLink(libdir+"/"+arname);
	    }
	    else
		throw new RuntimeException("nothing to import!");
	}
	
	if (search_pathes!=null)
	    for (int x=0; x<search_pathes.length; x++)
		param.addLibraryPath_enc_sysroot(search_pathes[x]);

	if (dependencies!=null)
	    for (int x=0; x<dependencies.length; x++)
		if (dependencies[x]!=null)
		    dependencies[x].store(param);

	param.setVersionCurrent(version_current);
	param.setVersionAge(version_age);
	param.setVersionRevision(version_revision);
    }
}
