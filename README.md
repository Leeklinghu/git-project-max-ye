# git-project-max-ye
2.1 CreateFolderFile() return false if there is no folder that is created,else return true, to check the initial folder is creater or not 
2.2 a hashfile that get the file pathline which output a sha-1 hashcode
2.3 createBlob() takes the file path and uses hashFile to get its SHA-1 hash, then creates a file in git/objects using the hash as its name and copies the original content into it. 