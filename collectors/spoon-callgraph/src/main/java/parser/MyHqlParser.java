package parser;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.util.NodeTraverser;

import java.util.*;

public class MyHqlParser {

    private final String hql;
    private Set<QueryAccess> entitiesAccessed;
    private String mode;
    private HashMap<String, String> aliasMap; // alias -> EntityName

    public MyHqlParser(String hql) {
        entitiesAccessed = new HashSet<>();
        aliasMap = new HashMap<>();
        this.hql = hql;
    }

    public Set<QueryAccess> parse() throws TokenStreamException, RecognitionException {
        HqlParser parser = HqlParser.getInstance(hql);
        parser.statement();
        AST ast = parser.getAST();
        EntitiesAccessedWithinQueryCollector entitiesAccessedWithinQueryCollector = new EntitiesAccessedWithinQueryCollector();

        checkMode(ast.getText());

        // traverse AST and register tables/entities accessed and access mode
        new NodeTraverser(entitiesAccessedWithinQueryCollector).traverseDepthFirst(ast);

        // replace alias in accesses
        for (QueryAccess a : entitiesAccessed) {
            while (true) {
                boolean changed = false;
                String entityName = a.getName();

                int dotIndex = entityName.indexOf('.');
                if (dotIndex == -1) break;

                String startString = entityName.substring(0, dotIndex); // alias candidate
                String restString = entityName.substring(dotIndex);
                for (Map.Entry<String, String> entry : aliasMap.entrySet()) { // replace alias in queries by Class names
                    if (startString.equals(entry.getKey())) {
                        a.setName(entry.getValue() + restString);
                        changed = true;
                        break;
                    }
                }
                if (!changed) break;
            }
        }

        return entitiesAccessed;
    }

    private class EntitiesAccessedWithinQueryCollector implements NodeTraverser.VisitationStrategy {

        private String lastIdentifierSeen;
        private List<AST> visited;

        EntitiesAccessedWithinQueryCollector() {
            this.visited = new ArrayList<>();
        }

        @Override
        public void visit(AST node) {
            int type = node.getType();
            if (visited.contains(node)) {
                visited.remove(node);
                return;
            }
            checkMode(node.getText()); // Sub-queries may change the access mode
            if (type == HqlSqlTokenTypes.COLON || type == HqlSqlTokenTypes.PARAM ) {
                visited.add(node.getFirstChild()); // ez way to ignore the parameter node
            }
            else if (type == HqlSqlTokenTypes.DOT) {
                String ident = parseDot(node.getFirstChild());
                entitiesAccessed.add(new QueryAccess(ident, mode));
                lastIdentifierSeen = ident;
            }
            else if (type == HqlSqlTokenTypes.IDENT) {
                entitiesAccessed.add(new QueryAccess(node.toString(), mode));
                lastIdentifierSeen = node.toString();
            }
            else if (type == HqlSqlTokenTypes.ALIAS) {
                aliasMap.put(node.toString(), lastIdentifierSeen);
            }
        }

        private String parseDot(AST node) {
            StringBuilder idName = new StringBuilder();

            parseDotAux(node, idName);
            idName.setLength(idName.length()-1);
            return idName.toString();
        }

        private void parseDotAux(AST node, StringBuilder idName) {
            if (node == null)
                return;
            else if (node.getType() == HqlSqlTokenTypes.IDENT)
                idName.append(node.toString()).append(".");

            parseDotAux(node.getFirstChild(), idName);
            parseDotAux(node.getNextSibling(), idName);
            this.visited.add(node);
        }
    }

    private void checkMode(String node) {
        switch (node) {
            case "update":
            case "delete":
            case "insert":
                mode = "W";
                break;
            case "query":
                mode = "R";
                break;
            default:
                break;
        }
    }
}
