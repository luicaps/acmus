/**
 * Created on Jun 8, 2006
 */
package acmus.audio;

import java.io.File;

/**
 * @author lku
 * 
 */
public interface AudioDevice {

	// under construction... (will possibly become RecordingAudioDevice only)

	// should record asynchronously
	public void record(File outputFile, int milliseconds);
	// public void stop();
}
