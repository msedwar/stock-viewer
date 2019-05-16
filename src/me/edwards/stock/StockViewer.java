package me.edwards.stock;

import java.awt.BorderLayout;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class StockViewer
{
    public static final String VERSION = "v0.1";
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    
    private JFrame frame;
    private StockViewport viewport;
    
    public StockViewer(String ticker, String start, String end)
    {
        frame = new JFrame("StockViewer " + VERSION);
        frame.setLayout(new BorderLayout());
        
        viewport = new StockViewport();
        frame.add(viewport, BorderLayout.CENTER);
        
        frame.setSize(1200, 675);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        try
        {
            StockHistory sh = StockHistory.generateStock(ticker, DF.parse(start), DF.parse(end));
            viewport.setViewport(sh);
            Thread.sleep(1000);
            viewport.repaint();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        new StockViewer(args[0], args[1], args[2]);
    }
}
