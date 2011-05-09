package acmus.dsp;

import org.eclipse.core.runtime.IProgressMonitor;


public class NewSignal {
	
	// Attributes
	private Complex[] value;
	private int size;
	//private int SR;

	// Constructors
	public NewSignal(double[] a){
		size = a.length;
		value = new Complex[size];
		for (int i = 0; i < size; i++) {
			value[i] = new Complex(a[i], 0.0);
		}		
	}
	
	public NewSignal(double[] a, int size){
		this.size = size;
		value = new Complex[size];
		Complex ZERO = new Complex(0.0,	0.0);
		for (int i = 0; i < size; i++) {
			if (i < a.length) {
				value[i] = new Complex(a[i], 0.0);
			} else {
				value[i] = ZERO;
			}
		}		
	}

	public NewSignal(int size){
		this.size = size;
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
		return this.size;
	}
	
	public void reSize(int size) {
		if (this.value.length < size) {
			Complex[] newValue = new Complex[size];
			Complex ZERO = new Complex(0.0, 0.0);
			int i = 0;
			while (i < this.value.length) {
				newValue[i] = this.value[i];
				i++;
			}
			while (i < size) {
				newValue[i] = ZERO;
				i++;
			}
		} else {
			this.size = size;
		}
	}

	public void set(int index, Complex c) {
		value[index] = c;
	}
	
	public NewSignal upsample(int factor) {
		NewSignal g = new NewSignal(factor * this.size);
		for (int i = 0; i < this.size; i++) {
			g.value[4 * i] = this.value[i / factor];
		}
		return g;
	}

	/**
	 * Compute the FFT of this signal, assuming its size is a power of 2
	 * @return the FFT NewSignal
	 */
	public NewSignal fft() {
		int N = this.size;

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

	/**
	 * Compute the iFFT of this signal, assuming its size is a power of 2
	 * @return the iFFT NewSignal
	 */
	public NewSignal ifft() {
		int N = this.size;        
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
		//f = f.unpad(0.01);
		return f;    	
	}

	// Compute the linear convolution of this signal and g
	public NewSignal convolve(NewSignal g, IProgressMonitor monitor) {
		NewSignal f = this;
		
		int size = g.size() + f.size() - 1;
		int sizePO2 = nextPowerOf2(size);
		
		NewSignal a = f.pad(sizePO2);
		NewSignal b = g.pad(sizePO2);
		
		NewSignal conv = a.cconvolve(b, monitor);
		conv.reSize(size);
		
		return conv;
	}

	public NewSignal pointwiseMultiply(NewSignal g) {
		NewSignal a = this;
		if (this.size() > g.size()) {
			g = g.pad(a.size());			
		}
		else {
			a = a.pad(g.size());
		}
		NewSignal b = new NewSignal(a.size());
		for (int i = 0; i < b.size(); i++) {
			b.value[i] = a.value[i].times(g.value[i]);
		}
		return b;
	}

//	private NewSignal pad() {
//		int padSize = this.nextPowerOf2(this.size());
//		return pad(padSize);
//	}
	
	private NewSignal pad(int size) {
		Complex ZERO = new Complex(0.0, 0.0);
		NewSignal padded = new NewSignal(size);

		for (int i = 0; i < this.size(); i++) {
			padded.value[i] = this.value[i];
		}
		for (int i = this.size(); i < size; i++) {
			padded.value[i] = ZERO;
		}
		return padded;
	}
	
//	private NewSignal unpad(double delta) {
//		int i;
//		int space = 11025;
//		NewSignal unpadded;
//		for (i = (this.size()) - 1; i >= 0; i--) {
//			if (this.value[i].re() > delta) break;
//		}
//		i += space;
//		if (i < this.size()) {
//			unpadded = new NewSignal(i);
//		}
//		else {
//			return this;
//		}
//		
//		for (int j = 0; j < i; j++) {
//			unpadded.value[j] = this.value[j];
//		}
//		return unpadded;
//	}

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
}
