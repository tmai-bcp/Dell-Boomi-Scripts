import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import java.util.List;
import java.util.Map;

//Used in Google Express orders process to concat the street address

logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
	InputStream is = dataContext.getStream(i);
	Properties props = dataContext.getProperties(i);
	json_string = is.getText();
	
	def object = new JsonSlurper().parseText(json_string); 
	
	//loops through each order in a batch call to concat addresses for each one
	def streetAddr1 = "";
	def streetAddr2 = "";
	def streetAddrSize = object.resources.deliveryDetails.address.streetAddress.flatten().size();
	for ( int j = 0; j < object.resources.size(); j++) {
	    if (streetAddrSize > 1) {
	        streetAddr1 = object.resources.deliveryDetails.address.streetAddress[j][0];
	        streetAddr2 = object.resources.deliveryDetails.address.streetAddress[j][1];
	    }
	    else {
	        streetAddr1 = object.resources.deliveryDetails.address.streetAddress[j][0];
	        
	    }
	}
	
	
	object.resources.deliveryDetails.address[0] << ['streetAddr1' : streetAddr1];
	object.resources.deliveryDetails.address[0] << ['streetAddr2' : streetAddr2];

	text = JsonOutput.toJson(object);
	is = new ByteArrayInputStream(text.getBytes("UTF-8"));
	dataContext.storeStream(is, props);
}
