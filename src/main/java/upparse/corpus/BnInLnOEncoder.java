package upparse.corpus;

import java.util.Arrays;

import upparse.util.Ipredicate;

public class BnInLnOEncoder extends TagEncoder {

  private final int numGroups;

  private static final int STOP = 0;
  private static final int O = 1;
  private static final int B = 2;
  private static final int I = 3;
  private static final int L = 4;

  public BnInLnOEncoder(String stop, Alpha alpha, int numGroups) {
    super(stop, alpha);
    this.numGroups = numGroups;
  }

  @Override
  public int[] bioTrain(ChunkedSegmentedCorpus corpus, int n)
      throws EncoderError {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChunkedSegmentedCorpus clumpedCorpusFromBIOOutput(int[] tokens,
      int[] output) throws EncoderError {
    assert tokens.length == output.length;

    // Count the number of sentences
    int numS = -1; // don't count first __eos__
    for (int w : tokens)
      if (isEos(w))
        numS++;

    int[][][][] clumpedCorpus = new int[numS][][][];

    int i = 1, j, sInd = 0, nextEos = 1, numSeg, segInd, nextEoSeg, numClumps, clumpInd;

    assert !isIState(output[i]) && !isLState(output[i]);

    // while (nextEos < tokens.length) {
    while (sInd < numS) {

      // process a sentence
      while (nextEos < tokens.length && !isEos(tokens[nextEos]))
        nextEos++;

      // count the number of segments
      numSeg = 0;
      j = i;
      while (j < tokens.length && j <= nextEos) {
        if (isStopOrEos(tokens[j]))
          numSeg++;
        j++;
      }

      clumpedCorpus[sInd] = new int[numSeg][][];
      segInd = 0;

      nextEoSeg = i;
      while (segInd < numSeg) {

        while (!isStopState(output[nextEoSeg]))
          nextEoSeg++;

        assert nextEoSeg <= nextEos;

        assert isStopState(output[nextEoSeg]);
        assert nextEoSeg >= output.length-1 || !isIState(output[nextEoSeg + 1]);

        // count the number of clumps
        numClumps = 0;
        j = i;
        while (j < nextEoSeg) {
          if (isBState(output[j]) || isOState(output[j]))
            numClumps++;
          j++;
        }

        clumpedCorpus[sInd][segInd] = new int[numClumps][];
        clumpInd = 0;

        boolean firstIn = true;
        while (i < nextEoSeg) {
          if (isOState(output[i])) {
            clumpedCorpus[sInd][segInd][clumpInd] = new int[] { tokens[i++] };
            clumpInd++;
            assert !isIState(output[i]) && !isLState(output[i]);
            firstIn = false;
          }

          else if (isBState(output[i])) {
            j = i + 1;
            assert isIState(output[j]) || isLState(output[j]);
            while (isIState(output[j]))
              j++;
            assert isLState(output[j]);
            j++;
            clumpedCorpus[sInd][segInd][clumpInd] = Arrays.copyOfRange(tokens,
                i, j);

            // next clump
            i = j;
            clumpInd++;
            assert !isIState(output[i]) && !isLState(output[i]);
            firstIn = false;
          }

          else {
            assert firstIn;
            assert !isStopState(output[i]) : "output[i] should not be STOP";
            assert !isIState(output[i]) : "output[i] should not be I";
            assert !isLState(output[i]): "output[i] should not be L";
            throw new EncoderError(String.format("Unexpected tag: %d",
                output[i]));
          }
        }

        assert i == nextEoSeg : String.format("i = %d nextEoSeg = %d", i,
            nextEoSeg);

        // next segment
        segInd++;
        nextEoSeg++;
        i = nextEoSeg;
      }

      // next sentence
      sInd++;

      // increment everything
      assert i == nextEos + 1 : String.format("(%d) i = %d nextEos = %d", sInd,
          i, nextEos);
      nextEos = i;
    }

    return ChunkedSegmentedCorpus.fromArrays(clumpedCorpus, alpha);
  }

  private boolean isIState(int j) {
    if (j < 2)
      return false;
    final int r = reducedTagVal(j);
    return r == I;
  }
  
  private boolean isBState(int j) {
    if (j < 2)
      return false;
    final int r = reducedTagVal(j);
    return r == B;
  }
  
  private boolean isLState(int j) {
    if (j < 2)
      return false;
    final int r = reducedTagVal(j);
    return r == L; 
  }
  
  private int reducedTagVal(int tag) {
    final int val = (tag - 2) % 3;
    return val + 2;
  }
  
  private boolean isOState(int j) {
    return j == O;
  }
  
  private boolean isStopState(int j) {
    return j == STOP;
  }

  @Override
  public Ipredicate isStopPred() {
    return new Ipredicate() {
      @Override
      public boolean pred(int t) {
        return t == STOP;
      }
    };
  }

  @Override
  public double[][] softTrain(int[] train) {
    return null;
  }

  @Override
  public int numTags() {
    return 2 + (3 * numGroups);
  }

  @Override
  public boolean[][] constraints() {
    final int n = numTags(); 
    final boolean[][] c = new boolean[n][n];
    for (int t1 = 0; t1 < n; t1++) {
      for (int t2 = 0; t2 < n; t2++) {
        if (isStopState(t1) || isOState(t1)) {
          // cannot go STOP -> I, STOP -> L ; otherwise OK
          // same for O
          c[t1][t2] = isIState(t2) || isLState(t2);
        }
        
        else if (isBState(t1)) {
          // B -> I, B -> L ok
          // B -> B, B -> O, B -> STOP not ok
          c[t1][t2] = isBState(t2) || isOState(t2) || isStopState(t2);
        }
        
        else if (isIState(t1)) {
          // I -> I and I -> L ok
          // I -> STOP and I -> B and I -> O not ok
          c[t1][t2] = isStopState(t2) || isBState(t2) || isOState(t2);
        }
        
        else if (isLState(t1)) {
          // L -> I, L -> L not ok
          c[t1][t2] = isIState(t2) || isLState(t2);
        }
        
        else {
          throw new RuntimeException("Unexpected tag " + t1);
        }
      }
    }
    return c;
  }

  @Override
  public double[] getInitTagProb() {
    double[] p = new double[numTags()];
    p[STOP] = 1;
    return p;
  }

  @Override
  public int[] allNonStopTags() {
    final int n = numTags()-1;
    int[] tags = new int[n];
    for (int i = 0; i < n; i++)
      tags[i] = i+1;
    return tags;
  }

  @Override
  public int[] allStopTags() {
    return new int[] { STOP };
  }
}
