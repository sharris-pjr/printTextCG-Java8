package com.pjrcorp.printTextCG.soot;

import soot.RefType;
import soot.SootClass;
import soot.Type;

public class SootTypeUtility
{
    private SootTypeUtility()
    {
    }

    /**
     * @param sootType
     * @return The soot class for the sootType, or null if it's not a reference
     *         type (i.e. a primitive)
     */
    public static SootClass getSootClass(Type sootType)
    {
        if (sootType instanceof RefType)
        {
            return ((RefType) sootType).getSootClass();
        }
        return null;
    }

    /**
     * @param sootType
     * @return The class name for the type, or "primitive" if not a reference
     *         type.
     */
    public static String toString(Type sootType)
    {
        SootClass sc = getSootClass(sootType);
        if (sc != null)
            return sc.getShortName();
        return "primitive";
    }
}