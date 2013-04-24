
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Michal Franczyk
 */

/**
 * Klasa porównująca po zmiennej: z
 * @author Michal
 */
class CompareByZ implements Comparator<Point3D> 
{
    @Override
    public int compare(Point3D o1, Point3D o2) 
    {
        return o1.Z.compareTo(o2.Z);
    }
}

/**
 * Klasa porównująca po zmiennej: kąt nachylenie 
 * @author Michal
 */
class CompareByAngle implements Comparator<Point2D> 
{
    @Override
    public int compare(Point2D o1, Point2D o2) 
    {
        return o1.Angle.compareTo(o2.Angle);
    }
}

/**
 * Klasa oslugujaca dane, które są następnie przekazane do
 * rozwiązania właściwym algorytmem
 * @author Michal
 */
public class Plain
{
    /**
     * lista wczytanych punktów w 3D
     */
    public Point3D[] points3D;  
    
    /**
     * lista zawierająca listy punktów podzielone na grupy wględem z
     */
    List<List<Point2D>> splitedPoints;
    
    /**
     * konstruktor inicjalizujacy
     * @param points punkty 3D
     */
    public Plain(List<Point3D> points)
    {
        this.points3D = points.toArray(new Point3D[points.size()]);
        splitedPoints = new ArrayList<>();
    }
    
    /**
     * funkcja przygotowujaca dane 
     * dzieli punkty podane w 3D wzgledem 
     * punktow z
     * zgodnie z instrukcja podana w zadaniu
     */
    public void prepare()
    {
        Arrays.sort(points3D, new CompareByZ());
        
        Set<Integer> s = new HashSet<Integer>();
        for (Point3D i : points3D)
            s.add(i.Z);
                
        splitedPoints.add(new ArrayList<Point2D>());
        List<Integer> zety = new ArrayList<>(s);
        
        int k=0;
        for(int i=0;i<points3D.length;++i)
        {
            if(points3D[i].Z.equals(zety.get(k)))
                splitedPoints.get(k).add(new Point2D(points3D[i].X, points3D[i].Y));
            else
            {
                k++;
                splitedPoints.add(new ArrayList<Point2D>());
                splitedPoints.get(k).add(new Point2D(points3D[i].X, points3D[i].Y));
            }
        }
    }
    
    /**
     * Funkcja obslugujaca rozwiazanie problemu
     * @return liste pól danych obszarow zlozonych z punktow
     */
    public List<Double> ResolveProblem()
    {
        prepare();
        
        QuickHullAlgorithm qh;
        IrregularPolygonArea ip;
        List<Double> result = new ArrayList<>();
        for( List<Point2D> i: splitedPoints )
        {
            double polePoligonu = 0.0;
            if(i.size() > 2)
            {
                qh = new QuickHullAlgorithm(i);
                qh.go();
                ip = new IrregularPolygonArea(i);
                result.add(ip.Resolve());
            }
        }
        return result;
    }
}

/**
 * Klasa obliczajaca pole nieregularnego poligonu zlozonego z pkt 2D
 * @author Michal
 */
class IrregularPolygonArea
{
    /**
     * tablica punktow
     */
    public Point2D[] points;
    
    /**
     * pole poligonu
     */
    Double AreaOfPolygon;

    /**
     * konsktruktor inicjalizujacy
     * @param points punkty wejsciowe z ktorym bedzie liczone pole
     */
    public IrregularPolygonArea(List<Point2D> points)
    {
        this.points = points.toArray(new Point2D[points.size()]);
        AreaOfPolygon = 0.0;
    }
    
    /**
     * funkcja oblugujaca rozwiazywanie problemu - pole poligonu
     * @return 
     */
    public double Resolve()
    {
        SortCornersInCounterClockwiseDirection();
        AreaOfPolygon = PolygonArea();
        return AreaOfPolygon;
    }
    
    /**
     * sortowanie podanych punktow względem pkt środkowego
     * godnie z ruchem wskazowek zegara
     */
    private void SortCornersInCounterClockwiseDirection()
    {
        int n = points.length;
        
        double  cx = 0.0,
                cy = 0.0;
        for(Point2D i : points)
        {
            cx+=i.X;
            cy+=i.Y;
        }
        
        cx/=(double)n;
        cy/=(double)n;
        
        List<Point2D> newPoints = new ArrayList<>();
        
        for(Point2D i : points)
        {
            double an = (Math.atan2(i.Y - cy, i.X - cx) + 2.0 * Math.PI) % (2.0 * Math.PI);
            newPoints.add(new Point2D(i.X, i.Y, an));
        }
        
        Collections.sort(newPoints, new CompareByAngle());
        points = newPoints.toArray(new Point2D[newPoints.size()]);
    }
    
    /**
     * Obliczanie pola poligonu
     * @return pole poligonu
     */
    private double PolygonArea()
    {
        int n = points.length;
        double area = 0.0;
        
        int j = 0;
        for(int i=0;i<n;++i)
        {
            j = (i + 1) % n;
            area += points[i].X * points[j].Y;
            area -= points[j].X * points[i].Y;
        }
        
        area = Math.abs(area) / 2.0;
        
        return area;
    }
}

/**
 * klasa reprezentujaca algorytm quickhull
 * więcej o nim: https://pl.wikipedia.org/wiki/Quickhull
 * @author Michal
 */
class QuickHullAlgorithm
{
    /**
     * punkty wejsciowe
     */
    public Point2D[] points;
    
    /**
     * punkty wynikowe
     * otoczka ...
     */
    public Point2D[] resultPoints;
    
    /**
     * zmienna pomocnicza 
     * okresla indeks w tablicy pod ktory algorytm ma wspisac roziwiazanie
     */
    public int num;

    /**
     * konstruktor inicjalizujacy
     * @param points 
     */
    public QuickHullAlgorithm(List<Point2D> points)
    {
        this.points = points.toArray(new Point2D[points.size()]);
        resultPoints = new Point2D[this.points.length];
        
        Arrays.fill(resultPoints, new Point2D(0, 0, 0.0));
    }

    /**
     * ustawienie parametrow poczatkowych i odpalenie alg
     */
    public void go()
    {
        num = 0;
        quickconvexhull();
    }
    
    /**
     * 
     */
    private void quickconvexhull()
    {
        // znalezienie dwoch punktow: prawy dol i lewy gora
	int r, l;
	r = l = 0;
	for ( int i = 1; i < points.length; i++ ) 
        {
	    if ( ( points[r].X > points[i].X ) || ( points[r].X == points[i].X && points[r].Y > points[i].Y ))
		r = i;
	    if ( ( points[l].X < points[i].X ) || ( points[l].X == points[i].X && points[l].Y < points[i].Y ))
		l = i;
	}

        if(Main.Test)
            System.out.println("l: "+l+", r: "+r);

	List<Integer> al1 = new ArrayList<Integer>();
	List<Integer> al2 = new ArrayList<Integer>();

	int upper;
	for ( int i = 0; i < points.length; i++ ) {
	    if ( (i == l) || (i == r) )
		continue;
	    upper = right(r,l,i);
	    if ( upper > 0 )
		al1.add(i);
	    else if ( upper < 0 )
		al2.add(i);
	}

	resultPoints[num].X = points[r].X;
	resultPoints[num].Y = points[r].Y;
	num++;
	quickhull(r, l, al1);
	resultPoints[num].X = points[l].X;
	resultPoints[num].Y = points[l].Y;
	num++;
	quickhull(l, r, al2);
    }
    
    
    /**
     * sprawdzamy czy pkt p jest po prawej od linii a-b
     * @param a pkt
     * @param b pkt
     * @param p pkt
     * @return wynik
     */
    private int right(int a, int b, int p)
    {
	return (points[a].X - points[b].X)
                *(points[p].Y - points[b].Y)
                - (points[p].X - points[b].X)
                *(points[a].Y - points[b].Y);
    }
    
    /**
     * odleglosc (kwadratowa) pkt p do lini a-b
     * @param a pkt
     * @param b pkt
     * @param p pkt
     * @return wynik
     */
    private float distance(int a, int b, int p)
    {
	float x, y, u;
	u = (((float)points[p].X - (float)points[a].X)*((float)points[b].X - (float)points[a].X) + ((float)points[p].Y - (float)points[a].Y)*((float)points[b].Y - (float)points[a].Y)) 
	    / (((float)points[b].X - (float)points[a].X)*((float)points[b].X - (float)points[a].X) + ((float)points[b].Y - (float)points[a].Y)*((float)points[b].Y - (float)points[a].Y));
	x = (float)points[a].X + u * ((float)points[b].X - (float)points[a].X);
	y = (float)points[a].Y + u * ((float)points[b].Y - (float)points[a].Y);
	return ((x - (float)points[p].X)*(x - (float)points[p].X) + (y - (float)points[p].Y)*(y - (float)points[p].Y));
    }
    
    /**
     * najdalszy pkt 
     * @param a pkt
     * @param b pkt
     * @param al lista pkt
     * @return wynik;
     */
    private int farthestpoint(int a, int b, List<Integer>al)
    {
	float maxD, dis;
	int maxP, p;
	maxD = -1;
	maxP = -1;
	for ( int i = 0; i < al.size(); i++ ) {
	    p = al.get(i);
	    if ( (p == a) || (p == b) )
		continue;
	    dis = distance(a, b, p);
	    if ( dis > maxD ) {
		maxD = dis;
		maxP = p;
	    }
	}
	return maxP;
    }
    
    /**
     * wlasciwy algorytm znajdowania pkt ...
     * @param a pkt
     * @param b pkt
     * @param al lista pkt
     */
    private void quickhull(int a, int b, List<Integer> al)
    {
	if(Main.Test)
            System.out.println("a:"+a+",b:"+b+" size: "+al.size());
	
        if ( al.size() == 0 )
	    return;

	int c, p;

	c = farthestpoint(a, b, al);

	List<Integer> al1 = new ArrayList<Integer>();
	List<Integer> al2 = new ArrayList<Integer>();

	for ( int i=0; i<al.size(); i++ ) 
        {
	    p = al.get(i);
	    if ( (p == a) || (p == b) )
		continue;
	    if ( right(a,c,p) > 0 )
		al1.add(p);
	    else if ( right(c,b,p) > 0 )
		al2.add(p);
	}

	quickhull(a, c, al1);
	resultPoints[num].X = points[c].X;
        resultPoints[num].Y = points[c].Y;
	num++;
	quickhull(c, b, al2);
    }
}


/**
 * Klasa punkt
 * zawiera potrzebne informacje do przechowania pkt 2D
 * @author Michal
 */
class Point2D
{
    /**
     * X
     */
    public Integer X;
    
    /**
     * Y
     */
    public Integer Y;

    /**
     * kąt
     */
    public Double Angle;
    
    /**
     * konstruktor inicjaluzujacy
     * @param X X
     * @param Y Y
     * @param angle kąt
     */
    public Point2D(Integer X, Integer Y, double angle)
    {
        this.X = X;
        this.Y = Y;
        this.Angle = angle;
    }

    /**
     * konstruktor inicjaluzujacy
     * @param X X
     * @param Y Y
     */
    public Point2D(Integer X, Integer Y)
    {
        this.X = X;
        this.Y = Y;
    }
    
}

/**
 * klasa pkt 3D
 * @author Michal
 */
class Point3D extends Point2D
{
    /**
     * Z
     */
    public Integer Z;
    
    /**
     * konstruktor inicjaluzujacy
     * @param X X
     * @param Y Y
     * @param Z Z
     */
    public Point3D(Integer X, Integer Y, Integer Z)
    {
        super(X, Y);
        this.Z = Z;
    }
}