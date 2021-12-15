package com.pjrcorp.printTextCG.soot;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pjrcorp.printTextCG.utils.Classes;

import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;

public class SootClassUtility
{
    /**
     * Thou shalt not implement an instance of this class.
     */
    private SootClassUtility()
    {
    }

    /**
     * Checks if the class implements the interface by checking all interfaces
     * implemented directly or implemented by *interface inheritance*. For
     * example, assume the the following:
     * 
     * <code>
     *     class Animal implements IHasName
     *     class Person extends Animal implements IHasContactInformation
     *     interface IHasContactInformation extends IHasPhoneNumber, IHasAddress
     * 
     *         implementsInterfaceRecursive(Person, IHasName) -> false
     *         implementsInterfaceRecursive(Person, IHasContactInformation) -> true
     *         implementsInterfaceRecursive(Person, IHasPhoneNumber) -> true
     *         implementsInterfaceRecursive(Person, IHasAddress) -> true
     *         implementsInterfaceRecursive(Animal, IHasName) -> true
     * </code>
     * 
     * @param sootClass
     *            The class for which to check if it implements the supplied
     *            interface.
     * @param interfaceName
     *            The name of the interface we're searching for.
     * @return True iff this class directly implements the interface or if one
     *         of the class's interfaces extends (directly or indirectly) the
     *         interface.
     */
    public static boolean implementsInterfaceRecursive(final SootClass sootClass, final String interfaceName)
    {
        if (sootClass.implementsInterface(interfaceName))
            return true;

        final Collection<SootClass> interfaces = sootClass.getInterfaces();
        for (final SootClass thisInterface : interfaces)
        {
            if (implementsInterfaceRecursive(thisInterface, interfaceName))
                return true;
        }
        return false;
    }

    /**
     * Check if the class is an instance of the supplied class name. Answers: Is
     * sootClass an instance of className?
     *
     * @param sootClass
     *            The class in question.
     * @param className
     *            A fully qualified class or interface name, e.g.
     *            javax.swing.JComponent
     * @return true iff sootClass or any of its super classes implements an
     *         interface with named className or sootClass or any super classes
     *         name is className.
     */
    public static boolean instanceOf(final SootClass sootClass, final String className)
    {
        SootClass iterClass = sootClass;
        while (iterClass != null)
        {
            if (iterClass.getName().equals(className) || implementsInterfaceRecursive(iterClass, className))
            {
                return true;
            }
            iterClass = iterClass.getSuperclassUnsafe();
        }

        return false;
    }

    /**
     * Checks if the type of the value is className.
     * 
     * @param value
     *            The value's type to check, must be a RefType otherwise the
     *            result will always be false.
     * @param className
     *            The class name to check if this class directly implements, or
     *            inherits from a parent class or interface.
     * @return True iff the type of the value implements or inherits from the
     *         supplied className.
     */
    public static boolean instanceOf(final Value value, final String className)
    {
        final Type sootType = value.getType();
        if (!(sootType instanceof RefType))
            return false;

        final RefType valueType = (RefType) sootType;
        return instanceOf(valueType.getSootClass(), className);
    }

    private static void forEachParentInterfaceClass(SootClass sootClass, Consumer<SootClass> consumer)
    {
        Queue<SootClass> toProcess = new ArrayDeque<>();
        toProcess.add(sootClass);

        while (!toProcess.isEmpty())
        {
            SootClass theClass = toProcess.poll();
            toProcess.addAll(theClass.getInterfaces());
            consumer.accept(theClass);
        }
    }

    private static void forEachParentClass(SootClass sootClass, Consumer<SootClass> consumer)
    {
        while (sootClass != null)
        {
            consumer.accept(sootClass);
            forEachParentInterfaceClass(sootClass, consumer);
            sootClass = sootClass.getSuperclassUnsafe();
        }
    }

    /**
     * @param sootClass
     *            The class to analyze.
     * @return All parent classes and interfaces and sootClass.
     */
    public static Collection<SootClass> getAllParentClasses(SootClass sootClass)
    {
        Collection<SootClass> parents = new HashSet<>();
        forEachParentClass(sootClass, parents::add);
        return parents;
    }

    /**
     * Gets all interfaces implemented by sootClass or by an interface of soot
     * class. Does not obtain interfaces implemented by a class which sootClass
     * extends.
     * 
     * Example A implement iB. iB implements iC. A extends D. D implements iE.
     * The result of getAllInterfaceClasses(A) == [iB, iC];
     * 
     * @param sootClass
     *            The class we want to find the interfaces of.
     * @return The set of all interfaces implemented by this class or
     *         implemented by a child-interface.
     */
    //This method seems to be never used/called?
    public static Collection<SootClass> getAllInterfaceClasses(SootClass sootClass)
    {
        Collection<SootClass> parents = new ArrayList<>();
        forEachParentInterfaceClass(sootClass, parents::add);
        return parents;
    }

    /**
     * Gets all dependencies necessary to successfully load the supplied
     * classes. In practice this means all interface classes implemented as well
     * as any classes referenced within static initialization.
     * 
     * @param forClasses
     *            The classes to search for dependencies.
     * @return All of the dependencies of forClasses as well as forClasses.
     */
    public static Set<SootClass> getDependentClasses(Stream<SootClass> forClasses)
    {
        Set<SootClass> dependencies = new HashSet<>();
        forClasses.forEach(sootClass -> {
            SootMethod staticInitMethod = sootClass.getMethodByNameUnsafe(SootMethod.staticInitializerName);
            forEachParentClass(sootClass, theClass -> {
                if (!ignoreClass(theClass) && dependencies.add(theClass) && (staticInitMethod != null))
                {
                    SootMethodUtility.getTypesUsedByMethodStream(staticInitMethod).filter(c -> !ignoreClass(c))
                            .collect(Collectors.toCollection(() -> dependencies));
                }
            });
        });
        return dependencies;
    }

    private static boolean ignoreClass(SootClass theClass)
    {
        return Classes.isGeneratedReflectionClass(theClass.getPackageName(), theClass.getShortName());
    }

    /**
     * Finds a method within the class with a subsignature that matches method.
     * This is defined as method name, parameters, and return type matching.
     * 
     * The intent of this method is to be more efficient than invoking
     * subSignature and comparing them, which creates intermediate objects, and
     * more efficient than invoking SootClass.getMethodUnsafe() which also
     * creates an intermediate object for the parameterTypes.
     * 
     * @param c
     *            The class to search
     * @param method
     *            The method whose subsignature we are searching for.
     * @return A method from the class whose subsignature matches the supplied
     *         method, otherwise null.
     */
    public static SootMethod searchSubSignature(SootClass c, SootMethod method)
    {
        List<SootMethod> methods = c.getMethods();
        for (final SootMethod m : methods)
        {
            if (m.getParameterCount() != method.getParameterCount() || m.getReturnType() != method.getReturnType()
                    || !m.getName().equals(method.getName()))
                continue;

            // Optimization: Avoid unnecessary object creation by fetching each
            // parameter type one by one.
            boolean match = true;
            for (int i = 0; i < m.getParameterCount() && match; ++i)
            {
                match = (method.getParameterType(i) == m.getParameterType(i));
            }
            if (match)
                return m;
        }
        return null;
    }
}
