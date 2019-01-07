package com.pshc.he.cloud.api;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Data
public class CloudFile {
	public @NonNull String name;
	public String size;
	public String lastModified;
	
//	private String changeByte(long size) {
//		System.out.println("2222"+size);
//		if (size < 9999) {
//			long sizeKB = size / 1024;
//			return Long.toString(sizeKB) + "KB";
//		} else {
//			long sizeMB = size / (1024 * 1024);
//			System.out.println("sizeMB"+sizeMB);
//			return Long.toString(sizeMB) + "MB";
//		}
//	}
//
//	public String getSize() {
//		if (size != null) {
//			System.out.println("11111111111   "+size);
//			long filesize = Long.parseLong(size);
//			System.out.println("filesize"+filesize);
//			String returnVal = changeByte(filesize);
//			System.out.println(returnVal);
//			return returnVal;
//		} else {
//			return null;
//		}
//
//	}

}
