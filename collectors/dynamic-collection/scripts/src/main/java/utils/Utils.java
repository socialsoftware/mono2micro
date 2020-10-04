package utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import domain.*;

public class Utils {
    private static final Set<String> readMethodPrefixes = new HashSet<String>(){
        {
            add("get");
        }
    };

    private static final Set<String> writeMethodPrefixes = new HashSet<String>(){
        {
            add("set");
            add("add");
            add("delete");
            add("remove");
        }
    };

    public static String getTraceID(String[] trace) {
        Pattern p = Pattern.compile("([0-9]+)\\[");
        Matcher m = p.matcher(trace[0]);
        m.find();
        return m.group(1);
    }

    public static List<Access> getMethodEntityAccesses(
        @Nullable String declaringType,
        @Nullable String methodName,
        @Nullable String callerDynamicType,
        @Nullable String[] argumentTypes,
        @Nullable String returnType,
        Map<String, Short> domainEntities
    ) {
//        Utils.print("Parsing method " + methodName + " of class " + declaringType, lineno());
        List<Access> accessesList = new ArrayList<>();
        String lowercasedMethodName = methodName.toLowerCase();
        String threeFirstLetters = lowercasedMethodName.substring(0, 3);

        if (readMethodPrefixes.contains(threeFirstLetters)) {
            if (callerDynamicType != null && domainEntities.containsKey(callerDynamicType)){
                accessesList.add(
                    new Access(
                        callerDynamicType,
                        Access.Type.R
                    )
                );
            }
                
            if (returnType != null && domainEntities.containsKey(returnType)) {
                accessesList.add(
                    new Access(
                        returnType,
                        Access.Type.R
                    )
                );
            }
    
            return accessesList;    

        } else if (
            writeMethodPrefixes.contains(threeFirstLetters) ||
            writeMethodPrefixes.contains(lowercasedMethodName.substring(0, 6))
        ) {
            if (callerDynamicType != null && domainEntities.containsKey(callerDynamicType)) {
                accessesList.add(
                    new Access(
                        callerDynamicType,
                        Access.Type.W
                    )
                );
            }
    
            if (argumentTypes != null) { // both static and dynamic
                for (String argType : argumentTypes) {
                    if (domainEntities.containsKey(argType)){
                        accessesList.add(
                            new Access(
                                argType,
                                Access.Type.W
                            )
                        );
                    }
                }          
            } 
    
            return accessesList;

        } else {
            Utils.print("[WARNING]: Method " + methodName + " from class " + declaringType + " not expected", lineno());
            System.exit(1);
            return accessesList; // Just because we have to return a String but the program already ended on the previous line
        }
    }

    public static Integer lineno() {
        return new Throwable().getStackTrace()[1].getLineNumber();
    }

    public static void print(String message, Integer lineNumber) {
        System.out.println("[" + lineNumber + "] " + message);
    }

    public static List<Access> compactConsecutiveEqualAccesses(List<Access> accesses) {
        if (accesses != null) {
            if (accesses.size() <= 1) {
                return accesses;
            }

            int i = accesses.size() - 2;
            int j = accesses.size() - 1;

            while (i >= 0) {
                if (accesses.get(i).equals(accesses.get(j))) {
                    accesses.remove(j);
                }

                j--;
                i--;
            }
        }

        return accesses;
    }
}