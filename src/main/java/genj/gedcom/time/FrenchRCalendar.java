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
 * Our own french republican
 */
public class FrenchRCalendar extends Calendar {
  
  /* valid from 22 SEP 1792 to not including 1 JAN 1806 */
  private static final int
    AN_0  = 2375474,
    AN_I  = new GregorianCalendar().toJulianDay(22-1, 9-1, 1792),
    UNTIL = new GregorianCalendar().toJulianDay( 1-1, 1-1, 1806);

  private static final String MONTHS[] 
   = { "VEND","BRUM","FRIM","NIVO","PLUV","VENT","GERM","FLOR","PRAI","MESS","THER","FRUC","COMP" };
  
  private static final String WEEKDAYS[] 
   = { "PRI", "DUO", "TRI", "QUA", "QUI", "SEX", "SEP", "OCT", "NON", "DEC", "VER", "GEN", "TRA", "OPI", "REC", "REV" };
   
  @SuppressWarnings("unused")
  private static final int[] LEAP_YEARS
   = { 3,7,11 };
   
  private static final String YEARS_PREFIX = "An ";
   
  private static final String[] YEARS 
   = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV" };

  private static final int
    DAYS_PER_MONTH   = 30,
    DAYS_PER_4_YEARS = 1461;
  
  /**
   * Constructor
   */
  protected FrenchRCalendar() {
    super("@#DFRENCH R@" , "french", "images/FrenchR", MONTHS, WEEKDAYS);
  }
  
  /**
   * @see genj.gedcom.time.Calendar#getDays(int, int)
   */
  public int getDays(int month, int year) {
    
    // standard month has 30 days
    if (month<12)
      return 30;
      
    // 5/6 jours complementaires
    return isLeap(year) ? 6 : 5;
    
    // noop
  }
  
  /**
   * Leap year test  3/7/11
   */
  private boolean isLeap(int year) {
    return (year+1) % 4 == 0;
  }
  
  /**
   * @see genj.gedcom.time.Calendar#getDayOfWeek(genj.gedcom.time.PointInTime)
   */
  protected String getDayOfWeek(PointInTime pit, boolean localize) throws IllegalArgumentException {
    if (!pit.isComplete()) 
      throw new IllegalArgumentException("");
    // localized?
    String[] result = localize ? localizedWeekDays : weekDays;
    // Jour Complementaire?
    if (pit.getMonth()==13-1)
      return result[10+pit.getDay()];
    // normal 30 days month
    return result[pit.getDay()%10];
  }

  /**
   * @see genj.gedcom.PointInTime.Calendar#toJulianDay(genj.gedcom.PointInTime)
   */
  protected int toJulianDay(int day, int month, int year) throws IllegalArgumentException {
    // calc
    int jd = ( year * DAYS_PER_4_YEARS / 4
      + month * DAYS_PER_MONTH
      + day+1
      + AN_0 );
    // check range
    if (jd<FrenchRCalendar.AN_I)
      throw new IllegalArgumentException(getResources().getString("frenchr.bef"));
    if (jd>=FrenchRCalendar.UNTIL)
      throw new IllegalArgumentException(getResources().getString("frenchr.aft"));
    // sum
    return jd;

  }
  
  /**
   * @see genj.gedcom.PointInTime.Calendar#toPointInTime(int)
   */
  protected PointInTime toPointInTime(int julianDay) throws IllegalArgumentException {

    // check range
    if (julianDay<FrenchRCalendar.AN_I)
      throw new IllegalArgumentException(getResources().getString("frenchr.bef"));
    if (julianDay>=FrenchRCalendar.UNTIL)
      throw new IllegalArgumentException(getResources().getString("frenchr.aft"));
    
    int temp = (julianDay - AN_0) * 4 - 1;
    
    int year = temp / DAYS_PER_4_YEARS;
    int dayOfYear = (temp % DAYS_PER_4_YEARS) / 4;
    int month = dayOfYear / DAYS_PER_MONTH + 1;
    int day = dayOfYear % DAYS_PER_MONTH + 1;
          
    // done
    return new PointInTime(day-1,month-1,year,this);
  }
  
  /**
   * @see genj.gedcom.time.Calendar#getDisplayYear(int)
   */
  public String getDisplayYear(int year) {
    if (year<1||year>FrenchRCalendar.YEARS.length)
      return super.getDisplayYear(year);
    return YEARS_PREFIX+FrenchRCalendar.YEARS[year-1];
  }

  /**
   * Getting into hook to parse a valid year - check for our years
   * @see genj.gedcom.time.Calendar#getYear(java.lang.String)
   */
  public int getYear(String year) throws IllegalArgumentException {
    // strip any 'An '
    if (year.length()>YEARS_PREFIX.length() && year.substring(0, YEARS_PREFIX.length()).equalsIgnoreCase(YEARS_PREFIX))
      year = year.substring(YEARS_PREFIX.length());
    // look for years
    for (int y=0;y<YEARS.length;y++)
      if (YEARS[y].equals(year))
        return y+1;
    // let super do it
    return super.getYear(year);
  }

} //FrenchRCalendar