package com.spoons.popparazzi.util;

import org.springframework.stereotype.Component;

@Component
public class ByteArrayStringToByteArry {
	
	public byte [] getByteArray(String byteArrayString) {
		
		if(byteArrayString == null) return null;
		
		byteArrayString = byteArrayString.replaceAll("\\[|\\]|\\s", ""); // 대괄호와 공백 제거
		String[] values = byteArrayString.split(",");

		StringBuilder sb = new StringBuilder();

		byte [] value = new byte[values.length];	
		
		for (int i = 0; i < values.length; i++) {
			
			int decimal = Integer.parseInt(values[i]);
			
			byte result = (byte)( decimal & 0xFF);
			
			value[i] = result;
			
		}
		
		return value;
		
	}
}
