package com.ats.engine;

/**
 * The central class which runs all strategies.
 * 
 * 1.  Parse all arguments/parameters to determine:
 *     - mode (live, simulation, backtesting)
 *     - which strategies and instruments
 *     - start/end dates (backtesting)
 * 
 * 2.  Initialize all managers & drivers
 *     - in live mode, store executions and state info to DB
 *     - in backtesting mode, store executions in an object for later rendering
 * 
 * 3.  Start the strategies
 * 
 * 4.  Wait for strategies to complete or a halt request
 * 
 * 
 * @author Adrian
 *
 */
public class ATS {

}
