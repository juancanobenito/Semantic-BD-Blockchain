package oeg.upm.extractor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import oeg.upm.WoT_Blockchain_Retriever.Tokens;

public class Extract_Temp_Device {
	
	public String recoverTempDevice(DefaultBlockParameter firstBlock, DefaultBlockParameter finalBlock) {
		Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
		
		Event MY_EVENT = new Event("MyEvent", Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Utf8String>(true) {},
				new TypeReference<Uint256>(true) {},
				new TypeReference<Uint256>(true) {},
				new TypeReference<Uint256>(true) {},
				new TypeReference<Uint256>(true) {},
				new TypeReference<Int256>(true) {}));
		String MY_EVENT_HASH = EventEncoder.encode(MY_EVENT);

		// Filter
		EthFilter filter = new EthFilter(firstBlock, finalBlock, Tokens.TEMPDIR);

		JsonArray finalJson = new JsonArray();
		// Pull all the events for this contract
		web3j.ethLogFlowable(filter).subscribe(log -> {
			String eventHash = log.getTopics().get(0); // Index 0 is the event definition hash
			if(eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent. You can also use filter.addSingleTopic(MY_EVENT_HASH) 
				JsonObject finalTempJson = new JsonObject();
				List<Type> eventParam = FunctionReturnDecoder.decode(log.getData(), MY_EVENT.getParameters());
				finalTempJson.addProperty("@context", "https://juancanobenito.github.io/Semantic-BD-Blockchain/context/devices.json");
				finalTempJson.addProperty("@id", "urn:" + eventParam.get(2).getValue().toString());
				finalTempJson.addProperty("buildingName", eventParam.get(3).getValue().toString());
				finalTempJson.addProperty("location", eventParam.get(4).getValue().toString());
				finalTempJson.addProperty("office", eventParam.get(5).getValue().toString());
				finalTempJson.addProperty("timestamp", eventParam.get(6).getValue().toString());
				finalTempJson.addProperty("lux", eventParam.get(7).getValue().toString());
				finalTempJson.addProperty("co2", eventParam.get(8).getValue().toString());
				finalTempJson.addProperty("humidity", eventParam.get(9).getValue().toString());
				finalTempJson.addProperty("temp", eventParam.get(10).getValue().toString());
				finalJson.add(finalTempJson);
			}
		});
//		System.out.println(finalJson.toString());
		return finalJson.toString();
	}

	public static void main(String[] args) {
		Extract_Temp_Device etd = new Extract_Temp_Device();
		DefaultBlockParameter initialBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(1000));
		DefaultBlockParameter endBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(1002));
		System.out.println(etd.recoverTempDevice(null, null));
	}
}
