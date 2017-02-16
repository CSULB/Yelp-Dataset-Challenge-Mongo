package com.csulb.yelp;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Helper {

	public static Set<String> removeDuplicates(List<String> aList) {
		return new LinkedHashSet<>(aList);
	}

	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static int milesToRadian(int miles) {
		int earthRadiusInMiles = 3959;
		return miles / earthRadiusInMiles;
	}

}
