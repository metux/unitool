
package org.de.metux.unitool.base;

import org.de.metux.util.StrSplit;
import org.de.metux.util.StrUtil;
import org.de.metux.propertylist.IPropertylist;
import org.de.metux.propertylist.EIllegalValue;

public class ToolConfig
{
    IPropertylist config;
    
    public String getConfigStr(String name,String def)
    {
	try
	{
	    String str = config.get_str(name);
//	    if (str==null)
	    if (StrUtil.isEmpty(str))
		return def;
	    else
		return str;
	}
	catch (EIllegalValue e)
	{
	    return def;
	}
    }

    public String getConfigStr(String name)
    {
	return getConfigStr(name,null);
    }

    public ToolConfig(IPropertylist cf)
    {
	config = cf;
    }
    
    public String toString()
    {
	return config.toString();
    }
    
    public IPropertylist getPropertylist()
    {
	return config;
    }
}
