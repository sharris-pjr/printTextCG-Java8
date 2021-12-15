package com.pjrcorp.printTextCG.utils;


public class Strings
{
    private Strings()
    {
    }

    public static final boolean endsWithIgnoreCase(String source, String suffix)
    {
        final int suffixLength = suffix.length();
        final boolean IGNORE_CASE = true;
        return source.regionMatches(IGNORE_CASE, source.length() - suffixLength, suffix, 0, suffixLength);
    }
}