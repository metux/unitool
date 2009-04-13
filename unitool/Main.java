
package org.de.metux.unitool.unitool;

import org.de.metux.unitool.base.*;

public class Main
{
    public static void main(String argv[]) throws EUnitoolError, Exception
    {
	if (!(new Unitool().run_main(argv)))
	    throw new RuntimeException("unitool failed");
    }
}
