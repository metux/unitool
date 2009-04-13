
package org.de.metux.unitool.db;

import org.de.metux.util.ShellVariableDef;
import org.de.metux.util.StrSplit;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;

import org.de.metux.unitool.base.PackageInfo;
import org.de.metux.unitool.base.EPkgConfigMissingProperty;
import org.de.metux.unitool.base.EPkgConfigParseError;
import org.de.metux.unitool.base.EPkgConfigUnhandledProperty;

public class LoadPkgConfig
{
    /* load the first part: variables/properties */
    static private void __handle_variable(PackageInfo inf, String line)
	throws EPkgConfigParseError
    {
	try
	{
	    ShellVariableDef shvar = new ShellVariableDef(line);
	    inf.properties.setProperty(shvar.name,shvar.value);
	}
	catch (ShellVariableDef.XEmpty e)
	{
	    throw new EPkgConfigParseError("emty line: "+e,e);
	}
	catch (ShellVariableDef.XParseFailed e)
	{
	    throw new EPkgConfigParseError("parse failed: "+e,e);
	}
    }

    static private void __handle_major(PackageInfo inf, String line)
	throws EPkgConfigParseError, EPkgConfigUnhandledProperty
    {
	int pos = line.indexOf(':');
	if (pos<1)
	    throw new EPkgConfigParseError("missing \":\" in line \""+line+"\"");

	String name  = line.substring(0,pos).trim();
	String value = line.substring(pos+1).trim();
	    
	if (name.equals("Name"))
	    inf.name = value;
	else if (name.equals("Description"))
	    inf.description = value;
	else if (name.equals("Version"))
	    inf.version = value;
	else if (name.equals("Cflags"))
	    inf.cflags = value;
	else if (name.equals("Cflags.private"))
	    inf.cflags_private = value;
	else if (name.equals("Libs"))
	    inf.ldflags = value;
	else if (name.equals("Libs.private"))
	    inf.ldflags_private = value;
	else if (name.equals("Requires"))
	    inf.requires = value;
	else if (name.equals("Requires.private"))
	    inf.requires_private = value;
	else if (name.equals("Conflicts"))
	    inf.conflicts = value;
	else 
	    throw new EPkgConfigUnhandledProperty(name,value);
    }

    static private String _strip_comments(String str)
    {
	int pos = str.indexOf('#');
	if (pos==-1)
	    return str;
	if (pos==0)
	    return "";
	return str.substring(0,pos);    
    }

    /* load the second part: major fields */
    static private void __load_lines(
	PackageInfo inf, 
	BufferedReader in,
	PathNormalizer norm
    )
	throws IOException, EPkgConfigParseError, EPkgConfigUnhandledProperty
    {
	String line;
	while (true)
	{
	    /* go out when no more to read */
	    if ((line=in.readLine())==null)
		return;
		
	    /* go out on newline */
	    line = _strip_comments(line).trim();
	    if (line.length()==0)
	    ;
	    else if (line.indexOf(':')>=0)
		__handle_major(inf, line);
	    else if (line.indexOf('=')>=0)
		__handle_variable(inf, line);
	    else 
		throw new EPkgConfigParseError("cannot parse line: \""+line+"\"");
	}
    }

    static private void __sanity_checks(PackageInfo inf)
	throws EPkgConfigMissingProperty
    {
	/* sanity checks for several required fields */
	if (inf.name==null)
	    throw new EPkgConfigMissingProperty("Name");
	if (inf.description==null)
	    throw new EPkgConfigMissingProperty("Description");
	if (inf.version==null)
	    throw new EPkgConfigMissingProperty("Version");
	if (inf.cflags==null)
	    throw new EPkgConfigMissingProperty("Cflags");
    }

    static public PackageInfo load_package(
	String absolute_filename,
	String sysroot
    )
	throws FileNotFoundException, IOException,
	    EPkgConfigMissingProperty,
	    EPkgConfigParseError,
	    EPkgConfigUnhandledProperty
    {
	PathNormalizer norm = new PathNormalizer();
	norm.setSysroot(sysroot);

	BufferedReader in =
	    new BufferedReader(new FileReader(absolute_filename));
	
	String line;
	PackageInfo inf = new PackageInfo();
	inf.sysroot = sysroot;

	__load_lines(inf,in,norm);
	__sanity_checks(inf);

	return inf;
    }	
}
