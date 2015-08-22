#The features of True Trade

# Features #

TrueTrade attempts to provide a powerful, feature-rich backtesting and automated trading system.  The current features and goals are:

  * Complex order types.  Currently supported are _market, stop, stop limit and trailing_ orders.  Want to add support for _one cancels all_ and other orders

  * Risk management support.  Position sizes are controlled by the strategy.  Want to provide support for account management

  * Variable position size depending on the entry signal, the price of the instrument or whatever other features you desire.  This includes entry and exit orders, so you can scale in and out of a position.

  * numerous time scales.  Strategies can request onBar() messages at whichever time frame they request.  Backtesting will be driven from a single data source, but will simulate onTrade messages to provide bars at higher and lower granularity.

  * multiple data sources.  TrueTrade supports downloading historical data from OpenTick, Yahoo and InteractiveBrokers and importing from any flat-file format through the use of customizable import templates.  Templates may be saved and reused to make importing from different data sources easier.

  * Responsive user interface.  TrueTrade is built upon Eclipse technology and features user-friendly windows-based navigation to test your strategies.  In runtime mode, only the essential information is presented to minimize memory and CPU usage.