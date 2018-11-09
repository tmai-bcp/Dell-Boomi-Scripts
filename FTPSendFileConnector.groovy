import java.util.Properties;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;
import com.boomi.execution.ExecutionUtil;

// Default 30 second timeout
timeout = null;
timeout = ExecutionUtil.getDynamicProcessProperty("FTP_TIMEOUT_MS");
if (timeout == null || timeout == "" || !timeout.isInteger()){
    timeout = 30000;
}


for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def file_name = props.getProperty("document.dynamic.userdefined.FTP_FILE");
    def host = props.getProperty("document.dynamic.userdefined.FTP_HOST");
    def user = props.getProperty("document.dynamic.userdefined.FTP_USER");
    def passwd = props.getProperty("document.dynamic.userdefined.FTP_PASSWD");
    def dir = props.getProperty("document.dynamic.userdefined.FTP_DIR");

    new FTPClient().with {
        setDefaultTimeout (timeout)
        setConnectTimeout (timeout)
        retry {
            connect host
            enterLocalPassiveMode()
            login user, passwd
            changeWorkingDirectory dir
            storeFile (file_name, is)
            disconnect()
        }
    }
    
    dataContext.storeStream(is, props);
}

def retry(int times = 5, Closure errorHandler = {e-> logger.warning("Retrying! Error Message: " + e.message)}
     , Closure body) {
  int retries = 0;
  def exceptions = null;
  while(retries++ < times) {
    try {
      return body.call();
    } catch(e) {
      exceptions = e;
      errorHandler.call(e);
    }        
  }
  throw new Exception("Failed after $times retries", exceptions);
}
