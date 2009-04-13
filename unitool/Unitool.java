
package org.de.metux.unitool.unitool;

import org.de.metux.unitool.base.*;

public class Unitool
{
    public boolean run_main(String args[]) 
    throws EUnitoolError, Exception
    {
	if (args.length<1)
	{
	    System.err.println("unitool: missing parameters");
	    return false;
	}
	
	String cmd = args[0];
	
	if (cmd.equals("--install"))
	    return new Install(args).run();
	    
	if (cmd.equals("--query"))
	    return new Query(args).run();
	    
	if (cmd.equals("--build"))
	    return new Build(args).run();

	if (cmd.equals("--pkgconfig-fixup"))
	    return new PkgConfigFixup(args).run();
    
	System.err.println("unitool: unsupported option: "+cmd);
	return false;
    }
}
