package net.ponvert.upparse.model;

import net.ponvert.upparse.corpus.*;

/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public interface Chunker {

  ChunkedSegmentedCorpus getChunkedCorpus(StopSegmentCorpus c) throws ChunkerError;
}
