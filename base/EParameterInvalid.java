
package org.de.metux.unitool.base;

public class EParameterInvalid extends EUnitoolError
{
    public EParameterInvalid(String s)
    {
	super(s);
    }
    public EParameterInvalid(String s, Throwable th)
    {
	super(s,th);
    }
}
