import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class GitWrapper {

    /**
     * Initializes a new Git repository.
     * Creates git/, git/objects/, git/index, and git/HEAD.
     * @throws IOException 
     */
    public static void init() throws IOException {
        Git.init();
    }

    /**
     * Stages a file for the next commit.
     * Throws IOException if the path does not exist or is a directory.
     * Does nothing if the file's content is already staged for that path.
     *
     * @param filePath The path to the file to stage.
     * @throws NoSuchAlgorithmException 
     */
    public static void add(String filePath) throws IOException, NoSuchAlgorithmException {
        Git.index(filePath);
        Git.createTreeFromIndex();
    }

    /**
     * Creates a commit capturing the current state of the index.
     * Builds trees, writes the commit file, updates HEAD.
     *
     * Commit format:
     *   tree: <tree_sha>
     *   parent: <parent_sha>   (omit line for initial commit)
     *   author: <author>
     *   date: <date>
     *   message: <message>
     *
     * @param author  The author name.
     * @param message The commit message.
     * @return The SHA-1 hash of the new commit.
          * @throws IOException 
          * @throws NoSuchAlgorithmException 
          */
         public static String commit(String author, String message) throws NoSuchAlgorithmException, IOException {
        return Git.commit(author, message);
    }

    /**
     * EXTRA CREDIT: Restores the working directory to the state of a specific commit.
     * Traces the commit's root tree and recreates all files and directories.
     *
     * @param commitHash The SHA-1 hash of the commit to check out.
     */
    public static void checkout(String commitHash) {
        // implement here
    }
}
