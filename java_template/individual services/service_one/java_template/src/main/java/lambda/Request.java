package lambda;

/**
 *
 * @author Wes Lloyd
 */
public class Request {

    String bucketname;
    String filename;
    String key;
    int col;
    int row;

    public String getBucketName() {
        return this.bucketname;
    }
    
    public String getBucketNameALLCAPS() {
        return bucketname.toUpperCase();
    }

    public void setBucketname(String theBucketname) {
        this.bucketname = theBucketname;
    }

	//filename
    public String getFileName() {
        return this.filename;
    }
    
    public String getFileNameALLCAPS() {
        return this.filename.toUpperCase();
    }

    public void setFilename(String theFilename) {
        this.filename = theFilename;
    }


    public String getKey() {
        return this.key;
    }
    
    public String getKeyALLCAPS() {
        return this.key.toUpperCase();
    }

    public void setKey(String theKey) {
        this.key = theKey;
    }
	//col

    public int getCol() {
        return this.col;
    }
    
    public void setCol(int theCol) {
        this.row = theCol;
    }

	//row

    public int getRow() {
        return this.row;
    }
    

    public void setRow(int theRow) {
        this.col = theRow;
    }



	//constructor
    public Request(String bucketname, String filename, String key, int col, int row) {
        this.setBucketname(bucketname);
        this.setFilename(filename);
        this.setCol(col);
        this.setRow(row);
        this.setKey(key);

    }

    public Request() {

    }
}

