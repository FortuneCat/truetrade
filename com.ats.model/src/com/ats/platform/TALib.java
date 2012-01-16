package com.ats.platform;

import java.util.Arrays;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

/**
 * Utility class to handle technical analysis
 * 
 * @author Adrian
 * @author Krzysztof Kazmierczyk
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

	public static double[] fi(BarSeries series, int startIdx, int endIdx,
			int period) {
		double[] close = series.getDoubleData(BarField.close);
		double[] vol = series.getDoubleData(BarField.volume);
		double[] fi = new double[close.length - 1];
		for (int i = 1; i < close.length; i++) {
			fi[i - 1] = (close[i] - close[i - 1]) * vol[i];
		}
		if (period == 1) {
			return Arrays.copyOfRange(fi, Math.max(startIdx - 1, 0), endIdx);
		} else {
			MInteger outBegIdx = new MInteger();
			MInteger outNbElement = new MInteger();
			double outReal[] = new double[fi.length];

			RetCode code = core.ema(startIdx - 1, endIdx - 1, fi, period, outBegIdx,
					outNbElement, outReal);
			if (code != RetCode.Success) {
				return null;
			}
			double ret[] = new double[outNbElement.value];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = outReal[i];
			}
			return ret;
		}
	}
	
	public static double [] ebull (BarSeries series, int startIdx, int endIdx,
			int period) {
		double [] ema = ema(series, BarField.close, period, startIdx, endIdx);
		double [] high = series.getDoubleData(BarField.high);
		double result [] = new double [endIdx - startIdx + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = high[startIdx + i] - ema[i]; 
		}
		return result;
	}
	
	public static double [] ebear (BarSeries series, int startIdx, int endIdx,
			int period) {
		double [] ema = ema(series, BarField.close, period, startIdx, endIdx);
		double [] low = series.getDoubleData(BarField.low);
		double result [] = new double [endIdx - startIdx + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = low[startIdx + i] - ema[i]; 
		}
		return result;
	}
}
