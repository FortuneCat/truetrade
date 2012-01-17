package com.ats.platform;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ib.client.Contract;

/**
 * Tests for TALib class
 * 
 * @author Krzysztof Kazmierczyk
 *
 */
public class TALibTest {

	private BarSeries series;
	
	public void initializeData1() {
		final int milisinday = 1000 * 3600 * 24;
		final TimeSpan span = TimeSpan.daily;
		List<Bar> history = new ArrayList<Bar>();
		history.add(new Bar(BarType.time, span, milisinday, milisinday * 0, 14.33, 14.33, 14.33, 14.33, 45579));
		history.add(new Bar(BarType.time, span, milisinday, milisinday * 1, 14.33, 14.33, 14.23, 14.23, 45579));
		history.add(new Bar(BarType.time, span, milisinday, milisinday * 2, 14.33, 14.33, 13.98, 13.98, 66285));
		history.add(new Bar(BarType.time, span, milisinday, milisinday * 3, 14.33, 14.33, 13.96, 13.96, 51761));
		history.add(new Bar(BarType.time, span, milisinday, milisinday * 4, 14.33, 14.33, 13.93, 13.93, 69341));
		series = new BarSeries(new Instrument(new Contract()), BarType.time, TimeSpan.daily);
		series.addHistory(history);		
	}
	
	public void initializeData2() {
		final int milisinday = 1000 * 3600 * 24;
		final TimeSpan span = TimeSpan.daily;
		List<Bar> history = new ArrayList<Bar>();
		for (int i = 0; i < 1000; i++) {
			history.add(new Bar(BarType.time, span, milisinday, i * milisinday, 10, 10, 10, 10, 100));
		}
		series = new BarSeries(new Instrument(new Contract()), BarType.time, TimeSpan.daily);
		series.addHistory(history);
	}
	
	
	@Test
	public void testFi1() {
		initializeData1();
		double [] fi = TALib.fi(series, 1, 4, 1);
		Assert.assertEquals(-4557, (int)(fi[0]));
		Assert.assertEquals(-16571, (int)(fi[1]));
		Assert.assertEquals(-1035, (int)(fi[2]));
		Assert.assertEquals(-2080, (int)(fi[3]));
	}

	@Test
	public void testFi2() {
		initializeData1();
		double [] fi = TALib.fi(series, 3, 4, 3);
		Assert.assertEquals(-7388, (int)(fi[0]));
		Assert.assertEquals(-4734, (int)(fi[1]));
	}
	
	@Test
	public void testFi3() {
		initializeData2();
		double [] fi = TALib.fi(series, 1, 999, 1);
		Assert.assertEquals(0, (int)(fi[0]));
		Assert.assertEquals(0, (int)(fi[998]));
	}
	
	@Test
	public void testFi4() {
		initializeData2();
		double [] fi = TALib.fi(series, 1, 999, 26);
		Assert.assertEquals(0, (int)(fi[0]));
		Assert.assertEquals(0, (int)(fi[972]));
	}
	
	@Test
	public void testEBull1 (){
		initializeData1();
		double [] ebull = TALib.ebull(series, 2, 4, 3);
		Assert.assertEquals(15, (int)(ebull[0] * 100 + 0.01));
		Assert.assertEquals(26, (int)(ebull[1] * 100 + 0.01));
	}
	
	@Test
	public void testEBear1 (){
		initializeData1();
		double [] ebear = TALib.ebear(series, 2, 4, 3);
		Assert.assertEquals(-20, (int)(ebear[0] * 100 - 0.01));
		Assert.assertEquals(-11, (int)(ebear[1] * 100 - 0.01));
	}
	
	@Test
	public void testEBull2 (){
		initializeData2();
		double [] ebull = TALib.ebull(series, 2, 999, 3);
		Assert.assertEquals(0, (int)(ebull[0] * 100 + 0.01));
		Assert.assertEquals(0, (int)(ebull[997] * 100 + 0.01));
	}
	
	@Test
	public void testEBear2 (){
		initializeData2();
		double [] ebear = TALib.ebear(series, 2, 999, 3);
		Assert.assertEquals(0, (int)(ebear[0] * 100 + 0.01));
		Assert.assertEquals(0, (int)(ebear[997] * 100 + 0.01));
	}
}
