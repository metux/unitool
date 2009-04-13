
package org.de.metux.unitool.base;

public class EUnitoolError extends Exception
{
    public EUnitoolError(String s)
    {
	super(s);
    }
    public EUnitoolError(String s, Throwable th)
    {
	super(s,th);
    }
}
