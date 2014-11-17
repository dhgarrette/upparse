package net.ponvert.upparse.eval;

import java.util.*;

import net.ponvert.upparse.corpus.*;


/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class TreebankEval { 

  private final UnlabeledBracketSetCorpus gold;
  private final boolean checkTerms;
  private final String name;
  
  public TreebankEval(
      String _name, UnlabeledBracketSetCorpus _gold) {
    name = _name;
    gold = _gold;
    checkTerms = false;
  }
  
  public TreebankEval(OutputType type, UnlabeledBracketSetCorpus gold2) {
    this("Chunker-" + type, gold2);
  }

  public String getEvalName() { return name; }

  private static UnlabeledBracket[] difference(
      final Set<UnlabeledBracket> a, final UnlabeledBracket[] tp) {
    final Set<UnlabeledBracket> result = new HashSet<UnlabeledBracket>(a);
    result.removeAll(Arrays.asList(tp));
    UnlabeledBracket[] arr = result.toArray(new UnlabeledBracket[0]);
    Arrays.sort(arr);
    return arr;
  }
  
  private static UnlabeledBracket[] intersection(
      final Set<UnlabeledBracket> a, final Set<UnlabeledBracket> b) {
    final Set<UnlabeledBracket> result = new HashSet<UnlabeledBracket>(a);
    result.retainAll(b);
    final UnlabeledBracket[] arr = result.toArray(new UnlabeledBracket[0]);
    Arrays.sort(arr);
    return arr;
  }
  
  private class ParsingExperiment extends UnlabeledExperimentEval {
    
    private static final int 
    TP = 0, FP = 1, FN = 2,
    MAXLEN = 10,
    NO_OVERLAP = 0, TP_SUB = 1, TP_SUP = 2, CROSSING = 3, NA = 4;
    
    private final int[][][] counts = new int[3][5][MAXLEN+1];
    private final int[] len = new int[3];
    
    ParsingExperiment(
        final String _name, 
        final UnlabeledBracketSetCorpus output) {
      super(_name);
      assert output != null;
      assert gold.size() == output.size():
        "gold size: " + gold.size() + "; output size = " + output.size();
      
      for (int i = 0; i < gold.size(); i++) {
        final UnlabeledBracketSet 
          outpB = output.get(i),
          goldB = gold.get(i);
        
        // assert outpB.getTokens().length == goldB.getTokens().length;
        if (checkTerms)
          for (int j = 0; j < outpB.getTokens().length; j++)
            assert outpB.getTokens()[j] == goldB.getTokens()[j];
        
        final Set<UnlabeledBracket>
          outpBs = outpB.getBrackets(),
          goldBs = goldB.getBrackets();
        final UnlabeledBracket[]
          tp = intersection(outpBs, goldBs),
          fp = difference(outpBs, tp),
          fn = difference(goldBs, tp);
       
        for (UnlabeledBracket b: tp) {
          counts[TP][NA][lenNorm(b)]++;
          len[TP] += b.len();
        }
        
        for (UnlabeledBracket b: fp) {
          final int closestI = Arrays.binarySearch(tp, b);
          final int errorType;
          if (closestI < 0)
            errorType = NO_OVERLAP;
          else {
            final UnlabeledBracket closest = tp[closestI];
            
            if (closest.contains(b)) 
              errorType = TP_SUP;

            else if (b.contains(closest))
              errorType = TP_SUB;
            
            else
              errorType = CROSSING;
          }
          
          counts[FP][errorType][lenNorm(b)]++;
          len[FP] += b.len();
        }
      
        for (UnlabeledBracket b: fn) {
          final int closestI = Arrays.binarySearch(fp, b);
          final int errorType;
          
          if (closestI < 0)
            errorType = NO_OVERLAP;
          
          else {
            UnlabeledBracket closest = fp[closestI];
            
            if (closest.contains(b))
              errorType = TP_SUB;
            
            else if (b.contains(closest))
              errorType = TP_SUP;
            
            else
              errorType = CROSSING;
          }
          
          counts[FN][errorType][lenNorm(b)]++;
          len[FN] += b.len();
        }
      }
    }

    private int lenNorm(UnlabeledBracket b) {
      return Math.min(MAXLEN, b.len());
    }

    @Override
    public String getEvalName() {
      return TreebankEval.this.getEvalName();
    }

    @Override
    public int[][] getFNcounts() {
      return counts[FN];
    }

    @Override
    public int getFNlen() {
      return len[FN];
    }

    @Override
    public int[][] getFPcounts() {
      return counts[FP];
    }

    @Override
    public int getFPlen() {
      return len[FP];
    }

    @Override
    public int[][] getTPcounts() {
      return counts[TP];
    }

    @Override
    public int getTPlen() {
      return len[TP];
    }
  }

  public UnlabeledExperimentEval getExperiment(String name,
      UnlabeledBracketSetCorpus output) {
    return new ParsingExperiment(name, output);
  }

  public UnlabeledExperimentEval getExperiment(
      final String n,
      final UnlabeledBracketSet[] a) {
    return getExperiment(n, UnlabeledBracketSetCorpus.fromArrays(a));
  }
}
