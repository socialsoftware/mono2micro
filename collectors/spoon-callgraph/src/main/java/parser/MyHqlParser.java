package parser;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.util.NodeTraverser;

import java.util.*;

// Assuming no full qualified path names inside From clauses
// TODO verificar os selects (select * = access to every field)
public class MyHqlParser {

    private final String hql;
    private Set<HqlAccess> entitiesAccessed;
    private String mode;
    private HashMap<String, String> aliasMap; // alias -> EntityName

    public MyHqlParser(String hql) throws TokenStreamException, RecognitionException {
        entitiesAccessed = new HashSet<>();
        aliasMap = new HashMap<>();
        this.hql = hql;
    }

    public Set<HqlAccess> parse() throws TokenStreamException, RecognitionException {
        HqlParser parser = HqlParser.getInstance(hql);
        parser.statement();
        AST ast = parser.getAST();
        EntitiesAccessedWithinQueryCollector entitiesAccessedWithinQueryCollector = new EntitiesAccessedWithinQueryCollector();

        checkMode(ast.getText());

        new NodeTraverser(entitiesAccessedWithinQueryCollector).traverseDepthFirst(ast);


        for (HqlAccess a : entitiesAccessed) {
            while (true) {
                boolean changed = false;
                String entityName = a.getEntityName();

                int dotIndex = entityName.indexOf('.');
                if (dotIndex == -1) break;

                String startString = entityName.substring(0, dotIndex); // alias candidate
                String restString = entityName.substring(dotIndex);
                for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
                    if (startString.equals(entry.getKey())) {
                        a.setEntityName(entry.getValue() + restString);
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
            checkMode(node.getText());
            if (type == HqlSqlTokenTypes.COLON || type == HqlSqlTokenTypes.PARAM ) {
                visited.add(node.getFirstChild()); // ez way to ignore the parameter node
            }
            else if (type == HqlSqlTokenTypes.DOT) {
                String ident = parseDot(node.getFirstChild());
                entitiesAccessed.add(new HqlAccess(ident, mode));
                lastIdentifierSeen = ident;
                System.out.println(ident);
            }
            else if (type == HqlSqlTokenTypes.IDENT) {
                entitiesAccessed.add(new HqlAccess(node.toString(), mode));
                lastIdentifierSeen = node.toString();
            }
            else if (type == HqlSqlTokenTypes.ALIAS) {
                System.out.println(node.toString() + " is alias of " + lastIdentifierSeen);
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

    public static void main(String[] args) throws RecognitionException, TokenStreamException {
//        new HQLParser("update Student s set s.marks=50 where (select max(s.id) from Student s)");

        new MyHqlParser("Select p.name, s.name FROM Person p WHERE (FROM Student s)").parse();

//        new HQLParser("insert into Product(productId,proName,price) " +
//                "select i.itemId,i.itemName,i.itemPrice from Items i where i.itemId= ?");

//        new HQLParser("delete from Student s where (select x from A a)");

//        new HQLParser("select cust\n" +
//                "from Product prod,\n" +
//                "    Store store\n" +
//                "    inner join store.customers cust\n" +
//                "where prod.name = 'widget'\n" +
//                "    and store.location.name in ( 'Melbourne', 'Sydney' )\n" +
//                "    and prod = all elements(cust.currentOrder.lineItems)");
    }
}
