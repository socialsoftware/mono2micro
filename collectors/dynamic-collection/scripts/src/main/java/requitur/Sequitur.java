package requitur;

import domain.Access;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requitur.content.Content;
import requitur.content.StringContent;
import requitur.content.TraceElementContent;

import java.io.*;
import java.util.*;

public class Sequitur {

   private static final Logger LOG = LoggerFactory.getLogger(Sequitur.class);

   Map<Digram, Digram> digrams = new HashMap<>();
   Map<String, Rule> rules = new HashMap<>();
   List<Rule> unusedRules = new LinkedList<>();
   private Symbol startSymbol = new Symbol(this, (StringContent) null);
   Symbol lastSymbol = startSymbol;
   private int ruleIndex = 0;

   public boolean isEmpty() {
      return digrams.size() == 0;
   }

   Digram link(final Symbol start, final Symbol end) {
      start.setSuccessor(end);
      end.setPredecessor(start);

      if (start.getValue() != null && end.getValue() != null) {
         final Digram newDigram = new Digram(start, end);
         handleDigram(newDigram);

         return newDigram;

      } else {
         return null;
      }
   }

   public void addElement(Content content) {
      Symbol symbol = new Symbol(this, content);
      // TraceStateTester.assureCorrectState(this);
      if (startSymbol == null) {
         startSymbol = symbol;
         lastSymbol = symbol;

      } else {
         lastSymbol.setSuccessor(symbol);
         symbol.setPredecessor(lastSymbol);
         lastSymbol = symbol;

         if (symbol.getPredecessor().getValue() != null) {
            final Digram digram = new Digram(symbol.getPredecessor(), symbol);
            handleDigram(digram);
         }
      }
      // TraceStateTester.assureCorrectState(this);
   }

   void handleDigram(final Digram digram) {
      final Digram existing = digrams.get(digram);

      if (existing != null) {
         if (digram.getStart() != existing.getEnd()) {
            if (existing.rule != null) {
               existing.rule.use(digram);

            } else {
               Rule rule;

               if (unusedRules.size() > 0) {
                  rule = unusedRules.remove(0);
                  rule.setDigram(existing);

               } else {
                  rule = new Rule(this, ruleIndex, existing);
                  ruleIndex++;
               }
               rules.put(rule.getName(), rule);

               rule.use(digram);
            }
         }

      } else {
         digrams.put(digram, digram);
      }
   }

   public List<Content> getTrace() {
      Symbol iterator = startSymbol.getSuccessor();
      final List<Content> trace = new ArrayList<>();

      while (iterator != null) {
         trace.add(iterator.getValue());
         iterator = iterator.getSuccessor();
      }

      return trace;
   }

   public List<Content> getUncompressedTrace() {
      Symbol iterator = startSymbol.getSuccessor();
      final List<Content> trace = new ArrayList<>();

      while (iterator != null) {
         for (int i = 0; i < iterator.getOccurrences(); i++) {
            trace.add(iterator.getValue());
         }
         iterator = iterator.getSuccessor();
      }

      return trace;
   }

   public Map<String, Rule> getRules() { return rules; }

   @Override
   public String toString() {
      return getTrace().toString();
   }

   public void addStringElement(final String stringElement) {
      addElement(new StringContent(stringElement));
   }

   public void addStringElements(final List<String> mytrace) {
      for (final String element : mytrace) {
         addElement(new StringContent(element));
      }
   }

   public void addTraceElement(final TraceElement traceElement) {
      addElement(new TraceElementContent(traceElement));
   }

   public void addTraceElements(final List<TraceElement> calls2) {
      for (final TraceElement element : calls2) {
         addElement(new TraceElementContent(element));
      }
   }

   public void addAccessElement(final Access access) {
      addElement(access);
   }

   public void addAccessElements(final List<Access> accessesList) {
      for (final Access access : accessesList) {
         addElement(access);
      }
   }

   public Symbol getStartSymbol() {
      return startSymbol;
   }

   public static List<String> getExpandedTrace(final File methodTraceFile) throws IOException {
      final List<String> trace1 = new ArrayList<>();

      try (BufferedReader br = new BufferedReader(new FileReader(methodTraceFile))) {
         String line;

         while ((line = br.readLine()) != null) {
            final List<String> elements = getCurrentValues(line, br).elements;
            final List<String> added = expandTraceElements(elements);
            trace1.addAll(added);
         }
      }
      return trace1;
   }

   public static List<String> expandTraceElements(final List<String> elements) {
      final List<String> added = new ArrayList<>();

      for (final String element : elements) {

         if (element.contains("(")) {
            final String parameters = element.substring(element.indexOf("(") + 2, element.length() - 2);
            final String[] splitted = parameters.split(",");
            StringBuilder withoutFQN = new StringBuilder("(");

            for (final String parameter : splitted) {
               withoutFQN.append(parameter.substring(parameter.lastIndexOf('.') + 1).trim()).append(",");
            }

            withoutFQN = new StringBuilder(withoutFQN.substring(0, withoutFQN.length() - 1) + ")");
            added.add(element.substring(0, element.indexOf("(")) + withoutFQN);

         } else {
            added.add(element);
         }
      }
      return added;
   }

   static class Return {
      int readLines = 1;
      List<String> elements = new ArrayList<>();
   }

   public static Return getCurrentValues(String line, final BufferedReader reader) throws IOException {
      final Return current = new Return();
      final String trimmedLine = line.trim();

      if (line.matches("[ ]*[0-9]+ x [#]?[0-9]* \\([0-9]+\\)")) {
         final String[] parts = trimmedLine.split(" ");
         final int count = Integer.parseInt(parts[0]);
         final int length = Integer.parseInt(parts[3].replaceAll("[\\(\\)]", ""));
         final List<String> subList = new ArrayList<>();

         for (int i = 0; i < length;) {
            line = reader.readLine();
            final Return lines = getCurrentValues(line, reader);
            current.readLines += lines.readLines;
            i += lines.readLines;
            subList.addAll(lines.elements);
         }

         for (int i = 0; i < count; i++) {
            current.elements.addAll(subList);
         }

      } else if (line.matches("[ ]*[0-9]+ x .*$")) {
         final String method = trimmedLine.substring(trimmedLine.indexOf("x") + 2);
         final String countString = trimmedLine.substring(0, trimmedLine.indexOf("x") - 1);
         final int count = Integer.parseInt(countString);

         for (int i = 0; i < count; i++) {
            current.elements.add(method);
         }

      } else if (line.matches("[ ]*[#]?[0-9]* \\([0-9]+\\)")) {
         // Do nothing - just info element, that same trace pattern occurs twice
      } else {
         current.elements.add(trimmedLine);

      }
      return current;
   }

}
