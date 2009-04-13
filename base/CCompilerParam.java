
package org.de.metux.unitool.base;

import org.de.metux.util.*;
import org.de.metux.propertylist.IPropertylist;

public class CCompilerParam extends ToolParam
{
    public static final String cf_make_depend_target = "make-depend-target";
    public static final String cf_make_depend_output = "make-depend-output";
    public static final String cf_compiler_define    = "compiler-define";
    public static final String cf_source             = "source";
    public static final String cf_output_file        = "output-file";
    public static final String cf_include_path       = "include-path";
    public static final String cf_include_file       = "include-file";
    public static final String cf_compiler_flag      = "compiler-flag";
    public static final String cf_warning_flag       = "warning-flag";
    public static final String flag_pic              = "pic";
    public static final String flag_nopic            = "nopic";
    public static final String cf_compiler_command   = "tools/compiler/c-binobj/compiler-command";

    public static final String flag_mkdepend_dummytargets = "mkdepend-dummy-targets";
    public static final String flag_mkdepend_default      = "mkdepend-default";

    public static final String flag_warn_pointer_arith               = "warn-pointer-arith";
    public static final String flag_warn_strict_prototypes           = "warn-strict-prototypes";
    public static final String flag_warn_missing_prototypes          = "warn-missing-prototypes";
    public static final String flag_warn_missing_declarations        = "warn-missing-declarations";
    public static final String flag_warn_declaration_after_statement = "warn-declaration-after-statement";
    public static final String flag_warn_cast_align                  = "warn-cast-align";
    public static final String flag_warn_nested_externs              = "warn-nested-externs";
    public static final String flag_warn_write_strings               = "warn-write-strings";
    public static final String flag_warn_no_cast_qual                = "warn-no-cast-qual";
    public static final String flag_warn_all                         = "warn-all";
    public static final String flag_warn_unused                      = "warn-unused";
    public static final String flag_warn_error                       = "warn-error";
    public static final String flag_warn_no_unused                   = "warn-no-unused";
    public static final String flag_warn_no_format                   = "warn-no-format";
    public static final String flag_warn_inline                      = "warn-inline";

    public CCompilerParam(ToolConfig cf)
    {
	super(cf);
    }

    public CCompilerParam(ToolConfig cf, IPropertylist pr)
    {
	super(cf,pr);
    }

    public void clearMkDependTarget()
    {
	remove(cf_make_depend_target);
    }

    public void clearCompilerFlag()
    {
	remove(cf_compiler_flag);
    }

    public void addWarningFlag(String flag)
    {
	add(cf_warning_flag,flag);
    }

    public void addCompilerFlag(String flag)
    {
	add(cf_compiler_flag,flag);    
    }

    public void addCompilerFlag(String[] flag)
    {
	add(cf_compiler_flag,flag);
    }
    
    public String[] getCompilerFlags()
    {
	return get_str_list(cf_compiler_flag);	
    }

    public String[] getWarningFlags()
    {
	return get_str_list(cf_warning_flag);
    }

    public void setPIC(boolean f)
    {
	if (f)
	    addCompilerFlag(flag_pic);
	else
	    addCompilerFlag(flag_nopic);
    }
	
    public void setMkDependTarget(String target)
    {
	set(cf_make_depend_target, target);
    }

    public String getMkDependTarget()
    {
	return get_str_def(cf_make_depend_target, null);
    }

    public String getMkDependOutput()
    {
	return get_str_def(cf_make_depend_output, null);
    }

    public void clearMkDependOutput()
    {	
	remove(cf_make_depend_output);
    }
    
    public void setMkDependOutput(String fn)
    {
	set(cf_make_depend_output, fn);
    }

    public String[] getDefines()
    {
	return get_str_list(cf_compiler_define);
    }

    public void addDefine(String def)
    {
	add(cf_compiler_define, def);
    }
    
    public void clearDefine()
    {
	remove(cf_compiler_define);
    }
    
    public void addDefine(String def[])
    {
	add(cf_compiler_define,def);
    }
    
    public String[] getSourceFiles()
    {
	return get_str_list(cf_source);
    }
    
    public void addSourceFile(String fn)
    {
	add(cf_source,fn);
    }

    public void clearSourceFile()
    {
	remove(cf_source);
    }
    
    public void addSourceFile(String fn[])
    {
	add(cf_source,fn);
    }

    public void setCompilerCommand(String fn)
    {
	System.err.println("WARN: setCompilerCommand() is obsolete");
	set(cf_compiler_command,fn);
    }
    
    public String getCompilerCommand()
    {
	return getConfigStr(cf_compiler_command);
    }
    
    public void clearCompilerCommand()
    {
	remove(cf_compiler_command);
    }
    
    public void setOutputFile(String fn)
    {
	set(cf_output_file,fn);
    }
    
    public String getOutputFile()
    {
	return get(cf_output_file);
    }
    
    public void clearOutputFile()
    {
	remove(cf_output_file);
    }

    public void addIncludeFile(String str)
    {
	add(cf_include_file,str);
    }
    
    public void addIncludeFile(String str[])
    {
	add(cf_include_file,str);
    }
    
    public void addIncludePath(String str)
    {
	add(cf_include_path,str);
    }
    
    public void addIncludePath(String str[])
    {
	add(cf_include_path,str);
    }

    public void addIncludePath_enc_sysroot(String str)
    {
	addIncludePath(normalizer.enc_sysroot(str));
    }
    
    public void addIncludePath_enc_sysroot(String str[])
    {
	if (str!=null)
	    for (int x=0; x<str.length; x++)
		addIncludePath(str[x]);
    }

    public void addIncludePath(UniqueNameList l)
    {
	if (l!=null)
	    addIncludePath(l.getNames());
    }

    public void addPackageImport(PackageInfo pkg)
    {
	if (pkg==null)
	    return;
	
	System.err.println(" include_pathes ==> "+pkg.include_pathes.toString());
	System.err.println(" include_pathes_private ==> "+pkg.include_pathes_private.toString());
	System.err.println(" cflags => "+pkg.cflags);
	System.err.println(" cflags_private => "+pkg.cflags_private);
	
	addIncludePath(pkg.include_pathes);
	addIncludePath(pkg.include_pathes_private);
    }

    public void addPackageImport(PackageInfo pkg[])
    {
	if (pkg!=null)
	    for (int x=0; x<pkg.length; x++)
		addPackageImport(pkg[x]);
    }
        
    public String[] getIncludePathes()
    {
	return UniqueValues.unique(get_str_list(cf_include_path));
    }

    public String[] getIncludeFiles()
    {
	return UniqueValues.unique(get_str_list(cf_include_file));
    }
    
    public void clearWarningFlag()
    {
	System.err.println("clearing warning-flag");
	remove(cf_warning_flag);
    }

    public void clearIncludePath()
    {
	remove(cf_include_path);
    }

    public void clearIncludeFile()
    {
	remove(cf_include_file);
    }

    public String[] getIncludePathes_add_sysroot()
    {
	String[] pathes = getIncludePathes();
	for (int x=0; x<pathes.length; x++)
	    pathes[x] = normalizer.dec_sysroot(pathes[x]);
	return pathes;
    }
}
