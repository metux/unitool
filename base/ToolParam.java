
package org.de.metux.unitool.base;

import org.de.metux.util.StrSplit;
import org.de.metux.util.StrReplace;
import org.de.metux.util.PathNormalizer;
import org.de.metux.util.UniqueValues;
import org.de.metux.util.StrUtil;
import org.de.metux.util.UniqueNameList;

import org.de.metux.propertylist.*;

import java.util.Enumeration;
import java.util.Hashtable;

public class ToolParam
{
    public final static String cf_verbatim  = "verbatim";
    public final static String cf_sysroot   = "system-root";
    public final static String cf_init_dirs = "init-dirs";

    IPropertylist properties         = new Propertylist();
    public ToolConfig config         = new ToolConfig(new Propertylist());
    public PathNormalizer normalizer = new PathNormalizer();
    
//    public IPropertylist getProperties()
//    {
//	return properties;
//    }

    public String getConfigStr(String name)
    {
	return config.getConfigStr(name);
    }

    public void setConfig(ToolConfig cf)
    {
	config = cf;
    }

    public ToolParam(IPropertylist p)
    {
	if (p==null)
	    properties = new Propertylist();
	else
	    properties = p;
    }

    public ToolParam(ToolConfig cf)
    {
	config = cf;
    }
    
    public ToolParam(ToolConfig cf, IPropertylist prop)
    {
	properties = prop;
	config = cf;
    }

    public void remove(String key)
    {
	properties.remove(key);
    }
    
    public void set(String name, String val)
    {
	if (name==null)
	    throw new RuntimeException("NULL name");
	
	if (val==null)
	    return;

	properties.set(name,val);
	if (name.equals(cf_sysroot))
	    normalizer.setSysroot(val);
    }

    public void add(String name, String value)
    {
	properties.add(name,value);
    }

    public void add(String name, UniqueNameList values)
    {
	if ((name==null)||(name.length()==0))
	    return;
	if (values==null)
	    return;
	String vals[] = values.getNames();
	for (int x=0; x<vals.length; x++)
	    add(name,vals[x]);
    }

    public void add(String name, String value[])
    {
	if (value!=null)
	    for (int x=0; x<value.length; x++)
		add(name,value[x]);
    }
    
    public void set(String name, int val)
    {
	set(name,String.valueOf(val));
    }
    
    public String get(String name)
    {
	try
	{
	    String str = properties.get_str(name);
	    if (str==null)
		return "";
	    else
		return str;
	}
	catch (EIllegalValue e)
	{
	    return "";
	}
    }

    public String get_mandatory(String name)
	throws EParameterMissing, EParameterInvalid
    {
	try
	{
	    String value = properties.get_str(name);
	    if (StrUtil.isEmpty(name))
		throw new EParameterMissing(name);
	    return value;
	}
	catch (EIllegalValue e)
	{
	    throw new EParameterInvalid(name);
	}
    }

    public boolean get_bool(String name)
	throws EParameterInvalid
    {
	try
	{
	    return properties.get_bool(name);
	}
	catch (EIllegalValue e)
	{
	    throw new EParameterInvalid(name);
	}
    }	
	
    public int get_int(String name)
    {
	return (new Integer(get(name))).intValue();
    }
    
    public void clearSysroot()
    {
	remove(cf_sysroot);
    }
    
    public void setSysroot(String sysroot)
    {
	set(cf_sysroot, sysroot);
	normalizer.setSysroot(sysroot);
    }

    public void addVerbatim(String option)
    {
	add(cf_verbatim, option);
    }

    public void addVerbatim(String options[])
    {
	for (int x=0; x<options.length; x++)
	    addVerbatim(options[x]);
    }
    
    public void clearVerbatim()
    {
	remove(cf_verbatim);
    }

    public Enumeration propertyNames()
    {
	return properties.propertyNames();
    }
    
    public String toString()
    {
	String s = "";
	Hashtable ht = new Hashtable();
	
	for (Enumeration e=properties.propertyNames(); e.hasMoreElements();)
	{
	    String name = (String)e.nextElement();
	    // FIXME: better use StringTokenizer directly or regex ?
	    String vals[] = StrSplit.split(get(name));
	    for (int x=0; x<vals.length; x++)
	    {
		String key = name+"||"+vals[x];
		
		if (ht.get(key)!=null)
		    continue;

		s += name+" "+vals[x]+" ";
		ht.put(key,key);
	    }
	}
	return s;
    }
    
    public String get_str_def(String name, String def)
    {
	String val = get(name);
	if ((val==null)||(val.length()==0))
	    return def;
	else
	    return val;    
    }

    public String[] get_str_list(String name)
    {
	String val = get(name);
	if ((val!=null)&&(val.length()!=0))
	    return StrSplit.split(val);
	
	String[] x = new String[1];
	x[0] = "";
	return x;
    }

    // fetch a path list, normalize and compress
    public String [] get_path_list(String name)
    {
	String [] p = get_str_list(name);
	for (int x=0; x<p.length; x++)
	    p[x] = PathNormalizer.normalize(p[x]);

	return UniqueValues.unique(p);
    }
    
    public String [] get_path_list_strip_sysroot(String name)
    {
	String [] p = get_str_list(name);
	for (int x=0; x<p.length; x++)
	    p[x] = normalizer.strip_sysroot(p[x]);

	return UniqueValues.unique(p);
    }
    
    public String getSysroot()
    {
	return get(cf_sysroot);
    }

    public String[] getVerbatims()
    {
	return get_str_list(cf_verbatim);
    }

    public void addInitDirs(String d)
    {
	add(cf_init_dirs,d);
    }
    
    public String[] getInitDirs()
    {
	return get_str_list(cf_init_dirs);
    }    

    public void clearInitDirs()
    {
	remove(cf_init_dirs);
    }
}
