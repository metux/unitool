
package org.de.metux.unitool.base;

public class EPkgConfigUnhandledProperty extends EPkgConfigError
{
    public EPkgConfigUnhandledProperty(String name,String val)
    {
	super(name+"=\""+val+"\"");
    }
}
