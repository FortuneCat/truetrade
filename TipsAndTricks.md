#Tips and tricks for running True Trade

# Introduction #

As benefits any early release program, many functions work but some require _work arounds_.  This is a collection of tips for would-be users.

The goal is to identify as many tricks as possible so people can use it, and then improve TrueTrade so we don't need tricks!

# Details #

  * When making a lot of changes to a strategy, run the TrueTrade Client Builder in **Debug** mode.  Most of the time, Eclipse will pick up the changes without requiring a restart.

  * Develop and test your strategy using a relatively small dataset so the tests run quickly and you can do a rough visual verification that the tests run as anticipated.  When it does what you want it to do, then scale up to test with bigger datasets.

  * Once you're ready to test with a lot of back data, TradingBlox offers [free historical EOD data](http://www.tradingblox.com/tradingblox/free-historical-data.htm) going back over a decade.