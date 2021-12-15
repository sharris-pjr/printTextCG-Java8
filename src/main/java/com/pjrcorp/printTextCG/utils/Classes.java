package com.pjrcorp.printTextCG.utils;

public class Classes
{

    public static final String EXTENSION = ".class";
    public static final int EXTENSION_LENGTH = EXTENSION.length();

    public static final String LAMBDA_PROXY_STRING = "$$Lambda$";
    public static final String LAMBDA_FUNCTION_STRING = "$lambda_";

    public static final String REFLECTION_PACKAGE = "sun.reflect";
    public static final String REFLECTION_GENERATED = "Generated";

    public static boolean isClassFile(String fileName)
    {
        return Strings.endsWithIgnoreCase(fileName, EXTENSION);
    }
    
    /**
     * Returns -1 if not lambda, otherwise the index to the start of the lambda identifier within the string.
     */
    public static int getLambdaPosition(String className)
    {
        int lambdaIndex = className.lastIndexOf(Classes.LAMBDA_PROXY_STRING);
        if (lambdaIndex == -1)
        {
            lambdaIndex = className.lastIndexOf(Classes.LAMBDA_FUNCTION_STRING);
        }
        return lambdaIndex;
    }
    
    public static boolean isLambda(String className)
    {
        return getLambdaPosition(className) != -1;
    }

    public static boolean isGeneratedReflectionClass(String dottedPackageName, String className)
    {
        return dottedPackageName.equals(REFLECTION_PACKAGE) && className.startsWith(REFLECTION_GENERATED);
    }
}
