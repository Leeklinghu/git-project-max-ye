import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Git {

    public static void main(String[] args) {
        try{
            if(!CreateFolderFile()){
                System.out.println("Git Repository Already Exists");
            }
            else{
                System.out.println("Git Repository Created");
            }
        }
        catch(IOException e){
            System.out.println("File error: " + e.getMessage());
        }
    }

    public static boolean CreateFolderFile()throws IOException{ //return false if there is no folder that is created, else return true;
        int CreatedCount=0;
        File gitFolder = new File("git/");
        if(!gitFolder.exists()){
            gitFolder.mkdir();
            CreatedCount++;
        }
        File objectFolder =new File(gitFolder,"objects/");
        if(!objectFolder.exists()){
            objectFolder.mkdir();
            CreatedCount++;
        }
        File indexFile = new File(gitFolder,"index");
        if(!indexFile.exists()){
            indexFile.createNewFile();
            CreatedCount++;
        }
        File HeadFile = new File(gitFolder,"Head");
        if(!HeadFile.exists()){
            HeadFile.createNewFile();
            CreatedCount++;
        }

        if(CreatedCount==0){
            return false;
        }
        else{
            return true;
        }

    }


}