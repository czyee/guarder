package org.czyee.guarder.util;

public class Utils {

	/**
	 * 将字符串中指定字符trim掉
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static String stringTrimWith(String source , char pattern){
		char[] charArray = source.toCharArray();
		int start = 0;
		int end = charArray.length;

		for(int i = 0 ; i < charArray.length ; i ++){
			if (charArray[i] == pattern){
				start++;
			}else{
				break;
			}
		}
		if (start == charArray.length){
			return "";
		}
		for(int i = charArray.length - 1 ; i > 0 ; i--){
			if (charArray[i] == pattern){
				end--;
			}else {
				break;
			}
		}
		return new String(charArray,start,end - start);
	}
}
