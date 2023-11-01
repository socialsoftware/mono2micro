package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranchHeuristics {
    public static final String LOOP_BRANCH_H = "loop_branch_heuristic";
    public static final String POINTER_H = "pointer_heuristic";
    public static final String CALL_H = "call_heuristic";
    public static final String OPCODE_H = "opcode_heuristic";
    public static final String LOOP_EXIT_H = "loop_exit_heuristic";
    public static final String RETURN_H = "return_heuristic";
    public static final String STORE_H = "store_heuristic";
    public static final String LOOP_HEADER_H = "loop_header_heuristic";
    public static final String GUARD_H = "guard_heuristic";

    /**
     * Represents the probability of the primary successor of the branch being taken
     */
    public static Map<String, Float> heuristicProbabilities = new HashMap<>();

    static {
        heuristicProbabilities.put(LOOP_BRANCH_H, 0.88f);
        heuristicProbabilities.put(POINTER_H,     0.4f);    // 1-0.6
        heuristicProbabilities.put(OPCODE_H,      0.16f);   // 1-0.84
        heuristicProbabilities.put(GUARD_H,       0.62f);
        heuristicProbabilities.put(LOOP_EXIT_H,   0.8f);
        heuristicProbabilities.put(LOOP_HEADER_H, 0.75f);
        heuristicProbabilities.put(CALL_H,        0.78f);
        heuristicProbabilities.put(STORE_H,       0.55f);
        heuristicProbabilities.put(RETURN_H,      0.72f);
    }

    /**
     * Receives the heuristic flags of 2 diverging paths returns appliable heuristics. If an heuristic is applied in reverse, it will have the "_i" suffix.
     * @param cond , the heuristic flags of the condition, if appliable
     * @param b1 , the heuristic flags of branch 1
     * @param b2 , the heuristic flags of branch 2
     * @return List (String) of heuristic that apply
     */
    public static List<String> getAppliableHeuristics(HeuristicFlags cond, HeuristicFlags b1, HeuristicFlags b2) {
        List<String> appliableHeuristics = new ArrayList<>();

        if (cond != null) {
            // POINTER_H
            if (cond.objectComparison) {
                appliableHeuristics.add(POINTER_H);
            }

            // OPCODE_H
            if (cond.zeroComparison) {
                appliableHeuristics.add(OPCODE_H);
            }

            // GUARD_H
            if (cond.laterChangedCVariable) {
                appliableHeuristics.add(GUARD_H);
            }
        }
        
        if (b1 != null && b2 != null) {
                  
            // LOOP_BRANCH_H
            if (b1.goingToLoopHead && !b2.goingToLoopHead) {
                appliableHeuristics.add(LOOP_BRANCH_H);
            } else if (b2.goingToLoopHead && !b1.goingToLoopHead) {
                appliableHeuristics.add(inverted(LOOP_BRANCH_H));
            }
            
            // LOOP_EXIT_H
            if (!b1.hasBreak && b2.hasBreak) {
                appliableHeuristics.add(LOOP_EXIT_H);
            } else if (!b2.hasBreak && b1.hasBreak) {
                appliableHeuristics.add(inverted(LOOP_EXIT_H));
            }
            
            // LOOP_HEADER_H
            if (b1.hasLoop && !b1.postDominant && !(b2.hasLoop && !b2.postDominant)) {
                appliableHeuristics.add(LOOP_HEADER_H);
            } else if (b2.hasLoop && !b2.postDominant && !(b1.hasLoop && !b1.postDominant)) {
                appliableHeuristics.add(inverted(LOOP_HEADER_H));
            }
            
            // CALL_H
            if (b1.hasCall && !b1.postDominant && !(b2.hasCall && !b2.postDominant)) {
                appliableHeuristics.add(CALL_H);
            } else if (b2.hasCall && !b2.postDominant && !(b1.hasCall && !b1.postDominant)) {
                appliableHeuristics.add(inverted(CALL_H));
            }
            
            // STORE_H
            if (b1.hasStore && !b1.postDominant && !(b2.hasStore && !b2.postDominant)) {
                appliableHeuristics.add(STORE_H);
            } else if (b2.hasStore && !b2.postDominant && !(b1.hasStore && !b1.postDominant)) {
                appliableHeuristics.add(inverted(STORE_H));
            }
            
            // RETURN_H
            if (!b1.hasReturn && !b1.postDominant && !(!b2.hasReturn && !b2.postDominant)) {
                appliableHeuristics.add(RETURN_H);
            } else if (!b2.hasReturn && !b2.postDominant && !(!b1.hasReturn && !b1.postDominant)) {
                appliableHeuristics.add(inverted(RETURN_H));
            }
            
        }
        
        
        return appliableHeuristics;
        
    }
    
    public static String inverted(String heuristic) {
        return heuristic + "_i";
    }
    
    public static float calculateBranchProbability(List<String> heuristics) {
        // calculate probability based on the Dempster-Shafer theory 
        // x*y/(x*y + (1-x)*(1-y))

        float result = 0.5f; // initial probability of each branch is 0.5; 0.5 is also neutral in the operation
        for (String h : heuristics) {
            float currentHeuristicProb;
            if (heuristicProbabilities.containsKey(h)) {
                currentHeuristicProb = heuristicProbabilities.get(h);
            } else if (heuristicProbabilities.containsKey(h.split("_i")[0])) {
                currentHeuristicProb = 1 - heuristicProbabilities.get(h.split("_i")[0]);
            } else {
                currentHeuristicProb = 0;
            }

            result = result*currentHeuristicProb/(result*currentHeuristicProb + (1-result)*(1-currentHeuristicProb));

        }

        return result;

    }
}
