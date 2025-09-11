package collector.frameworks;

import collector.AbstractStructuralCollector;
import collector.Configuration;
import collector.jpa.queryresults.EntityAttributes;
import collector.jpa.queryresults.FieldAnnotations;
import collector.jpa.queryresults.NamedQueries;
import collector.jpa.queryresults.RepoAccesses;
import collector.utils.Access;
import collector.utils.Classes;
import collector.utils.DomainEntity;
import collector.utils.Function;
import collector.utils.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static collector.Constants.ENTITY_ATTRIBUTES;
import static collector.Constants.FIELD_ANNOTATIONS;
import static collector.Constants.NAMED_QUERIES;
import static collector.Constants.REPO_ACCESSES;
import static collector.utils.TypeUtils.getTypes;

public class SpringDataJPACollector extends AbstractStructuralCollector {
    private static final Logger logger = Logger.getLogger(SpringDataJPACollector.class.getName());

    // Map table name to classes
    private Map<String, Classes> tableClassesAccessedMap;
    // Named query list
    private List<Query> namedQueriesList;

    public SpringDataJPACollector(Configuration config) {
        super(config);
        this.tableClassesAccessedMap = new HashMap<>();
        this.namedQueriesList = new ArrayList<>();
    }

    @Override
    public void generateIdEntityFiles() {
        super.generateIdEntityFiles();

        // Fill tableClassesAccessed map
        for (EntityAttributes ea : fileParser.readEntityAttributes(getFile(ENTITY_ATTRIBUTES))) {
            Classes classes = new Classes();
            classes.addClass(ea.getEntityName());
            tableClassesAccessedMap.put(ea.getTableName().toUpperCase(), classes);

            // Update domain entities
            DomainEntity de = locationToEntityMap.get(ea.getEntityLocation());

            if (de == null) continue;
            // Update entity
            de.setMappedSuperclass(ea.getMappedSuperclass());
            de.setTableName(ea.getTableName());
            locationToEntityMap.put(ea.getEntityLocation(), de);
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
            logger.warning("Error processing JSON files: " + e.getMessage());
        }
    }

    @Override
    public void checkForAccesses(String controllerMethodName, Function m) {
        List<Access> accesses = accessMap.getOrDefault(m.getFunctionId(), new ArrayList<>());

        for (Access access : accesses) {
            if (access.getEntity().isMappedSuperclass()) {
                String qualifierDomainLocation = getQualifierEntityLocationByCallLocation(m.getCallLocation());
                // Register access to domain entity
                addEntitySequenceAccess(
                    controllerMethodName,
                    locationToEntityMap.get(qualifierDomainLocation).getId(),
                    access.getMode()
                );
            } else {
                addEntitySequenceAccess(controllerMethodName, access.getEntity().getId(), access.getMode());
            }
        }
    }

    private void buildRepoMethodAccesses() throws IOException {
        for (RepoAccesses ra : fileParser.readRepoAccesses(getFile(REPO_ACCESSES))) {

            // Ignore repository methods with accesses already registered
            if (accessMap.containsKey(ra.getFunctionId())) {
                continue;
            }

            Access access;
            DomainEntity entity = locationToEntityMap.get(ra.getEntityLocation());
            Function method = new Function(ra.getFunctionId(), ra.getCallLocation());
            String methodName = ra.getMethodName();

            // Repository method was not declared
            if (!ra.isDeclared()) {
                access = reposityMethodUtils.getSpringDataRepositoryAccess(
                        method,
                        ra.getMethodName(),
                        locationToEntityMap.get(ra.getEntityLocation())
                );
                accessMap.computeIfAbsent(methodName, k -> new ArrayList<>()).add(access);
            } else {
                // @Query annotation is present
                if (!ra.getAnnotation().equals("null")) {
                    // Named query being used
                    if (!ra.getQueryName().equals("null") && !ra.getQueryName().equals("\"\"")) {
                        Query q = reposityMethodUtils.getNamedQuery(namedQueriesList, ra.getQueryName());
                        if (q == null) {
                            logger.warning("Couldn't find NamedQuery " + ra.getQueryName());
                            continue;
                        }
                        else {
                            if (q.isNative()) {
                                reposityMethodUtils.parseNativeQuery(q.getValue(), tableClassesAccessedMap, locationToEntityMap, method)
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
                        reposityMethodUtils.parseNativeQuery(ra.getAnnotation(), tableClassesAccessedMap, locationToEntityMap, method)
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
                                ra.getMethodName(),
                                locationToEntityMap.get(ra.getEntityLocation())
                        );
                        accessMap.computeIfAbsent(methodName, k -> new ArrayList<>()).add(access);
                    } else {
                        if (q.isNative()) {
                            reposityMethodUtils.parseNativeQuery(q.getValue(), tableClassesAccessedMap, locationToEntityMap, method)
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
        for (NamedQueries nq : fileParser.readNamedQueries(getFile(NAMED_QUERIES))) {
            namedQueriesList.add(new Query(nq.getQueryName(), nq.getQueryValue(), nq.isNative()));
        }
    }

    private void buildTableClassesMap() throws IOException {
        for (FieldAnnotations fa : fileParser.readFieldAnnotations(getFile(FIELD_ANNOTATIONS))) {
            Classes classes = new Classes();
            classes.addClass(fa.getDeclaringType());

            List<String> typeNames = getTypes(fa.getType());
            for (String type : typeNames) {
                classes.addClass(type);
            }

            if (!fa.getJoinTable().equals("null")) {
                tableClassesAccessedMap.put(fa.getJoinTable().toUpperCase(), classes);
            } else {
                String typeForName = typeNames.get(typeNames.size() - 1);
                String declaringTypeTableName = locationToEntityMap.get(fa.getEntityLocation()).getTableName();
                DomainEntity typeEntity = getEntityByName(typeForName);
                if (typeEntity != null) {
                    String typeTableName = typeEntity.getTableName();
                    String tableName = declaringTypeTableName + "_" + typeTableName;
                    tableClassesAccessedMap.put(tableName.toUpperCase(), classes);
                }
            }
        }
    }

}
