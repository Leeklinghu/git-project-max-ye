import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class Git {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Git <filePath>");
            return;
        }
        try {
            if (!init()) {
                System.out.println("Git Repository Already Exists adding " + args[0]);
            } else {
                System.out.println("Git Repository Created");
            }
            File argument = new File(args[0]); // if you hard code this ima crash out, arg zero just takes the first
                                               // arugemnt i.e java Git txt.file txt.file is the first arg
            // it also works for dir's now
            if (args[0].equals("tree")) {
                String rootHash = createTreeFromIndex();
                System.out.println("Root tree: " + rootHash);
                return;
            }
            if (argument.isDirectory()) {
                ArrayList<File> folders = new ArrayList<>();
                folders.add(argument);
                for (int i = 0; i < folders.size(); i++) {
                    File folder = folders.get(i);
                    File[] files = folder.listFiles();

                    if (files == null) {
                        throw new IOException("Could not read directory: " + folder.getPath());
                    }

                    for (File file : files) {
                        if (file.isDirectory()) {
                            folders.add(file);
                        } else {
                            index(file.getPath());
                        }
                    }
                }
            } else {
                index(args[0]);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    public static boolean init() throws IOException { // return false if there is no
                                                      // folder that is created,else
                                                      // return true;
                                                      // classic yiewi ye comment :<
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
        File HeadFile = new File(gitFolder, "HEAD");
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

    public static String createTree(ArrayList<String> workingList, String dirPath)
            throws IOException, NoSuchAlgorithmException {

        ArrayList<String> childern = new ArrayList<>();

        for (String entry : workingList) {
            String[] parts = entry.split(" ", 3);
            String path = parts[2];

            int end = path.lastIndexOf('/');
            String dir = "";

            if (end != -1) {
                dir = path.substring(0, end);
            }

            if (dir.equals(dirPath)) {
                String fileName = path.substring(end + 1);
                childern.add(parts[0] + " " + parts[1] + " " + fileName);
            }
        }

        for (int i = 0; i < childern.size() - 1; i++) {
            for (int j = i + 1; j < childern.size(); j++) {
                String currName = childern.get(i).split(" ", 3)[2];
                String prevName = childern.get(j).split(" ", 3)[2];

                if (currName.compareTo(prevName) > 0) {
                    String hold = childern.get(i);
                    childern.set(i, childern.get(j));
                    childern.set(j, hold);
                }
            }
        }

        StringBuilder content = new StringBuilder();

        for (String child : childern) {
            content.append(child).append('\n');
        }

        byte[] treeBytes = content.toString().getBytes("UTF-8");
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(treeBytes);

        StringBuilder hex = new StringBuilder();

        for (byte bit : hashBytes) {
            hex.append(String.format("%02x", bit & 0xff));
        }
        String hash = hex.toString();
        Path objectsFolder = Path.of("git", "objects");
        Files.createDirectories(objectsFolder);
        Path treeFile = objectsFolder.resolve(hash);

        if (!Files.exists(treeFile)) {
            Files.write(treeFile, treeBytes);
        }
        return hash;
    }

    public static String createTreeFromIndex() throws IOException, NoSuchAlgorithmException {

        ArrayList<String> workingList = new ArrayList<>();

        for (String line : Files.readAllLines(Path.of("git", "index"))) {
            if (!line.isBlank()) {
                workingList.add("blob " + line);
            }
        }

        for (int i = 0; i < workingList.size() - 1; i++) { // as seen on tv/above
            for (int j = i + 1; j < workingList.size(); j++) {
                String firstPath = workingList.get(i).split(" ", 3)[2];
                String secondPath = workingList.get(j).split(" ", 3)[2];

                if (firstPath.compareTo(secondPath) > 0) {
                    String hold = workingList.get(i);
                    workingList.set(i, workingList.get(j));
                    workingList.set(j, hold);
                }
            }
        }

        while (true) {
            String Deepdir = "";
            int Deepdirlevel = -1;

            for (String entry : workingList) {
                String path = entry.split(" ", 3)[2];
                int depth = path.split("/").length - 1;

                if (depth > Deepdirlevel) {
                    Deepdirlevel = depth;
                    Deepdir = getDirectory(path);
                }
            }

            String hash = createTree(workingList, Deepdir);

            for (int i = workingList.size() - 1; i >= 0; i--) {
                String coolpath = workingList.get(i).split(" ", 3)[2];

                if (getDirectory(coolpath).equals(Deepdir)) {
                    workingList.remove(i);
                }
            }

            workingList.add("tree " + hash + " " + Deepdir);
            if (Deepdir.isEmpty()) {
                return hash;
            }
        }
    }

    private static String getDirectory(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }
        return path.substring(0, lastSlash);
    }

    public void commit(String author, String message) throws IOException, NoSuchAlgorithmException {
        Path headPath = Paths.get("git/HEAD");
        // case 1: first commit
        File temp = new File("git/temp");
        temp.createNewFile();
        String rootHash = createTreeFromIndex();
        FileWriter writer = new FileWriter(temp);
        if (!Files.exists(headPath) || Files.size(headPath) == 0) {
            writer.write("tree: " + rootHash + "\nauthor: " + author + "\ndate: " + LocalDateTime.now()
                    + "\nmessage: " + message);
        } else {
            // case 2: previous commits exist
            writer.write("tree: " + rootHash + "\nparent: " + Files.readString(headPath) + "\nauthor: " + author
                    + "\ndate: " + LocalDateTime.now() + "\nmessage: " + message);
        }
        writer.close();
        // update head
        String commitHash = createBlob("git/temp");
        Path path = Paths.get("git/temp");
        Files.deleteIfExists(path);
        FileWriter headWriter = new FileWriter("git/HEAD");
        headWriter.write(commitHash);
        headWriter.close();
    }
}