package collector;

import collector.results.EntitySuperclass;
import collector.results.FileParser;
import collector.results.RepoAccesses;
import collector.utils.Access;
import collector.utils.Classes;
import collector.utils.DomainEntity;
import collector.utils.Method;
import collector.utils.Query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static collector.Constants.JSON_PATH;
import static collector.FilesEnum.ENTITY_SUPERCLASS;
import static collector.FilesEnum.FIELD_ANNOTATIONS;
import static collector.FilesEnum.METHOD_ACCESSES;
import static collector.FilesEnum.NAMED_QUERIES;
import static collector.FilesEnum.PREV_CALLEE;
import static collector.FilesEnum.REPO_ACCESSES;
import static collector.utils.TypeUtils.getTypes;

public class SpringDataJPACollector extends AbstractStructuralCollector {
    // Map table name to classes
    private Map<String, Classes> tableClassesAccessedMap;
    // Named query list
    private List<Query> namedQueriesList;

    public SpringDataJPACollector(String codeQLDbPath, String projectName, boolean runQueries) {
        super(codeQLDbPath, projectName, runQueries, new FileParser());
        this.tableClassesAccessedMap = new HashMap<>();
        this.namedQueriesList = new ArrayList<>();
        this.accessMap = new HashMap<>();
        SPECIFIC_FRAMEWORK_PATH = Constants.SPRING_DATA_JPA;
    }

    @Override
    public void generateIdEntityFiles() {
        try {
            super.generateIdEntityFiles();

            // Read entitySuperclasses file as a list
            List<EntitySuperclass> entitySuperclasses = fileParser.readEntitySuperclass(
                    mapper.readTree(new File(JSON_PATH + ENTITY_SUPERCLASS.file)));

            // Fill tableClassesAccessed map
            for (EntitySuperclass es : entitySuperclasses) {
                Classes classes = new Classes();
                classes.addClass(es.getEntity());
                tableClassesAccessedMap.put(es.getTableName().toUpperCase(), classes);
            }
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    @Override
    public void generateAccessesFile() {
        try {
            // Fill in named queries list
            buildNamedQueriesList();
            // Fill in table classes map
            buildTableClassesMap();
            // Fill in repository accesses map
            buildRepoMethodAccesses();

            // Call on super to generate accesses
            super.generateAccessesFile();
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    @Override
    protected void checkForAccesses(String controllerMethodName, Method m) {
        try {
            if (accessMap.containsKey(m.getFullMethodName())) {
                List<Access> accesses = accessMap.getOrDefault(m.getFullMethodName(), new ArrayList<>());

                for (Access access : accesses) {
                    if (access.getEntity().isMappedSuperclass()) {
                        String prevCallee = fileParser.getPrevCalleeByLocation(mapper.readTree(new File(JSON_PATH + PREV_CALLEE.file)),
                                m.getCallLocation());
                        addEntitySequenceAccess(controllerMethodName, nameToEntityMap.get(prevCallee).getId(), access.getMode());
                    } else {
                        addEntitySequenceAccess(controllerMethodName, access.getEntity().getId(), access.getMode());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing JSON files: " + e.getMessage());
        }
    }

    private void buildRepoMethodAccesses() throws IOException {
        // Read repoAccesses as a list
        List<RepoAccesses> repoAccesses = fileParser.readRepoAccesses(mapper.readTree(new File(JSON_PATH + REPO_ACCESSES.file)));

        for (RepoAccesses ra : repoAccesses) {

            // Ignore repository methods with accesses already
            if (accessMap.containsKey(ra.getFullName())) {
                continue;
            }

            Access access;
            DomainEntity entity = nameToEntityMap.get(ra.getEntity());
            Method method = new Method(ra.getTargetClass(), ra.getTargetMethod(), ra.getCallLocation());
            String methodName = ra.getFullName();

            // Repository method was not declared
            if (!ra.isDeclared()) {
                access = reposityMethodUtils.getSpringDataRepositoryAccess(
                        method,
                        nameToEntityMap.get(ra.getEntity())
                );
                accessMap.computeIfAbsent(methodName, k -> new ArrayList<>()).add(access);
            } else {
                // @Query annotation is present
                if (!ra.getAnnotation().equals("null")) {
                    // Named query being used
                    if (!ra.getQueryName().equals("null") && !ra.getQueryName().equals("\"\"")) {
                        Query q = reposityMethodUtils.getNamedQuery(namedQueriesList, ra.getQueryName());
                        if (q == null) {
                            System.err.println("Couldn't find NamedQuery " + ra.getQueryName());
                            continue;
                        }
                        else {
                            if (q.isNative()) {
                                reposityMethodUtils.parseNativeQuery(q.getValue(), tableClassesAccessedMap, nameToEntityMap, method)
                                        .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                                .add(access1));
                            } else {
                                reposityMethodUtils.parseHqlQuery(q.getValue(), entity, method)
                                        .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                                .add(access1));
                            }
                        }
                        continue;
                    }

                    if (ra.isNative()) {
                        reposityMethodUtils.parseNativeQuery(ra.getAnnotation(), tableClassesAccessedMap, nameToEntityMap, method)
                                .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                        .add(access1));
                    } else {
                        reposityMethodUtils.parseHqlQuery(ra.getAnnotation(), entity, method)
                                .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                        .add(access1));
                    }
                } else { // No @Query annotation present
                    Query q = reposityMethodUtils.getNamedQuery(namedQueriesList, methodName);

                    if (q == null) {
                        access = reposityMethodUtils.getSpringDataRepositoryAccess(
                                method,
                                nameToEntityMap.get(ra.getEntity())
                        );
                        accessMap.computeIfAbsent(methodName, k -> new ArrayList<>()).add(access);
                    } else {
                        if (q.isNative()) {
                            reposityMethodUtils.parseNativeQuery(q.getValue(), tableClassesAccessedMap, nameToEntityMap, method)
                                    .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                            .add(access1));
                        } else {
                            reposityMethodUtils.parseHqlQuery(q.getValue(), entity, method)
                                    .forEach(access1 -> accessMap.computeIfAbsent(methodName, k -> new ArrayList<>())
                                            .add(access1));
                        }
                    }
                }
            }
        }
    }

    private void buildNamedQueriesList() throws IOException {
        // Read namedQueries file as a list
        fileParser.readNamedQueries(mapper.readTree(new File(JSON_PATH + NAMED_QUERIES.file)))
            .forEach(q -> namedQueriesList.add(new Query(q.getQueryName(), q.getQueryValue(), q.isNative())));
    }

    private void buildTableClassesMap() throws IOException {
        // Read fieldAnnotations as list
        fileParser.readFieldAnnotations(mapper.readTree(new File(JSON_PATH + FIELD_ANNOTATIONS.file)))
            .forEach(f -> {
                Classes classes = new Classes();
                classes.addClass(f.getDeclaringClass());

                List<String> typeNames = getTypes(f.getType());
                for (String type : typeNames) {
                    classes.addClass(type);
                }

                if (!f.getJoinTable().equals("null")) {
                    tableClassesAccessedMap.put(f.getJoinTable().toUpperCase(), classes);
                } else {
                    String typeForName = typeNames.get(typeNames.size() - 1);
                    String typeTableName = nameToEntityMap.get(typeForName).getTableName();
                    String declaringTypeTableName = nameToEntityMap.get(f.getDeclaringClass()).getTableName();
                    String tableName = declaringTypeTableName + "_" + typeTableName;
                    tableClassesAccessedMap.put(tableName.toUpperCase(), classes);
                }
            });
    }

}
