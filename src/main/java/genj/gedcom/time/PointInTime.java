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

import genj.util.DirectAccessTokenizer;
import genj.util.WordBuffer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * A point in time - either hebrew, roman, frenchr, gregorian or julian
 */
public class PointInTime implements Comparable<PointInTime> {
  
  public final static int
    FORMAT_GEDCOM = 0,
    FORMAT_SHORT = 1,
    FORMAT_LONG = 2,
    FORMAT_NUMERIC = 3;

  /** resources */
  /*package*/ final static ResourceBundle resources = null;//Resources.get(PointInTime.class);

  /** marker for unknown day,month,year */
  public final static int 
    UNKNOWN = Integer.MAX_VALUE;
  
  /** calendars */
  public final static GregorianCalendar GREGORIAN = new GregorianCalendar();
  public final static    JulianCalendar JULIAN    = new    JulianCalendar();
  public final static    HebrewCalendar HEBREW    = new    HebrewCalendar();
  public final static   FrenchRCalendar FRENCHR   = new   FrenchRCalendar();
    
  public final static Calendar[] CALENDARS = { GREGORIAN, JULIAN, HEBREW, FRENCHR };
  
  /** calendar */
  protected Calendar calendar = GREGORIAN;
  
  /** values */
  private int 
    day   = UNKNOWN, 
    month = UNKNOWN, 
    year  = UNKNOWN,
    jd    = UNKNOWN;

  /**
   * Constructor
   */
  public PointInTime() {
  }

  /**
   * Constructor
   */
  public PointInTime(Calendar cal) {
    calendar = cal;
  }
  
  /**
   * Constructor
   */
  public PointInTime(java.util.Calendar cal) {
    calendar = GREGORIAN;
    day = cal.get(java.util.Calendar.DAY_OF_MONTH) - 1;
    month = cal.get(java.util.Calendar.MONTH);
    year = cal.get(java.util.Calendar.YEAR);
  }
  
  /**
   * Constructor
   */
  public PointInTime(int d, int m, int y) {
    this(d,m,y,GREGORIAN);
  }
  
  /**
   * Constructor
   */
  public PointInTime(int d, int m, int y, Calendar cal) {
    day = d;
    month = m;
    year = y;
    calendar = cal;
    jd = UNKNOWN;
  }
  
  /**
   * Constructor
   */
  public PointInTime(String yyyymmdd) throws IllegalArgumentException {
    
    if (yyyymmdd==null||yyyymmdd.length()!=8)
      throw new IllegalArgumentException(resources.getString("pit.noyyyymmdd"/*FIXME, yyyymmdd*/));
    
    // check date
    try {
      year  = Integer.parseInt(yyyymmdd.substring(0, 4));
      month = Integer.parseInt(yyyymmdd.substring(4, 6))-1;
      day   = Integer.parseInt(yyyymmdd.substring(6, 8))-1;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(resources.getString("pit.noyyyymmdd"/*FIXME , yyyymmdd*/));
    }

    // done
  }
    
  /**
   * Returns the calendar
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Returns the year
   */
  public int getYear() {
    return year;
  }
  
  /**
   * Returns the month (first month of year is 0=Jan)
   */
  public int getMonth() {
    return month;    
  }

  /**
   * Returns the day (first day of month is 0)
   */
  public int getDay() {
    return day;
  }
  
  /**
   * Accessor to an immutable point in time - now
   */
  public static PointInTime getNow() {
    java.util.Calendar now = java.util.Calendar.getInstance(); // default to current time
    return new PointInTime(
      now.get(java.util.Calendar.DATE) - 1,      
      now.get(java.util.Calendar.MONTH),      
      now.get(java.util.Calendar.YEAR)
    );      
  }  
  
  /**
   * Accessor to an immutable point in time
   * @param string e.g. 25 MAY 1970
   */
  public static PointInTime getPointInTime(String string) {
    PointInTime result = new PointInTime(UNKNOWN,UNKNOWN,UNKNOWN,GREGORIAN);
    result.set(string);
    return result;
  }
  
  /**
   * Accessor to a point in time from system time long
   */
  public static PointInTime getPointInTime(long millis) {
    // January 1, 1970 = 0 millis = 2440588 julian
    long julian = 2440588 + (millis / 24 / 60 / 60 / 1000);
    return GREGORIAN.toPointInTime((int)julian);
  }
  
  /**
   * Returns the time millis representation
   */
  public long getTimeMillis() throws IllegalArgumentException {
    // January 1, 1970 = 0 millis = 2440588 julian
    return (getJulianDay()-2440588L) * 24*60*60*1000;
  }

  /**
   * Accessor to a calendar transformed copy 
   */
  public PointInTime getPointInTime(Calendar cal) throws IllegalArgumentException {
    if (calendar==cal)
      return this;
    PointInTime result = new PointInTime();
    result.set(this);
    result.set(cal);
    return result;
  }
  
  /**
   * Calculate julian day
   */
  public int getJulianDay() throws IllegalArgumentException {
    if (jd==UNKNOWN)
      jd = calendar.toJulianDay(this);
    return jd;
  }
  
  /**
   * Setter
   */
  public void reset() {
    set(UNKNOWN,UNKNOWN,UNKNOWN);
  }
  
  
  /**
   * Calculate day of week
   */
  public String getDayOfWeek(boolean localize) throws IllegalArgumentException {
    return calendar.getDayOfWeek(this, localize);
  }
  
  public PointInTime add(int d, int m, int y) {
    java.util.Calendar c = java.util.Calendar.getInstance();
    c.set(java.util.Calendar.DAY_OF_MONTH, day!=UNKNOWN ? day+1 : 1);
    c.set(java.util.Calendar.MONTH, month!=UNKNOWN ? month : 0);
    c.set(java.util.Calendar.YEAR, year!=UNKNOWN ? year : 0);
    
    c.add(java.util.Calendar.DAY_OF_MONTH, d);
    c.add(java.util.Calendar.MONTH, m);
    c.add(java.util.Calendar.YEAR, y);
    
    set(c.get(java.util.Calendar.DAY_OF_MONTH)-1,
        c.get(java.util.Calendar.MONTH),
        c.get(java.util.Calendar.YEAR)
        );
    
    return this;
  }
  
  /**
   * Setter
   */
  public void set(Calendar cal) throws IllegalArgumentException {
    // is ok for 'empty' pit
    if (day==UNKNOWN&&month==UNKNOWN&&year==UNKNOWN) {
      calendar = cal;
      return;
    }
    // has to be valid
    if (!isValid())
      throw new IllegalArgumentException(resources.getString("pit.invalid"));
    // has to be complete
    if (!isComplete())
      throw new IllegalArgumentException(resources.getString("pit.incomplete"));
    // convert to julian date
    int jd = getJulianDay();
    // convert to new instance
    set(cal.toPointInTime(jd));
  }  
  
  /**
   * Setter
   */
  public void set(int d, int m, int y) {
    set(d,m,y,calendar);
  }
  
  /**
   * Setter (implementation dependant)
   */
  public void set(int d, int m, int y, Calendar cal) {
    day = d;
    month = m;
    year = y;
    calendar = cal;
    jd = UNKNOWN;
  }
  
  /**
   * Setter 
   */
  public void set(PointInTime other) {
    calendar = other.calendar;
    set(other.getDay(), other.getMonth(), other.getYear());
  }
  
  /**
   * Parse text into this PIT
   */
  public boolean set(String txt) {

    txt = txt.trim();
    
    // assume gregorian
    calendar = GREGORIAN;
    
    // check for leading calendar indicator  @#....@
    if (txt.startsWith("@#")) {
      int i = txt.indexOf("@", 1);
      if (i<0)  return false;
      String esc = txt.substring(0,i+1);
      txt = txt.substring(i+1);
      
      // .. has to be one of our calendar escapes
      for (int c=0;c<CALENDARS.length;c++) {
        Calendar cal = CALENDARS[c]; 
        if (cal.escape.equalsIgnoreCase(esc)) {
        // 20060109 made this ignore case - before was: if (cal.escape.startsWith(first)) {
          calendar = cal;
          break;
        }
      }
    }
    
    // check for trailing B.C.
    boolean bc = false;
    if (calendar == GREGORIAN) {
      if (txt.endsWith("B.C.")) {
        bc = true;
        txt = txt.substring(0,txt.length()-"B.C.".length()).trim();
      }
    }
    
    // no tokens - fine - no info
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(txt, " ", true);
    String first = tokens.get(0);
    if (first==null) {
      reset();
      return true;
    }
    int cont = 1;

    // grab second
    String second = tokens.get(cont++);
        
    // only one token? gotta be YYYY
    if (second==null) {
        try {
          set(UNKNOWN, UNKNOWN, addc(calendar.getYear(first), bc));
        } catch (Throwable t) {
          return false;
        }
        return getYear()!=UNKNOWN;
    }
    
    // grab third
    String third = tokens.get(cont++);
    
    // only two tokens? either MMM YYYY or french R calendar 'An Y'
    if (third==null) {
      try {
        if (calendar==FRENCHR) set(UNKNOWN, UNKNOWN, calendar.getYear(first + ' ' + second));
      } catch (Throwable t) {
      }
      try {
        set(UNKNOWN, calendar.parseMonth(first), addc(calendar.getYear(second), bc));
      } catch (Throwable t) {
        return false;
      }
      return getYear()!=UNKNOWN&&getMonth()!=UNKNOWN;
    }

    // everything after third is now combined to YYYY
    third = txt.substring(tokens.getStart());
    
    try {
      set( Integer.parseInt(first) - 1, calendar.parseMonth(second), addc(calendar.getYear(third), bc));
    } catch (Throwable t) {
      return false;
    }
    return getYear()!=UNKNOWN&&getMonth()!=UNKNOWN&&getDay()!=UNKNOWN;
    
  }
  
  private int addc(int year, boolean bc) {
    if (!bc) return year;
    if (year==0)
      throw new IllegalArgumentException("no year 0 allowed");
    return 0-year;
  }
  
  /**
   * Checks for gregorian calendar 
   */
  public boolean isGregorian() {
    return getCalendar()==GREGORIAN;    
  }

  /**
   * Checks for completeness - DD MMM YYYY
   */
  public boolean isComplete() {
    return year!=UNKNOWN && month!=UNKNOWN && day!=UNKNOWN;
  }

  /**
   * Checks for validity
   */
  public boolean isValid() {
    
    // ok if known JD    
    if (jd!=UNKNOWN)
      return true;

    // quick empty check 20040301 where lenient here before but lead to problems
    // relying on valid dates that *CAN* be compared reliably
    if (day==UNKNOWN&&month==UNKNOWN&&year==UNKNOWN)
      return false;

    // try calculating JD
    try {
      jd = calendar.toJulianDay(this);
    } catch (IllegalArgumentException e) {
    }
    
    // done
    return jd!=UNKNOWN;
  }
    
  /**
   * Compares this point in time to another
   * @return  a negative integer, zero, or a positive integer as this object
   *      is less than, equal to, or greater than the specified object.
   */
  public int compareTo(PointInTime other) {
    
    // check valid
    boolean
      v1 = isValid(),
      v2 = other!=null && other.isValid();
    if (!v1&&!v2)
      return 0;
    if (!v2)
      return 1;
    if (!v1)
      return -1; 
    // compare
    try {
      return getJulianDay() - other.getJulianDay();
    } catch (IllegalArgumentException e) {
      return 0; // shouldn't really happen
    }
  }

  /**
   * String representation (Gedcom format)
   */
  public String getValue() {
    return getValue(new WordBuffer()).toString();
  }
    
  /**
   * String representation (Gedcom format)
   */
  public WordBuffer getValue(WordBuffer buffer) {
    if (calendar!=GREGORIAN)
      buffer.append(calendar.escape);
    toString(buffer, FORMAT_GEDCOM);
    return buffer;
  }
    
  /**
   * String representation
   */
  public String toString() {
    return toString(new WordBuffer(), FORMAT_SHORT/*TODO Options.getInstance().dateFormat*/).toString();
  }

  /** our numeric format */
  private static DateFormat NUMERICDATEFORMAT = initNumericDateFormat();
  
  private static DateFormat initNumericDateFormat() {
    DateFormat result = DateFormat.getDateInstance(DateFormat.SHORT);
    try {
      // check SHORT pattern
      String pattern = ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
      // patch yy to yyyy if necessary
      int yyyy = pattern.indexOf("yyyy");
      if (yyyy<0) 
        result = new SimpleDateFormat(pattern.replaceAll("yy", "yyyy"));
    } catch (Throwable t) {
    }
    return result;
  }
  
  /**
   * Notifies all PointInTimes of a change in locale. This is exposed for
   * testing purposes only.
   */
  public static void localeChangedNotify() {
    NUMERICDATEFORMAT = initNumericDateFormat();
  }
  
  /**
   * String representation
   */
  public WordBuffer toString(WordBuffer buffer, int format) {
    
    int yearAdjusted = year;
    boolean bc = false;
    
    if (calendar==GREGORIAN&&year<0) {
      yearAdjusted = 0-year;
      bc = true;
    }
    
    // numeric && gregorian && complete
    if (format==FORMAT_NUMERIC) {
      if (calendar==GREGORIAN&&isComplete()) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set( yearAdjusted, month, day+1);
        synchronized (NUMERICDATEFORMAT) {
          buffer.append(NUMERICDATEFORMAT.format(c.getTime()));
        }
        // add bc for gregorian
        if (bc)
          buffer.append(" B.C.");
        return buffer;
      }
      
      // fallback to short
      format = FORMAT_SHORT;
    }
    
    // has to be valid
    if (!isValid()) {
      // we can generate a ? for rendering something meaningful, but not into a gedcom value
      if (format!=FORMAT_GEDCOM)
        buffer.append("?");
      return buffer;
    }
        
    // non-gregorian, Gedcom, short or long
    if (year!=UNKNOWN) {
      if (month!=UNKNOWN) {
        if (day!=UNKNOWN) {
          buffer.append(day+1);
        }
        buffer.append(format==FORMAT_GEDCOM ? calendar.getMonth(month) : calendar.getDisplayMonth(month, format==FORMAT_SHORT));
      }
      buffer.append(format==FORMAT_GEDCOM ? calendar.getYear(yearAdjusted) : calendar.getDisplayYear(yearAdjusted));
      
      // add calendar indicator for julian
      if (format!=FORMAT_GEDCOM&&calendar==JULIAN)
        buffer.append("(j)");
      
      // add bc for gregorian
      if (bc)
        buffer.append(" B.C.");
    }      
    
    // done
    return buffer;
  }

} //PointInTime