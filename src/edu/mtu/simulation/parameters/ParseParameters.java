package edu.mtu.simulation.parameters;

import java.io.File;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.beanutils.BeanUtils;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import edu.mtu.simulation.ForestSimException;

/**
 * Parse the settings contained in an INI file into the 
 */
public class ParseParameters {
	/**
	 * Read the values from the provided INI file into the given parameters object.
	 * 
	 * @param fileName of the INI file to load.
	 * @param parameters object to read into.
	 * @throws ForestSimException 
	 * @throws BackingStoreException 
	 */
	public static void read(String fileName, ParameterBase parameters) throws ForestSimException {
		try {
			// Open and parse the INI file
			Ini ini = new Ini(new File(fileName));
			Preferences prefs = new IniPreferences(ini);
			
			// Make sure the node exists
			if (!prefs.nodeExists("settings")) {
				throw new ForestSimException("'settings' node not found in INI file.");
			}
			
			// Load the parameters description (i.e., Java Bean)
			Map<String, String> bean = BeanUtils.describe(parameters);
			
			// Parse and apply the INI
			Preferences node = prefs.node("settings");
			for (String name : node.keys()) {
				if (bean.containsKey(name)) {
					bean.put(name, node.get(name, bean.get(name)));
				}
			}
			
			//  Update the parameters
			BeanUtils.populate(parameters, bean);
			
		} catch (Exception ex) {
			throw new ForestSimException("Unable to prase INI file.", ex);
		}
	}
}
