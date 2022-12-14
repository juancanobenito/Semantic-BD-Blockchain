package oeg.upm.mysql;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import oeg.upm.WoT_Blockchain_Retriever.Tokens;

public class Extract_Energy_Devices_MySQL {

	public void recoverEnergyDevice(DefaultBlockParameter firstBlock, DefaultBlockParameter finalBlock) {
		Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
		Event MY_EVENT = new Event("MyEvent", Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Uint256>(true) {},
				new TypeReference<Uint256>(true) {}));
		String MY_EVENT_HASH = EventEncoder.encode(MY_EVENT);

		// Filter
		EthFilter filter = new EthFilter(firstBlock, finalBlock, Tokens.ENERGYDIR);
		
		// Pull all the events for this contract
		web3j.ethLogFlowable(filter).subscribe(log -> {
			String eventHash = log.getTopics().get(0); // Index 0 is the event definition hash
			if(eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent. You can also use filter.addSingleTopic(MY_EVENT_HASH) 
				List<Type> eventParam = FunctionReturnDecoder.decode(log.getData(), MY_EVENT.getParameters());
				String context = eventParam.get(1).getValue().toString();
				String id = eventParam.get(2).getValue().toString();
				String build = eventParam.get(3).getValue().toString();
				String location = eventParam.get(4).getValue().toString();
				String timestamp = eventParam.get(5).getValue().toString();
				String energy = eventParam.get(6).getValue().toString();
				storeEnergy(context, id, build, location, timestamp, energy);
			}
		});
//		System.out.println(finalJson.toString());
	}

	public void storeEnergy(String context, String identifier, String building,String location, String timestamp, String energy) throws MalformedURLException, InterruptedException {
		Connection conn = null;
		try {
			Timestamp tsConverted = new Timestamp(Long.parseLong(timestamp));
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/energy?user=root&password=Lotus123_");
		    String consulta = "INSERT INTO `energy`.`energydata` (context, identifier, building, location, timestamp, energy) VALUES "
		    		+ "('"+context+"','"+identifier+"','"+building+"','"+location+"','"+tsConverted+"','"+Double.parseDouble(energy)/10000+"');";
		    Statement sentencia = conn.createStatement();
		    sentencia.executeUpdate(consulta);
		    conn.close();
		    
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

}
