package oeg.upm.management;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;

import oeg.upm.contract.Energy_IoT;
import oeg.upm.contract.IoT_Temp;
import oeg.upm.contract.Sensor_IoT;

public class ContractManagement {
	
	private Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
	
	public void introduceGeneralValues(String account, String priv_Key) {
		try {
			String contractAddress = "0x954c986b38f35FD7D7cBeA21134159ff1465596f"; //The deployed contract address, taken from truffle console or ganache logs
			Credentials credentials = Credentials.create(priv_Key);
			BigInteger gasPrice = new BigInteger("85000");
			BigInteger gasLimit = new BigInteger("6721975");
			System.out.println(web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).sendAsync().get().getBlock().getGasLimit());
			Sensor_IoT sensorContract = Sensor_IoT.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
			sensorContract.storeDeviceStatus(account, contractAddress, contractAddress, account, priv_Key, contractAddress, gasLimit, gasLimit, gasLimit, gasPrice, gasLimit);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void introduceEnergyValues(String account, String priv_Key, String context, String building, String location, long timestamp, int energy) {
		try {
			String contractAddress = "0x40c3bcF4cd5b555AF05a71B21CB4809345B8d48e"; //The deployed contract address, taken from truffle console or ganache logs
			Credentials credentials = Credentials.create(priv_Key);
			BigInteger gasPrice = new BigInteger("85000");
			BigInteger gasLimit = new BigInteger("6721975");
			System.out.println(web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).sendAsync().get().getBlock().getGasLimit());
			Energy_IoT energyContract = Energy_IoT.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
			energyContract.storeDeviceStatus(account, context, account, building, location, BigInteger.valueOf(timestamp), BigInteger.valueOf(energy));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getCurrentBlock() {
		try {		
			return web3j.ethBlockNumber().send().getBlockNumber().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
