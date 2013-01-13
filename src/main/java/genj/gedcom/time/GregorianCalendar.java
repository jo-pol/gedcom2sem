/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.gedcom.time;



/**
 * Our own gregorian - dunno if java.util.GregorianCalendar would be of much help
 */
public class GregorianCalendar extends Calendar {

  protected static final String MONTHS[]
    = { "JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC" };
    
  protected static final String WEEKDAYS[]
    = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

  private static final int MONTH_LENGTH[]
    = {31,28,31,30,31,30,31,31,30,31,30,31}; // 0-based
    
  private static final int LEAP_MONTH_LENGTH[]
    = {31,29,31,30,31,30,31,31,30,31,30,31}; // 0-based

  /**
   * Constructor
   */
  protected GregorianCalendar() {
    this("@#DGREGORIAN@", "gregorian", "images/Gregorian");
  }
  
  /**
   * Constructor
   */
  protected GregorianCalendar(String esc, String key, String img) {
    super(esc, key, img, MONTHS, WEEKDAYS);
  }

  /**
   * @see genj.gedcom.time.Calendar#getDays(int, int)
   */
  public int getDays(int month, int year) {
    int[] length = isLeap(year) ? GregorianCalendar.LEAP_MONTH_LENGTH : GregorianCalendar.MONTH_LENGTH;
    return length[month];
  }
  
  /**
   * Definition of Gregorian leap year
   */
  protected boolean isLeap(int year) {
    return ((year%4 == 0) && ((year%100 != 0) || (year%400 == 0)));
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#toJulianDay(int, int, int)
   */
  protected int toJulianDay(int day, int month, int year) {

    // there's no year 0 - anything B.C. has to be shifted
    if (year<0)
      year++;

    // Communications of the ACM by Henry F. Fliegel and Thomas C. Van Flandern entitled 
    // ``A Machine Algorithm for Processing Calendar Dates''. 
    // CACM, volume 11, number 10, October 1968, p. 657.  
    int
     d = day   + 1,
     m = month + 1,
     y = year     ;      
    
    return ( 1461 * ( y + 4800 + ( m - 14 ) / 12 ) ) / 4 +
           ( 367 * ( m - 2 - 12 * ( ( m - 14 ) / 12 ) ) ) / 12 -
           ( 3 * ( ( y + 4900 + ( m - 14 ) / 12 ) / 100 ) ) / 4 +
           d - 32075;
  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#getPointInTime(int)
   */
  protected PointInTime toPointInTime(int julianDay) {
   
    // see toJulianDay 
    int l = julianDay + 68569;
    int n = ( 4 * l ) / 146097;
        l = l - ( 146097 * n + 3 ) / 4;
    int i = ( 4000 * ( l + 1 ) ) / 1461001;
        l = l - ( 1461 * i ) / 4 + 31;
    int j = ( 80 * l ) / 2447;
    int d = l - ( 2447 * j ) / 80;
        l = j / 11;
    int m = j + 2 - ( 12 * l );
    int y = 100 * ( n - 49 ) + i + l;
    
    return new PointInTime(d-1,m-1,y<=0?y-1:y,this);
  }
  
} //GregorianCalendar