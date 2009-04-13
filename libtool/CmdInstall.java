 
package org.de.metux.unitool.libtool;

import java.io.File;

import org.de.metux.util.Environment;
import org.de.metux.util.StrReplace;
import org.de.metux.util.FileOps;
import org.de.metux.util.PathNormalizer;

import org.de.metux.propertylist.IPropertylist;

import org.de.metux.unitool.base.ToolConfig;
import org.de.metux.unitool.base.ToolParam;
import org.de.metux.unitool.base.InstallerParam;
import org.de.metux.unitool.base.EParameterMissing;
import org.de.metux.unitool.base.EParameterInvalid;
import org.de.metux.unitool.db.LoadLibtoolArchive;
import org.de.metux.unitool.tools.LTLibraryInstaller;

public class CmdInstall
{
    InstallerParam param;
    String args[];
    ToolConfig config;

    CmdInstall(String[] argv, ToolConfig cf) throws Exception
    {
	config = cf;
	args = argv;
    }	    

    public String[] stripfirst(String par[])
    {
	String n[] = new String[par.length];
	for (int x=0; x<(par.length-1); x++)
	    n[x] = par[x+1];
	return n;
    }

    public void run_c()
	throws EParameterMissing, EParameterInvalid
    {
	if (args[0].equals("-s"))
	{
	    param.setInstallStrip();
	    args = stripfirst(args);
	    run_c();
	    return;
	}
	
	if (args[0].equals("-m"))
	{
	    System.err.println("Ignoring Mode: "+args[1]);
	    args = stripfirst(stripfirst(args));
	    run_c();
	    return;
	}
	
	if (args[0].equals("-c"))
	{
	    System.err.println("[LT-Install] Ignoring -c");
	    args = stripfirst(args);
	    run_c();
	    return;
	}

	if (args[0].endsWith(".la") && args[1].endsWith(".la"))
	{
	    System.out.println("LTLibraryInstaller Param: "+param);
	    param.setInstallSource(args[0]);
	    param.setInstallTarget(args[1]);
	    new LTLibraryInstaller().run(param);
	}
	else if (args[0].endsWith(".la"))
	{
	    System.out.println("Assuming we wanna install .la to directory "+args[1]);
	    param.setInstallSource(args[0]);
	    param.setInstallTarget(args[1]+"/"+PathNormalizer.basename(args[0]));
	    new LTLibraryInstaller().run(param);
	}
	else 
	{
	    String src = args[0];
	    String dst = args[1];
	    String src_dirname  = PathNormalizer.dirname(src);
	    String src_basename = PathNormalizer.basename(src);
	    System.out.println("Assuming we wanna install an binary: "+src);

	    String real_src = 
		    ((src_dirname.length()!=0) ? (src_dirname+"/") : "")+
		    LoadLibtoolArchive.tmp_libdir+
		    src_basename;

	    System.out.println("Try_copy: real_src="+real_src);
	    System.out.println("               dst="+dst);
		
	    if (new File(real_src).exists())
	    {
		    System.out.println("OKAY; file exists. we can copy it.\n");
		    FileOps.cp(real_src,dst);
	    }
	    else
	        throw new RuntimeException("Source file does not exist. Either build incomplete or parameter scheme not understood");
        }
    }

    public void run()
	throws EParameterMissing, EParameterInvalid
    {
	if (args.length<2)
	    throw new RuntimeException("missing installer command set");

	if (args[1].equals("--finish"))
	{
	    System.out.println("Installer: finishing -> nothing to do");
	    return;
	}
	
	param = new InstallerParam(config);
	
	param.setSysroot(Environment.getenv("SYSROOT"));
	param.setInstallerCommand(args[1]);
	param.normalizer.addSkip(Environment.getenv("SRCDIR"));

	System.out.println("Installer command: "+args[1]);

	args = stripfirst(stripfirst(args));
	
	if (args[0].endsWith("/install-sh"))
	    args = stripfirst(args);
	
	if (args[0].equals("-c"))
	{
	    args = stripfirst(args);
	    run_c();	    
	}
	else
	    throw new RuntimeException("this kind of parameter not (yet) understood: \""+
		args[2]+"\" \""+args[3]+"\" \""+args[4]+"\"" );

	// FIXME !!!
    }
}
