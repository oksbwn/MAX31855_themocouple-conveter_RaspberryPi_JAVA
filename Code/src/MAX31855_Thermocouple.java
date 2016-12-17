
import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

public class MAX31855_Thermocouple {
    public static SpiDevice spi = null;
    public static void main(String args[]) throws InterruptedException, IOException {
    	
    	System.out.println("Starting Thermocouple Application.");
        spi = SpiFactory.getInstance(SpiChannel.CS0,SpiDevice.DEFAULT_SPI_SPEED,SpiDevice.DEFAULT_SPI_MODE);
        while(true){
        	System.out.println(getConversionValue()); //Print out red ADC Sample Counts
            Thread.sleep(2000);
        }
        
    }
    public static double getConversionValue() throws IOException {

        byte data[] = new byte[] {0,0, 0, 0};// Dummy payloads. It's not responsible for anything.
       
        byte[] result = spi.write(data); //Request data from MAX31855 via SPI with dummy pay-load
        
        if((result[0] & 128)==0 && (result[1] & 1)==1 ) {//Sign bit is 0 and D16 is high corresponds to Thermocouple not connected.
            System.out.println("Thermocouple is not connected");
            return 0;
        }
        String stringResult=String.format("%32s",Integer.toString(ByteBuffer.wrap(result).getInt(), 2)).replace(' ', '0');
        double valInt=0.0;
        
        if(stringResult.charAt(0)=='1' ){  //Checking for signed bit. If need to convert to 2's Complement.
         	StringBuilder onesComplementBuilder = new StringBuilder();
         	
        	for(char bit : stringResult.substring(0, 12).toCharArray()) {
        	    onesComplementBuilder.append((bit == '0') ? 1 : 0);  // if bit is '0', append a 1. if bit is '1', append a 0.
        	}
        	String onesComplement = onesComplementBuilder.toString();
        	valInt = -1*( Integer.valueOf(onesComplement, 2) + 1); // two's complement = one's complement + 1. This is the positive value of our original binary string, so make it negative again.
        	
        }else{
        	valInt=Integer.parseInt(stringResult.substring(0, 12),2); //+ve no convert to double value
        }
        
        if(stringResult.charAt(12)=='1') //Check for D18 and D19 for fractional values
        	valInt+=0.5;	
        if(stringResult.charAt(13)=='1')
        	valInt+=0.25;
        
        return valInt;
    }
}