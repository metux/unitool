
package org.de.metux.unitool.base;

public class LibraryInfo
{
    public String        sysroot;  	/* system root prefix */
    public String        arname;   	/* name of the library archive (.a) file */
    public String        module_name;   /* library name (for -l) */
    public String 	 dlname;   	/* dynamic library filename (w/ version) */
    public String        prefix;   	/* prefix / subdir for filenames */
    public String        cf;       	/* config file name (ie. .la) */
    public String        library_name; 	/* full lib name w/ "lib" */
    
    // take care that this arrays may also continue NULLs !
    public String[]      dynamic_libnames=null;
    public LibraryInfo[] dependencies=null;
    public String[]      dependency_names=null;
    public String[]      search_pathes=null;
    
    public int version_current  = 0;
    public int version_age 	= 0;
    public int version_revision = 0; 

    public String release = "";
    
    public boolean installed       = false;
    public boolean should_not_link = false;
    public boolean link_static     = false;
    
    public String param_dlopen     = "";
    public String param_dlpreopen  = "";
    public String libdir           = "";
    
    public String uninstalled_libdir = "";
}
 