import java.util.Properties;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
	InputStream is = dataContext.getStream(i);
	Properties props = dataContext.getProperties(i);
	
	String baseUrl = props.getProperty("document.dynamic.userdefined.import_query");
	logger.warning("URL to GET: " + baseUrl);
	String output = (baseUrl).toURL().text;
	logger.warning("Wrote output to string");
	
	is = new ByteArrayInputStream(output.getBytes("UTF-8")); 

	dataContext.storeStream(is, props);
}
