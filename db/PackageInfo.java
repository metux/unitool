
package org.de.metux.unitool.db;

import org.de.metux.util.Stringtable;

/* pkg-config style data record */
public class PackageInfo
{
    public String name        = null;
    public String description = null;
    public String version     = null;
    public String cflags      = "";
    public String libs        = "";
    public String sysroot     = null;
    
    Stringtable properties = new Stringtable();
}
