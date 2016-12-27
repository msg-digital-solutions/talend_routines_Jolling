/**
 * Copyright 2016, Jan Lolling, jan.lolling@cimt-ag.de
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package routines;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class to parse a String into a Date 
 * by testing a number of common pattern
 * This class is thread save.
 * 
 * @author jan.lolling@cimt-ag.de
 */
public class GenericDateUtil {
	
	private static ThreadLocal<DateParser> threadLocal = new ThreadLocal<DateParser>();
	
	public static Date parseDate(String source) throws ParseException {
		return parseDate(source, (String[]) null);
	}

    /**
     * parseDate: returns the Date from the given text representation
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * 
     * {Category} GenericDateUtil
     * 
     * {talendTypes} Date
     * 
     * {param} String(dateString)
     * {param} String(suggestedPattern)
     * 
     * {example} parseDate(dateString, suggestedPattern).
     */
	public static Date parseDate(String source, String ...suggestedPattern) throws ParseException {
		DateParser p = threadLocal.get();
		if (p == null) {
			p = new DateParser();
			threadLocal.set(p);
		}
		return p.parseDate(source, suggestedPattern);
	}
	
    /**
     * parseTime: returns the Date from the given text representation which consists only the time part
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * 
     * {Category} GenericDateUtil
     * 
     * {talendTypes} Date
     * 
     * {param} String(timeString)
     * {param} String(suggestedPattern)
     * 
     * {example} parseTime(timeString, suggestedPattern).
     */
	public static Date parseTime(String timeString, String ...suggestedPattern) throws ParseException {
		DateParser p = threadLocal.get();
		if (p == null) {
			p = new DateParser();
			threadLocal.set(p);
		}
		return p.parseTime(timeString, suggestedPattern);
	}

	static class DateParser {
		
		private List<String> datePatternList = null;
		private List<String> timePatternList = null;
		
		DateParser() {
			datePatternList = new ArrayList<String>();
			datePatternList.add("yyyy-MM-dd");
			datePatternList.add("dd.MM.yyyy");
			datePatternList.add("d.MM.yyyy");
			datePatternList.add("d.M.yy");
			datePatternList.add("dd.MM.yy");
			datePatternList.add("dd.MMM.yyyy");
			datePatternList.add("yyyyMMdd");
			datePatternList.add("dd/MM/yyyy");
			datePatternList.add("dd/MM/yy");
			datePatternList.add("dd/MMM/yyyy");
			datePatternList.add("d/M/yy");
			datePatternList.add("MM/dd/yyyy");
			datePatternList.add("MM/dd/yy");
			datePatternList.add("dd/MMM/yyyy");
			datePatternList.add("M/d/yy");
			datePatternList.add("dd-MM-yyyy");
			datePatternList.add("dd-MM-yy");
			datePatternList.add("dd-MMM-yyyy");
			datePatternList.add("d-M-yy");
			datePatternList.add("yyyyMM");
			datePatternList.add("yyyy");
			timePatternList = new ArrayList<String>();
			timePatternList.add("'T'HH:mm:ss.SSSZ");
			timePatternList.add(" HHmmss");
			timePatternList.add(" HH'h'mm'm'ss's'");
			timePatternList.add(" HH'h' mm'm' ss's'");
			timePatternList.add(" HH:mm:ss.SSS");
			timePatternList.add(" HH:mm:ss");
			timePatternList.add(" mm'′'ss'\"'");
			timePatternList.add(" HH'h'mm'm'");
			timePatternList.add(" HH'h' mm'm'");
		}
		
		public Date parseDate(String text, String ... userPattern) throws ParseException {
			if (text != null) {
				SimpleDateFormat sdf = new SimpleDateFormat();
				Date dateValue = null;
				if (userPattern != null) {
					for (String pattern : userPattern) {
						if (datePatternList.contains(pattern)) {
							datePatternList.remove(pattern);
						}
						datePatternList.add(0, pattern);
					}
				}
				for (String pattern : datePatternList) {
					sdf.applyPattern(pattern);
					try {
						dateValue = sdf.parse(text);
						// if we continue here the pattern fits
						// now we know the date is correct, lets try the time part:
						if (text.length() - pattern.length() >= 6) {
							// there is more in the text than only the date
							for (String timepattern : timePatternList) {
								String dateTimePattern = pattern + timepattern;
								sdf.applyPattern(dateTimePattern);
								try {
									dateValue = sdf.parse(text);
									// we got it
									pattern = dateTimePattern;
									break;
								} catch (ParseException e1) {
									// ignore parsing errors, we are trying
								}
							}
						}
						// set this pattern at the top of the list to shorten the next attempt
						int pos = datePatternList.indexOf(pattern);
						if (pos > 0) {
							datePatternList.remove(pos);
						}
						datePatternList.add(0, pattern);
						return dateValue;
					} catch (ParseException e) {
						// the pattern obviously does not work
						continue;
					}
				}
				throw new ParseException("The value: " + text + " could not be parsed to a Date.", 0);
			} else {
				return null;
			}
		}

		public Date parseTime(String text, String ... userPattern) throws ParseException {
			SimpleDateFormat sdf = new SimpleDateFormat();
			Date timeValue = null;
			if (userPattern != null) {
				for (String pattern : userPattern) {
					if (timePatternList.contains(pattern)) {
						timePatternList.remove(pattern);
					}
					timePatternList.add(0, pattern);
				}
			}
			for (String pattern : timePatternList) {
				sdf.applyPattern(pattern.trim());
				try {
					timeValue = sdf.parse(text);
					// if we continue here the pattern fits
					// set this pattern at the top of the list to shorten the next attempt
					int pos = timePatternList.indexOf(pattern);
					if (pos > 0) {
						timePatternList.remove(pos);
					}
					timePatternList.add(0, pattern);
					return timeValue;
				} catch (ParseException e) {
					// the pattern obviously does not work
					continue;
				}
			}
			throw new ParseException("The value: " + text + " could not be parsed to a Date (only with time).", 0);
		}

	}
	
}