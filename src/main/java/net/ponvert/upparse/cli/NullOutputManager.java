package net.ponvert.upparse.cli;

import java.io.*;

import net.ponvert.upparse.corpus.*;

/**
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class NullOutputManager extends OutputManager {
  
  private static NullOutputManager INSTANCE = new NullOutputManager();
  
  private NullOutputManager() { }

  public static OutputManager instance() { return INSTANCE; }

  @Override
  public boolean isNull() { return true; }

  @Override
  public PrintStream getResultsStream() { return System.out; }

  @Override
  public PrintStream getStatusStream() { return System.out; }
  
  @Override
  public void closeAll() { }

  @Override
  public void addChunkerOutput(ChunkedSegmentedCorpus chunkerOutput, String s) { 
  }

  @Override
  public void writeOutput() throws IOException { }

  @Override
  public void writeMetadata(Main prog) throws IOException { }

  @Override
  public String treeOutputFilename() { 
    return null;
  }

  @Override
  public String clumpsOutputFilename() {
    return null;
  }

  @Override
  public void useOutputText(String[][] testPos) { }
}
