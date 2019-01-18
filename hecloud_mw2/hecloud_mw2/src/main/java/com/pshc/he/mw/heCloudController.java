package com.pshc.he.mw;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pshc.he.cloud.command.DownFileCommand;
import com.pshc.he.cloud.command.FolderListCommand;
import com.pshc.he.mw.cloud.conf.RestURIConstants;
import com.pshc.he.mw.cloud.service.HeCloudService;
import com.pshc.he.mw.cloud.service.HeUpload;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class heCloudController {
	@Autowired
	private HeCloudService heCloud;
	@Autowired
	private HeUpload heUpload;

	protected String getRemoteIp() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String ip = req.getHeader("X-FORWARDED-FOR");
		if (ip == null) {
			ip = req.getRemoteAddr();
		}
		log.debug(req.getHeader("user-agent"));

		return ip;
	}

	protected String getClientInfo() {
		return "C:" + getRemoteIp() + ", Rq:";
	}

	@RequestMapping(value = RestURIConstants.GET_BUCKET_LIST, method = RequestMethod.POST)
	public List<Map<String, Object>> getBucketList(HttpServletResponse response) {
		log.info(getClientInfo() + RestURIConstants.GET_BUCKET_LIST);

		List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
		try {
			heCloud.getBucketList(objectList);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		return objectList;
	}

	@PostMapping("/getfile")
	public String getFolderList(HttpServletRequest request,
				 HttpServletResponse response)
			throws Exception {
		log.info(getClientInfo() + "/getfile");
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if (!isMultipart) {
			throw new Exception("Multipart is Not");
		}
		heUpload.doWork(request);
		
		return "sucess";

	}

	@PostMapping(value = RestURIConstants.GET_FILE_LIST)
	public List<Map<String, Object>> getFileList(FolderListCommand command, HttpServletResponse response) {

		log.info(getClientInfo() + RestURIConstants.GET_FILE_LIST + "?bucketName=" + command.bucketName);

		List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
		String bucketName = command.getBucketName();
		String studyDate = command.getStudyDate();
		try {
			if (bucketName != null && bucketName != "") {
				heCloud.getFolderToFileList(bucketName, studyDate, objectList);
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		// testtest
		return objectList;
	}

	@PostMapping(value = RestURIConstants.GET_DOWN_FILE)
	public void getDownFile(DownFileCommand command, HttpServletResponse response) {
		log.info(getClientInfo() + RestURIConstants.GET_DOWN_FILE + "?bucketName=" + command.getBucketName()
				+ "&studyDate=" + command.getSavePath() + "&studyUid=" + command.getStudyUid());

		String bucketName = command.getBucketName();
		String savePath = command.getSavePath();
		String studyUid = command.getStudyUid() + ".7z";
		OutputStream responseOut = null;

		try {
			response.setHeader("Content-Disposition", "attachment; filename=" + studyUid);
			responseOut = response.getOutputStream();
			heCloud.downloadFile(bucketName, savePath, studyUid, responseOut);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			try {
				if (responseOut != null)
					responseOut.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
