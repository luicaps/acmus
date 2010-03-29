/**
 * Created on Jun 8, 2006
 */
package acmus.audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import acmus.AcmusApplication;
import acmus.AcmusPlugin;

/**
 * @author lku
 * 
 */
public class DefaultAudioDevice implements AudioDevice {

	private TargetDataLine m_line;
	private AudioFileFormat.Type m_targetType;
	private AudioInputStream m_audioInputStream;
//	private DataLine.Info m_outputinfo;
	private File m_outputFile;
	private AudioFormat _format;
	private long duration;

	public DefaultAudioDevice() {
		m_targetType = AudioFileFormat.Type.WAVE;

		_format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float)AcmusApplication.SAMPLE_RATE,
				16, 2, 4, (float)AcmusApplication.SAMPLE_RATE, false);

		// Leandro - 23/05/2009 - Begin
//		m_outputinfo = new DataLine.Info(TargetDataLine.class, _format);
		// Leandro - 23/05/2009 - End			
	}

	public void record(File outputFile, int milliseconds) {
		duration = (long) milliseconds;
		m_outputFile = outputFile;
		(new Thread() {
			public void run() {
				try {
					// Leandro - 23/05/2009 - Begin
					m_line = AudioSystem.getTargetDataLine(null, AcmusPlugin.getDefault().in);
//					m_line = (TargetDataLine) AudioSystem.getLine(m_outputinfo);
					// Leandro - 23/05/2009 - End
					m_line.open(_format);
					m_audioInputStream = new AudioInputStream(m_line);
					System.out.println("rec start " + duration + "( " + System.currentTimeMillis() + " )");
					m_line.start();
					(new Thread() {
						public void run() {
							try {
								AudioSystem.write(m_audioInputStream,
										m_targetType, m_outputFile);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					try {
						Thread.sleep(duration);
					} catch (InterruptedException e) {
					}
					m_line.stop();
					m_line.close();
					System.out.println("rec end ( " + System.currentTimeMillis() + " )");
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
