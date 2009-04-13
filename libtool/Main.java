
package org.de.metux.unitool.libtool;

import org.de.metux.unitool.db.UnitoolConf;
import org.de.metux.util.StrUtil;
import org.de.metux.propertylist.IPropertylist;
import org.de.metux.unitool.base.ToolConfig;

public class Main
{
    static String  tag;
    static boolean silent = false;
    
    static void exit_err(String text)
    {
	System.err.println(text);
	System.exit(1);
    }

    static private String[] cutfirstarg(String argv[])
    {
	String newarg[] = new String[argv.length-1];
	for (int x=0; x<(argv.length-1); x++)
	    newarg[x] = argv[x+1];
	return newarg;
    }
    
    public static void main(String argv[]) throws Exception
    {
	if (argv.length==0)
	    exit_err("missing parameters");

	// FIXME: should be used somewhere ?
	if (argv[0].startsWith("--tag="))
	{
	    tag = argv[0].substring(6);  
	    main(cutfirstarg(argv));
	    return;
	}

	if (argv[0].equals("--preserve-dup-deps"))
	{
	    main(cutfirstarg(argv));
	    return;
	}

	if (argv[0].equals("--silent"))
	{
	    silent = true;
	    main(cutfirstarg(argv));
	    return;
	}
    
	ToolConfig config = UnitoolConf.getToolConfig();
	
	// load system	
	if (argv[0].equals("--mode=link"))
	    new CmdLink(argv,config).run();
	else if (argv[0].equals("--mode=compile"))
	    new CmdCompile(argv,config).run();
	else if (argv[0].equals("--mode=install"))
	    new CmdInstall(argv,config).run();
	else
	    exit_err("unsupported mode: "+argv[0]);

	System.exit(0);
//	return 0;
    }
}
