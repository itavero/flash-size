package nl.arnom.jenkins.flashsize;

/**
 * Created by arnom on 30/03/2017.
 */
public interface ViewInfoSource {

  FlashSizeReport getReport();

  boolean hasPreviousReport();

  FileSize getDeltaFromPreviousReport(final String file);

  FileSize getDeltaFromPreviousReport(final SizeEntry latest);
}
