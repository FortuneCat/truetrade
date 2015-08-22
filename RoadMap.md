#A wishlist of features with their targeted releases

# v0.6 Targeted Features #
  * Runtime reliability enhancements
    * extensive testing
    * save positions with entries & exits to DB to allow restarts
    * supporting stopping & restarting
    * support timeouts and resubmission of all requests
    * support internet interruptions
    * differentiate between SELL and SHORT
    * add TIF
    * add OCA groups
  * Sample "gapper" strategies which use external hotlists and datasources to gather stocks to trade
  * Usability enhancements
    * deleting imported data series
    * charting price & time indications
  * DB enhancements
    * performance tuning, query optimization
    * in-memory table
    * indexing
  * Optimization
    * simple brute-force against max two parameters with fixed steps
    * simple table output

Estimated release date: 14 June 2007


# Wishlist #

To start, these are a brainstorm of ideas for future versions:
  * JUnit support
  * ANT build script
  * Full OpenTick support
  * fix DataProvider interface to allow easy plugin of new providers
  * improved IB Data download support (more than just 3weeks of data)
  * auto-saving trade data to database
  * Runtime reliability enhancements, allowing strategies to be run with real money
    * extensive testing
    * remove defects
    * save positions with entries & exits to DB
    * support stopping & restarting
    * support internet interruptions
    * differentiate between SELL and SHORT
    * add TIF
    * add OCA groups
  * memory improvement so handling large datasets doesn't hurt the system
  * speed improvements so backtesting large datasets runs faster and smoother
  * smoother install
    * DB configuration wizard
    * classpath wizard
    * embedded DB support
    * install shield?
  * Strategy enhancements
    * allow exit (target and stoploss) orders to be attached to the entry order
    * helper functions for sizing and risk management
    * Portfolio utilities to gather current margin levels and current portfolio balance
  * Sample "gapper" strategies which use external hotlists and datasources to gather stocks to trade
  * Strategy optimization and genetic algorithms
  * Alerts
    * SMACK API to communicate with Google Talk
    * e-mail notification
  * Documentation
    * User's guide
    * Strategy guide with samples
    * Developer's guide
    * Architecture overview with diagrams
  * Supporting strategies with multiple symbols or data sources simultaneously, e.g.: ES data with the TICK or VIX

Some external suggestions:
  * Pluggable data feeds (acknowledging performance hit taken by having the flexibility)
  * Support for retrieving and incorporating non-standard market data e.g. for semantic news analysis etc.
  * Scripting of strategies or data retrieval
  * add onMarketDepth() and onQuote() methods
  * add tickFilter or orderFilter methods to simplify the processing of messages
  * adding Monte Carlo simulations to do a full analysis of expected drawdowns using fixed or fractional money management.  A primer can be found in [Acrary's system design thread](http://www.elitetrader.com/vb/showthread.php?s=&threadid=33654).


# Resources #
Genetic Algorithms: http://jgap.sourceforge.net/

Neural Networks: http://www.jooneworld.com/


# Milestones #