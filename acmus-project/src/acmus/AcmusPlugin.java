/*
 *  AcmusPlugin.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package acmus;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Line.Info;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import acmus.audio.AudioDevice;
import acmus.audio.DefaultAudioDevice;
import acmus.preferences.AcmusPreferencePage;
import acmus.preferences.PreferenceConstants;

/**
 * The main plugin class to be used in the desktop.
 */
public class AcmusPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static AcmusPlugin plugin;
	// Resource bundle.
	private ResourceBundle resourceBundle;

	public AudioDevice audioDevice;
	public String WORKSPACE_DIR;
	
	// Leandro - 23/05/2009
	public Hashtable<String, Mixer.Info> inputs = new Hashtable<String, Mixer.Info>();
	public Hashtable<String, Mixer.Info> outputs = new Hashtable<String, Mixer.Info>();
	public Mixer.Info in, out;

	/**
	 * The constructor.
	 */
	public AcmusPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("acmus.AcmusPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

		// WORKSPACE_DIR =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();

		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry
					.getExtensionPoint("acmus.audioDevice");
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i]
						.getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					if ("device".equals(elements[j].getName())) {
						audioDevice = (AudioDevice) elements[j]
								.createExecutableExtension("run");
						System.out.println("Found an executable extension: "
								+ audioDevice);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		if (audioDevice == null) {
			audioDevice = new DefaultAudioDevice();
		}
		
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// Leandro - 23/05/2009 - Begin
		// Carrega o canal de entrada/saída
		// TODO Descobrir em qual lugar padrão do Eclipse se deve fazer essa inicialização
		// TODO Quando você liga/desliga a interface de áudio durante a execução, a lista não se atualiza!
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerInfo.length; i++) {
			String name = mixerInfo[i].getName();
			Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);
			Info[] sourceInfos = mixer.getSourceLineInfo();
			for (int j = 0; j < sourceInfos.length; j++) {
				if (sourceInfos[j].getLineClass() == SourceDataLine.class) {
					outputs.put(name, mixerInfo[i]);
//					outputs.put(name, sourceInfos[j]);
				}
			}
			Info[] targetInfos = mixer.getTargetLineInfo();
			for (int j = 0; j < targetInfos.length; j++) {
				if (targetInfos[j].getLineClass() == TargetDataLine.class) {
					inputs.put(name, mixerInfo[i]);
//					inputs.put(name, targetInfos[j]);
				}
			}
		}
		System.out.println("Output device: " + getPreferenceStore().getString(PreferenceConstants.P_AUDIO_OUTPUT));
		out = outputs.get(getPreferenceStore().getString(PreferenceConstants.P_AUDIO_OUTPUT));
		System.out.println("Input device: " + getPreferenceStore().getString(PreferenceConstants.P_AUDIO_INPUT));
		in = inputs.get(getPreferenceStore().getString(PreferenceConstants.P_AUDIO_INPUT));
		// Leandro - 23/05/2009 - End
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static AcmusPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = AcmusPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
