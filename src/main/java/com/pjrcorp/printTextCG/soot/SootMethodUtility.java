package com.pjrcorp.printTextCG.soot;


import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;

public class SootMethodUtility
{
    private SootMethodUtility()
    {
    }

    private static void addType(Type sootType, Collection<SootClass> collection)
    {
        SootClass sootClass = SootTypeUtility.getSootClass(sootType);
        if (sootClass == null)
            return;
        collection.add(sootClass);
        collection.addAll(SootClassUtility.getAllParentClasses(sootClass));
    }

    public static Collection<SootClass> getTypesUsedByMethod(SootMethod method)
    {
        HashSet<SootClass> types = new HashSet<>();

        if (!method.hasActiveBody())
        {
            return types;
        }
        Body methodBody = method.getActiveBody();

        final PatchingChain<Unit> methodUnits = methodBody.getUnits();
        for (final Unit unit : methodUnits)
        {
            unit.getUseAndDefBoxes().forEach(valueBox -> addType(valueBox.getValue().getType(), types));
        }

        return types;
    }

    public static Stream<SootClass> getTypesUsedByMethodStream(SootMethod method)
    {
        if (!method.hasActiveBody())
        {
            return Stream.empty();
        }
        Body methodBody = method.getActiveBody();

        final PatchingChain<Unit> methodUnits = methodBody.getUnits();
        return methodUnits.stream()
                .flatMap(unit -> unit.getUseAndDefBoxes().stream()
                        .map(valueBox -> SootTypeUtility.getSootClass(valueBox.getValue().getType()))
                        .filter(Objects::nonNull))
                .distinct();
    }
    
    
    /**
     * Gets all methods from the classes that were designated as interface classes
     * 
     * @param interfaceClasses
     *            The classes to find methods in
     * @return All of the methods for all the interface classes
     */
    public static Collection<SootMethod> getInterfaceClassMethods(Collection<SootClass> interfaceClasses)
    {
        return interfaceClasses.stream().flatMap(sootclass -> sootclass.getInterfaces().stream())
                .flatMap(sootClass -> sootClass.getMethods().stream()).collect(Collectors.toSet());
    }

}
