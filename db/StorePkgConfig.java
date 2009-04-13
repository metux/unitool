
package org.de.metux.unitool.db;

import java.util.Properties;
import java.util.Enumeration;

import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StoreFile;
import org.de.metux.util.StrUtil;
import org.de.metux.util.UniqueNameList;

import org.de.metux.unitool.base.PackageInfo;
import org.de.metux.unitool.base.EPkgConfigMissingProperty;
import org.de.metux.unitool.base.EPkgConfigBrokenVariableReference;

public class StorePkgConfig
{
    static public boolean store_file(PackageInfo inf, String filename)
	throws EPkgConfigMissingProperty, EPkgConfigBrokenVariableReference
    {
	return StoreFile.store(filename,render(inf));
    }

    static String render_property(String name, String value)
    {
	if (name.equals("prefix") 	||
	    name.equals("exec_prefix")	||
	    name.equals("includedir")	||
	    name.equals("libdir"))
	    value = PathNormalizer.normalize(value);

	return name + "=" + value + "\n";
    }

    static String render_property_rec(
	Properties properties,
	Properties done,
	String name) 
	    throws 
		EPkgConfigMissingProperty,
		EPkgConfigBrokenVariableReference
    {
	if (done.get(name)!=null)
	    return "";

	String value = properties.getProperty(name);
	if (value==null)
	    return "<"+name+">\n";

	// check if we have some variable
	String res = "";
	int startpos = 0;
	int endpos  = -1;
	while ((startpos=value.indexOf("${",endpos))>=0)
	{
	    if ((endpos = value.indexOf("}",startpos))<startpos)
		throw new EPkgConfigBrokenVariableReference(value);

	    String refvar = value.substring(startpos+2,endpos);
	    res += render_property_rec(properties, done, refvar);
	    endpos++;
        }
	
	done.setProperty(name,value);
	res += render_property(name,value);
	return res;
    }

    static private String _field(String name, String value)
    {
	if (StrUtil.isEmpty(name))
	    return "";
	
	return name+": "+value+"\n";
    }

    static private String _fold(UniqueNameList l, String elem_prefix)
    {
	if (l==null)
	    return "";
	    
	String[] list = l.getNames();

	if ((list==null)||(list.length==0))
	    return "";

	String s = "";
	for (int x=0; x<list.length; x++)
	    s += elem_prefix+list[x];
	    
	return s;
    }

    static private String _render_cflags_public(PackageInfo inf)
    {
	return inf.cflags + _fold(inf.include_pathes," -I");
    }

    static private String _render_cflags_private(PackageInfo inf)
    {
	return inf.cflags_private+_fold(inf.include_pathes_private," -I");
    }

    static private String _render_requires_public(PackageInfo inf)
    {
	return 
	    inf.requires+
	    _fold(inf.requires_pkgconfig, " ");
    }
    
    static private String _render_requires_private(PackageInfo inf)
    {
	return 
	    inf.requires_private+
	    _fold(inf.requires_pkgconfig_private, " ");
    }
	    
    static private String _render_ldflags_public(PackageInfo inf)
    {
	return 
	    inf.ldflags+
	    _fold(inf.library_pathes," -L")+
	    _fold(inf.libraries," -l");
    }

    static private String _render_ldflags_private(PackageInfo inf)
    {
	return inf.ldflags_private+_fold(inf.library_pathes_private," -L");
    }

    static public String render(PackageInfo inf)
	throws EPkgConfigMissingProperty, EPkgConfigBrokenVariableReference
    {
	if (inf.name == null)
	    throw new EPkgConfigMissingProperty("Name");
	if (inf.description == null)
	    throw new EPkgConfigMissingProperty("Description");
	if (inf.version == null)
	    throw new EPkgConfigMissingProperty("Version");
	if (inf.cflags == null)
	    throw new EPkgConfigMissingProperty("Cflags");

	Properties got = new Properties();

	String str = "\n";
	Enumeration e = inf.properties.keys();
	while (e.hasMoreElements())
	    str += render_property_rec(
		inf.properties,
		got,
		(String)e.nextElement()
	    );
	str += "\n";

	// master fields
	str += _field("Name",			inf.name)+
	       _field("Description",		inf.description)+
	       _field("Version", 		inf.version)+
               _field("Conflicts",              inf.conflicts)+
	       _field("Requires", 		_render_requires_public(inf))+
	       _field("Requires.private", 	_render_requires_private(inf))+
	       _field("Cflags",  		_render_cflags_public(inf))+
	       _field("Cflags.private",		_render_cflags_private(inf))+
	       _field("Libs", 			_render_ldflags_public(inf))+
	       _field("Libs.private",		_render_ldflags_private(inf))
	;
	
	return str;
    }	
}
