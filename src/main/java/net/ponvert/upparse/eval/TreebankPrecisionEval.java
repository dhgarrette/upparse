package net.ponvert.upparse.eval;

import net.ponvert.upparse.corpus.*;

/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class TreebankPrecisionEval extends TreebankEvalFromChunkerOutput {
  
  private TreebankPrecisionEval(
      final OutputType treebankprec,
      final UnlabeledBracketSetCorpus gold) {
    super(treebankprec, gold);
  }

  public static TreebankPrecisionEval fromUnlabeledBracketSets(
      final OutputType treebankprec, 
      final UnlabeledBracketSetCorpus gold) {  
    assert gold != null;
    return new TreebankPrecisionEval(treebankprec, gold);
  }

  @Override
  protected UnlabeledBracketSetCorpus makeTreeCorpus(
      ChunkedSegmentedCorpus output) {
    return UnlabeledBracketSetCorpus.fromArrays(output.asChunked());
  }
}
