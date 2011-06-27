package com.sqt001.ipcall.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

/**
 * List<String> lst = new ArrayList<String>();
		lst.add("DDD");
		lst.add("ADC");
		lst.add("VD");
		lst.add("王陆");
		lst.add("王财");
		lst.add("中");
		
		lst = StrUitl.sort(lst);
 * @author Wanglu
 *
 */
public class StrUitl {
	// 国标码和区位码转换常量
	private static int GB_SP_DIFF = 160;
	// 存放国标一级汉字不同读音的起始区位码
	private static int[] secPosValueList = { 1601, 1637, 1833, 2078, 2274,
			2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
			4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600 };

	// 存放国标一级汉字不同读音的起始区位码对应读音
	private static char[] firstLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
			'h', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w',
			'x', 'y', 'z' };

	private static String[] AZ = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
			"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
			"x", "y", "z" };

	/**
	 * 将集群中的数据以头字母进行升序排列
	 * @param lst
	 * @return
	 */
	public static List<String> sort(List<String> lst) {
		if(lst==null || lst.size()==0) {
			throw new IllegalArgumentException();
		}
		
		int l = lst.size();
		String temp;
		int j;
		int n = l - 1;
		for (int i = 0; i < l; i++) {
			for (j = 0; j < n; j++) {
				if (Character.toLowerCase(firstChar(lst.get(j))) >= Character
						.toLowerCase(firstChar(lst.get(j + 1)))) {
					temp = lst.get(j);
					lst.set(j, lst.get(j + 1));
					lst.set(j + 1, temp);
				}
			}
			n--;
		}
		for (int k = 0; k < l - 1; k++) {
			if (Character.toLowerCase(firstChar(lst.get(k))) == Character
					.toLowerCase(firstChar(lst.get(k + 1)))) {
				temp = lst.get(k);
				lst.set(k, lst.get(k + 1));
				lst.set(k + 1, temp);
			}
		}

		List<String> buf = new ArrayList<String>();
		for (int k = 0; k < l; k++) {
//			System.out.print(" " + lst.get(k));
			buf.add(lst.get(k));
		}

		return buf;
	}
	
	public static List<String> sort(String[] array) {
	  List<String> list = new ArrayList<String>();
	  Arrays.asList(array);
	  list = Arrays.asList(array);
	  return sort(list);
	}
	

	/**
	 * 返回数据首字母
	 * @param value
	 * @return
	 */
	public static char firstChar(String value) {
	  value = value.trim();

	  if (value==null||value.length()==0||value.equals(" ")) {
      return ' ';
    }
	  
		String firstStr = value.substring(0, 1);
		Log.i("tag", firstStr);
		if (isExistAZ(firstStr)) {
			char fc = firstStr.charAt(0);
			return fc;
		} else {
			char fc = chinaConvert(value);
			return fc;
		}
	}

	/**
	 * 返回中文首字母
	 * @param ch
	 * @return
	 */
	private static char chinaConvert(String ch) {
		byte[] bytes = new byte[2];
		try {
			bytes = ch.getBytes("GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		char result = '-';
		int secPosValue = 0;
		int i;
		for (i = 0; i < bytes.length; i++) {
			bytes[i] -= GB_SP_DIFF;
		}
		secPosValue = bytes[0] * 100 + bytes[1];
		for (i = 0; i < 23; i++) {
			if (secPosValue >= secPosValueList[i]
					&& secPosValue < secPosValueList[i + 1]) {
				result = firstLetter[i];
				break;
			}
		}
		return result;
	}

	private static boolean isExistAZ(String value) {
		value = value.toLowerCase();

		for (int i = 0; i < AZ.length; i++) {
			if (AZ[i].equals(value))
				return true;
		}

		return false;
	}
	
	/**
   * 快速排序 StrUitl.QuickSort(array, 0, array.length-1);
   * 
   * @param ar
   * @param istart
   * @param iend
   * @return
   */
  public static String[] QuickSort(String[] ar, int istart, int iend) {
    if (istart < iend) {
      int i = istart - 1;
      int j = iend + 1;
      char itemp = Character.toLowerCase(firstChar(ar[istart])); // ar[istart];
      String t;
      // 根据中间键检测前后数的大小，然后互换；i != j的加入是为了在else情况下的处理
      while (i + 1 != j && i != j) {
        if (Character.toLowerCase(firstChar(ar[i + 1])) < itemp) {
          i++;
        } else if (Character.toLowerCase(firstChar(ar[j - 1])) > itemp) {
          j--;
        } else {
          t = ar[i + 1];
          ar[++i] = ar[j - 1];
          ar[--j] = t;
        }
      }

      QuickSort(ar, istart, i);
      QuickSort(ar, i + 1, iend);
    }
    return ar;
  }

}
