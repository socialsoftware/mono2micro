package collector.jpa;

import collector.utils.Access;
import collector.utils.Classes;
import collector.utils.DomainEntity;
import collector.utils.Function;
import collector.utils.Query;
import collector.utils.QueryAccess;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ReposityMethodUtils {
    private static final Logger logger = Logger.getLogger(ReposityMethodUtils.class.getName());

    private DomainEntity getEntityByName(Map<String, DomainEntity> locationToEntityMap, String entityName) {
        for(Map.Entry<String, DomainEntity> entry : locationToEntityMap.entrySet()) {
            if (entry.getValue().getName().equals(entityName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Access getSpringDataRepositoryAccess(Function function, String methodName, DomainEntity entity) {
        String mode;
        // Read Access
        if (methodName.startsWith("find") ||
                methodName.startsWith("get") ||
                methodName.startsWith("exists") ||
                methodName.startsWith("read") ||
                methodName.startsWith("count")) {
            mode = "R";
        }
        // Write Access
        else {
            mode = "W";
        }
        return new Access(entity, function, mode);
    }

    public Query getNamedQuery(List<Query> namedQueriesList, String query) {
        for (Query q : namedQueriesList) {
            if (q.getName().equals(query)) return q;
        }
        return null;
    }

    public List<Access> parseNativeQuery(String sql, Map<String, Classes> tableClassesMap, Map<String, DomainEntity> locationToEntityMap, Function method) {
        // Remove quotes at the start and the end
        if (sql.startsWith("\"") && sql.endsWith("\"")) {
            sql = sql.substring(1, sql.length() - 1);
        }

        List<Access> accessesToReturn = new ArrayList<>();
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            TableNamesFinderExt tablesNamesFinder = new TableNamesFinderExt();
            tablesNamesFinder.getTableList(stmt); // run visitor, populate QueryAccesses
            ArrayList<QueryAccess> accesses = tablesNamesFinder.getAccesses();
            for (QueryAccess qa : accesses) {
                String tableName = qa.getName();
                Classes classes = tableClassesMap.get(parseTableName(tableName));
                if (classes == null) {
                    logger.warning("Exception on query: " + sql);
                    logger.warning("Table not found: " + tableName);
                    continue;
                }
                for (String typeName : classes.getListOfClasses()) {
                    String mode = qa.getMode();
                    if (mode == null)
                        mode = "R";
                    DomainEntity de = getEntityByName(locationToEntityMap, typeName);
                    if (de == null) continue;
                    accessesToReturn.add(new Access(de, method, mode));
                }
            }
        } catch (Exception e) {
            logger.warning("Native Query Exception on query: " + sql);
            logger.warning(e.getCause().getMessage());
        }
        return accessesToReturn;
    }

    public List<Access> parseHqlQuery(String hql, DomainEntity entity, Function method) {
        List<Access> accessesList = new ArrayList<>();
        try {
            Set<QueryAccess> accesses = new MyHqlParser(hql, entity.getName()).parse();
            for (QueryAccess a : accesses) {
                accessesList.add(new Access(entity, method, a.getMode()));
            }
        } catch (Exception e) {
            logger.warning("HQL Query Exception on query: " + hql);
            logger.warning(e.getMessage());
        }
        return accessesList;
    }


    private String parseTableName(String tableName) {
        return tableName.toUpperCase().replace("`", "");
    }

}
