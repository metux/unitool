
package org.de.metux.unitool.db;

import org.de.metux.propertylist.*;
import org.de.metux.util.Environment;
import java.util.Enumeration;
import java.util.Properties;
import java.io.File;
import org.de.metux.util.StrUtil;
import org.de.metux.unitool.base.ToolConfig;

public class UnitoolConf
{
//    public static final String default_tool_config = "/etc/unitool/tools.cf";
    
    static public IPropertylist LoadToolConfig()
    {
	return LoadToolConfig((String)null);
    }

    static public ToolConfig getToolConfig()
    {
	return new ToolConfig(LoadToolConfig((String)null));
    }
    static public ToolConfig getToolConfig(File fn)
    {
	return new ToolConfig(LoadToolConfig(fn));
    }
    
    static public IPropertylist LoadToolConfig(String fn)
    {
	if (StrUtil.isEmpty(fn))
	    fn = Environment.getenv("UNITOOL_CONFIG");
	
	if (StrUtil.isEmpty(fn))
	    throw new RuntimeException("missing $UNITOOL_CONFIG");

	return LoadToolConfig(new File(fn));
    }

    static public IPropertylist LoadToolConfig(File fn)
    {
	Propertylist props = new Propertylist();
	props.loadTextDB_top(fn);
	loadEnvironment(props);
	
	try {
	    if (StrUtil.isEmpty(props.get_str("target-configured")))
		throw new RuntimeException("missing or broken unitool config file: "+fn);
	} catch (Exception e)
	{
	    throw new RuntimeException(e);
	}
	return props;
    }
    
    private static void loadEnvironment(IPropertylist pr)
    {
	Environment env = new Environment();
	for (Enumeration names = env.propertyNames(); names.hasMoreElements();)
	{
	    String n = (String)names.nextElement();
	    pr.set("@ENV/"+n,env.getProperty(n));
	}
    }
}
