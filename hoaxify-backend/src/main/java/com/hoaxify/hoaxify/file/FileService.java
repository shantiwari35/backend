package com.hoaxify.hoaxify.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.user.User;

@Service
@EnableScheduling
public class FileService {
	
	@Autowired
	AppConfiguration appConfiguration;
	
	
	Tika tika;
	
	@Autowired
	FileAttachmentRepository fileAttachmentRepository;
//
//	public FileService(AppConfiguration appConfiguration, FileAttachmentRepository fileAttachmentRepository) {
//		super();
//		this.appConfiguration = appConfiguration;
//		this.fileAttachmentRepository = fileAttachmentRepository;
//		tika = new Tika();
//	}
	
	public String saveProfileImage(String base64Image) throws IOException {
		String imageName = getRandomName();
		
		byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
		File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
		FileUtils.writeByteArrayToFile(target, decodedBytes);
		return imageName;
	}

	private String getRandomName() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public String detectType(byte[] fileArr) {
		tika = new Tika();
		return tika.detect(fileArr);
	}
	public void deleteAttachmentFile(String oldImageName) {
		if(oldImageName == null) {
			return;
		}
		deleteFile(Paths.get(appConfiguration.getFullAttachmentsPath(), oldImageName));
	}

	private void deleteFile(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void deleteProfileImage(String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath()+"/"+image));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public FileAttachment saveAttachment(MultipartFile file) {
		FileAttachment fileAttachment = new FileAttachment();
		fileAttachment.setDate(new Date());
		String randomName = getRandomName();
		fileAttachment.setName(randomName);
		
		File target = new File(appConfiguration.getFullAttachmentsPath() +"/"+randomName);
		try {
			byte[] fileAsByte = file.getBytes();
			FileUtils.writeByteArrayToFile(target, fileAsByte);
			fileAttachment.setFileType(detectType(fileAsByte));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileAttachmentRepository.save(fileAttachment);
	}

	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void cleanupStorage() {
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> oldFiles = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		for(FileAttachment file: oldFiles) {
			deleteAttachmentImage(file.getName());
			fileAttachmentRepository.deleteById(file.getId());
		}
		
	}

	public void deleteAttachmentImage(String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentsPath()+"/"+image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteAllStoredFilesForUser(User inDB) {
		deleteProfileImage(inDB.getImage());
		List<FileAttachment> filesToBeRemoved = fileAttachmentRepository.findByHoaxUser(inDB);
		for(FileAttachment file: filesToBeRemoved) {
			deleteAttachmentFile(file.getName());
		}
		
	}

}
