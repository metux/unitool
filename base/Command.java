
package org.de.metux.unitool.base;

import org.de.metux.util.StrUtil;
import org.de.metux.propertylist.Propertylist;

public class Command
{
    protected ToolParam parameters;

    // FIXME
    public String get_mandatory(String name)
	throws EParameterInvalid
    {
	return get_str_mandatory(name);
    }

    public String get_str_mandatory(String name)
	throws EParameterInvalid
    {
	String val = parameters.get(name);
	if (StrUtil.isEmpty(val))
	    throw new RuntimeException("missing mandatory parameter: "+name);
	return val;
    }

    public String get_str(String name)
	throws EParameterInvalid
    {
	String val = parameters.get(name);
	if ((val==null)||(val.length()==0))
	    return null;
	return val;
    }

    public Command()
    {
	parameters = new ToolParam(new Propertylist());
    }
        
    public Command(String[] argv)
    {
	parameters = new ToolParam(new Propertylist());
	for (int x=1; x<argv.length; x++)
	{
	    if ((x+1)<argv.length)
	    {
		parameters.add(argv[x],argv[x+1]);
		x++;
	    }
	    else
	    {
		System.out.println("missing value for "+argv[x]);
	    }
	}
    }
}
