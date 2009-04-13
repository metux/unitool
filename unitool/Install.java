
package org.de.metux.unitool.unitool;

import org.de.metux.util.*;
import org.de.metux.unitool.base.*;
import java.io.File;

public class Install extends Command
{
    public Install(String[] args)
    {
	super(args);
    }
    
    public boolean install_resource() 
	throws EParameterMissing, EParameterInvalid
    {
	String source = get_mandatory("source-filename");
	return false;
    }
    
    private String abs_filename(String filename)
	throws EParameterInvalid
    {
	String destdir = get_str("install-root");
	if (destdir==null)
	    return filename;
	    
	return destdir+"/"+filename;	    
    }
    
    public boolean install_shared_library() 
	throws EParameterMissing, EInstallFailed, EParameterInvalid
    {
	String libname = get_mandatory("name");
	String dir     = abs_filename(get_mandatory("destination-dir"));
	String mode    = get_str("mode");
	String srcdir  = get_str("source-dir");
	String libver  = get_str("shlib-version");
	
	if (mode==null) mode = "u=rwx,go=rx";
		
	String src  = ((srcdir==null)?"":srcdir+"/")+libname;
	String dest = dir+"/"+libname;
	
	if (libver!=null)
	    libname+="."+libver;
	    
	if (!FileOps.mkdir(dir))
	    throw new EInstallFailed("could not create directory: "+dir);
	
	if (!FileOps.cp(src,dir))
	    throw new EInstallFailed("could not copy "+src+" to "+dir);

	if (!FileOps.chmod(dest,mode))	
	    throw new EInstallFailed("could not chmod:"+dest);

	return true;
    }

    public boolean install_binary_executable() 
	throws EParameterMissing, EInstallFailed, EParameterInvalid
    {
	String name = get_mandatory("name");
	String dir     = abs_filename(get_mandatory("destination-dir"));
	String mode    = get_str("mode");
	String srcdir  = get_str("source-dir");
	
	if (mode==null) mode = "u=rwx,go=rx";
		
	String src  = ((srcdir==null)?"":srcdir+"/")+name;
	String dest = dir+"/"+name;
	
	if (!FileOps.mkdir(dir))
	    throw new EInstallFailed("could not create directory");
	
	if (!FileOps.cp(src,dir))
	    throw new EInstallFailed("could not copy file");

	if (!FileOps.chmod(dest,mode))	
	    throw new EInstallFailed("could not chmod: "+dest);
	
	return true;
    }

    public boolean install_directory() 
	throws EParameterMissing, EInstallFailed, EParameterInvalid
    {
	String dirname    = get_mandatory("name");
	String absdirname = abs_filename(dirname);
	String mode;
	
	if (!FileOps.mkdir(absdirname))
	    throw new EInstallFailed("could not create directory "+absdirname);

	if ((mode=get_str("mode"))!=null)
	{
	    if (!FileOps.chmod(absdirname,mode))
		throw new EInstallFailed("chmod failed");
	}
	
	return true;
    }
    
    public boolean run() 
	throws EParameterMissing, EParameterInvalid, EInstallFailed
    {
	String filetype = get_mandatory("type");
	
	if (filetype.equals("directory"))
	    return install_directory();
	    
	if (filetype.equals("resource"))
	    return install_resource();
	    
//	if (filetype.equals("manpage"))
//	    return install_manpage();
//	    
	if (filetype.equals("binary-executable"))
	    return install_binary_executable();
	    
	if (filetype.equals("shared-library") ||
	    filetype.equals("shlib"))
	    return install_shared_library();
	    
//	if (filetype.equals("static-library"))
//	    return install_static_library();
//	
//	if (filetype.equals("header"))
//	    return install_header();
//	    
//	if (filetype.equals("configfile"))
//	    return install_configfile();

	throw new EInstallFailed("unsupported filetype: "+filetype);
    }
}
