 
package org.de.metux.unitool.libtool;

import java.util.Enumeration;

import org.de.metux.util.Environment;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.HTMLEntities;
import org.de.metux.util.StrUtil;
import org.de.metux.util.UniqueNameList;

import org.de.metux.propertylist.IPropertylist;

import org.de.metux.unitool.base.CCompilerParam;
import org.de.metux.unitool.base.EUnhandledCompilerFlag;
import org.de.metux.unitool.base.EParameterMissing;
import org.de.metux.unitool.base.ObjectInfo;
import org.de.metux.unitool.base.ToolConfig;

import org.de.metux.unitool.db.LoadLibtoolArchive;
import org.de.metux.unitool.db.StoreLibtoolObject;
import org.de.metux.unitool.db.UnitoolConf;

import org.de.metux.unitool.tools.CCompiler;

public class CmdCompile
{
    String[] my_args;
    ToolConfig config;
        
    CmdCompile(String[] argv, ToolConfig cf) throws Exception
    {
	my_args = argv;
	config = cf;
    }

    boolean enable_debug = Environment.getenv_bool("LT_UNITOOL_DEBUG",false);

    private void notice(String s)
    {
	System.err.println(s);
    }
        
    private void debug(String s)
    {
	if (enable_debug)
	    System.out.println(s);
    }


    private boolean is_source(String fn)
    {
	return (fn.endsWith(".c") 	||
		fn.endsWith(".cc")   	||
	        fn.endsWith(".asm")  	||
		fn.endsWith(".S")    	||
		fn.endsWith(".nasm") 	||
		fn.endsWith(".cpp"));
    }
    
    public void run()
	throws EParameterMissing, EUnhandledCompilerFlag
    {
	String compiler_command = "";
	UniqueNameList defines        = new UniqueNameList();
	UniqueNameList sources        = new UniqueNameList();
	UniqueNameList include_pathes = new UniqueNameList();
	UniqueNameList verbatims      = new UniqueNameList();
	UniqueNameList compiler_flags = new UniqueNameList();
	String mkdepend_output = "";	
	String mkdepend_target = "";

	String output_filename = null;
	boolean cmd_is_shell = false;
	boolean cmd_is_nasm  = false;
	String sysroot;
	
	if ((sysroot=Environment.getenv("SYSROOT"))==null)
	    sysroot = "";

	String srcdir = Environment.getenv("SRCDIR");

	if (my_args.length<2)
	    throw new RuntimeException("missing compiler command");

	CCompilerParam pic   = new CCompilerParam(config);
	CCompilerParam nopic = new CCompilerParam(config);

	debug("PAR=\""+my_args[0]+"\" \""+my_args[1]+"\" \""+my_args[2]+"\"");
	
	cmd_is_shell = my_args[1].equals("sh");

	// compiler command is $1, but sometimes we've got an additional -c
	// yeah, libtool is *very* strange
	if (my_args[2].equals("-c"))
	    compiler_command = (my_args[1]+" -c ");
	else
	    compiler_command = (my_args[1]);

	for (int x=2; x<my_args.length; x++)
	{
	    String p = my_args[x];
	    debug("PAR["+x+"]=\""+p+"\"");
	    // FIXME !!!
	    if (p.equals("-Wpointer-arith"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_pointer_arith);
		nopic.addWarningFlag(CCompilerParam.flag_warn_pointer_arith);
	    }
	    else if (p.equals("-Wstrict-prototypes"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_strict_prototypes);
		nopic.addWarningFlag(CCompilerParam.flag_warn_strict_prototypes);
	    }
	    else if (p.equals("-Wmissing-prototypes"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_missing_prototypes);
		nopic.addWarningFlag(CCompilerParam.flag_warn_missing_prototypes);
	    }
	    else if (p.equals("-Wmissing-declarations"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_missing_declarations);
		nopic.addWarningFlag(CCompilerParam.flag_warn_missing_declarations);
	    }
	    else if (p.equals("-Wdeclaration-after-statement"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_declaration_after_statement);
		nopic.addWarningFlag(CCompilerParam.flag_warn_declaration_after_statement);
	    }
	    else if (p.equals("-Wno-cast-qual"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_no_cast_qual);
		nopic.addWarningFlag(CCompilerParam.flag_warn_no_cast_qual);
	    }
	    else if (p.equals("-Wcast-align"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_cast_align);
		nopic.addWarningFlag(CCompilerParam.flag_warn_cast_align);
	    }
	    else if (p.equals("-Wwrite-strings"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_write_strings);
		nopic.addWarningFlag(CCompilerParam.flag_warn_write_strings);
	    }
	    else if (p.equals("-Wnested-externs"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_nested_externs);
		nopic.addWarningFlag(CCompilerParam.flag_warn_nested_externs);
	    }
	    else if (p.equals("-Wall"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_all);
		nopic.addWarningFlag(CCompilerParam.flag_warn_all);
	    }
	    else if (p.equals("-Wunused"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_unused);
		nopic.addWarningFlag(CCompilerParam.flag_warn_unused);
	    }
	    else if (p.equals("-Werror"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_error);
		nopic.addWarningFlag(CCompilerParam.flag_warn_error);
	    }
	    else if (p.equals("-Wno-unused"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_no_unused);
		nopic.addWarningFlag(CCompilerParam.flag_warn_no_unused);
	    }
	    else if (p.equals("-Wno-format"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_no_format);
		nopic.addWarningFlag(CCompilerParam.flag_warn_no_format);
	    }
	    else if (p.equals("-Winline"))
	    {
		pic.addWarningFlag(CCompilerParam.flag_warn_inline);
		nopic.addWarningFlag(CCompilerParam.flag_warn_inline);
	    }
	    else if (p.equals("-R"))
		debug("Compiler ignoring runtime path: "+my_args[++x]);
	    else if (p.equals("-MP"))
		compiler_flags.add(CCompilerParam.flag_mkdepend_dummytargets);
	    else if (p.equals("-MD"))
		compiler_flags.add(CCompilerParam.flag_mkdepend_default);
	    else if (p.equals("-g")			||
		p.equals("-O")				||
		p.equals("-fforce-mem")			||
		p.equals("-fforce-addr")		||
		p.equals("-fthread-jumps")		||
		p.equals("-fcse-follow-jumps")		||
		p.equals("-fcse-skip-blocks")		||
		p.equals("-fregmove")			||
		p.equals("-fschedule-insns2")		||
		p.equals("-fstrength-reduce")		||
		p.equals("-fomit-frame-pointer")	||
		p.equals("-funroll-loops")		||
		p.equals("-finline-functions")		||
		p.equals("-O1")				||
		p.equals("-O20")			||
		p.equals("-O2")				||
		p.equals("-O3")				||
	        p.equals("-MD")				||
		p.equals("-fno-strict-aliasing")	||
		p.equals("-fexpensive-optimizations")	||
		p.equals("-fexceptions")		||
		p.equals("-fPIC")                       ||
		p.equals("-ffast-math")			||
		p.equals("-pthread")			||
		p.equals("-fsigned-char")		||
		p.equals("-U_OSF_SOURCE")		||
		p.equals("-mmmx")			||
		p.equals("-msse")			||
		p.equals("-W")				||
		p.equals("-pedantic")			||
		p.equals("-Wformat")			||
		p.equals("-Wimplicit")			||
		p.equals("-Wreturn-type")		||
		p.equals("-Wswitch")			||
		p.equals("-Wcomment")			||
		p.equals("-Wtrigraphs")			||
		p.equals("-Wchar-subscripts")		||
		p.equals("-Wuninitialized")		||
		p.equals("-Wparentheses")		||
		p.equals("-Wshadow")			||
		p.equals("-Waggregate-return")		||
		p.equals("-Wredundant-decls")		||
		p.equals("-Wsign-compare")		||
		p.equals("-Werror-implicit-function-declaration")	||
		p.equals("-Wpacked")					||
		p.equals("-Wswitch-enum")				||
		p.equals("-Wmissing-format-attribute")			||
		p.equals("-Wstrict-aliasing=2")				||
		p.equals("-Winit-self")					||
		p.equals("-Wold-style-definition")			||
		p.startsWith("-march=")			||
		p.startsWith("-mcpu=")			||
		p.startsWith("-std=")
	    )
	    {
		notice("WARN: adding verbatim parameter \""+p+"\" - FIXME!");
		verbatims.add(p);
	    }
	    else if (p.equals("-c"))
		; // silently ignore
	    else if (p.equals("-MF"))
		mkdepend_output = my_args[++x];
	    else if (p.equals("-MT"))
		mkdepend_target = my_args[++x];
	    else if (p.equals("-o"))
	    {
		if (output_filename!=null)
		    throw new RuntimeException("duplicate -o option passed");
		output_filename = my_args[++x];
	    }
	    else if (p.startsWith("-export-dynamic"))
		; /* silently ignore */
	    else if (p.startsWith("-D"))
		defines.add(HTMLEntities.encode(p.substring(2)));
	    else if (p.equals("-c"))
		throw new RuntimeException("got another -c !");
	    else if (p.startsWith("-I"))
	    {
		include_pathes.add(p.substring(2));
	    }
	    else if (is_source(p))
		sources.add(p);
	    else if (p.equals("-include"))
	    {
		String fn = my_args[++x];
		pic.addIncludeFile(fn);
		nopic.addIncludeFile(fn);
	    }
	    else if (p.equals("--param"))
		verbatims.add("--param \""+my_args[++x]+"\"");
	    else if (p.equals("-shared"))
		;	/* dummy - required */
	    else
	    {
		if (cmd_is_shell)
		{
		    if (p.equals("nasm"))
			cmd_is_nasm = true;
		    verbatims.add(p);
		}
		else
	    	    throw new RuntimeException("UNHANDLED PARAM: ["+x+"] \""+p+"\"");
	    }
	}

	{
    	    String src[] = sources.getNames();
	    if (src.length==0)
	    {
		notice("missing input files ... trying to guess from output file");
		if (output_filename==null)
	    	    throw new RuntimeException("missing input files, no output file to guess from");

		String n;
		if (output_filename.endsWith(".o"))
		    n = output_filename.substring(0,output_filename.length()-2);
		else if (output_filename.endsWith(".lo"))
		    n = output_filename.substring(0,output_filename.length()-3);
		else
		    throw new RuntimeException("missing input files, guessing from output failed "+output_filename);

		throw new RuntimeException("missing input files, guessing from output failed "+output_filename);
	    }
	    if (src.length>1)
		throw new RuntimeException("more than one input given");

	    // if no source file has been passed, guess it from output filename
	    // we currently do not even support explicit source files
	    // ... lets see whether we need them at all ...
	    if (output_filename==null)
		output_filename=PathNormalizer.replace_suffix(src[0],"o");
		
	    debug("output_filename=\""+output_filename);
	}

	ObjectInfo objinf = new ObjectInfo();
	String dirname  = PathNormalizer.dirname(output_filename);
	String basename = PathNormalizer.basename(output_filename);

	// process output filename
	if (output_filename.endsWith(".o"))
	{
	    objinf.lo_file = output_filename.substring(0,output_filename.length()-2)+".lo";
	    objinf.object_pic    = (StrUtil.isEmpty(dirname) ? "" : "/")+LoadLibtoolArchive.tmp_libdir+"/"+basename;
	    objinf.object_nonpic = output_filename;
	}
	else if (output_filename.endsWith(".lo"))
	{
	    dirname = PathNormalizer.dirname_slash(output_filename);
	    String objname  = basename.substring(0,basename.length()-3)+".o";
	    objinf.lo_file       = output_filename;
	    objinf.object_pic    = LoadLibtoolArchive.tmp_libdir+objname;
	    objinf.object_nonpic = objname;
	}
	else
	    throw new RuntimeException("compiler output file should end with .o: \""+output_filename+"\"");

	pic.setSysroot(sysroot);
	pic.normalizer.addSkip(srcdir);
	pic.setCompilerCommand(compiler_command);
	pic.addDefine(defines.getNames());
	pic.addIncludePath_enc_sysroot(include_pathes.getNames());
	pic.addInitDirs(LoadLibtoolArchive.tmp_libdir);
	pic.setOutputFile(dirname+objinf.object_pic);
	pic.addVerbatim(verbatims.getNames());
	pic.addSourceFile(sources.getNames());
	pic.addCompilerFlag(compiler_flags.getNames());
	pic.setMkDependOutput(mkdepend_output);
	pic.setMkDependTarget(mkdepend_target);
	pic.setPIC(true);

	nopic.normalizer.addSkip(srcdir);
	nopic.setSysroot(sysroot);
	nopic.setCompilerCommand(compiler_command);
	nopic.addDefine(defines.getNames());
	nopic.addIncludePath_enc_sysroot(include_pathes.getNames());
	nopic.addInitDirs(LoadLibtoolArchive.tmp_libdir);
	nopic.setOutputFile(dirname+objinf.object_nonpic);
	nopic.addVerbatim(verbatims.getNames());
	nopic.addSourceFile(sources.getNames());
	nopic.setMkDependOutput(mkdepend_output);
	nopic.setMkDependTarget(mkdepend_target);
	nopic.addCompilerFlag(compiler_flags.getNames());
	
	new CCompiler().run(pic);
	new CCompiler().run(nopic);

	// now write the .lo file
	StoreLibtoolObject.store(objinf);
    }
}
