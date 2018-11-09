import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.OutputStream;

//Used as part of the Salsify data cleaning process in every process that pulls data from Salsify.
//logger = ExecutionUtil.getBaseLogger();
def matcher = ~/SKY[0-9]{2,4}/

for (int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    json_string = is.getText();
     
    def object = new JsonSlurper().parseText(json_string); 
    def parentMap = [:]
    def childMap = [:]
    
    //If product is a variant, add product to childMap with the ID as the key and object as value
    //Else, add the parent to the parentMap with the parent ID as the key and object as value
    for (int k = 0; k < object.get(0).products.size(); k++ ) {
        if (object.get(0).products[k].'salsify:id' ==~ matcher && object.get(0).products[k].'salsify:parent_id' != null){
            childMap.put(object.get(0).products[k].'salsify:id' ,object.get(0).products[k]); 
           }
        else {
            parentMap[object.get(0).products[k].'salsify:id'] = object.get(0).products[k];
           }
    }

    //For products in childMap, if the key in parentMap matches the parent ID of the child, add the value from the parent to the child if it's not already there.
    childMap.each { childKey, childValue -> 
        if (parentMap.containsKey(childValue.'salsify:parent_id')){
            parentMap[childValue.'salsify:parent_id'].each {parKey, parValue ->
                if (childValue[parKey] == null){
                    childValue[parKey] = parValue;
                }
            }
        }
    }
   
   //Update variant values in the original list.
    for (int j = 0; j < object.get(0).products.size(); j++ ) {
        if (childMap.containsKey(object.get(0).products[j].'salsify:id')) {
            object.get(0).products[j] = childMap[object.get(0).products[j].'salsify:id']
        }
    }
 
    text = JsonOutput.toJson(object);
	is = new ByteArrayInputStream(text.getBytes("UTF-8"));
    dataContext.storeStream(is, props);
}
