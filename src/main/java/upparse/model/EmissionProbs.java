package upparse.model;

import static java.lang.Math.*;
import static upparse.util.Util.*;
import static java.lang.Double.*;

import java.util.*;

import upparse.util.*;

/**
 * Class for calculating emission probabilities
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class EmissionProbs {
  
  private final double[][] emiss;
  private double defaultProb;
  private double nvocab;
  private final Ipredicate isStop;
  private final double smoothParam;

  public EmissionProbs(
      final double[][] _emiss, 
      final Ipredicate _isStop, 
      final double _smoothParam) {
    emiss = _emiss;
    nvocab = emiss[0].length;
    defaultProb = log(1./nvocab);
    isStop = _isStop;
    smoothParam = _smoothParam;
  }

  public int numTags() {
    return emiss.length;
  }

  public int numTerms() {
    return emiss[0].length;
  }

  /**
   * @param t The tag index
   * @param w The term index
   * @return The probability state t emits w
   */
  public double getProb(final int t, final int w) {
    if (w >= numTerms())
      return isStop.pred(t) ? NEGATIVE_INFINITY : defaultProb;
    
    return emiss[t][w];
  }

  public void update(double[][] emissCount) {
    
    final int 
      ntags = numTags(), 
      nterms = numTerms();
    
    final int[] 
      _stopStates = new int[ntags],
      _nonStopStates = new int[ntags],
      _stopTerms = new int[nterms],
      _nonStopTerms = new int[nterms];
    
    int 
      numStopStates = 0, numNonStopStates = 0, 
      numStopTerms = 0, numNonStopTerms = 0;
    
    for (int t = 0; t < numTags(); t++)
      if (isStop.pred(t)) 
        _stopStates[numStopStates++] = t;
      else
        _nonStopStates[numNonStopStates++] = t;
    
    final int[] 
      stopStates = Arrays.copyOf(_stopStates, numStopStates),
      nonStopStates = Arrays.copyOf(_nonStopStates, numNonStopStates);

    for (int w = 0; w < numTerms(); w++) {
      boolean isStopTerm = false;
      for (int t: stopStates)
        if (emissCount[t][w] != 0) {
          isStopTerm = true;
        }

      if (isStopTerm) 
        _stopTerms[numStopTerms++] = w;
      else 
        _nonStopTerms[numNonStopTerms++] = w;
    }
    
    final int[]
      stopTerms = Arrays.copyOf(_stopTerms, numStopTerms),
      nonStopTerms = Arrays.copyOf(_nonStopTerms, numNonStopTerms);
    
    for (int t: stopStates) {
      final double sum = log(sum(emissCount[t]));
      for (int w: stopTerms)
        emiss[t][w] = log(emissCount[t][w]) - sum;
      
      for (int w: nonStopTerms)
        emiss[t][w] = NEGATIVE_INFINITY;
    }
    
    final double nvocab = (double) numNonStopTerms;
    for (int t: nonStopStates) {
      final double sum = log(sum(emissCount[t]) + smoothParam * nvocab);
      for (int w: nonStopTerms)
        emiss[t][w] = log(emissCount[t][w] + smoothParam) - sum;
      
      for (int w: stopTerms)
        emiss[t][w] = NEGATIVE_INFINITY;
    }
    
    defaultProb = log(1/nvocab);
  }
  
  public static EmissionProbs directFromProbs(
      final double[][] probs, Ipredicate isStop, double smoothParam) {
    return new EmissionProbs(probs, isStop, smoothParam);
  }

  public static EmissionProbs fromCounts(
      final double[][] emissCount, Ipredicate isStop, double smoothParam) {
    final int m = emissCount.length, n = emissCount[0].length;
    final EmissionProbs e = 
      new EmissionProbs(new double[m][n], isStop, smoothParam);
    e.update(emissCount);
    return e;
  }
}
