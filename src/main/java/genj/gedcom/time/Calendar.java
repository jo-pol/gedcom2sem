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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Calendars we support
 */
public abstract class Calendar {
  
  /** fields */
  protected String escape;
  protected String name;
  protected String[] months;
  protected String[] monthsLowerCase;
  protected String[] weekDays, localizedWeekDays;
  protected Map<String,String>
    localizedMonthNames = new HashMap<String,String>(),
    abbreviatedMonthNames = new HashMap<String,String>();
    
  private static final ResourceBundle resources = Resources.get(); 

  /** some messages */
  public final static String 
    TXT_CALENDAR_SWITCH = "cal.switch",//getResources().getString("cal.switch"),
    TXT_CALENDAR_RESET  = "cal.reset";//getResources().getString("cal.reset");

  /** 
   * Constructor 
   */
  protected Calendar(String esc, String key, String img, String[] mOnths, String[] weEkDays) {
    
    // initialize members
    months = mOnths;
    monthsLowerCase = new String[months.length];
    for (int i = 0; i < months.length; i++) monthsLowerCase[i] = months[i].toLowerCase();
    escape = esc;
    name = getResources().getString("cal."+key);
    
    // localize weekdays
    weekDays = weEkDays;
    localizedWeekDays = new String[weekDays.length];
    for (int wd=0;wd<weekDays.length;wd++)
      localizedWeekDays[wd] = getResources().getString("day."+weekDays[wd]);
    
    // localize months
    for (int m=0;m<months.length;m++) {

      // month key      
      String mmm = months[m];

      // month name
      String localized = getResources().getString("mon."+mmm);
  
      // calculate abbreviation
      //  1. substring(0,indexOf('|'))    ,if indexOf('|')>0
      //  2. substring(indexOf(',')+1)    ,if indexOf(',')>0
      //  3. substring(0,3)               ,otherwise
      String abbreviated;

      int marker = localized.indexOf('|'); 
      if (marker>0) {
        abbreviated = localized.substring(0, marker);
        localized = abbreviated + localized.substring(marker+1);
      } else {
        marker = localized.indexOf(',');
        if (marker>0) {
          abbreviated = localized.substring(marker+1);
          localized = localized.substring(0, marker);
        } else {
          abbreviated = localized.length()>3 ? localized.substring(0,3) : localized;
        }
      }
  
      // remember
      localizedMonthNames.put(mmm, localized);
      abbreviatedMonthNames.put(mmm, abbreviated);
  
      // next
    }  

    // done
  }
  
  /** accessor - name */
  public String getName() {
    return name;
  }
  
  /**
   * Parse month
   */
  protected int parseMonth(String mmm) throws NumberFormatException {
    // 20070128 compare lowercase'd values to avoid having to do the lowercase for calendar's months every time
    String mmmLowerCase = mmm.toLowerCase();
    for (int i=0;i<months.length;i++) {
      if (monthsLowerCase[i].equals(mmmLowerCase)) return i;
    }
    throw new NumberFormatException();
  }
  
  /**
   * Returns the (localized) day
   */
  public String getDay(int day) {
    if (day==PointInTime.UNKNOWN)
      return "";
    return ""+(day+1);
  }
  
  /**
   * Access to (localized) gedcom months
   */
  public String[] getMonths(boolean localize) {
    
    String[] result = new String[months.length];
    for (int m=0;m<result.length;m++) {
      String mmm = months[m];
      if (localize) 
        mmm = localizedMonthNames.get(mmm).toString();
      result[m] = mmm;
    }
    return result;
  }

  /**
   * Returns the month as gedcom value
   */
  public String getMonth(int month) {
    // what's the numeric value?
    if (month<0||month>=months.length)
      return "";
    // done
    return months[month];
  }
  
  public String getDisplayMonth(int month, boolean abbrev) {
    String mmm = getMonth(month);
    if (mmm.length()==0)
      return mmm;
    return abbrev ? abbreviatedMonthNames.get(mmm).toString() : localizedMonthNames.get(mmm).toString();
  }
  
  /**
   * Returns the year as a gedcom (numeric) value
   */
  public String getYear(int year) {
    if (year==PointInTime.UNKNOWN)
      return "";
    return ""+year;
  }
  
  public String getDisplayYear(int year) {
    return getYear(year);
  }
  
  /**
   * Returns the year from a string - opportunity for special year designations
   * to be introduced
   */
  public int getYear(String year) throws IllegalArgumentException {
    try {
      return Integer.parseInt(year);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(getResources().getString("year.invalid"));
    }
  }

  /**
   * Calculate number of months
   */
  public int getMonths() {
    return months.length;
  }
  
  /**
   * Calculate number of days in given month
   */
  public abstract int getDays(int month, int year);
      
  /**
   * Access to (localized) day of week
   */
  protected String getDayOfWeek(PointInTime pit, boolean localize) throws IllegalArgumentException {
    if (!pit.isComplete())
      throw new IllegalArgumentException(getResources().getString("pit.incomplete"));
    String[] result = localize ? localizedWeekDays : weekDays;
    int dow = (pit.getJulianDay() + 1) % 7;
    return result[dow >= 0 ? dow : dow+7];
  }
  
  /**
   * PIT -> Julian Day
   */
  protected final int toJulianDay(PointInTime pit) throws IllegalArgumentException {

    // grab data 
    int 
      year  = pit.getYear () ,
      month = pit.getMonth(),
      day   = pit.getDay  ();
      
    // YYYY is always needed - no calendar includes a year 0!
    if (year==PointInTime.UNKNOWN||year==0)
      throw new IllegalArgumentException(getResources().getString("year.invalid"));
      
    // MM needed if DD!
    if (month==PointInTime.UNKNOWN&&day!=PointInTime.UNKNOWN)
      throw new IllegalArgumentException(getResources().getString("month.invalid"));
      
    // months have to be within range
    if (month==PointInTime.UNKNOWN)
      month = 0;
    else if (month<0||month>=months.length)
      throw new IllegalArgumentException(getResources().getString("month.invalid"));

    // day has to be withing range
    if (day==PointInTime.UNKNOWN)
      day = 0;
    else if (day<0||day>=getDays(month,year))
      throw new IllegalArgumentException(getResources().getString("day.invalid"));

    // try to get julian day
    return toJulianDay(day, month, year);
  }
  
  /**
   * PIT -> Julian Day
   */
  protected abstract int toJulianDay(int day, int month, int year) throws IllegalArgumentException;
  
  /**
   * Julian Day -> PIT
   */
  protected abstract PointInTime toPointInTime(int julianDay) throws IllegalArgumentException;

  /** string representation */
  public String toString() {
    return getName();
  }

public static ResourceBundle getResources()
{
    return resources;
}


  
} //Calendar