package com.zavtech.morpheus.viz.chart;

import java.awt.Font;
import java.time.LocalDate;
import java.util.Arrays;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import org.junit.Test;
import org.testng.Assert;

public class ChartDatasetTest {

    @Test()
    public void testTimeSeries1() {
        final Array<LocalDate> dates = Range.ofLocalDates("2000-01-01", "2005-01-01").toArray();
        final Array<String> series = Array.of("AAPL", "ORCL", "BLK", "GOOG");
        final DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(dates, series).applyDoubles(v -> Math.random());
        final ChartModel<LocalDate,String> dataset = ChartModel.of(() -> frame);
        Assert.assertEquals(dataset.getDomainKeyType(), LocalDate.class);
        Assert.assertEquals(dataset.getSeriesKeyType(), String.class);
        Assert.assertEquals(dataset.getSeriesCount(), 4);
        Assert.assertEquals(dataset.getSize(), dates.length());
        for (int i=0; i<dataset.getSeriesCount(); ++i) {
            Assert.assertEquals(dataset.getSeriesKey(i), series.getValue(i));
        }
        for (int i=0; i<dataset.getSize(); ++i) {
            Assert.assertEquals(dataset.getDomainKey(i), dates.getValue(i));
        }
    }


    @Test()
    public void testTimeSeries2() {
        final Array<LocalDate> dates = Range.ofLocalDates("2000-01-01", "2005-01-01").toArray();
        final Array<Integer> rowKeys = Range.of(0, dates.length()).toArray();
        final Array<String> series = Array.of("AAPL", "ORCL", "BLK", "GOOG");
        final DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, series).applyDoubles(v -> Math.random());
        frame.cols().add("Date", dates);
        frame.out().print();
        final ChartModel<Integer,String> dataset = ChartModel.of("Date", () -> frame);
        Assert.assertEquals(dataset.getDomainKeyType(), LocalDate.class);
        Assert.assertEquals(dataset.getSeriesKeyType(), String.class);
        Assert.assertEquals(dataset.getSeriesCount(), 4);
        Assert.assertEquals(dataset.getSize(), dates.length());
        for (int i=0; i<dataset.getSeriesCount(); ++i) {
            Assert.assertEquals(dataset.getSeriesKey(i), series.getValue(i));
        }
        for (int i=0; i<dataset.getSize(); ++i) {
            Assert.assertEquals(dataset.getDomainKey(i), dates.getValue(i));
        }
    }

    @Test()
    public void testCombiningDatasets() {
        final Range<LocalDate> range1 = Range.ofLocalDates("2000-01-01", "2001-01-01");
        final Range<LocalDate> range2 = Range.ofLocalDates("2002-01-01", "2003-01-01");
        final Range<LocalDate> range3 = Range.ofLocalDates("2000-01-01", "2005-01-01");
        final DataFrame<LocalDate,String> frame1 = DataFrame.ofDoubles(range1, Arrays.asList("S1", "S2")).applyDoubles(v -> Math.random());
        final DataFrame<LocalDate,String> frame2 = DataFrame.ofDoubles(range2, Arrays.asList("S3", "S4")).applyDoubles(v -> Math.random());
        final DataFrame<LocalDate,String> frame3 = DataFrame.ofDoubles(range3, Arrays.asList("S5", "S6")).applyDoubles(v -> Math.random());
        final ChartModel<LocalDate,String> dataset1 = ChartModel.of(() -> frame1);
        final ChartModel<LocalDate,String> dataset2 = ChartModel.of(() -> frame2);
        final ChartModel<LocalDate,String> dataset3 = ChartModel.of(() -> frame3);
        final ChartModel<LocalDate,String> dataset = ChartModel.combine(Arrays.asList(dataset1, dataset2, dataset3));
        Assert.assertEquals(dataset.getSeriesCount(), 6);
        Assert.assertEquals(dataset.getSize(), range3.toArray().length());
    }


    @Test()
    public void testRangeTicks() {
        final Font font = new Font("Arial", Font.BOLD | Font.ITALIC, 12);
        System.out.println("Italic=" + (font.getStyle() == (Font.BOLD | Font.ITALIC)));
        System.out.println("Bold=" + (font.getStyle() == (Font.BOLD | Font.ITALIC)));
    }

}
