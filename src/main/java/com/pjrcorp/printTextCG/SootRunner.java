package com.pjrcorp.printTextCG;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.pjrcorp.printTextCG.soot.SootClassUtility;
import com.pjrcorp.printTextCG.soot.SootUtils;
import com.pjrcorp.printTextCG.utils.CallGraphUtil;
import com.pjrcorp.printTextCG.utils.ClassPath;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.Chain;
import soot.util.dot.DotGraph;

public class SootRunner
{
    private static Path outputDir = Paths.get("/tmp/sootsoot");
    
    private Consumer<SootClass> classConsumer = null;


    public static void main(String[] args)
    {
        System.out.println("SootRunner start");
        
        FileFilter jarFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return false; // return directories for recursion
                }
                return file.getName().endsWith(".jar"); // return .url files
            }
        };

        //String jarFile = "/Users/scott/SimpleSootTest1.jar";
        //String entryPointClass = "com.pjrcorp.simplesoottest.Parent";
        //String entryPointMethod = "main";
        //printCallGraph(jarFile, outputDir, entryPointClass, entryPointMethod);
        

        File jarsDirectory = new File("/Users/scott/Downloads/c/big_pile");
        File[] fileList = jarsDirectory.listFiles(jarFilter);

        new File(jarsDirectory + "/output").mkdirs();

        System.out.println("File count: " + fileList.length);
        
        for (File file : fileList)
        {
            String fileWithPath = file.getAbsolutePath();
            String fileName = file.getName();
            System.out.println("fileWithPath: " + fileWithPath);
            System.out.println("fileName: " + fileName);
            String fileWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

            System.out.println("fileWithoutExtension : " + fileWithoutExtension);
            System.out.println("");

            printCallGraph(fileWithPath, fileWithoutExtension, jarsDirectory.getAbsolutePath());

            String moveTo = jarsDirectory.getAbsoluteFile() + "/complete/" + fileName;
            System.out.println("moveTo:" + moveTo);

            file.renameTo(new File(moveTo));
            
        }

        System.out.println("SootRunner end");
    }




//    public static void printCallGraph(String jarFile, Path outputDir, String entryPointClass, String entryPointMethod)
    public static void printCallGraph(String fileWithPath, String fileWithoutExtension, String jarsDirectory)
    {
        //String cp = "/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jce.jar:/Users/scott/Documents/Projects/Eclipse_workspace/sootEclipse/sootOutput:" + fileWithPath;
        //String cp = fileWithPath +":/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jce.jar:/Users/scott/Documents/Projects/Eclipse_workspace/sootEclipse/sootOutput"; 
        String cp = fileWithPath +":/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jsse.jar";
        
        //String cp = "/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/jce.jar:" + fileWithPath;
        //String cp = fileWithPath;
        
        System.out.println("cp: " + cp);
        
        
        Options.v().set_include_all(true);
        Options.v().set_app(true);
        Options.v().set_no_bodies_for_excluded(false);      // this really makes the count go up in the cg when set to false

        

        Options.v().set_soot_classpath(cp);
        Options.v().set_output_dir(outputDir.toString());

        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        //SootClass c = Scene.v().forceResolve(entryPointClass, SootClass.BODIES);

        //c.setApplicationClass();
        //Scene.v().loadNecessaryClasses();

        //SootMethod method = c.getMethodByName("main");
        //SootMethod method = c.getMethodByName(entryPointMethod);

        //Scene.v().setEntryPoints(Collections.singletonList(method));

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
        //Options.v().set_no_bodies_for_excluded(true);


        //DotGraph dot = new DotGraph("callgraph");

        
       // Chain<SootClass> classes = Scene.v().getClasses();
        
        
        //Collection<SootClass> allClasses = Scene.v().getClasses();
        
        //classes.stream().forEach(c -> System.out.println("Classs name: " + c.getName()));
        //allClasses.stream().forEach(c -> System.out.println("Classs name: " + c.getName()));
        
        System.out.println("..............   SCOOTER loadClasses");
        System.out.println("Loading classes from " + fileWithPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);


        try
        {
            
            //Scene.v().addBasicClass("com.pjrcorp.simplesoottest.Parent",SootClass.SIGNATURES);
                Scene.v().addBasicClass("org.springframework.aop.AfterReturningAdvice",SootClass.HIERARCHY);
                Scene.v().addBasicClass("com.google.common.base.Supplier",SootClass.HIERARCHY);
                Scene.v().addBasicClass("io.netty.channel.ChannelFutureListener",SootClass.HIERARCHY);
                Scene.v().addBasicClass("com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider",SootClass.HIERARCHY);
                Scene.v().addBasicClass("com.hazelcast.map.listener.EntryUpdatedListener",SootClass.HIERARCHY);
                Scene.v().addBasicClass("com.hazelcast.map.listener.EntryAddedListener",SootClass.HIERARCHY);
                Scene.v().addBasicClass("com.hazelcast.core.LifecycleListener",SootClass.HIERARCHY);








            ClassPath.Type dontCare = ClassPath.Type.PRE_PROCESSING_OUTPUT;
            //MutableLong totalSize = new MutableLong(0);
            ClassPath.createEntry(Paths.get(fileWithPath), dontCare).forEachClass((className,size) ->
            {
                //System.out.println("classname: " + className);
            //Scene.v().addBasicClass("com.atlassian.crowd.embedded.hibernate2.HQLMembershipQueryTranslator",SootClass.SIGNATURES);
                //SootClass loadedClass = Scene.v().loadClass(className, SootClass.SIGNATURES);
                //SootClass loadedClass = Scene.v().loadClassAndSupport(className);
                SootClass c = Scene.v().forceResolve(className, SootClass.SIGNATURES);
                
                //c.setApplicationClass();
                
                //SootResolver.v().resolveClass(className, SootClass.SIGNATURES);

                
                //if  (c.getName().contains("com.atlassian.confluence") ) {
                    //System.out.println("found class: " + c.getName());
                    c.setApplicationClass();
                //}

                //loadedClass.setApplicationClass();
                //loadedClass.
                //if (classConsumer != null)
                //{
                    //classConsumer.accept(loadedClass);
                //}
                //totalSize.add(size);
            });

        }
        catch(Exception e)
        {
            System.out.println("........ EXCEPTION");
            e.printStackTrace();
        }

        //Collection<SootClass> allClasses = Scene.v().getClasses();
        //System.out.println("class count: " + allClasses.size());
        //allClasses.stream().forEach(c -> System.out.println("Classs name: " + c.getName()));

        Scene.v().loadNecessaryClasses();
        Collection<SootClass> allClasses2 = Scene.v().getClasses();
        System.out.println("class count: " + allClasses2.size());
        PackManager.v().runPacks();
        //Collection<SootClass> allClasses3 = Scene.v().getClasses();
        //System.out.println("class count: " + allClasses3.size());
        CallGraph callGraph = Scene.v().getCallGraph();
        
        System.out.println("CG size: " + callGraph.size());

        //drawCallGraph(callGraph, fileWithoutExtension, jarsDirectory);
        Set<SootClass> callgraphClasses = CallGraphUtil.allClasses(Scene.v().getCallGraph())
                .collect(Collectors.toSet());
        
        Set<SootClass> callgraphDependencies = Sets.difference(SootClassUtility.getDependentClasses(callgraphClasses.stream()), callgraphClasses);
        
        System.out.println("SIIIZZZZEEE: " + callgraphDependencies.size());
        
        System.out.println(".............. start of soot options");
        //SootUtils.printAllEnabledPhaseOptions();
        SootUtils.printAllOptions();
        System.out.println(".............. end of soot options");

        try
        {
            //OutputStreamUtil.writeCollection(callgraphDependencies, Paths.get(outputDir.toString(), "CallgraphDependencies.txt"));
            CallGraphUtil.writeToFile(Scene.v().getCallGraph(), outputDir.toString(), fileWithoutExtension);

        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        G.reset();
    }
    
    //private static void newCallGraph(CallGraph callGraph, String newDirectory, String jarsDirectory)
    //{
    //}

    private static void drawCallGraph(CallGraph callGraph, String newDirectory, String jarsDirectory)
    {
        DotGraph dot = new DotGraph("callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();
        
        try
        {
            System.out.println("writing to: " + jarsDirectory + "/output/" + newDirectory);
            FileWriter fileWriter = new FileWriter(jarsDirectory + "/output/" + newDirectory);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            //printWriter.printf("Product name is %s and its price is %d $", "iPhone", 1000);

            System.out.println("Call Graph size : " + callGraph.size());
            while (iteratorEdges.hasNext())
            {
                Edge edge = iteratorEdges.next();
                String node_src = edge.getSrc().toString();
                String node_tgt = edge.getTgt().toString();

                // if you want an actual call graph, uncomment the next line
                //dot.drawEdge(node_src, node_tgt);
                //System.out.println(node_src + "  -->  " + node_tgt);
                printWriter.printf(node_src + "  -->  " + node_tgt + "\n");
            }
            printWriter.close();

        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        dot.plot("/tmp/callgraph.dot");
    }

}
