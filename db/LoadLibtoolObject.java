
package org.de.metux.unitool.db;

import org.de.metux.util.ShellVariableDef;
import org.de.metux.util.StrUtil;
import org.de.metux.util.PathNormalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.de.metux.unitool.base.ObjectInfo;

//
// if we have an relative filename (within another directory),
// we assume this as a prefix.
//

public class LoadLibtoolObject
{
    static private String _fix_fn(String dirname, String filename)
    {
	if (dirname==null)
	    return filename;
	    
	if (filename.startsWith("/"))
	    return filename;
	
	if ((dirname==null)||(dirname.length()==0))
	    return filename;

	return dirname+"/"+filename;
    }

    static public ObjectInfo load_lo(String fn)
	throws FileNotFoundException, IOException
    {
	BufferedReader in = new BufferedReader(new FileReader(fn));
	
	String dirname = PathNormalizer.dirname(fn);
	if (!StrUtil.isEmpty(dirname))
	    dirname += "/";
	
	String line;
	ObjectInfo inf = new ObjectInfo();
	inf.lo_file = fn;
	
	while ((line=in.readLine())!=null)
	{
	    ShellVariableDef shvar;
	    
	    try
	    {
		shvar = new ShellVariableDef(line);
	    }
	    catch (ShellVariableDef.XEmpty e) 
	    {
		continue;
	    }
	    catch (ShellVariableDef.XParseFailed e)
	    {
		throw new RuntimeException("parse error in "+fn+": "+e);
	    }
	    
	    if (shvar.value.length()==0)
		continue;

	    if (shvar.name.equals("pic_object"))
		inf.object_pic = _fix_fn(dirname,shvar.value);
	    else if (shvar.name.equals("non_pic_object"))
		inf.object_nonpic = _fix_fn(dirname,shvar.value);
	    else
		throw new RuntimeException
		(
		    "Unhandled variable: \""+
		    shvar.name+"\" => \""+
		    shvar.value+"\""
		);
	}

	System.out.println("LoadLibtoolObject(): fn=\""+fn+"\" pic=\""+inf.object_pic+"\" nonpic=\""+inf.object_nonpic);

	return inf;
    }
}
