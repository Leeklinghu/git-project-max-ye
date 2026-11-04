import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class GitTester {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        GitWrapper.init();
        GitWrapper.add("myProgram/hello.txt");
        GitWrapper.add("myProgram/world.txt");
        String hash = GitWrapper.commit("John Doe", "Initial commit");
        System.out.println("Commit: " + hash);
    }
}
