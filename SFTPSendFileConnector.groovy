import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import com.jcraft.jsch.*;

logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def file_name = props.getProperty("document.dynamic.userdefined.FTP_FILE");
    def host = props.getProperty("document.dynamic.userdefined.FTP_HOST");
    def user = props.getProperty("document.dynamic.userdefined.FTP_USER");
    def passwd = props.getProperty("document.dynamic.userdefined.FTP_PASSWD");
    def dir = props.getProperty("document.dynamic.userdefined.FTP_DIR");
    
    try {
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
        def new_string = sftpChannel.put(is, (dir + "/" + file_name), ChannelSftp.OVERWRITE);
        
        sftpChannel.exit();
        session.disconnect();
        
    } catch (JSchException e) {
        logger.warning("Cannot connect to BazaarVoice SFTP: " + e);
    }

    dataContext.storeStream(is, props);
}
