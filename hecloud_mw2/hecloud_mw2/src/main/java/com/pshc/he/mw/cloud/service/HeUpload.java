package com.pshc.he.mw.cloud.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HeUpload {
	@Autowired
	private HeCloudService heCloud;

	private ServletContext context;
	private ServletFileUpload upload;
	private InputStream input;
	private OutputStream output;

	public HeUpload() {
		this.upload = new ServletFileUpload();
		upload.setSizeMax(50 * 1024 * 1024);// 최대 파일사이즈
	}

	public void doWork(HttpServletRequest request) throws FileUploadException, IOException {
		/*
		 * ServletFileUpload 생성자에 DiskFileItemFactory 굳이 안넣어도 upload 되는듯...이유는 모르겠네
		 * DiskFileItemFactory factory = new DiskFileItemFactory(); //임시 저장소 인스턴스 생성
		 * factory.setSizeThreshold(40000);//메모리 저장 사이즈 설정한 메모리 사이즈보다 큰 파일이면 저장소에 보관?
		 * factory.setRepository();//저장소 지정할수있음.
		 */
		context = request.getSession().getServletContext(); // 웹 컨테이너 저장소 ? 같은
		String fileUploadPath = context.getRealPath(""); // 굳이 context 경로 사용하지 않아도됨.
		FileItemIterator iterator = upload.getItemIterator(request); // 실제 업로드 하는부분
		// log
		while (iterator.hasNext()) {
			FileItemStream fileItem = iterator.next();
			String itemName = fileItem.getFieldName();
			log.info("Item : " + itemName);

			if (!fileItem.isFormField()) {
				String fileName = fileItem.getName();
				File uploadFile = new File(fileUploadPath + fileName);
				input = fileItem.openStream();
				output = new FileOutputStream(uploadFile);
				try {
					byte[] bytes = new byte[4096];
					int length = -1;
					while ((length = input.read(bytes)) != -1) {
						// log.info(Integer.toString(length));
						output.write(bytes, 0, length);
					}
					output.flush();
					testCode(uploadFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// Not FileItem
			}

			if (input != null)
				input.close();
			if (output != null)
				output.close();
		}

	}

	public void testCode(File file) {

		heCloud.uploadFile("hemn", file.getName(), file);

	}
}
