
package org.de.metux.unitool.base;

import java.util.Properties;
import org.de.metux.util.*;

/* pkg-config style data record */
public class PackageInfo
{
    public String name             = "";
    public String description      = "";
    public String version          = "";
    public String cflags           = "";
    public String cflags_private   = "";
    public String ldflags          = "";
    public String ldflags_private  = "";
    public String sysroot          = "";
    public String requires         = "";
    public String requires_private = "";
    public String conflicts        = "";

    public Properties properties                 = new Properties();
    public UniqueNameList include_pathes         = new UniqueNameList();
    public UniqueNameList include_pathes_private = new UniqueNameList();
    public UniqueNameList library_pathes         = new UniqueNameList();
    public UniqueNameList library_pathes_private = new UniqueNameList();
    public UniqueNameList libraries              = new UniqueNameList();
    public UniqueNameList requires_pkgconfig     = new UniqueNameList();
    public UniqueNameList requires_pkgconfig_private = new UniqueNameList();
}
