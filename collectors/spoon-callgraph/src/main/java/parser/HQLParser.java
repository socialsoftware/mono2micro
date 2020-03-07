package parser;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.util.NodeTraverser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HQLParser {

//    private SessionFactory sf;
//    private SessionFactoryImplementor sfi;
    private Set<String> entitiesAccessed;


    public HQLParser(String hql) throws TokenStreamException, RecognitionException {
//        sf = mock(SessionFactory.class, withSettings().extraInterfaces(SessionFactoryImplementor.class));
//        sfi = (SessionFactoryImplementor) sf;
        entitiesAccessed = new HashSet<>();

        HqlParser parser = HqlParser.getInstance(hql);
        parser.statement();
        EntitiesAccessedWithinQueryCollector entitiesAccessedWithinQueryCollector = new EntitiesAccessedWithinQueryCollector();
        new NodeTraverser(entitiesAccessedWithinQueryCollector).traverseDepthFirst(parser.getAST());
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
            if (type == HqlSqlTokenTypes.COLON || type == HqlSqlTokenTypes.PARAM ) {
                visited.add(node.getFirstChild()); // ez way to ignore the parameter node
            }
            if (type == HqlSqlTokenTypes.DOT) {
                String ident = parseDot(node.getFirstChild());
                entitiesAccessed.add(ident);
                lastIdentifierSeen = ident;
            }
            if (type == HqlSqlTokenTypes.IDENT) {
                entitiesAccessed.add(node.toString());
                lastIdentifierSeen = node.toString();
            }
            if (type == HqlSqlTokenTypes.ALIAS) {
                System.out.println(node.toString() + " is alias of " + lastIdentifierSeen);
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

    public static void main(String[] args) throws RecognitionException, TokenStreamException {

        new HQLParser("select cust\n" +
                "from Product prod,\n" +
                "    Store store\n" +
                "    inner join store.customers cust\n" +
                "where prod.name = 'widget'\n" +
                "    and store.location.name in ( 'Melbourne', 'Sydney' )\n" +
                "    and prod = all elements(cust.currentOrder.lineItems)");
    }
}
