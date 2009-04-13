
package org.de.metux.unitool.db;

import org.de.metux.util.ShellVariableDef;
import org.de.metux.util.StrSplit;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StrUtil;
import org.de.metux.util.Environment;
import org.de.metux.util.Exec;
import org.de.metux.util.CmdLineSplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;

import org.de.metux.unitool.base.PackageInfo;
import org.de.metux.unitool.base.EPkgConfigMissingProperty;
import org.de.metux.unitool.base.EPkgConfigParseError;
import org.de.metux.unitool.base.EPkgConfigUnhandledProperty;

public class QueryPkgConfig
{
    String query_command;
    String path;
    String sysroot;
        
    public QueryPkgConfig()
    {
	query_command = Environment.getenv("PKG_CONFIG");
    }
    
    public void setPath(String p)
    {
	path = p;
    }
    
    public void setCommand(String cmd)
    {
	query_command = cmd;
    }

    public void setSysroot(String sr)
    {
	sysroot = sr;
    }
    
    public PackageInfo queryPackage(String pkgname, String minversion)
    {
	if (StrUtil.isEmpty(query_command))
	    throw new RuntimeException("query_command not set");
	if (StrUtil.isEmpty(path))
	    throw new RuntimeException("path not set");

	String pkg_config = "export PKG_CONFIG_PATH=\""+path+"\" ; "+query_command;
	String suffix = " || echo PKG_CONFIG_QUERY_ERROR";

//	String cmd_cflags  = pkg_config+" --cflags "+pkgname+suffix;
//	String cmd_ldflags = pkg_config+" --libs "+pkgname+suffix;

	// we have to call pkgconfig
	String cflags  = new Exec().run_catch(pkg_config+" --cflags "+pkgname+suffix);
	String ldflags = new Exec().run_catch(pkg_config+" --libs "+pkgname+suffix);

	if ((cflags.indexOf("PKG_CONFIG_QUERY_ERROR")!=-1) ||
	    (ldflags.indexOf("PKG_CONFIG_QUERY_ERROR")!=-1))
	    throw new RuntimeException("Query error");
	
	PackageInfo pkg = new PackageInfo();
	if (!StrUtil.isEmpty(sysroot))
	    pkg.sysroot = sysroot;

	/* process cflags */
	{
	    String elems[] = CmdLineSplitter.split(cflags);
	    for (int x=0; x<elems.length; x++)
	    {
		if (elems[x].startsWith("-I"))
		    pkg.include_pathes.add(elems[x].substring(2));
		else
		    throw new RuntimeException("unhandled cflag: \""+elems[x]);
	    }
	}
	
	/* process ldflags */
	{
	    String elems[] = CmdLineSplitter.split(ldflags);
	    for (int x=0; x<elems.length; x++)
	    {
		if (elems[x].startsWith("-L"))
		    pkg.library_pathes.add(elems[x].substring(2));
		else if (elems[x].startsWith("-l"))
		    pkg.libraries.add(elems[x].substring(2));
		else 
		    throw new RuntimeException("unhandled ldflag: \""+elems[x]);
	    }
	}

	return pkg;
    }
}
