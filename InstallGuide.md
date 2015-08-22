#Install Guide

# Introduction #

This is the install guide.  **A work in progress**

# Details #

## Install from ZIP ##

Note that the ZIP files are limited.  You will not be able to create and run your own strategies.  Users are encouraged to use the source distribution (below).

  1. Download and install [Java 6](http://java.sun.com/javase/downloads/index.jsp)
  1. Download and install [MySQL v5+](http://mysql.org/downloads/mysql/5.0.html#downloads)
  1. Configure the MySQL Database using the [create\_tables.sql](http://truetrade.googlecode.com/svn/trunk/com.ats.model/src/create_tables.sql) script
  1. Download and unzip latest [TrueTrade Client Builder](http://code.google.com/p/truetrade/downloads/detail?name=TrueTradeClient.zip&can=2&q=) file from the Downloads
  1. Unpack the zip file and run `TrueTrade/TrueTrade.exe`.  It will probably generate lots of errors because your database hasn't been configured yet.
  1. Run Windows > Preferences > Databases and set the following:
    * JDBC URL: `jdbc:mysql://localhost/ats?enable-named-pipe&socketFactory=com.mysql.jdbc.NamedPipeSocketFactory `
    * JDBC Provider: ` com.mysql.jdbc.Driver `
  1. Click Apply or OK.  You will probably get an error message saying that there was a problem, but it shouldn't affect the saving of your preferences (defect 26).  Exit and then restart.  The client should come up without any errors


## Install from Source ##

  1. Download and install [Java 6](http://java.sun.com/javase/downloads/index.jsp)
  * Download and install [eclipse v3.2](http://www.eclipse.org/downloads/)
  * Install Subclipse plugin (http://subclipse.tigris.org/):
    * In Eclipse, run Help > Software Updates > Find and Install...
    * In the "Feature Updates" window, click "Search for new features to install" and click "Next"
    * Click "New Remote Site", and in the dialog enter:
      * Name: Subclipse Update Site
      * URL: http://subclipse.tigris.org/update_1.2.x
> > > Then make sure your new update site is selected and click Finish
    * In the "Search Results" dialog, expand the "Subclipse Update site" entry and select "Subclipse" and follow the rest of the wizard.
  * Install and [Draw2d and GEF](http://www.eclipse.org/gef/) from eclipse.org:
    * In Eclipse, run Help > Software Updates > Find and Install...
    * In the "Feature Updates" window, click on "Search for new features to install" and click "Next"
    * Select "Callisto Discovery Site" and click "Finish".  It will present a list of mirrors so select one close to you and click OK.
    * In the search results, expand "Callisto Discover Site" and expand "Models and Model Development" and select "Graphical Modeling Framework".  You will get an error about requirements, so click "Select Required" and click "Next".  Complete the rest of the wizard.
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/TrueTradeGEF.jpg](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/TrueTradeGEF.jpg)

  * Download and install [MySQL v5+](http://mysql.org/downloads/mysql/5.0.html#downloads)
  * Start Eclipse and select a new directory for the ATS workspace:
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/1-select-workspace.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/1-select-workspace.png)

> When Eclipse starts, you will have a blank workspace:
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/2-blank-workspace.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/2-blank-workspace.png)
  * In the Package Explorer, "Right Click > Import > Other > Checkout Projects from SVN" ![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/import-projects.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/import-projects.png)
> Select "Create a new repository location" and in the "Select/Create Location" field, enter "`http://truetrade.googlecode.com/svn/trunk/`" and click Next.  (_Google tells you to use "`http://truetrade.googlecode.com/svn/trunk/truetrade`" but I have not been able to get this to work, -t._)  You should see a listing of the different projects stored in SVN:
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/select-folder.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/select-folder.png)
  * Select `com.ats.resources` and click "Next".  In the "Check Out As" page, you can select "Revision" then click "Show Log" to see which versions exist.  Select v0.5Alpha or the latest version similar to this.  If you want the latest code, select "Head Revision" (_I haven't figured out versioning yet, so you'll have to start with Head until I do, sorry. -t._)  Click Finish.
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/check-out-as.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/check-out-as.png)
    * When it completes, you will have errors in the project.  Ignore this for the moment, we will fix the errors in a couple steps
> > ![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/import-com-ats-resources.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/import-com-ats-resources.png)
  * Repeat this process with the projects  (ignore 'com.ats.client.custom'):
    * `com.ats.model`
    * `com.ats.client`
    * {{com.ats.client.runtime}}}
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/all-imported.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/all-imported.png)
  * Many errors will go away, but some remain.  To fix these, go to "Window > Preferences > Java > Compiler" and set the "Compiler compliance level" to 5.0.  When you click OK, it will ask to do a full rebuild, click OK.
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/java-compiler-prefs.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/java-compiler-prefs.png)

> If you still have any errors, do a "Team > Update" to get the latest source.

  * Create the MySQL database and tables using com.ats.model/create\_tables.sql
Now that you have the Source, you can run the client.  To do this, you will need to configure the Eclipse runtime.
  * Right-click the "com.ats.client" project and select "Run As > Eclipse Application".  You will get a splash screen and then the TrueTrade Client will come up with nothing in it.
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/true-trade-client.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/true-trade-client.png)

  * Exit the TrueTrade Client and, back in Eclipse, open "Run > Run..." to get the Run dialog.  Select "Eclipse Application > Eclipse Application".  This is the client builder, so change the name to "TrueTrade Client builder" and in the Arguments tab add the VM argument -Xmx256m".
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/run-dlg.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/run-dlg.png)
> > Click "Apply" and "Close".  Now the Run button should have the TrueTrade client ready to run.
![http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/run-pulldown.png](http://truetrade.googlecode.com/svn/trunk/com.ats.client/html/gettingstarted/run-pulldown.png)

  * Now you can add instruments and strategies (there are sample strategies in the com.ats.model project under com.ats.strategy).


## Common Problems ##

  * If you have installed MySQL and ran the create\_tables.sql script but you see NullPointerExceptions appear in the TrueTradeClient when you start it up, check the Eclipse console.  If you see messages like:
```
java.io.FileNotFoundException
MESSAGE: \\.\pipe\MySQL (The system cannot find the file specified)

STACKTRACE:

java.io.FileNotFoundException: \\.\pipe\MySQL (The system cannot find the file specified
	at java.io.RandomAccessFile.open(Native Method)
	at java.io.RandomAccessFile.(Unknown Source)
	at java.io.RandomAccessFile.(Unknown Source)
	at com.mysql.jdbc.NamedPipeSocketFactory$NamedPipeSocket.(NamedPipeSocketFactory.java:57)
```

> Then you will need to remove the NamedPipeSocketFactory from the MySQL URL.  In the TrueTrade Client, run "Windows > Preferences > Databases" and change the URL to `jdbc:mysql://localhost/ats`  (i.e.: remove the "?" and everything after it)

