
package org.de.metux.unitool.unitool;

import org.de.metux.util.*;
import org.de.metux.unitool.base.*;
import java.io.File;
import org.de.metux.unitool.pi.*;

public class PIBuild extends Command
{
    public PIBuild(String[] args)
    {
	super(args);
    }

    public boolean run_CLibraryPlain(Recipe_CLibraryPlain lib)
    {
	return new Build_CLibraryPlain(lib).run_build();
    }
    
    public boolean run_Foo(Recipe_Foo foo)
    {
	System.err.println("Processing FOO");
	return false;
    }
        
    public boolean run() 
	throws EParameterMissing, EParameterInvalid, EInstallFailed, 
	    metux.propertylist.EIllegalValue, EUnsupportedSchema
    {
	String filename = get_mandatory("recipe");

	System.err.println("running PI build");
	
	LoadConfig cf = new LoadConfig(filename);
	
	Recipe r = cf.get_Recipe();
	
	try
	{
	    return run_CLibraryPlain((Recipe_CLibraryPlain)r);
	}
	catch (ClassCastException e) {} 

	try
	{
	    return run_Foo((Recipe_Foo)r);
	}
	catch (ClassCastException e) {} 

	System.err.println("could not handle this Recipe type! ");
	return false;
    }
}
