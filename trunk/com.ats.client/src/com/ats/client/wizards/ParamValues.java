package com.ats.client.wizards;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ParamValues {
	public String paramName;
	public Number initVal;
	public Number start;
	public Number finish;
	public Number stepSize;
	public int numTrials;
	
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("paramName", paramName)
			.append("start", start)
			.append("finish", finish)
			.append("stepSize", stepSize)
			.toString();
	}
}
