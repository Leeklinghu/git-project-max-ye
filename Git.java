import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Git {

    public static void main(String[] args) {
        try {
            if (!CreateFolderFile()) {
                System.out.println("Git Repository Already Exists");
            } else {
                System.out.println("Git Repository Created");
            }
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    public static boolean CreateFolderFile() throws IOException { // return false if there is no
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
        StringBuilder toSave = new StringBuilder();
        int character = reader.read();
        while (character != -1) {
            toSave.append((char) character);
            character = reader.read();
        }
        reader.close();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        byte[] hashBytes = digest.digest(toSave.toString().getBytes("UTF-8"));
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

}
