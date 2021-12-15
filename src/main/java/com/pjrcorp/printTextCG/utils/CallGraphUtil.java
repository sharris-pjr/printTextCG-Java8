package com.pjrcorp.printTextCG.utils;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import soot.MethodOrMethodContext;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CallGraphUtil
{
    private CallGraphUtil()
    {
    }

    public static Stream<SootClass> classesWithSourceEdges(CallGraph cg)
    {
        Stream<MethodOrMethodContext> methodStream = Streams.fromIterator(cg.sourceMethods());
        return methodStream.map(method -> method.method().getDeclaringClass()).distinct();
    }

    public static Stream<SootClass> allClasses(CallGraph cg)
    {
        return allMethods(cg).map(SootMethod::getDeclaringClass).distinct();
    }

    public static Stream<SootMethod> allMethods(CallGraph cg)
    {
        Stream<Edge> edgeStream = Streams.fromIterator(cg.iterator());
        return edgeStream.flatMap(edge -> Stream.of(edge.getSrc().method(), edge.getTgt().method())).distinct();
    }

    private static void writeEdges(Iterator<Edge> edgeit, Writer writer) throws IOException
    {
        while (edgeit.hasNext())
        {
            Edge e = edgeit.next();
            writer.write("   " + e.toString() + "\n");
        }
    }

    private static void writeEachMethod(CallGraph cg, Writer writer) throws IOException
    {
        Iterator<MethodOrMethodContext> methodIt = cg.sourceMethods();
        while (methodIt.hasNext())
        {
            MethodOrMethodContext method = methodIt.next();
            writer.write("\n\nMethod: ");
            // When a method is removed because we cut it, the callgraph still
            // contains the method despite all the edges being removed. In the
            // case of cut methods invoking toString() throws a runtime
            // exception, so we detect it and only print out its name instead:
            if (!method.method().isDeclared())
            {
                writer.write(method.method().getName()
                        + "\n has been cut and is no longer declared.  It should have no edges.");
            } else
            {
                writer.write(method.toString());
            }
            writer.write("\nInput Edges:\n");
            writeEdges(cg.edgesInto(method), writer);
            writer.write("\nOutput Edges:\n");
            writeEdges(cg.edgesOutOf(method), writer);
        }
    }

    public static void writeToFile(CallGraph cg, String debugOutputDir, String fileNameWithoutExtension)
    {
        final String callgraphFileName = fileNameWithoutExtension + ".txt.gz";
        try
        {
            final File outFile = (debugOutputDir != null) ? Paths.get(debugOutputDir, callgraphFileName).toFile()
                    : File.createTempFile("callgraph", ".txt.gz");

            try (Writer os = new BufferedWriter(
                    new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile)))))
            {
                writeEachMethod(cg, os);
            }

        } catch (IOException e)
        {
        }
    }

    public static Collection<SootMethod> findEntryMethods(CallGraph cg)
    {
        return Streams.fromIterator(cg.sourceMethods()).map(MethodOrMethodContext::method).filter(cg::isEntryMethod)
                .collect(Collectors.toSet());
    }
}
