package acmus.tools.structures;

public class Vector {
	float x;
	float y;
	float z;

	public Vector(float a, float b, float c) {
		x = a;
		y = b;
		z = c;
	}
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public Vector normalize(){
		float m = 1.0f/(float) Math.sqrt(x*x + y*y + z*z);
		
		return new Vector(x*m, y*m, z*m);
	}

	public Vector sub(Vector end) {
		return new Vector(x-end.x, y-end.y, z-end.z);
	}

	public Vector add(Vector end) {
		return new Vector(x+end.x, y+end.y, z+end.z);
	}

	public Vector crossProduct(Vector w) {
		return new Vector(y * w.z - z * w.y, z * w.x - x * w.z, x * w.y - y * w.x);
	}

	public float dotProduct(Vector w) {
		return this.x * w.x + this.y * w.y + this.z * w.z;
	}

	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}

	public Vector times(float esc) {
		return new Vector(x*esc, y*esc, z*esc);
	}

	public String toDat() {
		return this.x + " " + this.y + " " + this.z;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Vector other = (Vector) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}
	
}
