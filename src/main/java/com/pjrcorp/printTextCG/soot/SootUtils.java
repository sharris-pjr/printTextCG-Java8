package com.pjrcorp.printTextCG.soot;


import java.util.Iterator;
import java.util.Map;

import soot.HasPhaseOptions;
import soot.Pack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.options.Options;

public class SootUtils
{

    public static void printAllOptions()
    {
        System.out.println("check_int_throw_analysis: " + Options.v().check_init_throw_analysis());
        System.out.println("filed_type_mismatches: " + Options.v().field_type_mismatches());
        System.out.println("java_version: " + Options.v().java_version());
        System.out.println("main_class: " + Options.v().main_class());
        System.out.println("output_dir: " + Options.v().output_dir());
        System.out.println("output_format: " + Options.v().output_format());
        System.out.println("soot_classpath: " + Options.v().soot_classpath());
        System.out.println("src_prec: " + Options.v().src_prec());
        System.out.println("allow_phantom_elms: " + Options.v().allow_phantom_elms());
        System.out.println("allow_phantom_refs: " + Options.v().allow_phantom_refs());
        System.out.println("app: " + Options.v().app());
        System.out.println("coffi: " + Options.v().coffi());
        System.out.println("debug: " + Options.v().debug());
        System.out.println("debug_resolver: " + Options.v().debug_resolver());
        System.out.println("drop_bodies_after_load: " + Options.v().drop_bodies_after_load());
        System.out.println("force_overwrite: " + Options.v().force_overwrite());
        System.out.println("full_resolver: " + Options.v().full_resolver());
        System.out.println("gzip: " + Options.v().gzip());
        System.out.println("hierarchy_dirs: " + Options.v().hierarchy_dirs());
        System.out.println("ignore_classpath_errors: " + Options.v().ignore_classpath_errors());
        System.out.println("ignore_resolution_errors: " + Options.v().ignore_resolution_errors());
        System.out.println("ignore_resolving_levels: " + Options.v().ignore_resolving_levels());
        System.out.println("include_all: " + Options.v().include_all());
        System.out.println("interactive_mode: " + Options.v().interactive_mode());
        System.out.println("j2me: " + Options.v().j2me());
        System.out.println("jasmin_backend: " + Options.v().jasmin_backend());
        System.out.println("keep_line_number: " + Options.v().keep_line_number());
        System.out.println("keep_offset: " + Options.v().keep_offset());
        System.out.println("no_bodies_for_excluded: " + Options.v().no_bodies_for_excluded());
        System.out.println("no_output_inner_classes_attribute: " + Options.v().no_output_inner_classes_attribute());
        System.out.println("no_output_source_file_attribute: " + Options.v().no_output_source_file_attribute());
        System.out.println("no_writeout_body_releasing: " + Options.v().no_writeout_body_releasing());
        System.out.println("oaat: " + Options.v().oaat());
        System.out.println("omit_exepting_unit_edges: " + Options.v().omit_excepting_unit_edges());
        System.out.println("on_the_fly: " + Options.v().on_the_fly());
        System.out.println("output_jar: " + Options.v().output_jar());
        System.out.println("permissive_resolving: " + Options.v().permissive_resolving());
        System.out.println("phase_list: " + Options.v().phase_list());
        System.out.println("polyglot: " + Options.v().polyglot());
        System.out.println("prepend_classpath: " + Options.v().prepend_classpath());
        System.out.println("print_tags_in_output: " + Options.v().print_tags_in_output());
        System.out.println("process_multiple_dex: " + Options.v().process_multiple_dex());
        System.out.println("search_dex_in_archive: " + Options.v().search_dex_in_archives());
        System.out.println("show_expection_dests: " + Options.v().show_exception_dests());
        System.out.println("subtract_gc: " + Options.v().subtract_gc());
        System.out.println("time: " + Options.v().time());
        System.out.println("unfriendly_mode: " + Options.v().unfriendly_mode());
        System.out.println("validate: " + Options.v().validate());
        System.out.println("verbose: " + Options.v().verbose());
        System.out.println("via_grimp: " + Options.v().via_grimp());
        System.out.println("via_shimple: " + Options.v().via_shimple());
        System.out.println("whole_program: " + Options.v().whole_program());
        System.out.println("whole_shimple: " + Options.v().whole_shimple());
        System.out.println("write_local_annotations: " + Options.v().write_local_annotations());
        System.out.println("xml_attributes: " + Options.v().xml_attributes());
        System.out.println("dump_cfg: " + Options.v().dump_cfg().toString());

    }

    public static void printPhaseOptionsForPhase(String phaseName)
    {
        PhaseOptions.v().getPhaseOptions(phaseName).entrySet().stream()
            .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue())); 
    }

    public static void printAllEnabledPhaseOptions()
    {
        for (Pack pack : PackManager.v().allPacks())
        {
            for (Iterator<Transform> it = pack.iterator(); it.hasNext();)
            {
                HasPhaseOptions hasPhaseOptions = (HasPhaseOptions) it.next();
                String fullPhaseName = hasPhaseOptions.getPhaseName();

                Map<String, String> phaseOptions = PhaseOptions.v().getPhaseOptions(fullPhaseName);
                
                if (phaseOptions.get("enabled").equals("true"))
                {
                    System.out.println("Phase Name: " + fullPhaseName);
                    
                    for (Map.Entry<String,String> entry : phaseOptions.entrySet())  
                    {
                        System.out.println(entry.getKey() + " : " + entry.getValue()); 
                    }
                    System.out.println("");
                }
            }
            
        }
    }

    public static void printAllPhaseOptions()
    {
        for (Pack pack : PackManager.v().allPacks())
        {
            for (Iterator<Transform> it = pack.iterator(); it.hasNext();)
            {
                HasPhaseOptions hasPhaseOptions = (HasPhaseOptions) it.next();
                String fullPhaseName = hasPhaseOptions.getPhaseName();

                Map<String, String> phaseOptions = PhaseOptions.v().getPhaseOptions(fullPhaseName);
                
                System.out.println("Phase Name: " + fullPhaseName);
                    
                for (Map.Entry<String,String> entry : phaseOptions.entrySet())  
                {
                    System.out.println(entry.getKey() + " : " + entry.getValue()); 
                }
                System.out.println("");
            }
            
        }
    }

}
