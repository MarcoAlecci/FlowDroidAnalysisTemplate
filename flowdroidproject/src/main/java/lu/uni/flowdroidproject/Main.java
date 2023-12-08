package lu.uni.flowdroidproject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import soot.G;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class Main {

    //static String APK_PATH      = "/home/marco/Teaching/StaticAndDynamicSoftwareSecurityAnalysis/ProjectSDDSA/ToAnalyze/testApp.apk";
    static String APK_PATH      = "/home/marco/Teaching/StaticAndDynamicSoftwareSecurityAnalysis/ProjectSDDSA/ToAnalyze/koopaApp.apk";
    static String ANDROID_PATH  = "/home/marco/android/platforms";
    static String PKG_NAME      = "lu.snt.trux.koopaapp";

    public static void main(String[] args) {
        System.out.println(String.format("\n⭐ %s v1.0 started on %s ⭐\n", "Main", new Date()));
        
        // Initialize
        initializeSoot();

        // Retrieve the current Soot options
        Options sootOptions = Options.v();
        printSootOptions(sootOptions);

        final InfoflowAndroidConfiguration ifac = new InfoflowAndroidConfiguration();
        ifac.getAnalysisFileConfig().setTargetAPKFile(APK_PATH);
        ifac.getAnalysisFileConfig().setAndroidPlatformDir(ANDROID_PATH);
        ifac.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);

        // CHA or SPARK
        ifac.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.CHA); 
        //ifac.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK); 
        InfoflowConfiguration.CallgraphAlgorithm algorithm = ifac.getCallgraphAlgorithm();
        if (algorithm == InfoflowConfiguration.CallgraphAlgorithm.SPARK) {
            System.out.println("Algorithm:              " + "SPARK");
        } else if (algorithm == InfoflowConfiguration.CallgraphAlgorithm.CHA) {
            System.out.println("Algorithm:              " + "CHA");
        }

        // Setup
        SetupApplication sa = new SetupApplication(ifac);
		sa.getConfig().setSootIntegrationMode(InfoflowAndroidConfiguration.SootIntegrationMode.UseExistingInstance);

        // Time
        long startTime = System.currentTimeMillis();

        // Call Graph
        System.out.println("\n\n⚡ CALL GRAPH ⚡");
        sa.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();

        System.out.println("\n\n⚡ ENTRY POINTS ⚡");
        sa.printEntrypoints();

        // Nodes
        System.out.println("\n\n⚡ METHODS ⚡");
        int numberOfNodes = 0;
        Iterator<MethodOrMethodContext> iterator = callGraph.sourceMethods();
        try {
            // To Write
            BufferedWriter writer = new BufferedWriter(new FileWriter("allMethods.txt"));
            while (iterator.hasNext()) {
                MethodOrMethodContext method = iterator.next();
                String methodName = method.method().getSignature();

                // If Koopa App print on screen
                if (methodName.contains(PKG_NAME)) { 
                    System.out.println("Node name: " + methodName);   
                }
                // Else print on file
                numberOfNodes++;
                writer.write("Node name: " + methodName + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n #️⃣  Number of nodes in the call graph: " + numberOfNodes);

        // TIME
        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double durationSeconds = durationMillis / 1000.0; // Convert milliseconds to seconds

        System.out.println("\n 🕒 Time taken: " + durationSeconds + " seconds.");
    }

    private static void initializeSoot() {
		G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_process_multiple_dex(true);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_whole_program(true);
        Options.v().set_android_jars(ANDROID_PATH);

		List<String> dirs = new ArrayList<String>();
		dirs.add(APK_PATH);
		Options.v().set_process_dir(dirs);

		Scene.v().loadNecessaryClasses();
    }


    public static void printSootOptions(Options sootOptions) {
        // Print Source Precision as a descriptive string
        String sourcePrecision;
        switch (sootOptions.src_prec()) {
            case Options.src_prec_apk:
                sourcePrecision = "APK";
                break;
            case Options.src_prec_class:
                sourcePrecision = "Class";
                break;
            case Options.src_prec_java:
                sourcePrecision = "Java";
                break;
            default:
                sourcePrecision = "Unknown";
                break;
        }

        System.out.println("⚡ SOOT OPTIONS ⚡");
        System.out.println("Output Format:          " + sootOptions.output_format());
        System.out.println("Allow Phantom Refs:     " + sootOptions.allow_phantom_refs());
        System.out.println("Process Multiple Dex:   " + sootOptions.process_multiple_dex());
        System.out.println("Source Precision:       " + sourcePrecision);
        System.out.println("Whole Program:          " + sootOptions.whole_program());
        System.out.println("Process Directory:      " + sootOptions.process_dir());
        System.out.println("Android Jars:           " + sootOptions.android_jars());
    }
}