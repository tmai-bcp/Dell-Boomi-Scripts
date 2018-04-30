import java.util.Properties;
import java.io.InputStream;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.xpath.XPath;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import java.util.List;
import java.util.logging.Logger;
import com.boomi.execution.ExecutionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

CUTOFFQTY = 50;
NOPRIMETEMPL = 'NO PRIME Standard Sept 2016';
PRIMEALLTEMPL = 'PRIME ALL 2016';
INDIPRIMETEMPL = 'INDI PRIME 9/2016';
CALIPRIMETEMPL = 'CALI PRIME 9/2016';
GEORGIAPRIMETEMPL = 'SAVANNAH PRIME ONLY 2017';
CALIGEORGIAPRIMETEMPL = 'CA, SAVANNAH PRIME ONLY 2017';

itemMap = [:];
templateMap = [:];

Logger logger = ExecutionUtil.getBaseLogger();

//creates the mapping
for( int i = 0; i < dataContext.getDataCount(); i++ ) {
	InputStream is = dataContext.getStream(i);
	Properties props = dataContext.getProperties(i);

	// Build XML Document
	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(is);
	Text quantityText = XPath.newInstance("//*[local-name() = 'locationQuantityAvailable']/*/text()").selectSingleNode(doc);
	Attribute locationAttr = XPath.newInstance("//*[local-name() = 'inventoryLocationJoin']/*/*/@internalId").selectSingleNode(doc);
	Attribute itemNameAttr = XPath.newInstance("//*[local-name() = 'externalId']/*/@externalId").selectSingleNode(doc);


	if(itemNameAttr != null) {
		String itemName = itemNameAttr.getValue();
		String locationName = 'Error';
		Float quantity = 0;

		if(locationAttr != null){
			locationName = getLocationByID(locationAttr.getValue());
		}

		if(quantityText != null) {
			quantity = Float.parseFloat(quantityText.getValue());
		}

		if(!itemMap.containsKey(itemName)){
			itemMap.put(itemName, [:]);
		}

		if(locationName != 'Error' && !itemMap[itemName].containsKey(locationName)){
			itemMap[itemName].put(locationName, quantity);
		} 
		else if(locationName != 'Error' && itemMap[itemName].containsKey(locationName)){
			quantity += itemMap[itemName][locationName];
			itemMap[itemName][locationName] = quantity;
		}
	}
}

logger.warning(itemMap.toString())

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
	InputStream is = dataContext.getStream(i);
	Properties props = dataContext.getProperties(i);

	// Build XML Document
	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(is);

	Attribute itemNameAttr = XPath.newInstance("//*[local-name() = 'externalId']/*/@externalId").selectSingleNode(doc);

	if (itemNameAttr != null && !templateMap.containsKey(itemNameAttr.getValue())) {
		String template = PRIMEALLTEMPL;
		// String no_cali_template = INDIPRIMETEMPL;
		// template = no_cali_template;

		caUnder = false;
		indUnder = false;
		texUnder = false;
		gaUnder = false;

		if(itemNameAttr != null && itemMap.containsKey(itemNameAttr.getValue())){
			locationsMap = itemMap[itemNameAttr.getValue()];

			logger.warning(locationsMap.toString());

			locationsMap.each{ k, v -> 
				if(v <= CUTOFFQTY){
					if(k == "CA"){
						caUnder = true;
					} else if(k == "IND"){
						indUnder = true;
					}
					else if (k == "GA"){
						gaUnder = true;
					}
				}
			}
			
			// Comment this when turning off CALIPRIME for HighJump testing
			if(caUnder && indUnder && gaUnder){
				template = NOPRIMETEMPL;
			} else if (caUnder && !indUnder){
				template = INDIPRIMETEMPL;
			} else if(!caUnder && indUnder && gaUnder){
				template = CALIPRIMETEMPL;
			} 
			else if(caUnder && indUnder && !gaUnder){
				template = GEORGIAPRIMETEMPL;
				// template = NOPRIMETEMPL;
			}
			else if(!caUnder && indUnder && !gaUnder){
				template = CALIGEORGIAPRIMETEMPL;
				// template = CALIPRIMETEMPL;
			} 
			//
			//  End comments here
			
			// Turn off CALIPRIME for HighJump testing by uncommenting this 
			// and commenting the above
			/* Start Comment
			if(caUnder && indUnder && gaUnder){
				template = NOPRIMETEMPL;
			} else if (caUnder && !indUnder){
				template = INDIPRIMETEMPL;
			} else if(!caUnder && indUnder && gaUnder){
				template = NOPRIMETEMPL;
			} 
			else if(caUnder && indUnder && !gaUnder){
				template = GEORGIAPRIMETEMPL;
			}
			else if(!caUnder && indUnder && !gaUnder){
				template = GEORGIAPRIMETEMPL;
			} 
			End Comment */
			//
			
			templateMap.put(itemNameAttr.getValue(), template);
		}
		logger.warning(template)

	}
	//props.setProperty("document.dynamic.userdefined.template", template);


	XMLOutputter outputter = new XMLOutputter();
	is = new ByteArrayInputStream(outputter.outputString(doc).getBytes("UTF-8"));

	dataContext.storeStream(is, props);
}
def json = new ObjectMapper().writeValueAsString(templateMap)
ExecutionUtil.setDynamicProcessProperty("AmazonTemplateMap", json, false);

def getLocationByID(id){
	switch(id){
	case '1':
		return "CA";
		// case '2':  Only Indy 4 is active
		// case '13':
		// case '15':
	case '37':
		return "IND";
	case '43':
		return "GA";
	default:
		return "Error"
	}
}
