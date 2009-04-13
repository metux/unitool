
package org.de.metux.unitool.base;

import java.util.Hashtable;

public class ToolType
{
    // source to binary object compilers
    public static final int id_COMPILE_BINOBJ_C         = 1001;
    public static final int id_COMPILE_BINOBJ_CPLUSPLUS = 1002;
    public static final int id_COMPILE_BINOBJ_JAVA      = 1003;
    public static final int id_COMPILE_BINOBJ_JBYTECODE = 1004;
    // bytecode compilers
    public static final int id_COMPILE_BYTECODE_JAVA    = 2001;

    // source to binary object compilers
    public static final String tag_COMPILE_BINOBJ_C         = "compile-c";
    public static final String tag_COMPILE_BINOBJ_CPLUSPLUS = "compile-c++";
    public static final String tag_COMPILE_BINOBJ_JAVA      = "compile-java-binobj";
    public static final String tag_COMPILE_BINOBJ_JBYTECODE = "compile-jbytecode-binobj";
    // bytecode compilers
    public static final String tag_COMPILE_BYTECODE_JAVA    = "compile-java-bytecode";

    private static Hashtable map;

    public static String id2tag(int id)
    {
	switch(id)
	{
	    case id_COMPILE_BINOBJ_C:
		return tag_COMPILE_BINOBJ_C;
	    case id_COMPILE_BINOBJ_CPLUSPLUS:
		return tag_COMPILE_BINOBJ_CPLUSPLUS;
	    case id_COMPILE_BINOBJ_JAVA: 
		return tag_COMPILE_BINOBJ_JAVA;
	    case id_COMPILE_BINOBJ_JBYTECODE:
		return tag_COMPILE_BINOBJ_JBYTECODE;
	    case id_COMPILE_BYTECODE_JAVA:
		return tag_COMPILE_BYTECODE_JAVA;
	    default:
		throw new RuntimeException("undefined id:"+id);
	}
    }
    
    private static void __init_map()
    {
	if (map!=null)
	    return;
	    
	map = new Hashtable();
	map.put(tag_COMPILE_BINOBJ_C         ,new Integer(id_COMPILE_BINOBJ_C));
	map.put(tag_COMPILE_BINOBJ_CPLUSPLUS, new Integer(id_COMPILE_BINOBJ_CPLUSPLUS));
	map.put(tag_COMPILE_BINOBJ_JAVA,      new Integer(id_COMPILE_BINOBJ_JAVA));
	map.put(tag_COMPILE_BINOBJ_JBYTECODE, new Integer(id_COMPILE_BINOBJ_JBYTECODE));
	map.put(tag_COMPILE_BYTECODE_JAVA,    new Integer(id_COMPILE_BYTECODE_JAVA));
    }
    
    public static int tag2id(String tag)
    {
	__init_map();
	Integer id = (Integer)map.get(tag);
	if (id==null)
	    throw new RuntimeException("unhandled tag: "+tag);
	return id.intValue();
    }    
}
 