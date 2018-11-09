/**
Used in Salsify Publish/Unpublish Shopify Products Queue process
**/

import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;

logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    json_string = is.getText();
    
    def object = new JsonSlurper().parseText(json_string); 
    def idMap = [:];
    def newList = [];

// Add Product ID and product to idMap
// On second iteration, if Product ID is already a key, add the product as a value of that key
    for (int k = 0; k < object.get(0).products.size(); k++ ) {
        def valueList = [];
        valueList.push(object.get(0).products[k]);
        if (idMap.containsKey(object.get(0).products[k]."Shopify Product ID (US)")) {
            idMap[object.get(0).products[k]."Shopify Product ID (US)"].push(object.get(0).products[k]);
        } else {
            idMap.put(object.get(0).products[k]."Shopify Product ID (US)",valueList);
        }
    }

// For each value in the idMap, if it's not null and equals 1, add value to newList
    idMap.each { key, value ->
        if (value != null && value.size() == 1) {
            newList.push(value);
        }
    }
    
    newList = newList.flatten() - null;

// Remove everything from the object and only add values from newList
    if (newList != null && newList.size() > 0) {
        object.get(0).products.retainAll(newList);
    }

    text = JsonOutput.toJson(object);
	is = new ByteArrayInputStream(text.getBytes("UTF-8"));
    dataContext.storeStream(is, props);
}
