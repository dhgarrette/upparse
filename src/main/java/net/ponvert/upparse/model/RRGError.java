package net.ponvert.upparse.model;

/** Error from right-regular grammar parsing
 * @author eponvert@utexas.edu (Elias Ponvert)
 */
public class RRGError extends SequenceModelError {
  private static final long serialVersionUID = 1L;

  public RRGError(String e) {
    super(e);
  }
}
