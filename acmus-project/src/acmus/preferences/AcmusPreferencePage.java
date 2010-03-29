package acmus.preferences;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Line.Info;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import acmus.AcmusPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class AcmusPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public AcmusPreferencePage() {
		super(GRID);
		setPreferenceStore(AcmusPlugin.getDefault().getPreferenceStore());
//		setDescription("A demonstration of a preference page implementation");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {

		Set<String> set = AcmusPlugin.getDefault().outputs.keySet();
		String[][] aux = new String[set.size()][2];
		int i = 0;
		for (Iterator iterator = set.iterator(); iterator.hasNext();i++) {
			String str = (String) iterator.next();
			aux[i][0] = str;
			aux[i][1] = str;
		}
		
		addField(new ComboFieldEditor(
				PreferenceConstants.P_AUDIO_OUTPUT,
				"Audio Output",
				aux,
				getFieldEditorParent()));
		
		set = AcmusPlugin.getDefault().inputs.keySet();
		aux = new String[set.size()][2];
		i = 0;
		for (Iterator iterator = set.iterator(); iterator.hasNext();i++) {
			String str = (String) iterator.next();
			aux[i][0] = str;
			aux[i][1] = str;
		}
		
		addField(new ComboFieldEditor(
				PreferenceConstants.P_AUDIO_INPUT,
				"Audio Input",
				aux,
				getFieldEditorParent()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}