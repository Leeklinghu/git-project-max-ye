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
        File headsFolder = new File(gitFolder, "refs/heads/");
        if (!headsFolder.exists()) {
            headsFolder.mkdirs();
            CreatedCount++;
        }
        File HeadFile = new File(gitFolder, "HEAD");
        if (!HeadFile.exists() || HeadFile.length() == 0) {
            Files.writeString(HeadFile.toPath(), "ref: refs/heads/main");
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
            throw new IOException("no " + filePath);
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

    public static String commit(String author, String message) throws IOException, NoSuchAlgorithmException {
        String head = readHead();
        String branch = branchFromHead(head);
        String parent;
        if (branch != null) {
            parent = readBranch(branch);
        } else {
            parent = head.isEmpty() ? null : head;
        }

        String rootHash = createTreeFromIndex();
        File temp = new File("git/temp");
        FileWriter writer = new FileWriter(temp);
        writer.write("tree: " + rootHash + "\n");
        if (parent != null) {
            writer.write("parent: " + parent + "\n");
        }
        writer.write("author: " + author + "\ndate: " + LocalDateTime.now() + "\nmessage: " + message);
        writer.close();
        String commitHash = createBlob("git/temp");
        Files.deleteIfExists(temp.toPath());

        if (branch != null) {
            Files.writeString(branchPath(branch), commitHash);
        } else {
            Files.writeString(Path.of("git", "HEAD"), commitHash);
        }
        return commitHash;
    }

    private static String readHead() throws IOException {
        Path headPath = Path.of("git", "HEAD");
        if (!Files.exists(headPath)) {
            return "";
        }
        return Files.readString(headPath).trim();
    }

    private static String branchFromHead(String head) {
        String prefix = "ref: refs/heads/";
        if (head.startsWith(prefix)) {
            return head.substring(prefix.length());
        }
        return null;
    }

    private static Path branchPath(String name) {
        return Path.of("git", "refs", "heads", name);
    }

    private static String readBranch(String name) throws IOException {
        Path p = branchPath(name);
        if (!Files.exists(p)) {
            return null;
        }
        String hash = Files.readString(p).trim();
        return hash.isEmpty() ? null : hash;
    }

    public static String currentCommit() throws IOException {
        String head = readHead();
        String branch = branchFromHead(head);
        if (branch != null) {
            return readBranch(branch);
        }
        return head.isEmpty() ? null : head;
    }

    public static void createBranch(String name) throws IOException {
        if (name == null || name.isBlank() || name.contains("/") || name.contains(" ")) {
            throw new IOException("not real branch name " + name);
        }
        if (Files.exists(branchPath(name))) {
            throw new IOException("Branch already exists: " + name);
        }
        String current = currentCommit();
        if (current == null) {
            throw new IOException("fail" + name + ": there are no commits yet");
        }
        Files.createDirectories(branchPath(name).getParent());
        Files.writeString(branchPath(name), current);
    }

    public static void checkout(String target) throws IOException {
        String branchCommit = readBranch(target);
        if (branchCommit != null) {
            checkoutCommit(branchCommit);
            Files.writeString(Path.of("git", "HEAD"), "ref: refs/heads/" + target);
            return;
        }
        if (isCommit(target)) {
            checkoutCommit(target);
            Files.writeString(Path.of("git", "HEAD"), target);
            return;
        }
        throw new IOException("No such branch or commit: " + target);
    }

    private static boolean isCommit(String hash) throws IOException {
        if (!hash.matches("[0-9a-f]{40}")) {
            return false;
        }
        Path obj = Path.of("git", "objects", hash);
        return Files.isRegularFile(obj) && Files.readString(obj).startsWith("tree: ");
    }

    private static void checkoutCommit(String commitHash) throws IOException {
        String treeHash = null;
        for (String line : Files.readAllLines(Path.of("git", "objects", commitHash))) {
            if (line.startsWith("tree: ")) {
                treeHash = line.substring("tree: ".length()).trim();
            }
        }
        if (treeHash == null) {
            throw new IOException("Not a commit: " + commitHash);
        }

        ArrayList<String> entries = new ArrayList<>();
        readTree(treeHash, "", entries);

        Path index = Path.of("git", "index");
        ArrayList<String> targetPaths = new ArrayList<>();
        for (String e : entries) {
            targetPaths.add(e.substring(e.indexOf(' ') + 1));
        }
        if (Files.exists(index)) {
            for (String line : Files.readAllLines(index)) {
                int space = line.indexOf(' ');
                if (space < 0) {
                    continue;
                }
                String path = line.substring(space + 1);
                if (!targetPaths.contains(path)) {
                    Files.deleteIfExists(Path.of(path));
                }
            }
        }

        for (String e : entries) {
            int space = e.indexOf(' ');
            String blobHash = e.substring(0, space);
            Path out = Path.of(e.substring(space + 1));
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
            Files.copy(Path.of("git", "objects", blobHash), out,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        Files.writeString(index, String.join("\n", entries));
    }

    private static void readTree(String treeHash, String prefix, ArrayList<String> out) throws IOException {
        for (String line : Files.readAllLines(Path.of("git", "objects", treeHash))) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(" ", 3);
            String path = prefix.isEmpty() ? parts[2] : prefix + "/" + parts[2];
            if (parts[0].equals("tree")) {
                readTree(parts[1], path, out);
            } else {
                out.add(parts[1] + " " + path);
            }
        }
    }
}
