import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class GitTreeTest {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        // testing functionality of verification 1
        Git.index("tree-test/docs/hello.txt)");

        // original tester
        ArrayList<String> workingList = new ArrayList<>(); 

        workingList.add("blob 483b5e082cf5502b303ba3dd4f3469a49fd3421f layer1/layer2/entry.txt");
        workingList.add("blob 4377a91cdfd44db9a9bbf056849c7da0fc6cc7be layer1/README.md");
        workingList.add("blob 0a4d55a8d778e5022fab701977c5d840bbc486d0 layer1/layer2/Hello.txt");

        ArrayList<String> originalList = new ArrayList<>(workingList);

         String firstHash = Git.createTree(workingList, "layer1/layer2"); 
         String secondHash = Git.createTree(workingList, "layer1/layer2");
         
         Path tree = Path.of("git", "objects", firstHash);
         String check = Files.readString(tree);

         String ref = "blob 0a4d55a8d778e5022fab701977c5d840bbc486d0 Hello.txt\n" + "blob 483b5e082cf5502b303ba3dd4f3469a49fd3421f entry.txt\n";

        if (!check.equals(ref)) { 
            throw new AssertionError("Tree contents not right");
        }

        if (!firstHash.equals(secondHash)) {
            throw new AssertionError("The two tree ==");
        }

        if (!workingList.equals(originalList)) {
            throw new AssertionError("createTree changed the workingList");
        }

        System.out.println("passed.");
        System.out.println("hash: " + firstHash);
        System.out.println("contents:");
        System.out.print(check);
    }
}