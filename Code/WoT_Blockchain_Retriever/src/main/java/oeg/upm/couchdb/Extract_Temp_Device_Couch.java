package oeg.upm.couchdb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import oeg.upm.WoT_Blockchain_Retriever.Tokens;

public class Extract_Temp_Device_Couch {
	
	private IntroduceInDB iiDB = new IntroduceInDB();
	
	public void recoverTempDevice(DefaultBlockParameter firstBlock, DefaultBlockParameter finalBlock) {
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

		// Pull all the events for this contract
		web3j.ethLogFlowable(filter).subscribe(log -> {
			String eventHash = log.getTopics().get(0); // Index 0 is the event definition hash
			if(eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent. You can also use filter.addSingleTopic(MY_EVENT_HASH) 
				JsonObject finalTempJson = new JsonObject();
				List<Type> eventParam = FunctionReturnDecoder.decode(log.getData(), MY_EVENT.getParameters());
				finalTempJson.addProperty("@context", eventParam.get(1).getValue().toString());
				finalTempJson.addProperty("identifier", eventParam.get(2).getValue().toString());
				finalTempJson.addProperty("buildingName", eventParam.get(3).getValue().toString());
				finalTempJson.addProperty("location", eventParam.get(4).getValue().toString());
				finalTempJson.addProperty("office", eventParam.get(5).getValue().toString());
				finalTempJson.addProperty("timestamp", eventParam.get(6).getValue().toString());
				finalTempJson.addProperty("lux", eventParam.get(7).getValue().toString());
				finalTempJson.addProperty("co2", eventParam.get(8).getValue().toString());
				finalTempJson.addProperty("humidity", eventParam.get(9).getValue().toString());
				finalTempJson.addProperty("temp", eventParam.get(10).getValue().toString());
				store(finalTempJson.toString(), log.getBlockNumber() + log.getTransactionHash());
			}
		});
	}
	
	public void store(String JsonObject, String hash) throws MalformedURLException, InterruptedException {
		JsonElement element = JsonParser.parseString(JsonObject);
		JsonObject jsonObject = element.getAsJsonObject();
		//			System.out.println(jsonArray.get(i).getAsString());
		byte[] germanBytes = jsonObject.toString().getBytes();
		JsonObject = new String(germanBytes,StandardCharsets.UTF_8);
		InputStream jsonInputStream = new ByteArrayInputStream(jsonObject.toString().getBytes());
		StdHttpClient.Builder builder = new StdHttpClient.Builder();
		builder.username("admin");
		builder.password("andalucia");
		HttpClient httpClient = builder.url("http://localhost:5984").build();  
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);  
		CouchDbConnector db = new StdCouchDbConnector("devices", dbInstance);
		db.update(hash,
				jsonInputStream,
				jsonObject.toString().length(),
				null);
	}

}
