package collector.jpa;

import collector.utils.Access;
import collector.utils.Classes;
import collector.utils.DomainEntity;
import collector.utils.Method;
import collector.utils.Query;
import collector.utils.QueryAccess;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReposityMethodUtils {

    public Access getSpringDataRepositoryAccess(Method method, DomainEntity entity) {
        String mode;
        // Read Access
        if (method.getMethodName().startsWith("find") ||
                method.getMethodName().startsWith("get") ||
                method.getMethodName().startsWith("exists") ||
                method.getMethodName().startsWith("read") ||
                method.getMethodName().startsWith("count")) {
            mode = "R";
        }
        // Write Access
        else {
            mode = "W";
        }
        return new Access(entity, method, mode);
    }

    public Query getNamedQuery(List<Query> namedQueriesList, String query) {
        for (Query q : namedQueriesList) {
            if (q.getName().equals(query)) return q;
        }
        return null;
    }

    public List<Access> parseNativeQuery(String sql, Map<String, Classes> tableClassesMap, Map<String, DomainEntity> nameToEntityMap, Method method) {
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
                    System.err.println("Exception on query: " + sql);
                    System.err.println("Table not found: " + tableName);
                    continue;
                }
                for (String typeName : classes.getListOfClasses()) {
                    String mode = qa.getMode();
                    if (mode == null)
                        mode = "R";
                    accessesToReturn.add(new Access(nameToEntityMap.get(typeName), method, mode));
                }
            }
        } catch (Exception e) {
            System.err.println("Native Query Exception on query: " + sql);
            System.err.println(e.getCause().getMessage());
        }
        return accessesToReturn;
    }

    public List<Access> parseHqlQuery(String hql, DomainEntity entity, Method method) {
        List<Access> accessesList = new ArrayList<>();
        try {
            Set<QueryAccess> accesses = new MyHqlParser(hql, entity.getName()).parse();
            for (QueryAccess a : accesses) {
                accessesList.add(new Access(entity, method, a.getMode()));
            }
        } catch (Exception e) {
            System.err.println("HQL Query Exception on query: " + hql);
            System.err.println(e.getMessage());
        }
        return accessesList;
    }


    private String parseTableName(String tableName) {
        return tableName.toUpperCase().replace("`", "");
    }

}
