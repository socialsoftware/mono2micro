package collectors.JPA;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import util.Classes;
import util.Query;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAUtils {
    static void parseAtOneToOne(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation oneToOneAnnotation = getAnnotation(fieldAnnotations, "OneToOne");
        if (oneToOneAnnotation != null) {
            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (joinTableAnnotation != null) {
                if (joinTableAnnotation.getValues().get("name") != null) {
                    CtExpression name = joinTableAnnotation.getValue("name");
                    String joinTableName = getValueAsString(name);

                    Classes classes = new Classes();
                    classes.addClass(clazz.getSimpleName());
                    CtType fieldType = field.getType().getTypeDeclaration();
                    classes.addClass(fieldType.getSimpleName());
                    SpringDataJPACollector.tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
                }
            }
        }
    }

    static void parseAtManyToOne(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation manyToOneAnnotation = getAnnotation(fieldAnnotations, "ManyToOne");
        if (manyToOneAnnotation != null) {
            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (joinTableAnnotation != null) {
                if (joinTableAnnotation.getValues().get("name") != null) {
                    CtExpression name = joinTableAnnotation.getValue("name");
                    String joinTableName = getValueAsString(name);

                    Classes classes = new Classes();
                    classes.addClass(clazz.getSimpleName());
                    CtType fieldType = field.getType().getTypeDeclaration();
                    classes.addClass(fieldType.getSimpleName());
                    SpringDataJPACollector.tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
                }
            }
        }
    }

    static void parseAtManyToMany(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName) {
        CtAnnotation manyToManyAnnotation = getAnnotation(fieldAnnotations, "ManyToMany");
        if (manyToManyAnnotation != null) {
            Map manyToManyValues = ((CtAnnotationImpl) manyToManyAnnotation).getElementValues();
            if (manyToManyValues.get("mappedBy") == null) { // new table

                String joinTableName = "";
                CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
                if (joinTableAnnotation != null) {
                    if (joinTableAnnotation.getValues().get("name") != null) {
                        CtExpression name = joinTableAnnotation.getValue("name");
                        joinTableName = getValueAsString(name);
                    }
                }
                Classes classes = new Classes();
                classes.addClass(clazz.getSimpleName());

                // @ManyToMany fields won't be HashMaps, thus, get(0), and the Type will always be an Entity
                CtType fieldType = field.getType().getActualTypeArguments().get(0).getTypeDeclaration();
                classes.addClass(fieldType.getSimpleName());

                if (joinTableName.equals("")) {
                    joinTableName = tableName + "_" + getEntityTableName(fieldType);
                }
                SpringDataJPACollector.tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
            }
        }
    }

    static void parseAtOneToMany(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String tableName, Set<String> allDomainEntities) {
        CtAnnotation oneToManyAnnotation = getAnnotation(fieldAnnotations, "OneToMany");
        if (oneToManyAnnotation != null) {
            Map annotationValues = ((CtAnnotationImpl) oneToManyAnnotation).getElementValues();
            CtAnnotation joinColumnAnnotation = getAnnotation(fieldAnnotations, "JoinColumn");
            CtAnnotation joinTableAnnotation = getAnnotation(fieldAnnotations, "JoinTable");
            if (annotationValues.get("mappedBy") == null && joinColumnAnnotation == null
                    || joinTableAnnotation != null) { // new table

                Classes classes = new Classes();
                classes.addClass(clazz.getSimpleName());

                CtType fieldType = null;
                List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
                for (CtTypeReference type : actualTypeArguments) {

                    fieldType = type.getTypeDeclaration();

                    if (allDomainEntities.contains(fieldType.getSimpleName()))
                        classes.addClass(fieldType.getSimpleName());
                }

                String joinTableName = "";

                if (joinTableAnnotation != null) {
                    if (joinTableAnnotation.getValues().get("name") != null) {
                        CtExpression name = joinTableAnnotation.getValue("name");
                        joinTableName = getValueAsString(name);
                    }
                }

                if (joinTableName.equals("")) {
                    joinTableName = tableName + "_" + getEntityTableName(fieldType);
                }

                SpringDataJPACollector.tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
            }
        }
    }

    static void parseAtElementCollection(CtType<?> clazz, CtField field, List<CtAnnotation<? extends Annotation>> fieldAnnotations, String entityName, Set<String> allDomainEntities) {
        CtAnnotation elementCollectionAnnotation = getAnnotation(fieldAnnotations, "ElementCollection");
        if (elementCollectionAnnotation != null) {
            String joinTableName = "";
            CtAnnotation collectionTableAnnotation = getAnnotation(fieldAnnotations, "CollectionTable");
            if (collectionTableAnnotation != null) {
                CtExpression name = collectionTableAnnotation.getValue("name");
                joinTableName = getValueAsString(name);
            }
            Classes classes = new Classes();
            classes.addClass(clazz.getSimpleName());

            List<CtTypeReference<?>> actualTypeArguments = field.getType().getActualTypeArguments();
            for (CtTypeReference type : actualTypeArguments) {

                CtType fieldType = type.getTypeDeclaration();

                if (allDomainEntities.contains(fieldType.getSimpleName()))
                    classes.addClass(fieldType.getSimpleName());
            }

            if (joinTableName.equals("")) {
                joinTableName = entityName + "_" + field.getSimpleName();
            }
            SpringDataJPACollector.tableClassesAccessedMap.put(joinTableName.toUpperCase(), classes);
        }
    }

    static void parseAtNamedNativeQuery(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotation namedNativeQueryAnnotation = getAnnotation(clazzAnnotations, "NamedNativeQuery");
        if (namedNativeQueryAnnotation != null) {
            parseNamedQuery(namedNativeQueryAnnotation, true);
        }
    }

    static void parseAtNamedNativeQueries(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotationImpl namedNativeQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "NamedNativeQueries");
        if (namedNativeQueriesAnnotation != null) {
            List namedNativeQueryList = ((CtNewArray) namedNativeQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedNativeQueryObject : namedNativeQueryList) {
                CtAnnotation namedNativeQueryElement = ((CtAnnotation) namedNativeQueryObject);
                parseNamedQuery(namedNativeQueryElement, true);
            }
        }
    }

    static void parseAtNamedQuery(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotation namedQueryAnnotation = (CtAnnotation) getAnnotation(clazzAnnotations, "NamedQuery");
        if (namedQueryAnnotation != null) {
            parseNamedQuery(namedQueryAnnotation, false);
        }
    }

    static void parseAtNamedQueries(List<CtAnnotation<? extends Annotation>> clazzAnnotations) {
        CtAnnotationImpl namedQueriesAnnotation = (CtAnnotationImpl) getAnnotation(clazzAnnotations, "SpringDataJPACollector.NamedQueries");
        if (namedQueriesAnnotation != null) {
            List namedQueryList = ((CtNewArray) namedQueriesAnnotation.getElementValues().get("value")).getElements();

            for (Object namedQueryObject : namedQueryList) {
                CtAnnotation namedQueryElement = ((CtAnnotation) namedQueryObject);
                parseNamedQuery(namedQueryElement, false);
            }
        }
    }

    private static void parseNamedQuery(CtAnnotation namedQueryAnnotation, boolean isNative) {
        Map queryValues = ((CtAnnotationImpl) namedQueryAnnotation).getElementValues();
        String name = getValueAsString(queryValues.get("name"));
        String query = getValueAsString(queryValues.get("query"));
        SpringDataJPACollector.namedQueries.add(new Query(name, query, isNative));
    }

    static String parseAtTableAndAtInheritance(CtType clazz) {
        boolean isSingleTable = false;
        CtType<?> superClassType = null;
        if (clazz.getSuperclass() != null) {
            superClassType = clazz.getSuperclass().getTypeDeclaration();
            if (superClassType != null) {
                CtAnnotation<? extends Annotation> inheritanceAnnotation =
                        getAnnotation(superClassType.getAnnotations(), "Inheritance");
                if (inheritanceAnnotation != null) {
                    isSingleTable = inheritanceAnnotation.getValue("strategy").toString().contains("SINGLE_TABLE");
                }
            }
        }
        Classes classes = new Classes();
        String tableName;
        if (isSingleTable) {
            tableName = getEntityTableName(superClassType);
            // table is not created (= superclass table name)
        }
        else {
            tableName = getEntityTableName(clazz);
            classes.addClass(clazz.getSimpleName());
            SpringDataJPACollector.tableClassesAccessedMap.put(tableName.toUpperCase(), classes);
        }
        return tableName;
    }

    static String parseAtEntity(CtType clazz, CtAnnotation atEntityAnnotation) {
        String entityName = clazz.getSimpleName();

        Object name = ((CtAnnotationImpl) atEntityAnnotation).getElementValues().get("name");
        if (name != null)
            entityName = getValueAsString(name);

        SpringDataJPACollector.entityClassNameMap.put(entityName, clazz);

        return entityName;
    }

    private static String getEntityTableName(CtType clazz) {
        if (clazz == null) return "";

        String tableName = clazz.getSimpleName();
        List<CtAnnotation<? extends Annotation>> annotations = clazz.getAnnotations();
        // ------------------- @Table -----------------------
        CtAnnotation tableAnnotation = getAnnotation(annotations, "Table");
        if (tableAnnotation != null) {
            Object name = ((CtAnnotationImpl) tableAnnotation).getElementValues().get("name");
            if (name != null) {
                String redefinedTableName = getValueAsString(name);
                tableName = redefinedTableName.trim();
            }

        }
        return tableName;
    }

    public static CtAnnotation<? extends Annotation> getAnnotation(List<CtAnnotation<? extends Annotation>> annotations, String annotationName) {
        for (CtAnnotation<? extends Annotation> a : annotations) {
            if ( a.getAnnotationType().getSimpleName().equals(annotationName) )
                return a;
        }
        return null;
    }

    static String getValueAsString(Object name) {
        if (name instanceof CtLiteral)
            return (String) ((CtLiteral) name).getValue();
        else if (name instanceof CtFieldRead)
            return getValueAsString(((CtFieldRead) name).getVariable().getDeclaration().getAssignment());
        else if (name instanceof CtBinaryOperatorImpl)
            return getValueAsString(((CtBinaryOperatorImpl) name).partiallyEvaluate());
        else {
            System.err.println("Couldn't parse value! " + name.toString());
            System.exit(1);
            return null;
        }
    }
}
