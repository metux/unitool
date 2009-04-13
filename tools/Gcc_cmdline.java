
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.EUnhandledCompilerFlag;
import org.de.metux.util.HTMLEntities;
import org.de.metux.util.ShellEscape;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.FileOps;
import org.de.metux.util.StrUtil;
import org.de.metux.unitool.base.CCompilerParam;

public class Gcc_cmdline
{
    String cmdline = "";
    PathNormalizer normalizer;
    String warning_flags = "";

    boolean create_pic    = false;
    boolean create_shared = false;
        
    public Gcc_cmdline(String s)
    {
	cmdline = s;
    }

    public void setNormalizer(PathNormalizer n)
    {
	normalizer = n;
    }

    public String toString()
    {
	return cmdline+
	    warning_flags+
	    (create_pic    ? " -fPIC -DPIC " : "")+
	    (create_shared ? " -shared " : "");
    }

    public void libpath(String p)
    {
	if (!StrUtil.isEmpty(p))
	    cmdline += " -L"+normalizer.dec_sysroot(p);
    }

    public void libpath(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		libpath(par[x]);
    }

    public void addDefine(String par)
    {
	if (!StrUtil.isEmpty(par))
	{
	    par = HTMLEntities.decode(par);
	    cmdline += " "+ShellEscape.quoting("-D"+
// FIXME !!! just an ugly test to do this twice
		HTMLEntities.decode(HTMLEntities.decode(par)))+" ";
	}

    }

    public void addDefine(String[] par)
    {
	if (!StrUtil.isEmpty(par))	
	    for (int x=0; x<par.length; x++)
		addDefine(par[x]);
    }

    public void addIncludeFile(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		addIncludeFile(par[x]);
    }

    public void addIncludeFile(String par)
    {
	if (!StrUtil.isEmpty(par))
	    cmdline += " --include \""+par+"\"";
    }

    public void addIncludePath(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		addIncludePath(par[x]);
    }

    public void addIncludePath(String par)
    {
	if (!StrUtil.isEmpty(par))
	    cmdline += " -I"+par;
    }

    public void output_filename(String fn)
    {
	if (!StrUtil.isEmpty(fn))
	{
	    FileOps.mkdir(PathNormalizer.dirname(fn));
	    cmdline += " -o "+fn;
	}
    }
    
    public void dynlink(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		    cmdline += " -l"+par[x];
    }
    
    public void objlink(String[] par)
    {
	if (!StrUtil.isEmpty(par))	
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		    cmdline += " "+par[x];
    }

    public void linker_flag(String flag)
    {
	if (flag.equals("no-undefined"))
	    cmdline += " -no-undefined";
	else 
	    throw new RuntimeException("unhandled flag: "+flag);
    }

    public void warning_flag(String flag[])
    {
	if (flag!=null)
	    for (int x=0; x<flag.length; x++)
		warning_flag(flag[x]);
    }
    
    public void warning_flag(String flag)
    {
	if ((flag==null)||(flag.length()==0))
	    return;
	    
	if (flag.equals(CCompilerParam.flag_warn_pointer_arith))
	    warning_flags += " -Wpointer-arith";
	else if (flag.equals(CCompilerParam.flag_warn_strict_prototypes))
	    warning_flags += " -Wstrict-prototypes";
	else if (flag.equals(CCompilerParam.flag_warn_missing_prototypes))
	    warning_flags += " -Wmissing-prototypes";
	else if (flag.equals(CCompilerParam.flag_warn_missing_declarations))
	    warning_flags += " -Wmissing-declarations";
	else if (flag.equals(CCompilerParam.flag_warn_nested_externs))
	    warning_flags += " -Wnested-externs";
	else if (flag.equals(CCompilerParam.flag_warn_all))
	    warning_flags += " -Wall";
	else if (flag.equals(CCompilerParam.flag_warn_unused))
	    warning_flags += " -Wunused";
	else if (flag.equals(CCompilerParam.flag_warn_no_unused))
	    warning_flags += " -Wno-unused";
	else if (flag.equals(CCompilerParam.flag_warn_no_format))
	    warning_flags += " -Wno-format";
	else if (flag.equals(CCompilerParam.flag_warn_error))
	    warning_flags += " -Werror";
	else if (flag.equals(CCompilerParam.flag_warn_inline))
	    warning_flags += " -Winline";
	else if (flag.equals(CCompilerParam.flag_warn_cast_align))
	    warning_flags += " -Wcast-align";
	else if (flag.equals(CCompilerParam.flag_warn_write_strings))
	    warning_flags += " -Wwrite-strings";
	else if (flag.equals(CCompilerParam.flag_warn_declaration_after_statement))
	    warning_flags += " -Wdeclaration-after-statement";
	else
	    throw new RuntimeException("unsupported warning flag: "+flag);
    }	    

    public void compiler_flag(String flag)
	throws EUnhandledCompilerFlag
    {
	if (flag.equals(""))
	    ;
	else if (flag.equals("pic"))
	    create_pic = true;
	else if (flag.equals(CCompilerParam.flag_mkdepend_dummytargets))
	    cmdline += " -MP";
	else if (flag.equals(CCompilerParam.flag_mkdepend_default))
	    cmdline += " -MD";
	else 
	    throw new EUnhandledCompilerFlag(flag);
    }

    public void linker_flag(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		linker_flag(par[x]);
    }

    public void compiler_flag(String[] par)
	throws EUnhandledCompilerFlag
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		compiler_flag(par[x]);
    }
    
    public void staticlink(String[] par)
    {
	if (par!=null)
	    for (int x=0; x<par.length; x++)
		staticlink(par[x]);
    }

    public void staticlink(String filename)
    {
	if (!StrUtil.isEmpty(filename))
	    cmdline += " -Wl,--whole-archive "+filename;
    }

    /* add an --rpath option */
    public void rpath(String rpath)
    {
	if (!StrUtil.isEmpty(rpath))
	    cmdline += " -Wl,--rpath -Wl,"+rpath;
    }
    
    public void rpath(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		rpath(par[x]);
    }

    public void verbatims(String[] par)
    {
	if (!StrUtil.isEmpty(par))	
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		{
		    System.err.println("GCC-backend: adding verbatim: "+par[x]);
		    cmdline += " \""+par[x]+"\"";
		}
    }
    
    public void srcs(String[] par)
    {
	if (!StrUtil.isEmpty(par))
	    for (int x=0; x<par.length; x++)
		if (par[x].length()>0)
		    cmdline += " "+par[x];
    }
    
    public void mk_depend_output(String par)
    {
	if (!StrUtil.isEmpty(par))
	    cmdline += " -MF "+par;
    }

    public void mk_depend_target(String par)
    {
	if (!StrUtil.isEmpty(par))
	    cmdline += " -MT "+par;
    }
    
    public void shared(boolean sh)
    {
	create_shared = sh;
    }

    public void setPIC(boolean flag)
    {
	create_pic = flag;
    }

    public void exportSymbols(String re[])
    {
	if (re!=null)
	    for (int x=0; x<re.length; x++)
		if ((re[x]!=null) && (re[x].length()!=0))
		    System.err.println("WARN: -export-symbols not implemented yet");
//		    cmdline += " --export-symbols \""+re[x]+"\"";
    }

    public void exportSymbolsRegex(String re[])
    {
	if (re!=null)
	    for (int x=0; x<re.length; x++)
		if ((re[x]!=null) && (re[x].length()!=0))
		    System.err.println("WARN: -export-symbols-regex not implemented yet");
//		    cmdline += " --export-symbols-regex \""+re[x]+"\"";
    }
}
