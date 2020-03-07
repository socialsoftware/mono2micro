package parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.StringTokenizer;

public class HQLParser_Obsolete {

    private StreamTokenizer _tokenizer;

    public static void main(String[] args) throws IOException {
        new HQLParser_Obsolete("select cat.name from DomesticCat cat where cat.name like 'fri%'");
    }

    public HQLParser_Obsolete(String query) throws IOException {
        initTokenizer(query);
        parseQuery();
    }

    private void initTokenizer(String string) {
        _tokenizer = new StreamTokenizer(new StringReader(string));
        _tokenizer.eolIsSignificant(false);
    }

    private void parseQuery() throws IOException {
        int token = _tokenizer.nextToken();
        switch (token) {
            case StreamTokenizer.TT_EOF:
                return;
            case StreamTokenizer.TT_WORD:
                if (_tokenizer.sval.equalsIgnoreCase("FROM")) {
                    parseFromClause();
                }
                break;
        }
    }

    private void parseFromClause() throws IOException {
        int token = _tokenizer.nextToken();
        switch (token) {
            case StreamTokenizer.TT_EOF:
                return;
            case StreamTokenizer.TT_WORD:
                if (_tokenizer.sval.equalsIgnoreCase("LEFT")) {
                    parseFromClause();
                }
                break;
        }
    }
}
