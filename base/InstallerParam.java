
package org.de.metux.unitool.base;

import org.de.metux.propertylist.IPropertylist;

public class InstallerParam extends ToolParam
{
    public final static String cf_installer_command = "installer-command";
    public final static String cf_install_source    = "install-source";
    public final static String cf_install_target    = "install-target";
    public final static String cf_install_strip     = "install-strip";

/*
    public InstallerParam(IPropertylist pr)
    {
	super(pr);
    }
*/

    public InstallerParam(ToolConfig cf)
    {
	super(cf);
    }

//    public InstallerParam(ToolParam par)
//    {
//	super(par.getProperties());
//    }

    /* --- installer command --- */
    public void setInstallerCommand(String cmd)
    {
	set(cf_installer_command, cmd);
    }

    public String getInstallerCommand()
    {
	return get(cf_installer_command);
    }

    /* --- install source --- */
    public void setInstallSource(String fn)
    {
	set(cf_install_source, fn);
    }

    public String getInstallSource()
	throws EParameterMissing, EParameterInvalid
    {
	return get_mandatory(cf_install_source);
    }
    
    /* --- install dest --- */
    public void setInstallTarget(String fn)
    {
	set(cf_install_target, fn);
    }

    public String getInstallTarget()
	throws EParameterMissing, EParameterInvalid
    {
	return get_mandatory(cf_install_target);
    }
    
    public void setInstallStrip()
    {
	set(cf_install_strip, "yes");
    }
    
    public boolean getInstallStrip()
	throws EParameterMissing, EParameterInvalid
    {
	String str = get_mandatory(cf_install_strip);
	return str.equals("yes");
    }
}
