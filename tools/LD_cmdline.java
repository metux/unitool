
package org.de.metux.unitool.tools;

import org.de.metux.util.HTMLEntities;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.FileOps;
import org.de.metux.util.StrUtil;

public class LD_cmdline
{
    String cmdline = "";
    String addpar = "";
    String soname = null;
    String output = null;
    boolean prohibit_undefined = false;
    boolean strip_debug = false;
    
    PathNormalizer normalizer;

    public LD_cmdline(String ld_command, PathNormalizer n)
    {
	cmdline = ld_command;
	normalizer = n;
    }
    
    public String toString()
    {
	if (output==null)
	    throw new RuntimeException("LD_cmdline() missing output file");
	    
	// FIXME !!! we better should split off into gcc_cmdline and ld_cmdline
	return cmdline+
	    " -shared -o "+
	    output+" "+
	    (StrUtil.isEmpty(soname)?"":" -Wl,-soname -Wl,"+soname+" ")+
	    (prohibit_undefined ? " -Wl,-no-undefined ":"")+
	    (strip_debug        ? " -S ":"")+
	    addpar;
    }

    public void dlname(String n)
    {
	soname = n;
    }

    public void libpath(String p)
    {
	if (!StrUtil.isEmpty(p))
	    addpar += " -L"+normalizer.dec_sysroot(p);
    }

    public void libpath(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		libpath(par[x]);
    }

    public void output_filename(String fn)
    {
	if (StrUtil.isEmpty(fn))
	    throw new RuntimeException("output_filename() fn is empty!");

	FileOps.mkdir(PathNormalizer.dirname(fn));
	output = fn;
    }
    
    public void dynlink(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		    addpar += " -l"+par[x];
    }
    
    public void objlink(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		    addpar += " "+par[x];
    }

    public void linker_flag(String flag)
    {
	if (StrUtil.isEmpty(flag))
	    return;

	if (flag.equals("no-undefined"))
	    prohibit_undefined = true;
	else if (flag.equals("strip-debug"))
	    strip_debug = true;
	else 
	    throw new RuntimeException("unhandled flag: "+flag);
    }

    public void linker_flag(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		linker_flag(par[x]);
    }

    // FIXME: obsolete ?
    public void staticlink(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		staticlink(par[x]);
    }

    public void staticlink(String filename)
    {
	if (!StrUtil.isEmpty(filename))
	    addpar += " -Wl,--whole-archive -Wl,"+filename;
    }

    /* add an --rpath option */
    public void rpath(String rpath)
    {
	if (!StrUtil.isEmpty(rpath))
	    addpar += " -Wl,--rpath -Wl,"+rpath;
    }
    
    public void rpath(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		rpath(par[x]);
    }

    public void exportSymbols(String re[])
    {
	if (re!=null)
	    for (int x=0; x<re.length; x++)
		if ((re[x]!=null) && (re[x].length()!=0))
		    System.err.println("WARN: -export-symbols not implemented yet");
//		    addpar += " -Wl,-export-symbols -Wl,\""+re[x]+"\"";
    }
    
    public void exportSymbolsRegex(String re[])
    {
	if (re!=null)
	    for (int x=0; x<re.length; x++)
		if ((re[x]!=null) && (re[x].length()!=0))
		    System.err.println("-export-symbols-regex not implemented yet");
//		    addpar += " -Wl,--export-symbols-regex -Wl,\""+re[x]+"\"";
    }

    public void verbatims(String[] par)
    {
	if (!StrUtil.isEmpty(par))	
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		{
		    System.err.println("WARNING: LD: adding verbatim parameter \""+par[x]+"\" -- will be removed soon!");
		    cmdline += " \""+par[x]+"\"";
//		    throw new RuntimeException(" !!! VERBATIM PARAMETERS NO LONGER SUPPORTED !!!: "+par[x]);
		}
    }
}
