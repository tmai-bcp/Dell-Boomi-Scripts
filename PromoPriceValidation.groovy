// Boomi time uses EST. Promo times are PST. Need to convert time Boomi time to PST
import com.boomi.execution.ExecutionUtil;
import java.util.regex.*;

logger = ExecutionUtil.getBaseLogger();

def date = new Date()
def today = date.format( 'yyyy-MM-dd', TimeZone.getTimeZone('PST'))
def current_time = date.format('HH:mm:ss', TimeZone.getTimeZone('PST'))
//logger.warning("Current time : " + current_time);
//logger.warning("Current date : " + today);


def timeMatcher = ~/([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]/;
def validate_start = start_time ==~ timeMatcher;
def validate_end = end_time ==~ timeMatcher;

if (start_time != null && start_time != "" && validate_start){
    start_time = date.parse('HH:mm:ss', start_time).format('HH:mm:ss', TimeZone.getTimeZone('PST'))
}
if (end_time != null && end_time != "" && validate_end){
    end_time = date.parse('HH:mm:ss', end_time).format('HH:mm:ss', TimeZone.getTimeZone('PST'))
}

//if this is true, date validation failed - seller_cost is set
if ((start_date > today || 
    end_date < today || 
    start_date > end_date || 
    start_date == "" || 
    end_date == ""))
       promo_price = seller_cost;
//date validation passed, now check time validation
else if (start_date == today) {
    //if this is true, time validation failed - seller_cost is set
    if (start_time > current_time) 
         promo_price = seller_cost;
    //if date and time validation passed - promo_price is set         
    }
    
else if (end_date == today){
//   logger.warning("End date = today");
  if (end_time <= current_time) {
//      logger.warning("end_time <= current_time");
         promo_price = seller_cost;
  }
}
