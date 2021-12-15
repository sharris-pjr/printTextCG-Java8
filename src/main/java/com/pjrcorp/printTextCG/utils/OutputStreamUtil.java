package com.pjrcorp.printTextCG.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;

public class OutputStreamUtil
{
    private OutputStreamUtil()
    {
    }

    public static FileOutputStream createFileOutputStream(File file) throws FileNotFoundException
    {
        try
        {
            return new FileOutputStream(file);
        } catch (FileNotFoundException fnfe)
        {
            file.getParentFile().mkdirs();
            return new FileOutputStream(file);
        }
    }

    public static void writeCollection(Collection<? extends Object> collection, Path path) throws FileNotFoundException
    {
        writeCollection(collection, new BufferedOutputStream(createFileOutputStream(path.toFile())));
    }
    
    /**
     * Writes the collection to the output stream, and closes the stream.
     * @param collection
     * @param os
     */
    public static void writeCollection(Collection<? extends Object> collection, OutputStream os)
    {
        try (PrintWriter w = new PrintWriter(os))
        {
            for (Object o : collection)
            {
                w.println(o.toString());
            }
        }
    }
}
