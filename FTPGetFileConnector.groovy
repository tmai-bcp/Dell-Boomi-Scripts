import java.util.Properties;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def file_name = props.getProperty("document.dynamic.userdefined.FTP_FILE");
    def host = props.getProperty("document.dynamic.userdefined.FTP_HOST");
    def user = props.getProperty("document.dynamic.userdefined.FTP_USER");
    def passwd = props.getProperty("document.dynamic.userdefined.FTP_PASSWD");
    def dir = props.getProperty("document.dynamic.userdefined.FTP_DIR");
    
    def incomingFile = new File(file_name)

    new FTPClient().with {
        connect host
        enterLocalPassiveMode()
        login user, passwd
        changeWorkingDirectory dir
        incomingFile.withOutputStream { ostream -> retrieveFile file_name, ostream}
        disconnect()
    }
    
    String new_string = incomingFile.text
    is = new ByteArrayInputStream(new_string.getBytes("UTF-8"))
    dataContext.storeStream(is, props);
}
