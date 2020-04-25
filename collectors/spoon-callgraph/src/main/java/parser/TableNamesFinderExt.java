package parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

/*
* Same implementation as TablesNamesFinder.
* Just needed a getter for "tables", so i created getTables() and
* kept all the methods that updated it inside this class
* */
public class TableNamesFinderExt extends TablesNamesFinder {
    private ArrayList<QueryAccess> accesses;
    private String mode;

    public List<String> getTables() {
        return tables;
    }

    public ArrayList<QueryAccess> getAccesses() {
        return accesses;
    }

    @Override
    public void visit(Delete delete) {
        mode = "W";
        super.visit(delete);
    }

    @Override
    public void visit(Insert insert) {
        mode = "W";
        super.visit(insert);
    }

    @Override
    public void visit(Select select) {
        mode = "R";
        super.visit(select);
    }

    @Override
    public void visit(Update update) {
        mode = "W";
        super.visit(update);
    }

    @Override
    public void visit(Table tableName) {
        int sizeBefore = getTables().size();


        // ------------------ DO NOT TOUCH ------------------
        // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
        String tableWholeName = extractTableName(tableName);
        if (!otherItemNames.contains(tableWholeName.toLowerCase())
                && !tables.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // ------------------ DO NOT TOUCH ------------------


        int sizeAfter = getTables().size();
        if (sizeAfter > sizeBefore) { // new table was found
            String tName = extractTableName(tableName);
            QueryAccess queryAccess = new QueryAccess(tName, mode);
            accesses.add(queryAccess);
        }
    }


    /**
     * Initializes table names collector. Important is the usage of Column instances to find table
     * names. This is only allowed for expression parsing, where a better place for tablenames could
     * not be there. For complete statements only from items are used to avoid some alias as
     * tablenames.
     *
     * @param allowColumnProcessing
     */
    @Override
    protected void init(boolean allowColumnProcessing) {
        otherItemNames = new ArrayList<String>();
        tables = new ArrayList<String>();
        this.allowColumnProcessing = allowColumnProcessing;
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // ------------------ DO NOT TOUCH ------------------

        this.accesses = new ArrayList<>();
    }


    // ------------------ DO NOT TOUCH ------------------
    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    private List<String> tables;
    private boolean allowColumnProcessing = false;
    private List<String> otherItemNames;

    @Override
    public List<String> getTableList(Statement statement) {
        init(false);
        statement.accept(this);
        return tables;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     */
    @Override
    public List<String> getTableList(Expression expr) {
        init(true);
        expr.accept(this);
        return tables;
    }

    @Override
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getName().toLowerCase());
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(Column tableColumn) {
        if (allowColumnProcessing && tableColumn.getTable() != null && tableColumn.getTable().getName() != null) {
            visit(tableColumn.getTable());
        }
    }
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // ------------------ DO NOT TOUCH ------------------
}
