package com.pjrcorp.printTextCG.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class that represents a classpath, which currently supports archives
 * (zip/jar/war) and directories. Note that this does not support nested
 * archives, so jar within a jar, jar within a war, jar within a directory, etc.
 * 
 */
public class ClassPath
{
    public static final Predicate<String> CLASS_FILE_NAME_FILTER = (fileName -> Strings.endsWithIgnoreCase(fileName,
            Classes.EXTENSION));
    public static final Predicate<Path> CLASS_FILE_PATH_FILTER = (path -> CLASS_FILE_NAME_FILTER.test(path.toString()));

    public enum EntryType {
        JAR, DIRECTORY
    }

    public enum Type {
        /// From the application entries we will identify entrypoints and seeds.
        APPLICATION,
        /// The library inputs are other entries which must be specified for the
        /// application to run.
        LIBRARY,
        /// Java core libraries (rt.jar, etc.) not specified for runtime, but
        /// are specified for soot analysis.
        JAVA_CORE_LIBRARY,
        /// Output from pre-processing, typically these will be the first
        /// entries in the classpath, and may contain a combination of all of
        /// the above. However should generally be ignored when re-packaging.
        /// Not part of the original source. (i.e. Tamiflex output).
        PRE_PROCESSING_OUTPUT
    }

    public static Entry createEntry(final Path entryPath, final Type entryType) throws IOException
    {
        if (entryPath.toFile().isDirectory())
        {
            return new DirEntry(entryPath, entryType);
        } else if (Strings.endsWithIgnoreCase(entryPath.toString(), ".jar")
                || Strings.endsWithIgnoreCase(entryPath.toString(), ".zip"))
        {
            return new JarEntry(entryPath, entryType);
        } else
        {
            throw new IOException("Path is not a directory or zip/jar file: " + entryPath);
        }
    }

    public abstract static class Entry
    {
        // Could be a path to a directory or to a jar/war/zip.
        protected final Path entryPath;
        protected final Type entryType;

        private Entry(Path path, Type type)
        {
            this.entryPath = path;
            this.entryType = type;
        }

        public Type getType()
        {
            return entryType;
        }

        public Path getPath()
        {
            return entryPath;
        }

        public abstract boolean containsPackage(String packageWithSlashes);

        public abstract EntryType getEntryType();

        /**
         * Iterates over every class in this entry and invokes the supplied
         * consumer with the full class name. The class name will be formatted
         * as: "<package name>.<class name>", e.g. "java.lang.String".
         */
        public abstract void forEachClass(BiConsumer<String, Long> classNameSizeConsumer) throws IOException;
    }

    private static class DirEntry extends Entry
    {
        DirEntry(Path path, Type type)
        {
            super(path, type);
        }

        public EntryType getEntryType()
        {
            return EntryType.DIRECTORY;
        }

        public boolean containsPackage(String packageWithSlashes)
        {
            Path packagePath = Paths.get(entryPath.toString(), packageWithSlashes);
            return packagePath.toFile().isDirectory();
        }

        private static String classNameFromPath(Path classFilePath, Path startingPath)
        {
            String className = startingPath.relativize(classFilePath).toString();
            className = className.substring(0, className.length() - Classes.EXTENSION_LENGTH);
            className = className.replace(File.separatorChar, '.');
            return className;
        }

        public void forEachClass(BiConsumer<String, Long> classNameConsumer) throws IOException
        {
            try (Stream<Path> pathStream = Files.walk(entryPath))
            {
                pathStream.filter(CLASS_FILE_PATH_FILTER).forEach(filePath -> classNameConsumer
                        .accept(classNameFromPath(filePath, entryPath), filePath.toFile().length()));
            }
        }
    }

    private static class JarEntry extends Entry
    {
        ZipFile zipFile;

        JarEntry(Path path, Type type) throws IOException
        {
            super(path, type);
            zipFile = new ZipFile(path.toFile());
        }

        public EntryType getEntryType()
        {
            return EntryType.JAR;
        }

        public boolean containsPackage(String packageWithSlashes)
        {
            // TODO this seems like it won't work unless there's an entry for
            // the directory, which is not required for zip files.
            ZipEntry ze = zipFile.getEntry(packageWithSlashes);
            return ze != null && ze.isDirectory();
        }

        private static String classNameFromZipName(String zipEntryName)
        {
            final String className = zipEntryName.substring(0, zipEntryName.length() - Classes.EXTENSION_LENGTH);
            return className.replace('/', '.');
        }

        public void forEachClass(BiConsumer<String, Long> classNameConsumer) throws IOException
        {
            zipFile.stream().filter(entry -> CLASS_FILE_NAME_FILTER.test(entry.getName()))
                    .forEach(entry -> classNameConsumer.accept(classNameFromZipName(entry.getName()), entry.getSize()));
        }
    }

    List<Entry> entries;

    public ClassPath(List<Entry> entries) throws IOException
    {
        this.entries = new ArrayList<>(entries.size() + 2);
        this.entries.addAll(entries);
        addCoreLibraries();
    }

    public ClassPath(Entry... entries) throws IOException
    {
        this(Arrays.asList(entries));
    }

    private void addCoreLibraries() throws IOException
    {
        final Path libDir = Paths.get(System.getProperty("java.home"), "lib");
        entries.add(new JarEntry(Paths.get(libDir.toString(), "rt.jar"), Type.JAVA_CORE_LIBRARY));
        entries.add(new JarEntry(Paths.get(libDir.toString(), "jce.jar"), Type.JAVA_CORE_LIBRARY));
        entries.add(new JarEntry(Paths.get(libDir.toString(), "jsse.jar"), Type.JAVA_CORE_LIBRARY));
    }

    /**
     * @param packageWithSlashes
     *            package name in directory-style separator: (i.e. java/lang)
     * @return the first classpath entry which contains the specified package,
     *         or null if none exist.
     */
    public Path findPackage(String packageWithSlashes)
    {
        for (Entry entry : entries)
        {
            if (entry.containsPackage(packageWithSlashes))
                return entry.entryPath;
        }
        return null;
    }

    public List<Entry> getAllEntries()
    {
        return entries;
    }

    /**
     * @return A string of the libraries within this classPath separated by the
     *         path.separator property (":" or ";"), such that the string is
     *         easily used to set the classpath.
     */
    public String getLibraries()
    {
        return getEntriesAsString(entry -> entry.entryType == Type.LIBRARY);
    }

    /**
     * @param p
     *            A predicate to test for inclusion of the entry in the string.
     * @return A string of the entries which pass the predicate within this
     *         classPath separated by the path.separator property (":" or ";"),
     *         such that the string is easily used to set the classpath.
     */
    public String getEntriesAsString(Predicate<Entry> p)
    {
        StringBuilder libraryString = new StringBuilder(1024);
        String separator = System.getProperty("path.separator");
        boolean first = true;

        for (Entry entry : entries)
        {
            if (!p.test(entry))
                continue;

            libraryString.append(first ? "" : separator);
            libraryString.append(entry.entryPath.toString());
            first = false;
        }
        return libraryString.toString();
    }
}
