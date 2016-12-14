package Frames;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Created by Jakub on 10.10.2016.
 *
 * A dynamic chart embedded in JPanel
 */
public class DynamicTimeChart extends JPanel {

    private final DynamicTimeSeriesCollection   dataset;
    private final JFreeChart                    chart;

    public DynamicTimeChart(final String title, final String yTitle, final int xSize, final int ySize) {
        dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
        dataset.setTimeBase(new Second());
        dataset.addSeries(new float[1], 0, title);

        chart = ChartFactory.createTimeSeriesChart(
                title,
                "time",
                yTitle,
                dataset,
                false,
                false,
                false
        );

        chart.setBackgroundPaint(new Color(238, 238, 238));

        final XYPlot plot = chart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setFixedAutoRange(10000);
        axis.setDateFormatOverride(new SimpleDateFormat("ss.SS"));
        axis.setVisible(false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(xSize, ySize));
        add(chartPanel);
    }

    public void update(float value) {
        float[] newData = new float[1];
        newData[0] = value;
        dataset.advanceTime();
        dataset.appendData(newData);
    }
}
