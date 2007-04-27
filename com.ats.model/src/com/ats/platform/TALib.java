package com.ats.platform;

import java.util.Arrays;

import com.ats.platform.Bar.BarField;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

/**
 * Utility class to handle technical analysis
 * 
 * @author Adrian
 *
 */
public class TALib {
	private static final Core core = new Core();
	
	public static double[] adx(BarSeries series, int period, int startIdx, int endIdx) {
		double inHigh[] = series.getDoubleData(BarField.high);
		double inLow[] = series.getDoubleData(BarField.low);
		double inClose[] = series.getDoubleData(BarField.close);
		int optInTimePeriod = period;
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double outReal[] = new double[inHigh.length];
		
		RetCode code = core.adx(startIdx, endIdx, inHigh, inLow, inClose, optInTimePeriod, outBegIdx, outNbElement, outReal);
		if( code != RetCode.Success ) {
			return null;
		}
		double ret[] = new double[outNbElement.value];
		for( int i = 0; i < ret.length; i++ ) {
			ret[i] = outReal[i];
		}
		return ret;
	}


	public static double[] sma(BarSeries series, BarField field, int period) {
		return sma(series, field, period, 0, series.size()-1);
	}
	public static double[] sma(BarSeries series, BarField field, int period, int startIdx, int endIdx) {
		double inReal[] = series.getDoubleData(field);
		int optInTimePeriod = period;
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double outReal[] = new double[inReal.length];
		
		RetCode code = core.sma(startIdx, endIdx, inReal, optInTimePeriod, outBegIdx, outNbElement, outReal);
		if( code != RetCode.Success ) {
			return null;
		}
		double ret[] = new double[outNbElement.value];
		for( int i = 0; i < ret.length; i++ ) {
			ret[i] = outReal[i];
		}
		return ret;
	}
	
	public static double[] ema(BarSeries series, BarField field, int period) {
		return ema(series, field, period, 0, series.size()-1);
	}
	public static double[] ema(BarSeries series, BarField field, int period, int startIdx, int endIdx) {
		double inReal[] = series.getDoubleData(field);
		int optInTimePeriod = period;
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		double outReal[] = new double[inReal.length];
		
		RetCode code = core.ema(startIdx, endIdx, inReal, optInTimePeriod, outBegIdx, outNbElement, outReal);
		if( code != RetCode.Success ) {
			return null;
		}
		double ret[] = new double[outNbElement.value];
		for( int i = 0; i < ret.length; i++ ) {
			ret[i] = outReal[i];
		}
		return ret;
	}
	
	public static int macd(BarSeries series, BarField field,
			int fastPeriod, int slowPeriod, int signalPeriod, int startIdx, int endIdx,
			double outMACD[], double outMACDSignal[], double outMACDHist[] ) {
		double inReal[] = series.getDoubleData(field);
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();
		
		RetCode code = core.macd(startIdx, endIdx, inReal, fastPeriod, slowPeriod, signalPeriod, outBegIdx, outNbElement, outMACD, outMACDSignal, outMACDHist);
		
		if( code != RetCode.Success ) {
			return 0;
		}
		return outNbElement.value;
	}

	public static int stoch(BarSeries series, int fastK, int slowK, int slowD,
			int startIdx, int endIdx, double outSlowK[], double outSlowD[]) {
		double inHigh[] = series.getDoubleData(BarField.high);
		double inLow[] = series.getDoubleData(BarField.low);
		double inClose[] = series.getDoubleData(BarField.close);
		MInteger outBegIdx = new MInteger();
		MInteger outNbElement = new MInteger();

		RetCode code = core.stoch(startIdx, endIdx, inHigh, inLow, inClose,
				fastK, slowK, MAType.Sma, slowD, MAType.Sma, outBegIdx,
				outNbElement, outSlowK, outSlowD);
		if (code != RetCode.Success) {
			return 0;
		}
		return outNbElement.value;
	}

}
