import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Git {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Git <filePath>");
            return;
        }
        try {
            if (!init()) {
                System.out.println("Git Repository Already Exists");
            } else {
                System.out.println("Git Repository Created");
            }
            index(args[0]);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    public static boolean init() throws IOException { // return false if there is no
                                                                  // folder that is created,else
                                                                  // return true;
        int CreatedCount = 0;
        File gitFolder = new File("git/");
        if (!gitFolder.exists()) {
            gitFolder.mkdir();
            CreatedCount++;
        }
        File objectFolder = new File(gitFolder, "objects/");
        if (!objectFolder.exists()) {
            objectFolder.mkdir();
            CreatedCount++;
        }
        File indexFile = new File(gitFolder, "index");
        if (!indexFile.exists()) {
            indexFile.createNewFile();
            CreatedCount++;
        }
        File HeadFile = new File(gitFolder, "Head");
        if (!HeadFile.exists()) {
            HeadFile.createNewFile();
            CreatedCount++;
        }

        if (CreatedCount == 0) {
            return false;
        } else {
            return true;
        }

    }

    public static String hashFile(String filePath) throws IOException, NoSuchAlgorithmException {
        Path path = Path.of(filePath);  
        if (!Files.isRegularFile(path)) {
            throw new IOException("No such files: " + filePath);
        }
        FileReader reader = new FileReader(filePath);
        StringBuilder fileContent = new StringBuilder();
        int character = reader.read();
        while (character != -1) {
            fileContent.append((char) character);
            character = reader.read();
        }
        reader.close();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        byte[] hashBytes = digest.digest(fileContent.toString().getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();

        for (byte bite : hashBytes) {
            hex.append(String.format("%02x", bite & 0xff));
        }

        return hex.toString();

    }

    public static String createBlob(String filePath) throws IOException, NoSuchAlgorithmException {
        String hash = hashFile(filePath);
        File objectsFolder = new File("git/objects");

        if (!objectsFolder.exists()) {
            if (!objectsFolder.mkdirs()) {
                throw new IOException("Could not create objects folder");
            }
        }

        File blobFile = new File(objectsFolder, hash);

        if (!blobFile.exists()) {
            FileReader reader = new FileReader(filePath);
            FileWriter writer = new FileWriter(blobFile);

            int currentByte = reader.read();

            while (currentByte != -1) {
                writer.write(currentByte);
                currentByte = reader.read();
            }

            reader.close();
            writer.close();
        }

        return hash;
    }

    public static void index(String filePath) throws IOException, NoSuchAlgorithmException {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path file = root.resolve(filePath).normalize();
		if (!file.startsWith(root) || !Files.isRegularFile(file)) {
     	   throw new IOException("no" + filePath);
    	}
        String relativePath = root.relativize(file).toString()
                .replace(File.separatorChar, '/');

        String hash = createBlob(file.toString());

        Path index = Path.of("git", "index");
        Files.createDirectories(index.getParent());

        ArrayList<String> lines = new ArrayList<>();

		if (Files.exists(index)) {
        	for (String line : Files.readAllLines(index)) {
                if (line.equals(hash + " " + relativePath)) {
                    return;
                }
            	int space = line.indexOf(' ');
        		if (space >= 0 && !line.substring(space + 1).equals(relativePath)) {
                	lines.add(line);
            	}
        	}
 		}

    	lines.add(hash + " " + relativePath);
    	Files.writeString(index, String.join("\n", lines));
	}
        
}
