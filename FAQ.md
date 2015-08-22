#Frequently Asked Questions

**Q: Where can I go to ask questions?**

A: Use the [TrueTrade Thread](http://www.elitetrader.com/vb/showthread.php?s=&threadid=93630) on EliteTrader.com to ask question or offer suggestions.

**Q: Where do I put my custom Strategies?**

A: In a full release, users will create a standard Java JAR which will be placed in the com.ats.resources plugin.  During development, it is more convenient to place them in the com.ats.model plugin so that Eclipse can perform incremental builds.  If you wish to avoid having your custom strategies placed in SVS, add an SVS ignore rule.


**Q: I get a message which says "An error has occurred.  See error log for more details."  What does this mean?  Where is the error log?**

A:  This means that an exception has been thrown which was not caught and filtered back to the Eclipse RCP framework.  The exception and all associated stack traces are stored in the `runtime-EclipseApplication/.metadata/.log` file which will be located in which ever directory your eclipse workspace is found.

**Q: I heard there was an alternate charting view.  How do I use it?**

A: There is.  My goal is to drop JFreeChart for the candlestick chart for the next release.  To use the custom charting system which supports selecting trades and executions, and much faster scrolling (but no labels or grids yet):
  * Open `com.ats.client.perspectives.BacktestPerspective`
  * Replace the line
> > `folder.addView(JFreeChartView.ID);`

> With
> > `folder.addView(ChartView.ID);`

**Q: How can I join your project?**

A: Thanks for your interest, we love to grow the community!  You can start by installing the source and running it on your own system.  Then:
  * file clear, easy to replicate defects
  * create new test cases
  * find some defects or features that you want to fix and then fix them.  When you're done, generate a patch file from SVN and e-mail it to the current defect owner.
  * review the RoadMap and send in additional suggestions or pick a feature, e-mail the feature owner and start working on it
  * When you've had a few defects and features added, e-mail the project owner and ask to be added as an official member