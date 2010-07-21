package upparse.corpus;

import java.io.*;
import java.util.*;

/**
 * @author ponvert@mail.utexas.edu (Elias Ponvert)
 */
public class ChunkedSegmentedCorpus implements Corpus {

  /** Utilities to convert ChunkedSegmentedCorpus into a set of unlabeled 
   * bracket sets */
  public interface BracketConv {
    Collection<UnlabeledBracket> conv(int[][][] s);
  }
  
  private final int[][][][] corpus;
  final Alpha alpha;
  
  private ChunkedSegmentedCorpus(final int[][][][] _corpus, final Alpha _alpha) {
    corpus = _corpus;
    alpha = _alpha;
  }

  public int[][][][] getArrays() {
    return corpus;
  }

  /** Utility to convert this corpus into sets of just the chunks */
  private static BracketConv CHUNKCONV = new BracketConv() {
    @Override
    public Collection<UnlabeledBracket> conv(int[][][] s) {
      List<UnlabeledBracket> b = new ArrayList<UnlabeledBracket>();
      int m = 0;
      for (int[][] seg: s)
        for (int[] chunk: seg) {
          if (chunk.length > 1)
            b.add(new UnlabeledBracket(m, m + chunk.length));
          m += chunk.length;
        }
      
      return b;
    }
  };

  public UnlabeledBracketSet[] asChunked() {
    return conv(CHUNKCONV);
  }
  
  /** Utility to convert this corpus into a right-branching baseline */ 
  private static BracketConv RBCONV = new BracketConv() {
    @Override
    public Collection<UnlabeledBracket> conv(int[][][] s) {
      List<UnlabeledBracket> b = new ArrayList<UnlabeledBracket>();
      int m = 0;
      for (int[][] seg: s) {
        final int seglen = segLen(seg);
        if (seglen > 1) {
          b.add(new UnlabeledBracket(m, m + seglen));
          if (seg.length > 1) {
            for (int[] chunk: seg) {
              for (int n = 0; n < chunk.length - 2; n++)
                b.add(new UnlabeledBracket(m + n, m + chunk.length));
              m += chunk.length;
            }
          }
        }
      }
      
      return b;
    }
  };
  
  public UnlabeledBracketSet[] asRB() {
    return conv(RBCONV);
  }
  
  private static BracketConv FLATCONV = new BracketConv() {
    @Override
    public Collection<UnlabeledBracket> conv(int[][][] s) {
      List<UnlabeledBracket> b = new ArrayList<UnlabeledBracket>();
      int m = 0;
      for (int[][] seg: s) {
        final int seglen = segLen(seg);
        if (seglen > 1) {
          b.add(new UnlabeledBracket(m, m + seglen));
          if (seg.length > 1) {
            for (int[] chunk: seg) {
              if (chunk.length > 1)
                b.add(new UnlabeledBracket(m, m + chunk.length));
              m += chunk.length;
            }
          }
        }
      }
      return b;
    }
  };

  public UnlabeledBracketSet[] asFlat() {
    return conv(FLATCONV);
  }
  
  
  private UnlabeledBracketSet[] conv(BracketConv b) {
    UnlabeledBracketSet[] outputUB = 
      new UnlabeledBracketSet[nSentences()];
    
    for (int i = 0; i < nSentences(); i++) 
      outputUB[i] = new UnlabeledBracketSet(tokens(i), b.conv(corpus[i]));
    
    return outputUB;
  }

  /**
   * Iterate over the sentences of the original training corpus, returning
   * strings representing clumped sentences
   */
  public Iterable<String> strIter() {
    return new Iterable<String>() {
      
      @Override
      public Iterator<String> iterator() {
        
        return new Iterator<String>() {
          
          int i = 0;
          
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
          
          @Override
          public String next() {
            return clumps2str(corpus[i++]);
          }
          
          @Override
          public boolean hasNext() {
            return i < corpus.length;
          }
        };
      }
    };
  }
  
  /** Returns string representation of clumped sentence */
  public String clumps2str(int[][][] clumps) {
    StringBuffer sb = new StringBuffer();
    
    int lasti = clumps.length-1, lastj, lastk;
    for (int i = 0; i < clumps.length; i++) {
      lastj = clumps[i].length - 1;
      for (int j = 0; j <= lastj; j++) {
        lastk = clumps[i][j].length - 1;
        
        if (lastk == 0)
          sb.append(alpha.getString(clumps[i][j][0]));
        
        else {
          sb.append("(");

          for (int k = 0; k <= lastk; k++) {
            sb.append(alpha.getString(clumps[i][j][k]));
            if (k != lastk)
              sb.append(" ");
          }
          
          sb.append(")");
        }
        
        if (j != lastj)
          sb.append(" ");
      }
      
      if (i != lasti)
        sb.append(" ");
    }
    
    return sb.toString();
  }

  public void writeTo(String fname) throws IOException {
    ChunkedCorpus.fromChunkedSegmentedCorpus(this).writeTo(fname);
  }

  public static ChunkedSegmentedCorpus fromArrays(
      int[][][][] clumpedCorpus, Alpha alpha) {
    return new ChunkedSegmentedCorpus(clumpedCorpus, alpha);
  }

  public ChunkedCorpus toChunkedCorpus() {
    return ChunkedCorpus.fromChunkedSegmentedCorpus(this);
  }

  @Override
  public int nSentences() {
    return corpus.length;
  }

  public String[] tokens(int i) {
    int n = 0;
    for (int[][] seg: corpus[i]) n += segLen(seg);
    
    final String[] tokens = new String[n];
    int j = 0;
    for (int[][] seg: corpus[i])
      for (int[] chunk: seg)
        for (int w: chunk)
          tokens[j++] = alpha.getString(w);
    
    return tokens;
  }

  public Collection<UnlabeledBracket> chunkBrackets(int i) {
    final List<UnlabeledBracket> b = new ArrayList<UnlabeledBracket>();
    int curr = 0;
    for (int[][] seg: corpus[i]) {
      b.add(new UnlabeledBracket(curr, curr + segLen(seg)));
      for (int[] chunk: seg) {
        final int next = curr + chunk.length;
        b.add(new UnlabeledBracket(curr, next));
        curr = next;
      }
    }
    return b;
  }

  private static int segLen(int[][] seg) {
    int n = 0;
    for (int[] chunk: seg) n += chunk.length;
    return n;
  }

  public void writeTo(String output, String outputType) throws IOException {
    if (outputType.equals("clumps"))
      writeTo(output);
  }
}