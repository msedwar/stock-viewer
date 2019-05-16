package me.edwards.stock;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class YQLBuilder
{
    private static final String url = "http://query.yahooapis.com/v1/public/yql?q=%QUERY%&env=%ENV%";
    private static final String env = "https://raw.githubusercontent.com/cynwoody/yql-tables/finance-1/tables.env"; //"store://datatables.org/alltableswithkeys";
    private static final String basicQuery = "select * from %TABLE% where symbol in ( \"%SYMBOL%\" ) and startDate = \"%SDATE%\" and endDate = \"%EDATE%\"";
    
    public static URL getURL(String query)
    {
        try
        {
            return new URL(getURLString(query));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getURLString(String query)
    {
        return url.replaceFirst("%QUERY%", getEncodedString(query)).replaceFirst("%ENV%", env);
    }
    
    public static String getEncodedString(String query)
    {
        try
        {
            return URLEncoder.encode(query, "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String createQuery(String table, String symbol, String startDate, String endDate)
    {
        return basicQuery.replaceFirst("%TABLE%", table).replaceFirst("%SYMBOL%", symbol).replaceFirst("%SDATE%", startDate).replaceFirst("%EDATE%", endDate);
    }
}
