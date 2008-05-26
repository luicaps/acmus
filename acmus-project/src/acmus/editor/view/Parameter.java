/**
 * 
 */
package acmus.editor.view;

public class Parameter {
	double _channels[] = new double[11];
	String _name;

	public Parameter(String name, double[] ch) {
		_name = name;
		_channels = ch;
	}

	public String name() {
		return _name;
	}

	public double channel(int i) {
		return _channels[i];
	}

}