
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.LinkerParam;
import org.de.metux.util.FileStock;
import org.de.metux.util.Exec;
import org.de.metux.util.Environment;
import org.de.metux.util.rm;
import org.de.metux.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class LinkerBase
{
    public LinkerParam param;
    public String cmd_ar = null;
    public String cmd_ranlib = null;
    
    public LinkerBase(LinkerParam p)
    {
	param = p;
    }

    public String ar_command()
    {
	if (cmd_ar != null)
	    return cmd_ar;
	    
	// fetch ar/ranlib command
	cmd_ar = Environment.getenv("AR");

	if (StrUtil.isEmpty(cmd_ar))
	    throw new RuntimeException("missing env $AR");
	
	return cmd_ar;
    }

    public String ranlib_command()
    {
	if (cmd_ranlib != null)
	    return cmd_ranlib;
	    
	// fetch ar/ranlib command
	cmd_ranlib = Environment.getenv("RANLIB");

	if (StrUtil.isEmpty(cmd_ranlib))
	    throw new RuntimeException("missing env $RANLIB");
	
	return cmd_ranlib;
    }
    
    public void ar_x(String filename, String target)
    {
	if (filename==null)
	    throw new NullPointerException("filename == null");
	if (filename.length()==0)
	    throw new NullPointerException("filename == \"\"");
	if (target==null)
	    throw new NullPointerException("target == null");
	if (target.length()==0)
	    throw new NullPointerException("target == \"\"");

	// fixme: use an java class for that
	new Exec().run("rm -R "+target+" ; mkdir -p "+target);
	String cmdline = "cd "+target+" && "+ar_command()+" x "+filename;
	System.out.println("AR-X: executing: "+cmdline);
	new Exec().run(cmdline);
    }
    
    public void handle_static(String arname)
    {
	if (StrUtil.isEmpty(arname))
	    return;

	String subdir = ".DIR-"+arname.replace('/','+');

	// fixme: use our java classes for us
//	rm.remove_recursive(subdir);

	// extract the .a archive
	ar_x(new File(arname).getAbsolutePath(), subdir);

	/* now list all filenames in our working dir and 
	   add them to our object list */
	File subs[] = new File(subdir).listFiles();
	for (int x=0; x<subs.length; x++)
	{
	    String objfile = subs[x].getAbsolutePath();
	    if (objfile.endsWith(".o"))
	    {
//		System.out.println(".a file added: "+objfile);
		param.addObjectLink(objfile);
	    }
	    else
		throw new RuntimeException("ListFiles(): returned: "+objfile);
	}
    }

    public void handle_static_libs()
    {
	String[] libs = param.getStaticLinks();
	for (int x=0; x<libs.length; x++)
	    handle_static(libs[x]);
	param.clearStaticLink();
    }
    
    public void filter_objects()
    {
	FileStock stock = new FileStock();    
		
	String objs[] = param.getObjectLinks();
	int num_old = objs.length;
	
	param.clearObjectLink();
	try
	{
	    stock.add(objs);
	}
	catch (IOException e)
	{
	    throw new RuntimeException(e);
	}
	objs = stock.getFilenames();
//	System.out.println("filter_object(): compressed "+num_old+" to "+objs.length+" object files");
	param.addObjectLink(objs);
    }
}
