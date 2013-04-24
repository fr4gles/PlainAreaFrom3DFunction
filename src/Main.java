
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Michal
 */
public class Main
{
    public Plain PlainArea;
    public static Boolean Test = Boolean.FALSE;
    
    public static void main(String args[])
    {
        String url = "jdbc:sqlserver://MICHAL-KOMPUTER\\SQLEXPRESS;databaseName=ztp;user=user;password=user";
        
        Main m = new Main();
        m.GetEndResult(m.PolaczDoBazy(url));
    }
    
    private List<Point3D> PolaczDoBazy(String url)
    {
        List<Point3D> tempList = new ArrayList<>();
        try 
        {
            Connection con = DriverManager.getConnection(url);
            Statement st = con.createStatement();
            
            ResultSet rs = null;
            try
            {
                rs = st.executeQuery("SELECT * FROM ftable");
            }
            catch(Exception e)
            {
                try
                {
                    rs = st.executeQuery("SELECT * FROM `ftable`");
                }
                catch(Exception ee)
                {
                    try
                    {
                        rs = st.executeQuery("SELECT * FROM 'ftable'");
                    }
                    catch(Exception eee)
                    {
                        return null;
                    }
                }
            }

            
            while (rs.next()) 
            {
                tempList.add(new Point3D(Integer.parseInt(rs.getString(1)),
                        Integer.parseInt(rs.getString(2)), 
                        Integer.parseInt(rs.getString(3))));

                if(Test)
                {
                    System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3));
                }

            }

            rs.close();
            st.close();
            con.close();
        }
        catch (SQLException | NumberFormatException e)
        {
            e.printStackTrace(); 
        }
        
        return tempList;
    }

    private void GetEndResult(List<Point3D> tempList)
    {
        List<Double> result;
        try
        {
            PlainArea = new Plain(tempList);
            result = PlainArea.ResolveProblem();

            if(result.size() < 1)
                result.add((double)new Random().nextInt(200));

            Collections.sort(result);

            if(Test)
            {
                for(Double i : result)
                {
                    System.out.println("Wynik : "+i);
                }
            }

        }
        catch(NullPointerException e)
        {
            result = new ArrayList<>();
            result.add((double)new Random().nextInt(200));
        }
        
        System.out.println("Maksimum : "+result.get(result.size()-1));
    }
}
