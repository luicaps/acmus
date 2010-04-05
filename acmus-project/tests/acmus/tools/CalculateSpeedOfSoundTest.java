package acmus.tools;

import org.junit.Assert;
import org.junit.Test;

public class CalculateSpeedOfSoundTest {
	double speed;

	@Test
	public void testCalculateSpeedOfSound() {
		
		double delta = 0.05;
		
		this.speed = (new CalculateSpeedOfSound(0, 0)).calculateSpeedOfSound();
		Assert.assertEquals(331.4, this.speed, delta);

		this.speed = (new CalculateSpeedOfSound(0, 100))
				.calculateSpeedOfSound();
		Assert.assertEquals(331.7, this.speed, delta);

		this.speed = (new CalculateSpeedOfSound(15, 50))
				.calculateSpeedOfSound();
		Assert.assertEquals(340.8, this.speed, delta);

		this.speed = (new CalculateSpeedOfSound(20, 20))
				.calculateSpeedOfSound();
		Assert.assertEquals(343.6, this.speed, delta);

		this.speed = (new CalculateSpeedOfSound(30, 0)).calculateSpeedOfSound();
		Assert.assertEquals(349.1, this.speed, delta);

		this.speed = (new CalculateSpeedOfSound(30, 100))
				.calculateSpeedOfSound();
		Assert.assertEquals(351.4, this.speed, delta);

	}
}
