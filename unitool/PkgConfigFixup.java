
package org.de.metux.unitool.unitool;

import org.de.metux.unitool.base.PackageInfo;
import org.de.metux.unitool.base.EUnitoolError;
import org.de.metux.unitool.base.Command;
import org.de.metux.unitool.db.StorePkgConfig;
import org.de.metux.unitool.db.LoadPkgConfig;

import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StrSplit;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;

public class PkgConfigFixup extends Command
{
    public PkgConfigFixup(String args[])
    {
	super(args);
    }
    
    public String _fix_path(String path)
    {
	String list[] = StrSplit.split(path);
	for (int x=0; x<list.length; x++)
	{
	    if (list[x].startsWith("-I"))
		list[x] = "-I"+parameters.normalizer.strip_sysroot(list[x].substring(2));
	    else if (list[x].startsWith("-L"))
		list[x] = "-L"+parameters.normalizer.strip_sysroot(list[x].substring(2));
	}
	
	path = "";
	for (int x=0; x<list.length; x++)
	    path += " "+list[x];
    
	return path.trim();    
    }

    public boolean run()
	throws EUnitoolError, IOException, FileNotFoundException
    {
	String source = get_mandatory("source");
	String output = get_mandatory("output");
	String sr = parameters.getSysroot();

	if ((sr==null)||(sr.length()==0))
	    throw new RuntimeException("missing property system-root");
	
	parameters.normalizer.setSysroot(sr);
	
	PackageInfo inf = LoadPkgConfig.load_package(source,sr);

	// we have to fixup cflags and ldflags since we can get 
	// broken pathes here
	inf.cflags          = _fix_path(inf.cflags);
	inf.cflags_private  = _fix_path(inf.cflags_private);
	inf.ldflags         = _fix_path(inf.ldflags);
	inf.ldflags_private = _fix_path(inf.ldflags_private);
	
	for (Enumeration e = inf.properties.keys(); e.hasMoreElements(); )
	{
	    String par = (String)e.nextElement();
	    inf.properties.setProperty(par, _fix_path(inf.properties.getProperty(par)));
	}
	
	return StorePkgConfig.store_file(inf,output);
    }
}
