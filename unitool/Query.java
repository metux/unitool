
package org.de.metux.unitool.unitool;

import org.de.metux.unitool.base.PackageInfo;
import org.de.metux.unitool.base.EUnitoolError;
import org.de.metux.unitool.base.Command;
import org.de.metux.unitool.db.StorePkgConfig;
import org.de.metux.unitool.db.LoadPkgConfig;
import org.de.metux.unitool.db.UnitoolConf;

import org.de.metux.util.PathNormalizer;
import org.de.metux.util.StrSplit;
import org.de.metux.propertylist.IPropertylist;
import org.de.metux.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;

public class Query extends Command
{
//    private String dbname;
    private String query;

    public Query(String args[])
    {
	super();
//	dbname = Environment.getenv("UNITOOL_PROFILE");
//	if ((dbname == null)||(dbname.length()==0))
//	    throw new RuntimeException("query: missing $UNITOOL_PROFILE env variable");

	query = args[1];
    }
    
    public boolean run()
	throws EUnitoolError, IOException, FileNotFoundException
    {
	try
	{
//	    Propertylist proplist = new Propertylist();
//	    proplist.loadTextDB_low(dbname);
	    IPropertylist proplist = UnitoolConf.LoadToolConfig();
	    String val = proplist.get_str(query);
	    System.out.print(val);
	}
	catch (Exception e)
	{
	    throw new RuntimeException("propertylist error", e);
	}

	return true;
    }
}
