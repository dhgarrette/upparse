package net.ponvert.upparse.eval;

import net.ponvert.upparse.corpus.*;

/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class NullEval extends Eval {
  
  private static NullEval INSTANCE = new NullEval();

  private NullEval() {
    super("null");
  }

  @Override
  public void eval(String string, ChunkedSegmentedCorpus output)
      throws EvalError { }

  public static Eval instance() { return INSTANCE; }
}
