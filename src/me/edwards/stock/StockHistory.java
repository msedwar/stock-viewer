package me.edwards.stock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StockHistory
{
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    
    private String ticker;
    private float maxHigh;
    private float maxLow;
    private float maxVolume;
    private float totalVolume;

    private Date[] date;
    private float[] open;
    private float[] close;
    private float[] high;
    private float[] low;
    private float[] volume;
    
    private StockHistory()
    {
        //
    }
    
    public int getDataSize()
    {
        return date.length;
    }
    
    public String getTicker()
    {
        return ticker;
    }
    
    public float getOpen(int index)
    {
        return open[index];
    }
    
    public float getClose(int index)
    {
        return close[index];
    }

    public float getHigh(int index)
    {
        return high[index];
    }
    
    public float getMaxHigh()
    {
        return maxHigh;
    }

    public float getLow(int index)
    {
        return low[index];
    }
    
    public float getMaxLow()
    {
        return maxLow;
    }
    
    public float getVolume(int index)
    {
        return volume[index];
    }
    
    public float getMaxVolume()
    {
        return maxVolume;
    }
    
    public Date getBegin()
    {
        return date[0];
    }
    
    public Date getEnd()
    {
        return date[date.length - 1];
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("TICKER: ");
        sb.append(ticker);
        sb.append("\nDATE: ");
        sb.append(DF.format(date[0]));
        sb.append(" -> ");
        sb.append(DF.format(date[date.length - 1]));
        sb.append(" (");
        sb.append(date.length);
        sb.append(")");
        sb.append("\nHIGH: ");
        sb.append(maxHigh);
        sb.append("\nLOW: ");
        sb.append(maxLow);
        sb.append("\nVOLUME: ");
        sb.append(maxVolume);
        sb.append("\nTOTAL VOLUME: ");
        sb.append(totalVolume);
        return sb.toString();
    }
    
    public static StockHistory generateStock(String ticker, Date begin, Date end) throws IOException, ParserConfigurationException, SAXException
    {
        StockHistory sh = new StockHistory();
        sh.ticker = ticker;

        URL url = YQLBuilder.getURL(YQLBuilder.createQuery("yahoo.finance.historicaldata", ticker, DF.format(begin), DF.format(end)));
        InputStream stream = url.openStream();
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        
//        System.out.println(XMLUtil.xmlToString(doc));
        
        NodeList quotes = doc.getElementsByTagName("results").item(0).getChildNodes();
        int quoteNum = quotes.getLength();
        
        if (quoteNum == 0)
        {
            return null;
        }

        sh.date = new Date[quoteNum];
        sh.open = new float[quoteNum];
        sh.close = new float[quoteNum];
        sh.high = new float[quoteNum];
        sh.low = new float[quoteNum];
        sh.volume = new float[quoteNum];
        
        try
        {
            for (int i = 0; quoteNum > i; i++)
            {
                NodeList quoteVals = quotes.item(i).getChildNodes();
                int arrayIndex = quoteNum - 1 - i;
                sh.date[arrayIndex] = DF.parse(quoteVals.item(0).getTextContent());
                sh.open[arrayIndex] = Float.parseFloat(quoteVals.item(1).getTextContent());
                sh.high[arrayIndex] = Float.parseFloat(quoteVals.item(2).getTextContent());
                sh.low[arrayIndex] = Float.parseFloat(quoteVals.item(3).getTextContent());
                sh.close[arrayIndex] = Float.parseFloat(quoteVals.item(4).getTextContent());
                sh.volume[arrayIndex] = Float.parseFloat(quoteVals.item(5).getTextContent());
                
                sh.totalVolume += sh.volume[arrayIndex];
                
                if (sh.maxHigh == 0 || sh.high[arrayIndex] > sh.maxHigh)
                {
                    sh.maxHigh = sh.high[arrayIndex];
                }
                if (sh.maxLow == 0 || sh.low[arrayIndex] < sh.maxLow)
                {
                    sh.maxLow = sh.low[arrayIndex];
                }
                if (sh.maxVolume == 0 || sh.volume[arrayIndex] > sh.maxVolume)
                {
                    sh.maxVolume = sh.volume[arrayIndex];
                }
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        
        return sh;
    }
}
