package acmus.util.math;

public class Vector {
	public float x;
	public float y;
	public float z;

	public Vector(float a, float b, float c) {
		this.x = a;
		this.y = b;
		this.z = c;
	}

	public Vector(Vector otherVector) {
		this(otherVector.x, otherVector.y, otherVector.z);
	}

	/**
	 * 
	 * @param otherVector
	 * @return a new vector witch is the instance - otherVector
	 */
	public Vector sub(Vector otherVector) {
		return new Vector(this.x - otherVector.x, this.y - otherVector.y,
				this.z - otherVector.z);
	}

	/**
	 * Does the subtraction instance - otherVector and stores the result in the
	 * instance
	 * 
	 * @param otherVector
	 */
	public void subFromSelf(Vector otherVector) {
		this.x -= otherVector.x;
		this.y -= otherVector.y;
		this.z -= otherVector.z;
	}

	/**
	 * 
	 * @param otherVector
	 * @return a new vector witch is the instance + otherVector
	 */
	public Vector add(Vector otherVector) {
		return new Vector(this.x + otherVector.x, this.y + otherVector.y,
				this.z + otherVector.z);
	}

	/**
	 * Does the addition instance + otherVector and stores the result in the
	 * instance
	 * 
	 * @param otherVector
	 */
	public void addToSelf(Vector otherVector) {
		this.x += otherVector.x;
		this.y += otherVector.y;
		this.z += otherVector.z;
	}

	public Vector crossProduct(Vector otherVector) {
		return new Vector(this.y * otherVector.z - this.z * otherVector.y,
				this.z * otherVector.x - this.x * otherVector.z, this.x
						* otherVector.y - this.y * otherVector.x);
	}

	public float dotProduct(Vector otherVector) {
		return this.x * otherVector.x + this.y * otherVector.y + this.z
				* otherVector.z;
	}

	/**
	 * Creates a Vector witch is the original vector scaled with factor
	 * 
	 * @param esc
	 *            .
	 * 
	 * @param esc
	 *            a float scalar
	 * @return a new Vector with the new value of the original times
	 * @param esc
	 *            .
	 */
	public Vector scale(float esc) {
		return new Vector(this.x * esc, this.y * esc, this.z * esc);
	}

	/**
	 * Scale the vector with factor
	 * 
	 * @param esc
	 *            .
	 * 
	 * @param esc
	 *            a float scalar
	 */
	public void scaleSelf(float esc) {
		this.x *= esc;
		this.y *= esc;
		this.z *= esc;
	}

	/**
	 * 
	 * @return the norm of the Vector, squared
	 */
	public float normSquared() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	/**
	 * 
	 * @return the norm of the Vector
	 */
	public float norm() {
		return (float) Math.sqrt(this.normSquared());
	}

	public Vector normalized() {
		float m = 1.0f / this.norm();

		return new Vector(this.x * m, this.y * m, this.z * m);
	}

	public Vector normalized(float norm) {
		float m = norm / this.norm();

		return new Vector(this.x * m, this.y * m, this.z * m);
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

	public double cipicAzimuth() {
		return -Math.PI / 2 + Math.acos(this.y);

	}

	public double cipicElevation() {
		int signX = 1;
		if (this.x < 0) {
			signX = -1;
		}
		return Math.acos(signX / Math.sqrt(1 + (this.z * this.z)
				/ (this.x * this.x)));
	}

	public double azimuth() {
		return Math.acos(this.z
				/ (this.x * this.x + this.y * this.y + this.z * this.z));
	}

	public double elevation() {
		return Math.atan2(this.y, this.x);
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
		// return true;
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
}
