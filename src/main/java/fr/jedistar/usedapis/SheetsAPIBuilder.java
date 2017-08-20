package fr.jedistar.usedapis;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetsAPIBuilder{

    private final String APPLICATION_NAME ="Bot JediStar";
    
    private static String AUTH_FILE = null;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private HttpTransport HTTP_TRANSPORT;
    
    private Sheets sheetsAPI = null;

    private final List<String> SCOPES_READONLY = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);
    private final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

    private boolean readOnly;
    	
    private String sheetID;
  
    public SheetsAPIBuilder(String sheetId,boolean readonly) throws IOException, GeneralSecurityException {

    	InputStream in = new FileInputStream(AUTH_FILE);
    	
    	HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    	GoogleCredential credential = null;

    	if(readonly) {
    		credential = GoogleCredential.fromStream(in).createScoped(SCOPES_READONLY);
    	}
    	else {
    		credential = GoogleCredential.fromStream(in).createScoped(SCOPES);
    	}

    	sheetsAPI = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

    	this.sheetID = sheetId;
    	this.readOnly = readonly;
    }

    /**
     * Permet de définir de manière statique le chemin du fichier d'authentification à Google API
     * @param path
     */
    public static void setAuthFilePath(String path) {
    	AUTH_FILE = path;
    }
    
    public List<List<Object>> getRange(String range) throws IOException{
    	
    	ValueRange response = sheetsAPI.spreadsheets().values().get(sheetID,range).execute();
    	
    	return response.getValues();
    }

    public void write(String range,List<List<Object>> data) throws IOException {

    	if(readOnly) {
    		throw new UnsupportedOperationException("Trying to write to Google Sheets using a readonly instance of Sheets API");
    	}

    	ValueRange oRange = new ValueRange();
    	oRange.setRange(range);
    	oRange.setValues(data);

    	List<ValueRange> oList = new ArrayList<>();
    	oList.add(oRange);

    	BatchUpdateValuesRequest oRequest = new BatchUpdateValuesRequest();
    	oRequest.setValueInputOption("RAW");
    	oRequest.setData(oList);


    	BatchUpdateValuesResponse oResp1 = sheetsAPI.spreadsheets().values().batchUpdate(sheetID, oRequest).execute();
    }
    
    public Integer readInteger(Object valueFromSheet) {
    	if(valueFromSheet instanceof Integer) {
    		return (Integer)valueFromSheet;
    	}
    	else if(valueFromSheet instanceof String) {
    		return Integer.parseInt((String)valueFromSheet);
    	}
    	else {
    		return null;
    	}
    }
}
