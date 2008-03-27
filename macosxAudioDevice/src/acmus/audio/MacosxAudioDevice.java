/**
 * Created on Jun 9, 2006
 */
package acmus.audio;

import java.io.File;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.sound.SICompletion;
import quicktime.sound.SPB;
import quicktime.sound.SPBDevice;
import quicktime.sound.SndHandle;
import quicktime.sound.SoundConstants;
import quicktime.util.QTUtils;
import acmus.dsp.Util;

/**
 * @author lku
 *
 */
public class MacosxAudioDevice implements AudioDevice {

 
  private static int kOSType = 1 << 1;

  private static int sndBufferSize = 327680;

  SndHandle sndHndl;

  SPBDevice sndDevice;

  SPB recorder;
  File outFile;
  long duration;

  public MacosxAudioDevice() {
  }

  public void record(File outputFile, int milliseconds) {

    duration = (long)milliseconds;
    outFile = outputFile;
    
    try {
      QTSession.open();
      sndDevice = new SPBDevice(null, SoundConstants.siWritePermission);
      System.out
          .println("OptionsDialog==" + sndDevice.hasOptionsDialog());
      System.out
          .println("NumChannels=" + sndDevice.getChannelAvailable());
      System.out.println("SampleSize=" + sndDevice.getSampleSize());
      System.out.println("SampleRate=" + sndDevice.getSampleRate());
      printArray("Names", sndDevice.getInputSourceNames());
      printArray("Compression", sndDevice.getCompressionAvailable(),
          kOSType);
      printArray("SampleSize", sndDevice.getSampleSizeAvailable(), 0);
      printArray("SampleRate", sndDevice.getSampleRateAvailable());
      sndDevice.setPlayThruOnOff(0);
//      sndHndl = new SndHandle(sndDevice.getNumberChannels(), sndDevice
//          .getSampleRate(), sndDevice.getSampleSize(), sndDevice
//          .getCompressionType(), 60);
      sndHndl = new SndHandle(2, sndDevice
          .getSampleRate(), sndDevice.getSampleSize(), sndDevice
          .getCompressionType(), 60);
      sndBufferSize = (int) Math.ceil((double)duration/1000 * sndDevice.getSampleRate() * (sndDevice.getSampleSize()/8) * sndDevice.getNumberChannels());

      sndHndl.appendSoundBuffer(sndBufferSize); // this is the size of
      // our sound buffer

      System.out
      .println("SndBuffer==" + sndBufferSize + "  " + duration);

      // Set up recording to reuse object for multiple recordings
      // we hold onto this variable so that it isn't finalized before we
      // finish recording
      // we also need to remove the completion proc as we shut this down -
      // see closingWindow
      System.out.println("Will Record:"
          + sndDevice.bytesToMilliseconds(sndBufferSize) + " msecs");
      recorder = new SPB(sndDevice, 0, sndDevice
          .bytesToMilliseconds(sndBufferSize), sndHndl.getSoundData());

      // We're going to record this block ASynchronously
      // and we're installing a completion proc to notify us when
      // the recording is finished
      recorder.setCompletionProc(new SICompletion() {
        final SndHandle soundHdl = sndHndl;

        final SPBDevice device = sndDevice;

        public void execute(SPB paramBlock) {
          System.out.println("FinishedRecording");
          
          byte[] b = soundHdl.getBytes();
          
          try {
            Util.wavWrite(b, device.getSampleRate(), device.getSampleSize(),device.getNumberChannels(),true, outFile.getPath());
            recorder.removeCompletionProc(); //clean this up as we installed it
            QTSession.close();
            // set up sndHndl after recording is finished so we cam
            // play it
//            soundHdl.setupHeader(device.getNumberChannels(), device
//                .getSampleRate(), device.getSampleSize(),
//                device.getCompressionType(), 60, paramBlock
//                    .getCount());
          } catch (QTException ee) {
            ee.printStackTrace();
          }
        }
      });

    } catch (Exception ee) {
      ee.printStackTrace();
      QTSession.close();
    }

    try{          
      System.out.println ("StartRecording");
      recorder.record (true);
    } catch (QTException ee){
      ee.printStackTrace();
    } 

  }

  private static void printArray(String prefix, int[] array, int printTypeFlag) {
    System.out.print(prefix + "=[");
    if (array.length == 0) {
      System.out.println("]");
      return;
    }
    for (int i = 0; i < array.length - 1; i++) {
      if ((printTypeFlag & kOSType) != 0)
        System.out.print(QTUtils.fromOSType(array[i]) + ",");
      else
        System.out.print(array[i] + ",");
    }
    if ((printTypeFlag & kOSType) != 0)
      System.out.println(QTUtils.fromOSType(array[array.length - 1])
          + "]");
    else
      System.out.println(array[array.length - 1] + "]");
  }

  private static void printArray(String prefix, float[] array) {
    System.out.print(prefix + "=[");
    if (array.length == 0) {
      System.out.println("]");
      return;
    }
    for (int i = 0; i < array.length - 1; i++) {
      System.out.print(array[i] + "F,");
    }
    System.out.println(array[array.length - 1] + "F]");
  }

  private static void printArray(String prefix, String[] array) {
    System.out.print(prefix + "=[");
    if (array.length == 0) {
      System.out.println("]");
      return;
    }
    for (int i = 0; i < array.length - 1; i++) {
      System.out.print(array[i] + ",");
    }
    System.out.println(array[array.length - 1] + "]");
  }



}
