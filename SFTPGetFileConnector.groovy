import java.util.Properties;
import java.io.InputStream;
import com.jcraft.jsch.*;

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def file_name = props.getProperty("document.dynamic.userdefined.FTP_FILE");
    def host = props.getProperty("document.dynamic.userdefined.FTP_HOST");
    def user = props.getProperty("document.dynamic.userdefined.FTP_USER");
    def passwd = props.getProperty("document.dynamic.userdefined.FTP_PASSWD");
    def dir = props.getProperty("document.dynamic.userdefined.FTP_DIR");
    
    JSch jsch = new JSch();
    jsch.setKnownHosts(host);
    Session session = jsch.getSession(user, host, 22);
    session.setPassword(passwd);
    
    java.util.Properties config = new java.util.Properties(); 
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
    
    session.connect();
    
    Channel channel = session.openChannel("sftp");
    channel.connect();

    ChannelSftp sftpChannel = (ChannelSftp) channel;

    def new_string = sftpChannel.get(file_name).getText();

    sftpChannel.exit();
    session.disconnect();
    
    is = new ByteArrayInputStream(new_string.getBytes("UTF-8"))
    dataContext.storeStream(is, props);
}
