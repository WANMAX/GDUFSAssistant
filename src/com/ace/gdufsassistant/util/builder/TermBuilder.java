/**
 * 
 */
package com.ace.gdufsassistant.util.builder;

import java.util.Calendar;

import com.ace.gdufsassistant.util.Reload;
import com.ace.gdufsassistant.util.ReloadApplication;

import main.API;

/**
 * @author wan
 *
 */
public class TermBuilder implements Reload{
	private static String[] year_list;
	private static String[] term_list = { "1", "2", "3" };
	
	private TermBuilder(){ReloadApplication.getInstance("login").addReload(this);}
	private static class TermBuilderContainer{private static TermBuilder instance = new TermBuilder();}
	public static TermBuilder getInstance(){
		return TermBuilderContainer.instance;
	}

	public String[] getYear() {
		if (year_list == null) {
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			if (month < 8) {
				year -= 1;
			}
			int len = 6;
			API api = API.getInstance();
			if(api.isLogged()){
				try{
					String temp = api.getStudentNumber();
					int endYear = Integer.parseInt(temp.substring(0,4));
					len = year + 1 - endYear;
					if(len>6)
						len=6;
				}catch(Exception e){}
			}
			year_list = new String[len];
			for (int i = 0; i < len; i++) {
				year_list[i] = String.valueOf(year - i) + "-" + String.valueOf(year - i + 1);
			}
			return year_list;
		}
		return year_list;
	}

	public String[] getTerm() {
		return term_list;
	}

	@Override
	public void reload() {
		year_list = null;
	}
}
