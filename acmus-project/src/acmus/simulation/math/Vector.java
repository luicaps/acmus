package acmus.simulation.math;

public class Vector {
	private float x;
	private float y;
	private float z;

	public Vector(float a, float b, float c) {
		this.x = a;
		this.y = b;
		this.z = c;
	}

	public Vector(Vector otherVector) {
		this.set(otherVector);
	}

	public void set(Vector otherVector) {
		this.x = otherVector.getX();
		this.y = otherVector.getY();
		this.z = otherVector.getZ();
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public Vector normalized() {
		float m = 1.0f / this.norm();

		return new Vector(x * m, y * m, z * m);
	}

	public Vector normalized(float norm) {
		float m = norm / this.norm();

		return new Vector(x * m, y * m, z * m);
	}

	public void normalize() {
		this.normalize(1.0f);
	}

	public void normalize(float norm) {
		float m = norm / this.norm();

		this.x *= m;
		this.y *= m;
		this.z *= m;
	}

	public Vector sub(Vector otherVector) {
		return new Vector(x - otherVector.x, y - otherVector.y, z
				- otherVector.z);
	}

	public void subFromSelf(Vector otherVector) {
		x -= otherVector.getX();
		y -= otherVector.getY();
		z -= otherVector.getZ();
	}

	public Vector add(Vector otherVector) {
		return new Vector(x + otherVector.x, y + otherVector.y, z
				+ otherVector.z);
	}

	public void addToSelf(Vector otherVector) {
		x += otherVector.getX();
		y += otherVector.getY();
		z += otherVector.getZ();
	}

	public Vector crossProduct(Vector w) {
		return new Vector(y * w.z - z * w.y, z * w.x - x * w.z, x * w.y - y
				* w.x);
	}

	public float dotProduct(Vector w) {
		return this.x * w.x + this.y * w.y + this.z * w.z;
	}

	public float normSquared() {
		return x * x + y * y + z * z;
	}
	
	public float norm() {
		return (float) Math.sqrt(this.normSquared());
	}

	public Vector times(float esc) {
		return new Vector(x * esc, y * esc, z * esc);
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
		// TODO Does that make any sense?
		// == isn't the same as .equals()?
		// if (this == obj)
		//	return true;
		if (obj == null)
			return false;
		// TODO What about (!( obj instanceof Vector)) ?
		// What if obj is under Object runtime class?
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

	public void scale(float esc) {
		this.x *= esc;
		this.y *= esc;
		this.z *= esc;
	}

}
