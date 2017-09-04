## Morpheus Visualization API

The Morpheus visualization library defines a **simple chart abstraction API** with adapters supporting both 
[JFreeChart](http://www.jfree.org/jfreechart/) as well as [Google Charts](https://developers.google.com/chart/) (with others
to follow by popular demand). This design makes it possible to generate interactive [Java Swing](https://en.wikipedia.org/wiki/Swing_(Java)) 
charts as well as HTML5 browser based charts via the same API. By default, the framework is configured to use the JFreeChart 
adapter, however this can be re-configured on a global basis by calling either `htmlMode()` or `swingMode()` as shown 
below.

```java
//Switch chart adapter to HTML mode globally 
Chart.create().htmlMode();

//Switch chart adapter to SWING mode globally 
Chart.create().swingMode();
```

It is also possible to operate in **mixed mode** from within the the same application rather than switching the adapter 
globally. By explicitly calling `asHtml()` or `asSwing()` prior to invoking one of the plotting functions on the `Chart`
interface, Html and Swing based charts can be generated from within the same application as shown below.  

```java
//Create chart using SWING adapter
Chart.create().asSwing().withLinePlot(frame, chart -> {
    chart.title().withText("Chart Title Goes Here...");
    chart.legend().on().bottom();
    chart.show();
});

//Create chart using HTML adapter
Chart.create().asHtml().withLinePlot(frame, chart -> {
    chart.title().withText("Chart Title Goes Here...");
    chart.legend().on().bottom();
    chart.show();
});
```

The following sections demonstrate how to use the Morpheus charting API, and provide various examples of what kind of 
charts are supported. The illustrations below are PNG files generated using the JFreeChart adapter, however a [gallery](./gallery1) 
of the same plots generated via the **Google adapter** show just how similar the plots from the two implementations are. 
While most of the functionality exposed by the Morpheus Charting API are supported by both adapters, there are some gaps 
in the Google adapter which are documented below.

## Line Charts

### Single Series

Consider the `DataFrame` below with dimensions `1000x1` which has a row axis of type `LocalDate` and 1 column of 
double precision values representing the cumulative sum of an `Array` of normally distributed random values. The first 
10 rows of this frame are printed below.

```java
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

int rowCount = 1000;
LocalDate startDate = LocalDate.of(2013, 1, 1);
Range<LocalDate> dates = Range.of(0, rowCount).map(startDate::plusDays);
DataFrame<LocalDate,String> frame = DataFrame.of(dates, String.class, columns -> {
    columns.add("A", Array.randn(rowCount).cumSum());
});
```

<div class="frame"><pre class="frame">
   Index     |       A       |
------------------------------
 2013-01-01  |   0.58638321  |
 2013-01-02  |  -0.44176283  |
 2013-01-03  |  -0.07187819  |
 2013-01-04  |  -1.31157143  |
 2013-01-05  |  -1.69375864  |
 2013-01-06  |  -2.23840733  |
 2013-01-07  |  -2.42279587  |
 2013-01-08  |  -2.95871372  |
 2013-01-09  |  -3.63748847  |
 </pre></div>

To generate an line plot of this series, we can use the dates in the `DataFrame` row axis as the **x-values**, and 
the numeric values in column `A` as the range or **y-values**. The `withLines()` method on the `ChartFactory` interface
expects the `DataFrame` containing the data to plot, and a `Consumer` which is used to configure various features of 
the chart. In the example below, we simply display the chart with no further customization by calling `show()`.

```java
Chart.create().withLinePlot(frame, chart -> {
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-basic-1.png"/>
</p>

### Multiple Series

A common scenario is to generate an line plot where the domain or **x-axis** is based on data in a specific column of the frame 
rather than the **row axis** as in the previous example. This can be done by passing the label of the column to use for 
the domain axis to the `withLines()` method. In the example below, we create a similar dataset to above but with dimensions 
`1000x5`, and in this case the row axis is simply a sequence of integers with the dates included as a column keyed as `DataDate`. 

```java
int rowCount = 1000;
LocalDate startDate = LocalDate.of(2013, 1, 1);
Range<Integer> rowKeys = Range.of(0, rowCount);
Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    columns.add("DataDate", dates);
    Stream.of("A", "B", "C", "D").forEach(label -> {
        columns.add(label, Array.randn(rowCount).cumSum());
    });
});
```

<div class="frame"><pre class="frame">
 Index  |   DataDate   |      A       |       B       |       C       |       D       |
---------------------------------------------------------------------------------------
     0  |  2013-01-01  |  1.29439793  |  -0.91248479  |  -0.51141634  |   0.45271667  |
     1  |  2013-01-02  |  1.04502117  |   -1.5841936  |   1.04050209  |    0.3773374  |
     2  |  2013-01-03  |   3.0924616  |  -2.35489228  |   3.35960923  |  -1.52130364  |
     3  |  2013-01-04  |  2.58882443  |  -1.80476051  |   3.79982322  |  -2.03083571  |
     4  |  2013-01-05  |  2.95265199  |  -3.22752153  |   4.92200533  |  -0.05268766  |
     5  |  2013-01-06  |  2.54600084  |  -3.46228629  |   3.34627638  |  -0.73170641  |
     6  |  2013-01-07  |  2.11506239  |  -3.34459359  |   3.68209019  |  -0.80961531  |
     7  |  2013-01-08  |   3.1889199  |  -3.75034967  |    3.7681561  |    0.0668972  |
     8  |  2013-01-09  |  4.83991797  |  -3.21969887  |   2.99041415  |   0.89681662  |
     9  |  2013-01-10  |  4.33025037  |  -2.65846363  |   2.44799574  |   0.62896343  |
</div></pre>

Given that we are plotting multiple series, we also turn on the chart legend, and place it at the bottom of the chart.

```java
Chart.create().withLinePlot(frame, "DataDate", chart -> {
    chart.legend().on().bottom();
    chart.show();
});
```
<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-basic-2.png"/>
</p>

### Series Specific Style

Extending the prior example, below we generate the same `1000x5` dataset but add an additional column post construction 
which represents the sum of values in each row. In addition, we add **text** to the chart in the form of a **title**, 
**subtitle**, **x-axis** and **y-axis** label. Finally, we explicitly configure the chart to render the `Total` column 
in **black**, and using a **thicker** point size to make it distinguishable from the other series.

```java
int rowCount = 1000;
LocalDate startDate = LocalDate.of(2013, 1, 1);
Range<Integer> rowKeys = Range.of(0, rowCount);
Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    columns.add("DataDate", dates);
    Stream.of("A", "B", "C", "D").forEach(label -> {
        columns.add(label, Array.randn(rowCount).cumSum());
    });
});

//Add a total column that sumns A+B+C+D
frame.cols().add("Total", Double.class, v -> v.row().stats().sum());
```

<div class="frame"><pre class="frame">
 Index  |   DataDate   |      A       |       B       |       C       |       D       |     Total     |
-------------------------------------------------------------------------------------------------------
     0  |  2013-01-01  |  1.25934218  |   0.69220501  |  -0.31238595  |  -0.56357062  |   1.07559062  |
     1  |  2013-01-02  |  2.29908995  |   0.17146519  |  -1.60587813  |   0.05049886  |   0.91517587  |
     2  |  2013-01-03  |  2.28895812  |   0.19584665  |  -1.81601295  |  -0.93573356  |  -0.26694175  |
     3  |  2013-01-04  |   0.5587514  |   1.73915178  |   0.29149944  |  -0.29126603  |   2.29813659  |
     4  |  2013-01-05  |  2.11189209  |   0.72883374  |   0.22025859  |  -0.15000083  |   2.91098359  |
     5  |  2013-01-06  |  2.26632523  |  -0.65280247  |  -0.48525074  |   1.66929384  |   2.79756585  |
     6  |  2013-01-07  |  3.32634531  |  -0.37416553  |  -0.71419742  |   0.73102638  |   2.96900874  |
     7  |  2013-01-08  |  2.85345213  |   1.50372178  |   1.14531164  |   1.84564308  |   7.34812862  |
     8  |  2013-01-09  |  1.70266008  |   1.20891251  |   0.87620806  |  -1.10380341  |   2.68397724  |
     9  |  2013-01-10  |  1.90697991  |   2.20887263  |  -1.22202884  |  -0.30028665  |   2.59353704  |
 </div></pre>

```java
Chart.create().withLinePlot(frame, "DataDate", chart -> {
    chart.title().withText("Example Time Series Chart");
    chart.subtitle().withText("Cumulative Sum of Random Normal Data");
    chart.plot().axes().domain().label().withText("Data Date");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.plot().style("Total").withLineWidth(2f).withColor(Color.BLACK);
    chart.legend().on();
    chart.show();
});
```
<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-basic-3.png"/>
</p>

### Multiple Axis

It is often useful to be able to plot multiple series on the same chart even when those series happen to have very
different scales. This is supported by the Morpheus charting API in that is is possible to add many `DataFrames` to
a single chart, and each frame can be found to its own range axis. 

Below we create a frame similar to prior examples, however we impose a larger scale on series `C` and `D` compared 
with `A` and `B`. If we plotted this as a single frame, the scale of `C` and `D` would dominate and it would be hard 
to see changes in `A` and `B`. In order to address this, we filter the frame into 2 sets of columns, and bind the 
second `DataFrame` to a secondary axis via the `setRangeAxis()` method. The arguments to this method are the dataset 
index, and the index of the range axis to bind it to. In principal you can bind as many frames to as many axis, however 
this would rapidly become hard to read.

```java
int rowCount = 1000;
Range<Integer> rowKeys = Range.of(0, rowCount);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    Stream.of("A", "B").forEach(c -> columns.add(c, Array.randn(rowCount).cumSum()));
    Stream.of("C", "D").forEach(c -> {
        columns.add(c, Array.randn(rowCount).mapToDoubles(v -> v.getDouble() * 100).cumSum());
    });
});

Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
    chart.plot().data().add(frame.cols().select("C", "D"));
    chart.plot().data().setRangeAxis(1, 1);
    chart.title().withText("Time Series Chart - Multiple Axis");
    chart.subtitle().withText("Cumulative Sum of Random Normal Data");
    chart.plot().axes().domain().label().withText("Data Date");
    chart.plot().axes().range(0).label().withText("Random Value-1");
    chart.plot().axes().range(1).label().withText("Random Value-2");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-basic-4.png"/>
</p>

### Multiple Renderers

In the prior example, the idea that multiple `DataFrames` can be added to a chart was introduced in order to demonstrate 
how to bind different data series to different axis. This same idea can be used to bind different rendering strategies
to different series.

In the example below, we generate a `DataFrame` with dimensions `20x6` and then plot this data by filtering the frame
into 3 pairs of columns and use different rendering strategies to draw each pair of series. The first frame containing 
columns `A` and `B` is plotted with straight **lines** and **shapes** rendered at each datum. The second frame containing 
columns `C` and `D` is plotted with a **spline** renderer thereby yielding the smooth trajectory for these series. Finally, 
the third frame is rendered with **dashed lines** and no shapes at the datum points.

```java
int rowCount = 20;
Range<Integer> rowKeys = Range.of(0, rowCount);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
        columns.add(label, Array.randn(rowCount).cumSum());
    });
});

Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
    chart.plot().data().add(frame.cols().select("C", "D"));
    chart.plot().data().add(frame.cols().select("E", "F"));
    chart.plot().render(0).withLines(true, false);
    chart.plot().render(1).withSpline(false, false);
    chart.plot().render(2).withLines(false, true);
    chart.title().withText("Time Series Chart - Multiple Renderers");
    chart.subtitle().withText("Cumulative Sum of Random Normal Data");
    chart.plot().axes().domain().label().withText("Data Date");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-basic-5.png"/>
</p>

## Simple Bar Charts

### Discrete Domain Axis

To create bar charts with Morpheus we use an identical API as in the prior examples, but in this case we simply make a 
call to `Chart.ofBars()` instead of `Chart.ofLines()`. Plotting a `DataFrame` where the **domain axis** is initialized 
from the **row axis** keys or alternatively a column of data in the frame works the same way. In the example below we 
generate a bar chart based on a **discrete** or **categorical** domain axis using the frame row axis of type `Year`.
Generating bar charts based on **continuous** data is also possible and is demonstrated in the next example.

```java
Range<Year> years = Range.of(2000, 2006).map(Year::of);
DataFrame<Year,String> data = DataFrame.of(years, String.class, columns -> {
    Stream.of("A", "B", "C", "D").forEach(label -> {
        columns.add(label, Array.of(Double.class, 6).applyDoubles(v -> Math.random()).cumSum());
    });
});

Chart.create().withBarPlot(data, false, chart -> {
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Bar Chart - Categorical Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-bars-1.png"/>
</p>

Switching to a horizontal orientation can be done by simply calling the `orient().horizontal()` method as shown below.

```java
Chart.create().withBarPlot(data, false, chart -> {
    chart.plot().orient().horizontal();
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Bar Chart - Categorical Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-bars-2.png"/>
</p>

### Continuous Domain Axis

Bar charts are often used to display **categorical** or **discrete** data, however there are scenarios where a 
**continuous** variable in the **domain axis** is appropriate. In such cases, the question is how wide to make the bars? 
In a discrete variable bar chart, the width of the bars is sized so that all content fits on the plot, so it is a 
function of how many discrete observations and series exist in the data. When creating a bar chart with a continuous 
variable in the domain, we need to be explicit about how wide we want to make the bars, otherwise they will get 
represented as single vertical line.

Consider the `DataFrame` below with dimensions of `20x1` and a row axis of type `LocalDateTime` where the row key 
interval is 10 minutes wide. The data series `A` is simply initialized to the sum of a randomly generated array of 
values, and the first 10 rows of this frame are shown below.

```java
int rowCount = 20;
LocalDateTime start = LocalDateTime.of(2014, 1, 1, 8, 30);
Range<LocalDateTime> rowKeys = Range.of(0, rowCount).map(i -> start.plusMinutes(i * 10));
DataFrame<LocalDateTime,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    columns.add("A", Array.of(Double.class, rowCount).applyDoubles(v -> Math.random()).cumSum());
});
```

<div class="frame"><pre class="frame">
        Index         |      A       |
--------------------------------------
 2014-01-01T08:30:00  |  0.27653233  |
 2014-01-01T08:40:00  |  0.63501106  |
 2014-01-01T08:50:00  |  1.19265518  |
 2014-01-01T09:00:00  |   1.9021335  |
 2014-01-01T09:10:00  |  2.32102833  |
 2014-01-01T09:20:00  |  3.02188896  |
 2014-01-01T09:30:00  |  3.08679679  |
 2014-01-01T09:40:00  |  3.43408424  |
 2014-01-01T09:50:00  |  4.35521882  |
 2014-01-01T10:00:00  |  5.13252283  |
</pre></div>

If we simply plot this as a bar chart in the usual fashion as per the code below, we end up with a plot where each
observation is represented by a vertical bar at each point in time that is 1 pixel wide. This may indeed be the result
one desires, but given that this `DataFrame` most likely represents some cumulative observations for each 10 minute
interval, it would be nice to explicitly make the bars 10 minutes wide.

```java
Chart.create().withBarPlot(frame, false, chart -> {
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Bar Chart - Continuous Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-bars-3.png"/>
</p>

Thankfully this can be achieved very easily through the API, namely via two methods on the `ChartModel` interface
called `withLowerDomainInterval()` and `withUpperDomainInterval()` which each accept a lambda expression with an
adjustment function. In this particular example, let us assume that the instantaneous times in the `DataFrame` row
axis are the end of the measurement intervals, so the values in series `A` represent the aggregate values for the
prior 10 minutes. We therefore bind an adjustment function that subtracts 10 minutes from row keys as shown in the
code example below, with a call to `withLowerDomainInterval()`.

```java
Chart.create().withBarPlot(frame, false, chart -> {
    chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Bar Chart - Continuous Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-bars-4.png"/>
</p>

The flexibility of the `withLowerDomainInterval()` and `withUpperDomainInterval()` methods means lambdas with any
degree of complexity can be used to change the width of bars, and even make variable width bars on the same plot.

## Stacked Bar Charts

### Discrete Domain Axis

Stacked bar charts are useful for visualizing the decomposition of some quantity into various sub-categories. For 
example, it might be useful to decompose the total revenue of a company into various product and service categories
to visualize how well balanced revenue sources are. Creating a stacked bar plot with the Morpheus API simply boils
down to passing `true` for the second argument of the `withBarPlot()` function. To illustrate, consider a `DataFrame`
with 10 rows and 5 columns of randomly initialized values which can be generated as follows.

```java
Range<Year> years = Range.of(2000, 2010).map(Year::of);
DataFrame<Year,String> frame = DataFrame.of(years, String.class, columns -> {
    Stream.of("A", "B", "C", "D", "E", "F", "G").forEach(label -> {
        columns.add(label, Array.randn(10).applyDoubles(v -> Math.abs(v.getDouble())).cumSum());
    });
});
```

<div class="frame"><pre class="frame">
 Index  |      A       |       B       |       C       |      D       |      E       |      F       |      G       |
--------------------------------------------------------------------------------------------------------------------
  2000  |  1.31995055  |   0.53881463  |    0.3279245  |  0.11259494  |  1.67395423  |  0.18586162  |  0.87989445  |
  2001  |  1.72056405  |   1.68922095  |   0.49067667  |  0.23015735  |  2.13037774  |  0.44835517  |  1.36713607  |
  2002  |  2.79809076  |   3.94390632  |   3.30168445  |  1.29757044  |  3.06911301  |   0.9306128  |  1.80211665  |
  2003  |  3.59071864  |   5.88855153  |   4.37231856  |  2.52348568  |   4.7653336  |  0.94635161  |  3.99710948  |
  2004  |  6.08102384  |    7.7523729  |    4.5286778  |  2.59847512  |  4.85460383  |  2.42353332  |  5.06462412  |
  2005  |  6.55934094  |   9.17683641  |   6.23641685  |  3.34769981  |  4.96006758  |   3.0262002  |  5.14085585  |
  2006  |  7.60618601  |   9.59875411  |   6.42311354  |  3.58823615  |  5.81808152  |  3.59782465  |  6.08934143  |
  2007  |  8.50669158  |  10.79619646  |   8.76745963  |  4.71219626  |  6.06259072  |   4.7197505  |  6.80808702  |
  2008  |   9.3043879  |  10.98733186  |   9.91013585  |  5.78904589  |  7.63390658  |  6.01700338  |  8.27038276  |
  2009  |  9.40761316  |  11.53533956  |  10.32051078  |  5.85462161  |  8.54673512  |   6.1083706  |  9.53989261  |
</pre></div>

We can plot this data using stacked bars with the following code: 

```java
Chart.create().withBarPlot(frame, true, chart -> {
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Stacked Bar Chart - Categorical Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```
<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-stacked-bars-1.png"/>
</p>

Switching to a horizontal orientation can be done by simply calling the `orient().horizontal()` method as shown below.

```java
Chart.create().withBarPlot(frame, true, chart -> {
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.plot().orient().horizontal();
    chart.title().withText("Stacked Bar Chart - Categorical Domain Axis");
    chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-stacked-bars-2.png"/>
</p>

### Continuous Domain Axis

Stacked bar charts involving a continuous domain axis are also supported, much the same way as in the simple bar plot example
discussed earlier. In order to control the **width of the bars**, it is necessary to inject a function that expresses the 
domain interval for each datum in the domain. The bar width can be tailored by applying either a lower internal, upper interval
or both to each observation in the dataset. In the example below we create a `DataFrame` with datetimes to serve as the domain, 
and then proceed to make the bars 10 minutes wide by injecting a **lower** domain interval function:

```java
int rowCount = 40;
LocalDateTime start = LocalDateTime.of(2014, 1, 1, 8, 30);
Range<LocalDateTime> rowKeys = Range.of(0, rowCount).map(i -> start.plusMinutes(i * 10));
DataFrame<LocalDateTime,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
        columns.add(label, Array.randn(40, 1, 5).cumSum());
    });
});

Chart.create().withBarPlot(frame, true, chart -> {
    chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
    chart.plot().axes().domain().label().withText("Year");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Stacked Bar Chart - Continuous Domain Axis");
    chart.subtitle().withText("Random Uniform Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-stacked-bars-3.png"/>
</p>

**Compatibility Note**: The Google Chart adapter **does not** support lower / upper domain interval functions as the underlying library
only supports the ability to specify inter-bar spacing. Bar plots created by the Google adapter that involve a continuous 
domain axis, stacked or otherwise, will by default render bars with no spacing between them, which for the most part seems 
like an appropriate strategy for most scenarios. Calling `withLowerDomainInterval()` or `withUpperDomainInterval()` with the
Google adapter will not have any effect on the output. Future support for more customizable HTML charting libraries may 
address this limitation.

## Histograms

### Single Distribution

The Morpheus Charting API includes some convenience functions to plot frequency distributions of column data in a 
`DataFrame`. In the example below we generate a single column `1000000x1` frame of data randomly generated from a 
standard normal distribution. Calling `withHistPlot()` and indicating the number of bins, 50 in this case, yields
the plot below.

```java
int recordCount = 1000000;
DataFrame<Integer,String> frame = DataFrame.of(Range.of(0, recordCount), String.class, columns -> {
    columns.add("A", Array.randn(recordCount));
});

Chart.create().withHistPlot(frame, 50, chart -> {
    chart.title().withText("Normal Distribution");
    chart.subtitle().withText("Single Distribution");
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-hist-1.png"/>
</p>

### Multiple Distributions

A slight extension of the prior example is to consider a frequency distribution involving multiple series. In this case we 
create a `DataFrame` with 4 columns, where each column of 1 million observations is initialized from a normal distribution 
all with a mean of zero but different standard deviations. The different spreads of each series requires bins to be computed
on a per series basis, as it would not be optimal to create one set of bins across all series. We call the same `withHistPlot()`
function as before, and in this example we generate 100 bins per series to yield the plot below.

```java
int recordCount = 1000000;
DataFrame<Integer,String> frame = DataFrame.of(Range.of(0, recordCount), String.class, columns -> {
    columns.add("A", Array.randn(recordCount, 0d, 1d));
    columns.add("B", Array.randn(recordCount, 0d, 0.8d));
    columns.add("C", Array.randn(recordCount, 0d, 0.6d));
    columns.add("D", Array.randn(recordCount, 0d, 0.4d));
});

Chart.create().withHistPlot(frame, 100, chart -> {
    chart.title().withText("Normal Distribution");
    chart.subtitle().withText("Multiple Distributions");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-hist-2.png"/>
</p>

### Fitted Distribution

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-hist-3.png"/>
</p>

## Scatter Charts

To demonstrate scatter plots, consider the random data generating function defined below, which yields an `nx2` 
`DataFrame` with a column of `X` and `Y` values based on a linear model with noise. The method expects the intercept 
(`alpha`), slope (`beta`), the standard deviation for the noise term (`sigma`), the step size for the domain interval
and the number of data points to generate. The table below illustrates the basic structure of the data.

```java
/**
 * Returns a 2D sample dataset given a slope and intercept while adding white noise based on sigma.
 * @param alpha     the intercept term for data
 * @param beta      the slope term for for data
 * @param sigma     the standard deviation for noise
 * @param stepSize  the step size for domain variable
 * @param n         the size of the sample to generate
 * @return          the frame of XY values
 */
private DataFrame<Integer,String> sample(double alpha, double beta, double sigma, double stepSize, int n) {
    final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> 0d + v.index() * stepSize);
    final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
        final double yfit = alpha + beta * xValues.getDouble(v.index());
        return new NormalDistribution(yfit, sigma).sample();
    });
    final Array<Integer> rowKeys = Range.of(0, n).toArray();
    return DataFrame.of(rowKeys, String.class, columns -> {
        columns.add("X", xValues);
        columns.add("Y", yValues);
    }).cols().demean(true);
}
```

<div class="frame"><pre class="frame">
 Index  |      X      |        Y        |
-----------------------------------------
     0  |  -249.7500  |  -134.80224667  |
     1  |  -249.2500  |  -120.77578676  |
     2  |  -248.7500  |  -119.33607918  |
     3  |  -248.2500  |  -126.59963476  |
     4  |  -247.7500  |  -131.08470325  |
     5  |  -247.2500  |  -120.08991786  |
     6  |  -246.7500  |  -124.15182976  |
     7  |  -246.2500  |   -115.3617302  |
     8  |  -245.7500  |   -119.0303664  |
     9  |  -245.2500  |  -141.49936933  |
</pre></div>

The following examples leverage this data generating function to create various scatter plots.

### Single Series

The example below generates a 1000 point sample dataset using our data generating function, and creates a scatter plot 
via the `withScatterPlot()` method using the column labelled `X` for the domain axis. 

```java
DataFrame<Integer,String> frame = scatter(4d, 0.5d, 20d, 0.5, 1000);

Chart.create().withScatterPlot(frame, false, "X", chart -> {
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Y-Value");
    chart.title().withText("Scatter Chart");
    chart.subtitle().withText("Single DataFrame, Single Series");
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-scatter-1.png"/>
</p>

## Multiple Series

Below we combine 3 `DataFrames` using our data generating function and re-label the various `Y` columns `A`, 
`B` and `C` respectively so that they are distinct in the combined frame. We plot the result using `withScatterPlot()`
in this case, and proceed to set specific colors for each series, and configure the `C` series to render with a 
**diamond** shape. Note that when we combine the frames, the 3 versions of the `X` column collapses into a single
column in the resulting frame based on a combine first rule. Since all 3 frames have identical x-values, this is 
immaterial in this case.

```java
DataFrame<Integer,String> frame = DataFrame.concatColumns(
    scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
    scatter(4d, 6d, 100d, 0.5, 500).cols().replaceKey("Y", "B"),
    scatter(4d, 12d, 180d, 0.5, 500).cols().replaceKey("Y", "C")
);

Chart.create().withScatterPlot(frame, false, "X", chart -> {
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Y-Value");
    chart.plot().style("A").withColor(new Color(255, 225, 25));
    chart.plot().style("B").withColor(new Color(0, 130, 200));
    chart.plot().style("C").withColor(new Color(245, 0, 48));
    chart.title().withText("Scatter Chart");
    chart.subtitle().withText("Single DataFrame, Multiple Series, Custom Style");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-scatter-2.png"/>
</p>

## Multiple Frames

Consider a scenario where you have results from multiple experiments which you would like to combine in a scatter
chart, and while the measurements share the same domain (i.e. x-dimension), the actual observations of `x` are
not the same across the samples. It is possible to create a single sparse `DataFrame` with all the results combined, 
however it is most likely to be more convenient to keep them separate. 

In the example below we generate two `DataFrames`, each containing two series, and the x-values between the two frames 
are **not** coincident since we provide a different step size to our data generating function in each case. We can still 
plot both frames together with very little additional effort, namely by adding the second frame and instructing the chart 
to also render it with dots as follows.

```java
DataFrame<Integer,String> frame1 = DataFrame.concatColumns(
    sample(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
    sample(4d, 3d, 100d, 0.5, 500).cols().replaceKey("Y", "B")
);

DataFrame<Integer,String> frame2 = DataFrame.concatColumns(
    sample(4d, 7d, 80d, 0.55, 600).cols().replaceKey("Y", "C"),
    sample(4d, -10d, 100d, 0.55, 600).cols().replaceKey("Y", "D")
);

Chart.create().withScatterPlot(frame1, false, "X", chart -> {
    chart.plot().data().add(frame2, "X");
    chart.plot().render(1).withDots();
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Y-Value");
    chart.title().withText("Scatter Chart");
    chart.subtitle().withText("Multiple DataFrames, Multiple Series");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-scatter-3.png"/>
</p>

## Regression Charts

### Single Frame

The Morpheus charting API supports fitting a linear trendline to a dataset based on an [Ordinary Least Squares](http://www.zavtech.com/morpheus/regression/ols)
regression model. Both the JFreeChart and Google adapters expose the model equation as a tooltip when hovering over
any of the data points that make up the trendline. By default, the trend line is rendered in black with a thicker
line stroke, however this can easily be adjusted using the standard series `style()` controller.

In the example below we use the same data generating function introduced earlier, and the only additional call is to the 
`trend()` function which accepts the key of the series to which a linear model should be fitted. The resulting plot is shown 
below with the standard style applied to the regression line.

```java
DataFrame<Integer,String> frame = scatter(4d, 1d, 80d, 0.5d, 1000);
Chart.create().withScatterPlot(frame, false, "X", chart -> {
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Y-Value");
    chart.plot().trend("Y");
    chart.title().withText("Regression Chart");
    chart.subtitle().withText("Single DataFrame, Single Series");
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-regress-1.png"/>
</p>

### Multiple Frames

Fitting linear trend lines to multiple series or frames follows naturally through the same API as the single model
example. Below we generate two `DataFrames` each of which has a non-intersecting set of domain values. By calling the 
`trend()` function on the plot and passing the relevant series keys across the two frames, trend lines are generated as 
expected. Once again, the model equations are exposed via tooltips when hovering over data points that make up the 
fitted line.

```java
DataFrame<Integer,String> frame1 = DataFrame.concatColumns(
    scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
    scatter(4d, 4d, 100d, 0.5, 500).cols().replaceKey("Y", "B")
);

DataFrame<Integer,String> frame2 = DataFrame.concatColumns(
    scatter(4d, -3d, 80d, 0.55, 600).cols().replaceKey("Y", "C"),
    scatter(4d, -10d, 100d, 0.45, 600).cols().replaceKey("Y", "D")
);

Chart.create().withScatterPlot(frame1, false, "X", chart -> {
    chart.plot().<String>data().add(frame2, "X");
    chart.plot().render(1).withDots();
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Y-Value");
    chart.plot().trend("A");
    chart.plot().trend("B");
    chart.plot().trend("C");
    chart.title().withText("Regression Chart");
    chart.subtitle().withText("Multiple DataFrame, Multiple Series");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-regress-2.png"/>
</p>

## Area Charts

An area chart is basically the same as a line chart however the area between the domain axis and the line is filled
with a series specific color. There is one variation on this to note, which is that more often than not, area charts 
are generated as stacked areas, which is useful to visualize the decomposition of some aggregate quantity. The two
examples below show a stacked and non-stacked area chart for the identical dataset.

```java
int rowCount = 100;
Range<Integer> rowKeys = Range.of(0, rowCount);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    Stream.of("A", "B", "C", "D", "E").forEach(label -> {
        columns.add(label, Array.randn(rowCount, 10d, 100d).cumSum());
    });
});

Chart.create().withAreaPlot(frame, true, chart -> {
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Stacked Area Chart");
    chart.subtitle().withText("Cumulative Sum of Random Normal Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-area-1.png"/>
</p>

The following code plots the identical dataset but with overlapping areas rather than stacked (which is simply achieved by
passing `stacked=false` to the `withAreaPlot()` method) . Notice the different scale in the y-axis between the two plots, the 
former being of a larger scale given the aggregate nature of that visualization.

```java
int rowCount = 100;
Range<Integer> rowKeys = Range.of(0, rowCount);
DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    Stream.of("A", "B", "C", "D", "E").forEach(label -> {
        columns.add(label, Array.randn(rowCount, 10d, 100d).cumSum());
    });
});

Chart.create().withAreaPlot(frame, false, chart -> {
    chart.plot().render(0).withArea(false);
    chart.plot().axes().domain().label().withText("X-Value");
    chart.plot().axes().range(0).label().withText("Random Value");
    chart.title().withText("Overlapping Area Chart");
    chart.subtitle().withText("Cumulative Sum of Random Normal Data");
    chart.legend().on();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-area-2.png"/>
</p>


## Pie Charts

Pie charts are perhaps the simplest of all the plots, although there are a few customizations that are worth being
aware of. Generally speaking pie plots are used to illustrate the decomposition of some measurement into different
categories, and the Morpheus API allows a specific column of a `DataFrame` to be presented in this way. Pie section 
labels can be customized to show the actual value of the quantity, its percentage of the total or simply the name of 
the category (which can also be shown via an optional legend).

Consider a single column `DataFrame` of random values that we will assume represent some measurement for different
countries which we have defined as the row keys in terms of their 3-character ISO codes.  

```java
DataFrame<String,String> frame = DataFrame.ofDoubles(
    Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
    Array.of("Random"),
    value -> Math.random() * 10d
);
```

<div class="frame"><pre class="frame">
 Index  |    Random    |
------------------------
   AUS  |  4.05807605  |
   GBR  |  2.80847587  |
   USA  |  2.92350137  |
   DEU  |  3.39330077  |
   ITA  |  7.52006927  |
   ESP  |  1.86101073  |
   ZAF  |  4.03358896  |
</pre></div>

### Default Style

The simplest pie chart can be generated via the code below, which by default labels the pie sections with the percentage 
of the total for the column being plotted. In this example we not indicate a specific column in the `DataFrame` to plot, 
in which case the **first numeric column** is chosen. Both the Swing version and Google version of this plot includes 
informative tooltips with additional data as you mouse over each section.

```java
Chart.create().withPiePlot(frame, false, chart -> {
    chart.title().withText("Pie Chart of Random Data");
    chart.subtitle().withText("Labels with Section Percent");
    chart.legend().on().right();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-pie-1.png"/>
</p>

### Donut Pie Plot

A slight adjustment to the pie plot is to include a hole in order to create a donut chart. In this example we also
explicitly pass the name of the column to plot, although given that there is only one numeric column in the `DataFrame`
this is superfluous.

```java
Chart.create().withPiePlot(frame, false, "Random", chart -> {
    chart.title().withText("Donut Pie Chart of Random Data");
    chart.subtitle().withText("Labels with Section Value");
    chart.plot().withPieHole(0.4);
    chart.plot().labels().on().withValue();
    chart.legend().on().right();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-pie-2.png"/>
</p>

### 3D Pie Plot

Turning this into a 3D plot is as easy as passing in `true` as the second argument to the `withPiePlot()` method. 

```java
Chart.create().withPiePlot(frame, true, chart -> {
    chart.title().withText("3D Pie Chart of Random Data");
    chart.subtitle().withText("Labels with Section Name");
    chart.plot().labels().on().withName();
    chart.legend().on().right();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-pie-3.png"/>
</p>

### Exploded Section

Calling out one or more pie sections is possible by imposing an offset from the center as shown below. In this case
we explicitly access the section controller for `AUS` and apply an offset which must be a value between 0 and 1. In
addition, we have rotated the plot clockwise by 90 degrees by calling the `withStartAngle()` method as illustrated. 

```java
Chart.create().withPiePlot(frame, false, chart -> {
    chart.title().withText("Pie Chart of Random Data");
    chart.subtitle().withText("Custom Label Style with Exploded Pie Section");
    chart.plot().labels().withBackgroundColor(Color.WHITE).withFont(new Font("Arial", Font.BOLD, 11));
    chart.plot().section("AUS").withOffset(0.2);
    chart.plot().withStartAngle(90);
    chart.legend().on().right();
    chart.show();
});
```

<p align="center">
    <img class="chart" src="http://www.zavtech.com/morpheus/images/charts/chart-pie-4.png"/>
</p>

----

<p align="center">
    <img style="background: none; border: none;" src="http://www.zavtech.com/morpheus/images/morpheus-logo1.png"/>
</p>
