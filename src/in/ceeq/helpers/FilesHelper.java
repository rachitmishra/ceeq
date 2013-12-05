package in.ceeq.helpers;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class FilesHelper {

	private Context context;
	private DataOutputStream out;
	private File storageLocation;
	private Helpers helpers;
	public static final String APP_PATH = "/data/ceeq";
	public static final String BACKUP_PATH = "/data/ceeq/backups";
	public static final String CAM_PATH = "/data/ceeq/camera";

	public FilesHelper(Context context) {
		this.context = context.getApplicationContext();
		helpers = Helpers.getInstance(context);
	}

	public static FilesHelper getInstance(Context context) {
		return new FilesHelper(context);
	}

	public File createFile(String path, String type) throws IOException,
			ExternalStorageNotFoundException {
		if (!helpers.hasExternalStorage()) {
			throw new ExternalStorageNotFoundException();
		}
		storageLocation = new File(Environment.getExternalStorageDirectory(),
				path);

		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}

		File file = new File(storageLocation, getFileName(type));
		file.createNewFile();
		return file;
	}

	public boolean haveBackupFiles() {
		if (!helpers.hasExternalStorage()) {
			Toast.makeText(context, "Sorry, External storage not found.",
					Toast.LENGTH_SHORT).show();
		}
		storageLocation = new File(Environment.getExternalStorageDirectory(),
				BACKUP_PATH);
		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}
		if ((storageLocation.listFiles()).length == 0)
			return false;
		return true;
	}

	public File[] getFiles(String path) {
		if (!helpers.hasExternalStorage()) {
			Toast.makeText(context, "Sorry, External storage not found.",
					Toast.LENGTH_SHORT).show();
		}
		storageLocation = new File(Environment.getExternalStorageDirectory(),
				path);
		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}
		File[] files = storageLocation.listFiles();
		return files;
	}

	public HashMap<String, ArrayList<String>> getFileNames(File[] files) {
		HashMap<String, ArrayList<String>> fileNames = new HashMap<String, ArrayList<String>>();
		for (File file : files)
			fileNames.put(
					file.getName(),
					new ArrayList<String>(Arrays.asList(new String[]{
							fileType(file.getName()),
							(file.length() / 1024) + "",
							DateTime.forInstant(file.lastModified(),
									TimeZone.getDefault()).toString()
									.substring(0, 10)})));
		return fileNames;
	}

	public String fileType(String name) {
		if (name.contains("contact"))
			return "Contacts";
		if (name.contains("message"))
			return "Messages";
		if (name.contains("calls"))
			return "Call logs";
		if (name.contains("dictionary"))
			return "User Dictionary";
		return name;
	}

	public String writeFile(String text, String type) throws IOException,
			ExternalStorageNotFoundException {

		FileOutputStream fos = new FileOutputStream(createFile(BACKUP_PATH,
				type));
		out = new DataOutputStream(fos);
		out.writeBytes(text);
		out.close();

		return getFileName(type);
	}

	public InputStream readFile(String fileName) throws FileNotFoundException,
			ExternalStorageNotFoundException {
		if (!helpers.hasExternalStorage()) {
			throw new ExternalStorageNotFoundException();
		}
		return new FileInputStream(Environment.getExternalStorageDirectory()
				+ BACKUP_PATH + "/" + fileName);
	}

	public boolean deleteFile(String path, String[] fileName)
			throws ExternalStorageNotFoundException {
		boolean deleted = false;
		if (!helpers.hasExternalStorage()) {
			throw new ExternalStorageNotFoundException();
		} else {

			storageLocation = new File(
					Environment.getExternalStorageDirectory(), path);
			for (int i = 0; i < fileName.length; i++) {
				new File(storageLocation + "/" + fileName[i]).delete();
			}
			deleted = true;
		}
		return deleted;
	}

	public boolean uploadFile(String filename) {
		return false;
	}

	public String getFileName(String type) {
		if (type.equals("cam"))
			return type + "_" + helpers.getNewFileName() + ".jpg";
		else
			return type + "_" + helpers.getNewFileName() + ".xml";
	}
}