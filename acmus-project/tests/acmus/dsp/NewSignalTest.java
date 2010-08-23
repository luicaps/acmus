package acmus.dsp;

import org.junit.Assert;
import org.junit.Test;

public class NewSignalTest {

	@Test
	public void benchmark() {
		int N = 1000;
		NewSignal x = new NewSignal(N);
		System.out.println("size = " + x.size());
		Assert.assertNotNull(x);

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
		//NewSignal d = x.convolve(x);
		long t1 = System.currentTimeMillis();
		System.out.println("Time = " + ((t1 - t0) / 1000.0) + "s");
//		show(d, "d = convolve(x, x)");
	}
	
	/*
	public static void show(NewSignal x, String title) {
		System.out.println(title);
		System.out.println("-------------------");
		for (int i = 0; i < x.size(); i++) {
			System.out.println(x.get(i));            
		}
		System.out.println();
	}*/
}
