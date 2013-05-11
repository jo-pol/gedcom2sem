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
 * 
 * 
 * This file contains a conversion algorithm from Scott E. Lee 
 * based on Common Lisp code from "Calendrical Calculations" 
 * by Nachum Dershowitz and Edward M. Reingold
 * 
 * @see http://www.scottlee.net/
 * @see http://emr.cs.iit.edu/~reingold/calendar.ps
 *
 * Original Copy Right Statement for jewish.c,v 2.0 1995/10/24 01:13:06
 * 
 *  Copyright 1993-1995, Scott E. Lee, all rights reserved.
 *  Permission granted to use, copy, modify, distribute and sell so long as
 *  the above copyright and this permission statement are retained in all
 *  copies.  THERE IS NO WARRANTY - USE AT YOUR OWN RISK.
 *
 */
package genj.gedcom.time;


/**
 * The hebrew calendar 
 */
public class HebrewCalendar extends Calendar {

  private static final String[] MONTHS 
   = { "TSH","CSH","KSL","TVT","SHV","ADR","ADS","NSN","IYR","SVN","TMZ","AAV","ELL" };

  private static final String[] WEEKDAYS 
  = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAB" };
   
  /**
   * Constructor
   */
  protected HebrewCalendar() {
    super("@#DHEBREW@", "hebrew", "images/Hebrew", MONTHS, WEEKDAYS);
  }
  
  /**
   * Julian Day -> PIT
   */
  protected PointInTime toPointInTime(int julianDay) throws IllegalArgumentException {
    
    // before Hebrew calendar start - ANNO MUNDI?
    if (julianDay<=SDN_OFFSET)
      throw new IllegalArgumentException(getResources().getString("hebrew.bef"));

    // call implementation
    return SdnToJewish(julianDay);
  }
  
  /**
   * d,m,y -> Julian Day
   */
  protected int toJulianDay(int day, int month, int year) throws IllegalArgumentException {

    // year ok?
    if (year<1)
      throw new IllegalArgumentException(getResources().getString("hebrew.one"));

    // call implementation
    return JewishToSdn(year, month+1, day+1);
  }
  
  /**
   * @see genj.gedcom.time.Calendar#getDays(int, int)
   */
  public int getDays(int month, int year) {
    
    //  easy for the months fixed to 29
    switch (month) {
      case  3: //TVT
      case  6: //ADS
      case  8: //IYR
      case 10: //TMZ
      case 12: //ELL
        return 29;
      case  1: //CSH - depends on length of year
        if (getDays(year)%10!=5)
          return 29;
        break; 
      case  2: //KSL - depends on length of year
        if (getDays(year)%10==3)
          return 29;
        break;
    }   

    // standard is 30
    return 30;
  }

  /** 
   * Calculate days in a year
   */  
  private int getDays(int year) {
    try {
      return toJulianDay(1,1,year+1) - toJulianDay(1,1,year); 
    } catch (Throwable t) {
      // shouldn't happen
      throw new RuntimeException();
    }
  }

  // =======================================================================
  // *                   Extensions to algorithm below                     * 
  // =======================================================================

  private class Molad {
    int day;
    int halakim;
  }

  private class Metonic {
    int cycle;
    int year;
  }
  
  private PointInTime wrap(int day, int month, int year) {
    return new PointInTime(day-1, month-1, year, this);
  }
  
  // =======================================================================
  // *      Conversion algorithm from Scott E. Lee - see notice above      * 
  // =======================================================================
  
  @SuppressWarnings("unused")
private final static int
   HALAKIM_PER_HOUR = 1080,
   HALAKIM_PER_DAY = 25920,
   HALAKIM_PER_LUNAR_CYCLE = ((29 * HALAKIM_PER_DAY) + 13753),
   HALAKIM_PER_METONIC_CYCLE = (HALAKIM_PER_LUNAR_CYCLE * (12 * 19 + 7)),

   SDN_OFFSET = 347997,
   NEW_MOON_OF_CREATION = 31524,

   SUNDAY    = 0,
   MONDAY    = 1,
   TUESDAY   = 2,
   WEDNESDAY = 3,
   THURSDAY  = 4,
   FRIDAY    = 5,
   SATURDAY  = 6,

   NOON      = (18 * HALAKIM_PER_HOUR),
   AM3_11_20 = ((9 * HALAKIM_PER_HOUR) + 204),
   AM9_32_43 = ((15 * HALAKIM_PER_HOUR) + 589);
  
  private static int[] monthsPerYear = {
      12, 12, 13, 12, 12, 13, 12, 13, 12, 12, 13, 12, 12, 13, 12, 12, 13, 12, 13
  };

  private static int[] yearOffset = {
      0, 12, 24, 37, 49, 61, 74, 86, 99, 111, 123,
      136, 148, 160, 173, 185, 197, 210, 222
  };
  
  /**
   * Given the year within the 19 year metonic cycle and the time of a molad
   * (new moon) which starts that year, this routine will calculate what day
   * will be the actual start of the year (Tishri 1 or Rosh Ha-Shanah).  This
   * first day of the year will be the day of the molad unless one of 4 rules
   * (called dehiyyot) delays it.  These 4 rules can delay the start of the
   * year by as much as 2 days.
   */
  private int getTishri1(int metonicYear, Molad molad) {
    
    int tishri1;
    int dow;
    boolean leapYear;
    boolean lastWasLeapYear;

    tishri1 = molad.day;
    dow = tishri1 % 7;
    
    leapYear = metonicYear == 2 || metonicYear == 5 || metonicYear == 7
      || metonicYear == 10 || metonicYear == 13 || metonicYear == 16
      || metonicYear == 18;
    lastWasLeapYear = metonicYear == 3 || metonicYear == 6
      || metonicYear == 8 || metonicYear == 11 || metonicYear == 14
      || metonicYear == 17 || metonicYear == 0;

    /* Apply rules 2, 3 and 4. */
    if ((molad.halakim >= NOON) ||
      ((!leapYear) && dow == TUESDAY && molad.halakim >= AM3_11_20) ||
      (lastWasLeapYear && dow == MONDAY && molad.halakim >= AM9_32_43)) {
      tishri1++;
      dow++;
      if (dow == 7) {
          dow = 0;
      }
    }

    /* Apply rule 1 after the others because it can cause an additional
     * delay of one day. */
    if (dow == WEDNESDAY || dow == FRIDAY || dow == SUNDAY) {
      tishri1++;
    }

    return(tishri1);
  }
  
  /**
   * Given a metonic cycle number, calculate the date and time of the molad
   * (new moon) that starts that cycle.  Since the length of a metonic cycle
   * is a constant, this is a simple calculation, except that it requires an
   * intermediate value which is bigger that 32 bits.  Because this
   * intermediate value only needs 36 to 37 bits and the other numbers are
   * constants, the process has been reduced to just a few steps.
   */
  private void getMoladOfMetonicCycle(int metonicCycle, Molad molad) {
    
      int r1, r2, d1, d2;

      /* Start with the time of the first molad after creation. */
      r1 = NEW_MOON_OF_CREATION;

      /* Calculate metonicCycle * HALAKIM_PER_METONIC_CYCLE.  The upper 32
       * bits of the result will be in r2 and the lower 16 bits will be
       * in r1. */
      r1 += metonicCycle * (HALAKIM_PER_METONIC_CYCLE & 0xFFFF);
      r2 = r1 >> 16;
      r2 += metonicCycle * ((HALAKIM_PER_METONIC_CYCLE >> 16) & 0xFFFF);

      /* Calculate r2r1 / HALAKIM_PER_DAY.  The remainder will be in r1, the
       * upper 16 bits of the quotient will be in d2 and the lower 16 bits
       * will be in d1. */
      d2 = r2 / HALAKIM_PER_DAY;
      r2 -= d2 * HALAKIM_PER_DAY;
      r1 = (r2 << 16) | (r1 & 0xFFFF);
      d1 = r1 / HALAKIM_PER_DAY;
      r1 -= d1 * HALAKIM_PER_DAY;

      molad.day  = (d2 << 16) | d1;
      molad.halakim = r1;
      
  }

  /**
   * Given a day number, find the molad of Tishri (the new moon at the start
   * of a year) which is closest to that day number.  It's not really the
   * *closest* molad that we want here.  If the input day is in the first two
   * months, we want the molad at the start of the year.  If the input day is
   * in the fourth to last months, we want the molad at the end of the year.
   * If the input day is in the third month, it doesn't matter which molad is
   * returned, because both will be required.  This type of "rounding" allows
   * us to avoid calculating the length of the year in most cases.
   */
  private void FindTishriMolad(int inputDay, Metonic metonic, Molad molad) { 

    int metonicCycle;
    int metonicYear;

    /* Estimate the metonic cycle number.  Note that this may be an under
     * estimate because there are 6939.6896 days in a metonic cycle not
     * 6940, but it will never be an over estimate.  The loop below will
     * correct for any error in this estimate. */
    metonicCycle = (inputDay + 310) / 6940;

    /* Calculate the time of the starting molad for this metonic cycle. */
    getMoladOfMetonicCycle(metonicCycle, molad);

    /* If the above was an under estimate, increment the cycle number until
     * the correct one is found.  For modern dates this loop is about 98.6%
     * likely to not execute, even once, because the above estimate is
     * really quite close. */
    while (molad.day < inputDay - 6940 + 310) {
      metonicCycle++;
      molad.halakim += HALAKIM_PER_METONIC_CYCLE;
      molad.day += molad.halakim / HALAKIM_PER_DAY;
      molad.halakim = molad.halakim % HALAKIM_PER_DAY;
    }

    /* Find the molad of Tishri closest to this date. */
    for (metonicYear = 0; metonicYear < 18; metonicYear++) {
      if (molad.day > inputDay - 74)
        break;
      molad.halakim += HALAKIM_PER_LUNAR_CYCLE * monthsPerYear[metonicYear];
      molad.day += molad.halakim / HALAKIM_PER_DAY;
      molad.halakim = molad.halakim % HALAKIM_PER_DAY;
    }

    metonic.cycle = metonicCycle;
    metonic.year = metonicYear;
      
  }

  /**
   * Given a year, find the number of the first day of that year and the date
   * and time of the starting molad.
   */
  private int FindStartOfYear(int year, Metonic metonic, Molad molad) {
    
    metonic.cycle = (year - 1) / 19;
    metonic.year = (year - 1) % 19;
    
    getMoladOfMetonicCycle(metonic.cycle, molad);

    molad.halakim += HALAKIM_PER_LUNAR_CYCLE * yearOffset[metonic.year];
    molad.day += molad.halakim / HALAKIM_PER_DAY;
    molad.halakim = molad.halakim % HALAKIM_PER_DAY;

    return getTishri1(metonic.year, molad);
  }

  /**
   * Given a serial day number (SDN), find the corresponding year, month and
   * day in the Jewish calendar.  The three output values will always be
   * modified.  If the input SDN is before the first day of year 1, they will
   * all be set to zero, otherwise *pYear will be > 0; *pMonth will be in the
   * range 1 to 13 inclusive; *pDay will be in the range 1 to 30 inclusive.
   */
  private PointInTime SdnToJewish(int sdn) {

    int year, month, day;
    
    Molad molad = new Molad();
    Metonic metonic = new Metonic();
    int inputDay;
    int tishri1;
    int tishri1After;
    int yearLength;

    if (sdn <= SDN_OFFSET)
      return null;
      
    inputDay = sdn - SDN_OFFSET;

    FindTishriMolad(inputDay, metonic, molad);
    
    tishri1 = getTishri1(metonic.year, molad);

    if (inputDay >= tishri1) {
      /* It found Tishri 1 at the start of the year. */
      year = metonic.cycle * 19 + metonic.year + 1;
      if (inputDay < tishri1 + 59) {
        if (inputDay < tishri1 + 30) {
          month = 1;
          day = inputDay - tishri1 + 1;
        } else {
          month = 2;
          day = inputDay - tishri1 - 29;
        }
        return wrap(day, month, year);
      }

      /* We need the length of the year to figure this out, so find
       * Tishri 1 of the next year. */
      molad.halakim += HALAKIM_PER_LUNAR_CYCLE * monthsPerYear[metonic.year];
      molad.day += molad.halakim / HALAKIM_PER_DAY;
      molad.halakim = molad.halakim % HALAKIM_PER_DAY;
      tishri1After = getTishri1((metonic.year + 1) % 19, molad);
      
    } else {
      
      /* It found Tishri 1 at the end of the year. */
      year = metonic.cycle * 19 + metonic.year;
      if (inputDay >= tishri1 - 177) {
        
        /* It is one of the last 6 months of the year. */
        if (inputDay > tishri1 - 30) {
          month = 13;
          day = inputDay - tishri1 + 30;
        } else if (inputDay > tishri1 - 60) {
          month = 12;
          day = inputDay - tishri1 + 60;
        } else if (inputDay > tishri1 - 89) {
          month = 11;
          day = inputDay - tishri1 + 89;
        } else if (inputDay > tishri1 - 119) {
          month = 10;
          day = inputDay - tishri1 + 119;
        } else if (inputDay > tishri1 - 148) {
          month = 9;
          day = inputDay - tishri1 + 148;
        } else {
          month = 8;
          day = inputDay - tishri1 + 178;
        }
        
        return wrap(day, month, year);
        
      } else {
        
        if (monthsPerYear[(year - 1) % 19] == 13) {
          month = 7;
          day = inputDay - tishri1 + 207;
          if (day > 0) 
            return wrap(day, month, year);
          (month)--;
          (day) += 30;
          if (day > 0) 
            return wrap(day, month, year);
          (month)--;
          (day) += 30;
        } else {
          month = 6;
          day = inputDay - tishri1 + 207;
          if (day > 0) 
            return wrap(day, month, year);
          (month)--;
          (day) += 30;
        }
        if (day > 0) 
          return wrap(day, month, year);
        (month)--;
        (day) += 29;
        if (day > 0) 
          return wrap(day, month, year);

        /* We need the length of the year to figure this out, so find
         * Tishri 1 of this year. */
        tishri1After = tishri1;
        FindTishriMolad(molad.day - 365, metonic, molad);
        tishri1 = getTishri1(metonic.year, molad);
      }
    }

    yearLength = tishri1After - tishri1;
    day = inputDay - tishri1 - 29;

    if (yearLength == 355 || yearLength == 385) {
      /* Heshvan has 30 days */
      if (day <= 30) {
        month = 2;
        return wrap(day, month, year);
      }
      day -= 30;
    } else {
      /* Heshvan has 29 days */
      if (day <= 29) {
        month = 2;
        return wrap(day, month, year);
      }
      day -= 29;
    }

    /* It has to be Kislev. */
    month = 3;
    
    return wrap(day, month, year);
  }

  /**
   * Given a year, month and day in the Jewish calendar, find the
   * corresponding serial day number (SDN).  Zero is returned when the input
   * date is detected as invalid.  The return value will be > 0 for all valid
   * dates, but there are some invalid dates that will return a positive
   * value.  To verify that a date is valid, convert it to SDN and then back
   * and compare with the original.
   */
  private int JewishToSdn(int year, int month, int day) {
    
    int sdn;
    int tishri1;
    int tishri1After;
    int yearLength;
    int lengthOfAdarIAndII;
    Molad molad = new Molad();
    Metonic metonic = new Metonic();

    if (year <= 0 || day <= 0 || day > 30)
      return(0);

    switch (month) {
      case 1:
      case 2:
        /* It is Tishri or Heshvan - don't need the year length. */
        tishri1 = FindStartOfYear(year, metonic, molad);
        if (month == 1) {
            sdn = tishri1 + day - 1;
        } else {
            sdn = tishri1 + day + 29;
        }
        break;
      case 3:
        /* It is Kislev - must find the year length. */

        /* Find the start of the year. */
        tishri1 = FindStartOfYear(year, metonic, molad);

        /* Find the end of the year. */
        molad.halakim += HALAKIM_PER_LUNAR_CYCLE * monthsPerYear[metonic.year];
        molad.day += molad.halakim / HALAKIM_PER_DAY;
        molad.halakim = molad.halakim % HALAKIM_PER_DAY;
        
        tishri1After = getTishri1((metonic.year + 1) % 19, molad);

        yearLength = tishri1After - tishri1;

        if (yearLength == 355 || yearLength == 385) {
            sdn = tishri1 + day + 59;
        } else {
            sdn = tishri1 + day + 58;
        }
        break;
      case 4:
      case 5:
      case 6:
        /* It is Tevet, Shevat or Adar I - don't need the year length. */
        tishri1After = FindStartOfYear(year + 1, metonic, molad);

        if (monthsPerYear[(year - 1) % 19] == 12) {
          lengthOfAdarIAndII = 29;
        } else {
          lengthOfAdarIAndII = 59;
        }

        if (month == 4) {
          sdn = tishri1After + day - lengthOfAdarIAndII - 237;
        } else if (month == 5) {
          sdn = tishri1After + day - lengthOfAdarIAndII - 208;
        } else {
          sdn = tishri1After + day - lengthOfAdarIAndII - 178;
        }
        break;
      default:
        /* It is Adar II or later - don't need the year length. */
        tishri1After = FindStartOfYear(year + 1, metonic, molad);

        switch (month) {
        case  7:
          sdn = tishri1After + day - 207;
          break;
        case  8:
          sdn = tishri1After + day - 178;
          break;
        case  9:
          sdn = tishri1After + day - 148;
          break;
        case 10:
          sdn = tishri1After + day - 119;
          break;
        case 11:
          sdn = tishri1After + day - 89;
          break;
        case 12:
          sdn = tishri1After + day - 60;
          break;
        case 13:
          sdn = tishri1After + day - 30;
          break;
        default:
          return(0);
        }
      }
      return(sdn + SDN_OFFSET);
  }
    
} //HebrewCalendar