package net.ponvert.upparse.eval;

import net.ponvert.upparse.corpus.*;

/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class TreebankFlatEval extends TreebankEvalFromChunkerOutput {
  
  private TreebankFlatEval(
      final OutputType type, 
      final UnlabeledBracketSetCorpus gold) { 
    super(type, gold);
  }

  public static Eval fromUnlabeledBracketSets(
      final OutputType type,
      final UnlabeledBracketSetCorpus unlabeledBracketSetCorpus) { 
    return new TreebankFlatEval(type, unlabeledBracketSetCorpus);
  }

  @Override
  protected UnlabeledBracketSetCorpus makeTreeCorpus(
      final ChunkedSegmentedCorpus output) {
    return UnlabeledBracketSetCorpus.fromArrays(output.asFlat());
  }
}
