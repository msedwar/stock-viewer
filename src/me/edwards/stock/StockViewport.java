package me.edwards.stock;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.JComponent;

public class StockViewport extends JComponent
{
    private StockHistory viewport;
    
    public StockViewport()
    {
        this(null);
    }
    
    public StockViewport(StockHistory viewport)
    {
        this.viewport = viewport;
    }
    
    public StockHistory getViewport()
    {
        return viewport;
    }
    
    public void setViewport(StockHistory viewport)
    {
        this.viewport = viewport;
        repaint();
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (viewport == null)
        {
            g.setFont(new Font("Monaco", Font.PLAIN, 16));
            g.setColor(Color.WHITE);
            String noStock = "NO STOCK FOUND";
            Rectangle2D noStockBounds = g.getFontMetrics().getStringBounds(noStock, g);
            g.drawString(noStock, (int) (getWidth() - noStockBounds.getWidth()) / 2, (int) (getHeight() - noStockBounds.getHeight() / 2) / 2);
            g.drawRect((int) (getWidth() - noStockBounds.getWidth()) / 2 - 10, (int) (getHeight() - noStockBounds.getHeight() * 2) / 2 - 10, (int) noStockBounds.getWidth() + 20, (int) noStockBounds.getHeight() + 20);
            
        }
        else
        {
            paintLine(g, new Rectangle(0, 0, getWidth(), getHeight() - 150));
            paintVolume(g, new Rectangle(0, getHeight() - 150, getWidth(), 150));
            paintStamp(g, new Rectangle(10, 15, 0, 0));
        }
    }
    
    private void paintStamp(Graphics g, Rectangle bounds)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        g.setFont(new Font("Monaco", Font.PLAIN, 10));
        g.setColor(Color.WHITE);
        g.drawString(viewport.getTicker(), bounds.x, bounds.y);
        float currPrice = viewport.getClose(viewport.getDataSize() - 1);
        float prevPrice = viewport.getClose(viewport.getDataSize() - 2);
        float change = (currPrice - prevPrice) / prevPrice;
        g.setColor(currPrice >= prevPrice ? Color.GREEN : Color.RED);
        g.drawString(moneyFormat(currPrice) + " (" + changeFormat(change) + ") " + (change < 0 ? "\u25BC" : "\u25B2"), bounds.x + g.getFontMetrics().stringWidth(viewport.getTicker() + " "), bounds.y);
        g.setColor(Color.WHITE);
        g.drawString(shortFormatNumber(viewport.getVolume(viewport.getDataSize() - 1)), bounds.x, bounds.y + 10);
        g.drawString("(" + df.format(viewport.getBegin()) + " -> " + df.format(viewport.getEnd()) + ")", bounds.x, bounds.y + 20);
    }
    
    private void paintLine(Graphics g, Rectangle bounds)
    {
        float maxVal = (viewport.getMaxHigh() + (viewport.getMaxHigh() - viewport.getMaxLow()) / 10);
        float minVal = (viewport.getMaxLow() - (viewport.getMaxHigh() - viewport.getMaxLow()) / 10);
        
        int numBars = (int) (bounds.height / 20);
        for (int i = 0; numBars > i; i++)
        {
            int y = (int) (bounds.y + bounds.height - (bounds.height / numBars * i));
            g.setColor(new Color(50, 50, 50));
            g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monaco", Font.PLAIN, 10));
            g.drawString(moneyFormat((maxVal - minVal) / (numBars - 1) * i + minVal), bounds.x + bounds.width - 45, y - 2);
        }
        
        float w = (float) (bounds.width - 50) / viewport.getDataSize();
        float h = (bounds.height / (maxVal - minVal));
        
        float prevClose = 0;
        float xAgg = bounds.x;
        for (int i = 0; viewport.getDataSize() > i; i++)
        {
            float highVal = Math.max(viewport.getOpen(i), viewport.getClose(i));
            float lowVal = Math.min(viewport.getOpen(i), viewport.getClose(i));
            int width = (int) w + (int) ((i * w) - (xAgg - bounds.x));
            g.setColor(viewport.getClose(i) > prevClose ? Color.GREEN : Color.RED);
            g.drawLine((int) xAgg + width / 2, bounds.y + bounds.height - (int) ((viewport.getHigh(i) - minVal) * h), (int) xAgg + width / 2, bounds.y + bounds.height - (int) ((viewport.getLow(i) - minVal) * h));
            g.fillRect((int) xAgg, bounds.y + bounds.height - (int) ((highVal - minVal) * h), width, Math.max(1, (int) ((highVal - lowVal) * h)));
            xAgg += width;
            prevClose = viewport.getClose(i);
        }
        g.setColor(Color.WHITE);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    private void paintMovingAverage(Graphics g, Rectangle bounds, int avg)
    {
        //
    }
    
    private void paintVolume(Graphics g, Rectangle bounds)
    {
        int numBars = (int) (bounds.height / 20);
        for (int i = 0; numBars > i; i++)
        {
            int y = (int) (bounds.y + bounds.height - (bounds.height / numBars * i));
            g.setColor(new Color(50, 50, 50));
            g.drawLine(bounds.x, y, bounds.x + bounds.width, y);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monaco", Font.PLAIN, 10));
            g.drawString(shortFormatNumber(viewport.getMaxVolume() / (numBars - 1) * i), bounds.x + bounds.width - 45, y - 2);
        }
        
        float w = (float) (bounds.width - 50) / viewport.getDataSize();
        float h = (bounds.height * (numBars - 1) / (float) numBars) / viewport.getMaxVolume();
        
        float prevVolume = 0;
        float xAgg = bounds.x;
        for (int i = 0; viewport.getDataSize() > i; i++)
        {
            int width = (int) w + (int) ((i * w) - (xAgg - bounds.x));
            g.setColor(viewport.getVolume(i) > prevVolume ? Color.GREEN : Color.RED);
            g.fillRect((int) xAgg, bounds.y + bounds.height - (int) (viewport.getVolume(i) * h), width, (int) (viewport.getVolume(i) * h));
            xAgg += width;
            prevVolume = viewport.getVolume(i);
        }
        g.setColor(Color.WHITE);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    private String changeFormat(float num)
    {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(num);
    }
    
    private String moneyFormat(float num)
    {
        char app = ' ';
        if (num < 1000)
        {
            //
        }
        else if (num / 1000 < 1000)
        {
            app = 'K';
            num /= 1000;
        }
        else if (num / 1000000 < 1000)
        {
            app = 'M';
            num /= 1000000;
        }
        else if (num / 1000000000 < 1000)
        {
            app = 'B';
            num /= 1000000000;
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        return nf.format(num) + app;
    }
    
    private String shortFormatNumber(float num)
    {
        char app = ' ';
        if (num < 1000)
        {
            //
        }
        else if (num / 1000 < 1000)
        {
            app = 'K';
            num /= 1000;
        }
        else if (num / 1000000 < 1000)
        {
            app = 'M';
            num /= 1000000;
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        String str = nf.format(num);
        return str + app;
    }
}
