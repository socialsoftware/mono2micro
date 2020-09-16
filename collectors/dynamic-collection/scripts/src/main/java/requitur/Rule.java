package requitur;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rule {
   private static final Logger LOG = LoggerFactory.getLogger(Rule.class);

   private final Sequitur sequitur;
   private final Symbol anchor;
   private String name;
   int usage;

   public Rule(Sequitur sequitur, int index, Digram existing) {
      this.sequitur = sequitur;
      anchor = new Symbol(this.sequitur, null, this);
      name = "#" + index;
      LOG.debug("Create rule: " + name);

      setDigram(existing);
   }

   public void setDigram(Digram existing) {
      if (usage != 0) {
         throw new RuntimeException("Trying to re-use an already in-use rule!");
      }

      final Symbol start = new Symbol(this.sequitur, existing.getStart());
      final Symbol end = new Symbol(this.sequitur, existing.getEnd());

      this.sequitur.digrams.remove(existing);
      this.sequitur.link(anchor, start);
      this.sequitur.link(end, anchor);

      start.setSuccessor(end);
      end.setPredecessor(start);

      final Digram ruleDigram = new Digram(start, end);
      ruleDigram.rule = this;

      useRule(existing, new Symbol(this.sequitur, this));

      sequitur.digrams.put(ruleDigram, ruleDigram);
   }

   public Symbol getAnchor() {
      return anchor;
   }

   public void decrementUsage() {
      usage--;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ReducedTraceElement> getElements() {
      final List<ReducedTraceElement> result = new LinkedList<>();
      Symbol iterator = anchor.getSuccessor();

      while (iterator != anchor) {
         LOG.trace(String.valueOf(iterator));
         result.add(new ReducedTraceElement(iterator.getValue(), iterator.getOccurrences()));
         iterator = iterator.getSuccessor();
      }

      return result;
   }

   public Symbol use(Digram digram) {
      if (usage == 0) {
         LOG.error("Trying to re-use unused rule " + name + " " + anchor.getValue() + " " + anchor.getSuccessor() + " " + anchor.getSuccessor().getSuccessor());
         throw new RuntimeException("Can not re-use unused rule!");
      }

      final Symbol ruleSymbol = new Symbol(this.sequitur, this);
      useRule(digram, ruleSymbol);

      return ruleSymbol;
   }

   private void useRule(Digram digram, Symbol ruleSymbol) {
      digram.rule = this;

      usage++;

      digram.getStart().getPredecessor().setSuccessor(ruleSymbol);
      ruleSymbol.setPredecessor(digram.getStart().getPredecessor());

      if (digram.getEnd().getSuccessor() != null) {
         digram.getEnd().getSuccessor().setPredecessor(ruleSymbol);
         ruleSymbol.setSuccessor(digram.getEnd().getSuccessor());

      } else {
         this.sequitur.lastSymbol = ruleSymbol;
      }

      // Remove wrong Digrams
      if (digram.getStart().getPredecessor().getValue() != null) {
         final Digram prevDigram = new Digram(digram.getStart().getPredecessor(), digram.getStart());
         final Digram oldDigram = sequitur.digrams.get(prevDigram);

         if (oldDigram.getStart() == digram.getStart().getPredecessor() && oldDigram.getEnd() == digram.getStart()) {
            this.sequitur.digrams.remove(oldDigram);
         }

         final Digram newDigram = new Digram(digram.getStart().getPredecessor(), ruleSymbol);
         this.sequitur.handleDigram(newDigram);

         if (newDigram.getStart().getPredecessor() == newDigram.getEnd().getSuccessor()) {
            newDigram.rule = newDigram.getEnd().getSuccessor().getRule();
         }
      }
      if (digram.getEnd().getSuccessor() != null && digram.getEnd().getSuccessor().getValue() != null) {
         final Digram sucDigram = new Digram(digram.getEnd(), digram.getEnd().getSuccessor());
         this.sequitur.digrams.remove(sucDigram);

         if (digram.getEnd().getSuccessor().getSuccessor() != null) {
            final Digram overlappingDigram = new Digram(digram.getEnd().getSuccessor(), digram.getEnd().getSuccessor().getSuccessor());

            if (overlappingDigram.equals(sucDigram)) {
               this.sequitur.digrams.put(overlappingDigram, overlappingDigram);
            }
         }

         final Digram newDigram = new Digram(ruleSymbol, digram.getEnd().getSuccessor());
         this.sequitur.handleDigram(newDigram);

         if (newDigram.getStart().getPredecessor() == newDigram.getEnd().getSuccessor()) {
            newDigram.rule = newDigram.getEnd().getSuccessor().getRule();
         }
      }

      digram.getStart().decrementUsage(this);
      digram.getEnd().decrementUsage(this);
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder(name + " -> [");
      Symbol iterator = anchor.getSuccessor();

      while (iterator != anchor && result.length() < 10000) {
         if (iterator.getOccurrences() == 1) {
            result.append(iterator.getValue()).append(" ");

         } else {
            result.append(iterator.getOccurrences()).append(" x ").append(iterator.getValue()).append(" ");
         }

         iterator = iterator.getSuccessor();
      }
      return result + "]";
   }

   public int getUsage() {
      return usage;
   }
}