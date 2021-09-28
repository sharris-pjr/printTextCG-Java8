package com.pjrcorp.printTextCG;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class SootRunner
{
    private static Path outputDir = Paths.get("/tmp/sootsoot");

    public static void main(String[] args)
    {
        System.out.println("SootRunner start");

        String jarFile = "/Users/scott/SimpleSootTest1.jar";
        String entryPointClass = "com.pjrcorp.simplesoottest.Parent";
        String entryPointMethod = "main";
        printCallGraph(jarFile, outputDir, entryPointClass, entryPointMethod);

        System.out.println("SootRunner end");
    }


    private static void drawCallGraph(CallGraph callGraph)
    {
        DotGraph dot = new DotGraph("callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();

        System.out.println("Call Graph size : " + callGraph.size());
        while (iteratorEdges.hasNext())
        {
            Edge edge = iteratorEdges.next();
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();

            // if you want an actual call graph, uncomment the next line
            //dot.drawEdge(node_src, node_tgt);
            System.out.println(node_src + "  -->  " + node_tgt);
        }

        dot.plot("/tmp/callgraph.dot");
    }

    private static void drawProcedureDependenceGraph(SootMethod entryMethod)
    {
        Body body = entryMethod.retrieveActiveBody();
        ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);
        HashMutablePDG hashMutablePDG = new HashMutablePDG(exceptionalUnitGraph);
        CFGToDotGraph pdgForMethod = new CFGToDotGraph();
        DotGraph pdgDot = pdgForMethod.drawCFG(hashMutablePDG, body);
        pdgDot.plot("/tmp/pdg.dot");
    }

    private static void drawMethodDependenceGraph(SootMethod entryMethod)
    {
        Body body = entryMethod.retrieveActiveBody();
        ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(body);

        CFGToDotGraph cfgForMethod = new CFGToDotGraph();
        cfgForMethod.drawCFG(exceptionalUnitGraph);
        DotGraph cfgDot = cfgForMethod.drawCFG(exceptionalUnitGraph);
        cfgDot.plot("/tmp/cfg.dot");
    }


    public static void printCallGraph(String jarFile, Path outputDir, String entryPointClass, String entryPointMethod)
    {
        String cp = "/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jce.jar:/Users/scott/Documents/Projects/Eclipse_workspace/sootEclipse/sootOutput:/Users/scott/SimpleSootTest1.jar";

        Options.v().set_soot_classpath(cp);
        Options.v().set_output_dir(outputDir.toString());

        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        SootClass c = Scene.v().forceResolve(entryPointClass, SootClass.BODIES);

        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        //SootMethod method = c.getMethodByName("main");
        SootMethod method = c.getMethodByName(entryPointMethod);

        Scene.v().setEntryPoints(Collections.singletonList(method));

        Options.v().set_src_prec(Options.src_prec_java);

        Options.v().setPhaseOption("cg", "on");
        // Options.v().setPhaseOption("cg.spark", "verbose:false");

        List<String> excludeList = new LinkedList<String>();
        excludeList.add("java.");
        excludeList.add("sun.misc.");
        excludeList.add("android.");
        excludeList.add("org.apache.");
        excludeList.add("soot.");
        excludeList.add("javax.servlet.");
        Options.v().set_exclude(excludeList);
        Options.v().set_no_bodies_for_excluded(true);

        PackManager.v().runPacks();

        DotGraph dot = new DotGraph("callgraph");

        CallGraph callGraph = Scene.v().getCallGraph();

        drawCallGraph(callGraph);

    }

}
