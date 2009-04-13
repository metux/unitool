
package org.de.metux.unitool.tools;

import org.de.metux.unitool.base.EParameterMissing;
import org.de.metux.unitool.base.EUnhandledCompilerFlag;
import org.de.metux.unitool.base.CCompilerParam;
import org.de.metux.util.Exec;
import org.de.metux.util.Environment;
import java.util.Enumeration;
import org.de.metux.propertylist.*;
import org.de.metux.util.*;

public class CCompiler
{
    public void run(CCompilerParam param)
	throws EParameterMissing, EUnhandledCompilerFlag
    {
	// FIXME: verbatims
	// FIXME: compiler-command
	String sysroot = param.getSysroot();
	Gcc_cmdline gcc;

	// process init-dirs
	FileOps.mkdir(param.getInitDirs());

	// fetch linker command
	String compiler_command = param.getCompilerCommand();
 	if (StrUtil.isEmpty(compiler_command))
	    compiler_command = Environment.getenv("CC");
 	if (StrUtil.isEmpty(compiler_command))
	    throw new EParameterMissing("compiler-command");
	
	// FIXME !!!
        gcc = new Gcc_cmdline(compiler_command+" -c ");

	gcc.warning_flag(param.getWarningFlags());
	gcc.compiler_flag(param.getCompilerFlags());
	gcc.verbatims(param.getVerbatims());
	gcc.mk_depend_output(param.getMkDependOutput());
	gcc.mk_depend_target(param.getMkDependTarget());
	gcc.output_filename(param.getOutputFile());
	gcc.addIncludePath(param.getIncludePathes_add_sysroot());
	gcc.addDefine(param.getDefines());
	gcc.srcs(param.getSourceFiles());
	gcc.addIncludeFile(param.getIncludeFiles());

	param.clearVerbatim();
	param.clearMkDependOutput();
	param.clearMkDependTarget();
	param.clearOutputFile();
	param.clearIncludePath();
	param.clearDefine();
	param.clearSourceFile();
	param.clearCompilerFlag();
	param.clearCompilerCommand();
	param.clearSysroot();
	param.clearInitDirs();
	param.clearWarningFlag();
	param.clearIncludeFile();
	
	for (Enumeration e=param.propertyNames(); e.hasMoreElements(); )
	{
	    String par = (String)e.nextElement();
	    String val = (String)param.get(par);
	    throw new RuntimeException("tools/CCompiler: unhandled param: "+par+"=\""+val+"\"");
	}

	String cmdline = gcc.toString();
	
	Exec exec = new Exec();
	System.out.println("===> CCompiler: "+cmdline);

	if (!exec.run(cmdline))
	    throw new RuntimeException("Exec failed: "+cmdline);
    }
}
