package oeg.upm.mysql;

import java.math.BigInteger;
import java.net.MalformedURLException;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class IntroduceInMySQL {

	private static Extract_Energy_Devices_MySQL energy = new Extract_Energy_Devices_MySQL();
	private static Extract_Temp_Device_MySQL temp = new Extract_Temp_Device_MySQL();

	public static void configure(int initial, String end, Boolean isEnergy) {
		DefaultBlockParameter initialBlock;
		DefaultBlockParameter endBlock;
		if(initial == 0) {
			initialBlock = DefaultBlockParameterName.EARLIEST;
		}else {
			initialBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(initial));
		}
		if(end.contentEquals("*")) {
			endBlock = DefaultBlockParameterName.LATEST;
		}else{
			try{
				if(initial > Integer.parseInt(end)) {
					System.out.println("The initial block must be higher than the final block");
				}
			}catch(Exception e) {
				System.out.println("The final block must be * or a number");
			}
			endBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(Integer.parseInt(end)));
		}
		if(!isEnergy) {
			temp.recoverTempDevice(initialBlock, endBlock);
		}else {
			energy.recoverEnergyDevice(initialBlock, endBlock);
		}
	}

	public static void main( String[] args ) throws MalformedURLException{
		IntroduceInMySQL iiDB = new IntroduceInMySQL();
		long startTime = System.nanoTime();
		iiDB.configure(0, "*", true);
		long stopTime = System.nanoTime();
		System.out.println(stopTime - startTime);
		System.exit(0);
	}

}
