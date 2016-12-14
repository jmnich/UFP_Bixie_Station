package Frames;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import sun.awt.resources.awt;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Created by Jakub on 10.10.2016.
 *
 * A dynamic chart embedded in JPanel (shows 3 series of data)
 */
public class DynamicTimeChart3Series extends JPanel {

    private final DynamicTimeSeriesCollection   dataset;
    private final JFreeChart                    chart;

    public DynamicTimeChart3Series(final String title, final String ser1Title, final String ser2Title,
                                   final String ser3Title, final String yTitle, final int xSize, final int ySize) {
        dataset = new DynamicTimeSeriesCollection(3, 1000, new Second());
        dataset.setTimeBase(new Second());
        dataset.addSeries(new float[1], 0, ser1Title);
        dataset.addSeries(new float[1], 1, ser2Title);
        dataset.addSeries(new float[1], 2, ser3Title);

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

        plot.setBackgroundPaint(new Color(0xffffe0));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setFixedAutoRange(10000);
        axis.setDateFormatOverride(new SimpleDateFormat("ss.SS"));
        axis.setVisible(false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(xSize, ySize));
        add(chartPanel);
    }

    public void update(float value1, float value2, float value3) {
        float[] newData = new float[3];
        newData[0] = value1;
        newData[1] = value2;
        newData[2] = value3;
        dataset.advanceTime();
        dataset.appendData(newData);
    }
}
