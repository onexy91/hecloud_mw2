package com.pshc.he.cloud.command;

import lombok.Data;

@Data
public class DownFileCommand {
	public String bucketName;
	public String studyUid;
	public String savePath;
}
