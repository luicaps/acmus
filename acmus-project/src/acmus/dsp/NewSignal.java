package acmus.dsp;

import org.eclipse.core.runtime.IProgressMonitor;


public class NewSignal {
	
	// Attributes
	private Complex[] value;

	// Constructors
	public NewSignal(double[] a){
		value = new Complex[a.length];
		for (int i = 0; i < value.length; i++) {
			value[i] = new Complex(a[i], 0.0);
		}		
	}

	public NewSignal(int size){
		value = new Complex[size];
		Complex ZERO = new Complex(0.0,	0.0);
		for (int i = 0; i < size; i++) {
			value[i] = ZERO;
		}
	}

	// Methods	
	public Complex get(int index) {
		return value[index];
	}

	public int size() {
		return value.length;
	}

	public void set(int index, Complex c) {
		value[index] = c;
	}

	// Compute the FFT of this signal, assuming its length is a power of 2
	public NewSignal fft() {
		int N = this.value.length;

		// Base
		if (N == 1) {
			NewSignal base = new NewSignal(N);
			//base.set(0, f.get(0));
			base.value[0] = this.value[0];
			return base;
		}

		// Radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) {
			throw new RuntimeException("N is not a power of 2");
		}

		int max = N / 2;
		// FFT of even terms
		NewSignal even = new NewSignal(max);
		for (int k = 0; k < max; k++) {        	
			even.value[k] = this.value[2 * k];
		}
		NewSignal q = even.fft();

		// FFT of odd terms
		NewSignal odd = even;  // reuse the array        
		for (int k = 0; k < max; k++) {
			odd.value[k] = this.value[(2 * k) + 1];
		}
		NewSignal r = odd.fft();

		// Combine both
		NewSignal signal = new NewSignal(N);
		double coef = -2 * Math.PI / N;
		for (int k = 0; k < max; k++) {
			double kth = k * coef;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			signal.value[k] = q.value[k].plus(wk.times(r.value[k]));
			signal.value[k + max] = q.value[k].minus(wk.times(r.value[k]));
		}

		return signal;
	}

	// Compute the iFFT of this signal, assuming its length is a power of 2
	public NewSignal ifft() {
		int N = this.value.length;        
		NewSignal signal = new NewSignal(N);        

		// Take conjugate
		for (int i = 0; i < N; i++) {
			signal.value[i] = this.value[i].conjugate();
		}

		// Compute forward FFT
		signal = signal.fft();
		
		// Take conjugate and divide by N
		double lambda = 1.0 / N;
		for (int i = 0; i < N; i++) {
			signal.value[i] = signal.value[i].conjugate().times(lambda);
		}
		
		return signal;
	}

	public NewSignal cconvolve(NewSignal g, IProgressMonitor monitor) {
		NewSignal f = this;
		f = f.fft();
		g = g.fft();
		f = f.pointwiseMultiply(g);
		f = f.ifft();
		return f;    	
	}

	// Compute the linear convolution of this signal and g
	public NewSignal convolve(NewSignal g, IProgressMonitor monitor) {
		Complex ZERO = new Complex(0.0, 0.0);
		NewSignal f = this;

		f = f.pad();
		g = g.pad();

		NewSignal a = new NewSignal(2 * f.size());
		for (int i = 0; i < f.size(); i++) {
			a.value[i] = f.value[i];
		}
		
		for (int i = f.size(); i < 2 * f.size(); i++) {
			a.value[i] = ZERO;
		}
		
		NewSignal b = new NewSignal(2 * g.size());
		for (int i = 0; i < g.size(); i++) {
			b.value[i] = g.value[i];
		}
		
		for (int i = g.size(); i < 2 * g.size(); i++) {
			b.value[i] = ZERO;
		}
		
		return a.cconvolve(b, monitor);
	}

	public NewSignal pointwiseMultiply(NewSignal g) {
		//TODO Pad com o maior?
		if (this.size() != g.size()) {
			throw new RuntimeException("f and g are not compatible");
		}
		NewSignal a = this;
		NewSignal b = new NewSignal(a.size());
		for (int i = 0; i < b.size(); i++) {
			b.value[i] = a.value[i].times(g.value[i]);
		}
		return b;    	
	}

	private NewSignal pad() {
		int padSize = this.nextPowerOf2(this.size());
		Complex ZERO = new Complex(0.0, 0.0);
		NewSignal padded = new NewSignal(padSize);

		for (int i = 0; i < this.size(); i++) {
			padded.value[i] = this.value[i];
		}
		for (int i = this.size(); i < padSize; i++) {
			padded.value[i] = ZERO;
		}
		return padded;
	}

	private int nextPowerOf2(int n) {
		int BITSPACE = 32;
		n--;
		n = n | (n >> 1);
		for (int i = 2; i < BITSPACE; i *= i) {    		
			n = n | (n >> i);
		}
		n++;
		return n;
	}

	public static void show(NewSignal x, String title) {
		System.out.println(title);
		System.out.println("-------------------");
		for (int i = 0; i < x.size(); i++) {
			System.out.println(x.get(i));            
		}
		System.out.println();
	}

	public static void main(String[] args) { 
		int N = Integer.parseInt(args[0]);
		NewSignal x = new NewSignal(N);
		System.out.println("size = " + x.value.length);

		// original data
		for (int i = 0; i < N; i++) {
			x.set(i, new Complex(-2 * Math.random() + 1, 0.0));        	
		}

		/*
        x.set(0, new Complex(-0.03480425839330703, 0.0));
        x.set(1, new Complex(0.07910192950176387, 0.0));
        x.set(2, new Complex(0.7233322451735928, 0.0));
        x.set(3, new Complex(0.1659819820667019, 0.0));
		*/ 

		//show(x, "x");

		// FFT of original data
//		Signal y = x.fft();
//		show(y, "y = fft(x)");

		// take inverse FFT
//		Signal z = y.ifft();
//		show(z, "z = ifft(y)");


		// circular convolution of x with itself
//		Signal c = x.cconvolve(x);
//		show(c, "c = cconvolve(x, x)");

		// linear convolution of x with itself
		long t0 = System.currentTimeMillis();
		//Signal d = x.convolve(x);
		long t1 = System.currentTimeMillis();
		System.out.println("Time = " + ((t1 - t0) / 1000.0) + "s");
//		show(d, "d = convolve(x, x)");
	}
}
