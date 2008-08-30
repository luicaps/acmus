package acmus.jna;

import com.sun.jna.Structure;

public class JnaTriade extends Structure {

    public double x;
    public double y;
    public double z;

    public JnaTriade() {

    }

    public JnaTriade(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }

}
