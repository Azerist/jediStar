package fr.jedistar.commands;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.UncheckedTimeoutException;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.formats.CommandAnswer;
import fr.jedistar.formats.Unit;
import fr.jedistar.utils.DbUtils;
import fr.jedistar.utils.GuildUnitsSWGOHGGDataParser;

public class GuildReportCommand implements JediStarBotCommand{

	final static Logger logger = LoggerFactory.getLogger(JediStarBotCommand.class);

	private final String COMMAND;
	private final String COMMAND_ALL;

	private final String ERROR_FORBIDDEN;
	private final String ERROR_GUILD_NOT_FOUND;
	private final String ERROR_NO_CHANNEL;
	private final String ERROR_SQL;
	private final String ERROR_SWGOHGG_BLOCKER;	
	private final String ERROR_SWGOHGG_BUG;
	private final String OUTDATED_DATA;



	private final String EXCEL_HEADER_UNIT_TYPE;
	private final String EXCEL_HEADER_UNIT_NAME;
	private final String EXCEL_HEADER_UNIT_RARITY;
	private final String EXCEL_HEADER_UNIT_POWER;
	private final String EXCEL_HEADER_UNIT_LEVEL;
	private final String EXCEL_UNIT_TYPE_SHIP;
	private final String EXCEL_UNIT_TYPE_TOON;
	private final String EXCEL_FILENAME;

	private final static String SQL_FIND_HISTORY = "SELECT expiration,fileContent FROM guildReportHistory WHERE guildID=?;";
	private final static String SQL_FIND_GUILD_UNITS = "SELECT player,power,rarity,level,charID,expiration FROM guildUnits WHERE guildID=? ORDER BY player;";
	private final static String SQL_INSERT_FILE_HISTORY = "REPLACE INTO guildReportHistory(guildID,fileContent,expiration) VALUES (?,?,?);";

	//Nom des champs JSON
	private final static String JSON_GUILD_REPORT = "guildReportCommandParameters";

	private final static String JSON_GUILD_REPORT_COMMANDS = "commands";
	private final static String JSON_GUILD_REPORT_COMMAND_MAIN = "main";
	private final static String JSON_GUILD_REPORT_COMMAND_ALL = "all";

	private final static String JSON_GUILD_REPORT_ERRORS = "errorMessages";
	private final static String JSON_GUILD_REPORT_ERROR_FORBIDDEN = "forbidden";
	private final static String JSON_GUILD_REPORT_ERROR_NO_GUILD_NUMBER = "noGuildNumber";
	private final static String JSON_GUILD_REPORT_ERROR_NO_CHANNEL = "noChannel";
	private final static String JSON_GUILD_REPORT_ERROR_SQL = "sqlError";
	private final static String JSON_GUILD_REPORT_ERROR_SWGOHGG_BUG = "swgohGGbug";
	private final static String JSON_GUILD_REPORT_ERROR_SWGOHGG_BLOCKER = "swgohGGblocker";
	private final static String JSON_GUILD_REPORT_ERROR_OUTDATED_DATA = "outdatedData";

	private final static String JSON_GUILD_REPORT_EXCEL = "excelValues";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS = "headers";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_TYPE = "unitType";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_NAME = "unitName";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_RARITY = "unitRarity";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_POWER = "unitPower";
	private final static String JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_LEVEL = "unitLevel";
	private final static String JSON_GUILD_REPORT_EXCEL_UNIT_TYPES = "unitTypes";
	private final static String JSON_GUILD_REPORT_EXCEL_UNIT_TYPES_SHIP = "ship";
	private final static String JSON_GUILD_REPORT_EXCEL_UNIT_TYPES_TOON = "toon";
	private final static String JSON_GUILD_REPORT_EXCEL_FILENAME = "filename";


	public GuildReportCommand() {
		super();

		//Lire le Json
		JSONObject parameters = StaticVars.jsonMessages;

		JSONObject guildReportParams = parameters.getJSONObject(JSON_GUILD_REPORT);

		JSONObject commandsParams = guildReportParams.getJSONObject(JSON_GUILD_REPORT_COMMANDS);
		COMMAND = commandsParams.getString(JSON_GUILD_REPORT_COMMAND_MAIN);
		COMMAND_ALL = commandsParams.getString(JSON_GUILD_REPORT_COMMAND_ALL);

		JSONObject errorMessages = guildReportParams.getJSONObject(JSON_GUILD_REPORT_ERRORS);
		ERROR_FORBIDDEN = errorMessages.getString(JSON_GUILD_REPORT_ERROR_FORBIDDEN);
		ERROR_GUILD_NOT_FOUND = errorMessages.getString(JSON_GUILD_REPORT_ERROR_NO_GUILD_NUMBER);
		ERROR_NO_CHANNEL = errorMessages.getString(JSON_GUILD_REPORT_ERROR_NO_CHANNEL);
		ERROR_SQL = errorMessages.getString(JSON_GUILD_REPORT_ERROR_SQL);
		ERROR_SWGOHGG_BUG = errorMessages.getString(JSON_GUILD_REPORT_ERROR_SWGOHGG_BUG);
		ERROR_SWGOHGG_BLOCKER = errorMessages.getString(JSON_GUILD_REPORT_ERROR_SWGOHGG_BLOCKER);
		OUTDATED_DATA = errorMessages.getString(JSON_GUILD_REPORT_ERROR_OUTDATED_DATA);

		JSONObject excelParams = guildReportParams.getJSONObject(JSON_GUILD_REPORT_EXCEL);

		JSONObject excelHeaders = excelParams.getJSONObject(JSON_GUILD_REPORT_EXCEL_HEADERS);
		EXCEL_HEADER_UNIT_TYPE = excelHeaders.getString(JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_TYPE);
		EXCEL_HEADER_UNIT_NAME = excelHeaders.getString(JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_NAME);
		EXCEL_HEADER_UNIT_RARITY = excelHeaders.getString(JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_RARITY);
		EXCEL_HEADER_UNIT_POWER = excelHeaders.getString(JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_POWER);
		EXCEL_HEADER_UNIT_LEVEL = excelHeaders.getString(JSON_GUILD_REPORT_EXCEL_HEADERS_UNIT_LEVEL);

		JSONObject excelUnitTypes = excelParams.getJSONObject(JSON_GUILD_REPORT_EXCEL_UNIT_TYPES);
		EXCEL_UNIT_TYPE_SHIP = excelUnitTypes.getString(JSON_GUILD_REPORT_EXCEL_UNIT_TYPES_SHIP);
		EXCEL_UNIT_TYPE_TOON = excelUnitTypes.getString(JSON_GUILD_REPORT_EXCEL_UNIT_TYPES_TOON);

		EXCEL_FILENAME = excelParams.getString(JSON_GUILD_REPORT_EXCEL_FILENAME);

	}

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public CommandAnswer answer(List<String> params, Message receivedMessage, boolean isAdmin) {

		if(!isAdmin) {
			return new CommandAnswer(ERROR_FORBIDDEN, null);
		}

		if(receivedMessage.getChannelReceiver() == null) {
			return new CommandAnswer(ERROR_NO_CHANNEL,null);
		}

		Integer guildID = DbUtils.getGuildIDFromDB(receivedMessage);

		if(guildID == null) {
			return new CommandAnswer(ERROR_SQL, null);
		}
		if(guildID == -1) {
			return new CommandAnswer(ERROR_GUILD_NOT_FOUND, null);
		}

		boolean updateOK = true;
		String errorMessage = "";

		try {
			updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseGuildUnits(guildID);

			updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseShips();

			updateOK = updateOK && GuildUnitsSWGOHGGDataParser.parseCharacters();
		}
		catch(IOException|UncheckedTimeoutException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if(e.getMessage().contains("Server returned HTTP response code: 402")) {
				errorMessage = ERROR_SWGOHGG_BLOCKER;
			}
			else {
				errorMessage= ERROR_SWGOHGG_BUG;
			}

			updateOK = false;
		}

		if(!updateOK) {
			Calendar lastUpdate = DbUtils.findLastUpdateDateForGuild(guildID);

			if(lastUpdate == null) {
				return new CommandAnswer(errorMessage,null);
			}

			String message = receivedMessage.getAuthor().getMentionTag();

			message += "\r\n"+errorMessage+ "\r\n" + String.format(OUTDATED_DATA, lastUpdate);

			//send warning message, but continue treatment
			receivedMessage.reply(message);
		}

		if(COMMAND_ALL.equals(params.get(0))) {

			boolean historyFileFound = findHistoryFile(guildID,receivedMessage);

			if(historyFileFound) {
				return null;
			}

			new GuildReportGenerator(guildID,receivedMessage).start();
		}
		return null;
	}

	private boolean findHistoryFile(Integer guildID, Message receivedMessage) {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = StaticVars.getJdbcConnection();

			stmt = conn.prepareStatement(SQL_FIND_HISTORY);

			stmt.setInt(1, guildID);

			logger.debug("Executing query : "+stmt.toString());

			rs = stmt.executeQuery();

			if(rs.next() && rs.getTimestamp("expiration").after(new Date())) {
				Blob file = rs.getBlob("fileContent");

				InputStream stream = file.getBinaryStream();

				Calendar dataDate = Calendar.getInstance();
				dataDate.setTime(rs.getTimestamp("expiration"));
				dataDate.add(Calendar.HOUR_OF_DAY, -24);
				
				String fileName = String.format(EXCEL_FILENAME,dataDate);
				receivedMessage.getChannelReceiver().sendFile(stream,fileName);
				
				return true;
			}
			return false;

		}
		catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private class GuildReportGenerator extends Thread{

		Integer guildID;
		Message receivedMessage;

		public GuildReportGenerator(Integer guildID, Message receivedMessage) {
			this.guildID = guildID;
			this.receivedMessage = receivedMessage;
		}

		@Override
		public void run() {

			Workbook wb = new XSSFWorkbook();

			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;

			try {
				conn = StaticVars.getIndependantJdbcConnection();

				stmt = conn.prepareStatement(SQL_FIND_GUILD_UNITS);

				stmt.setInt(1, guildID);

				logger.debug("executing query : "+stmt.toString());
				rs = stmt.executeQuery();

				String currentPlayer = "";
				XSSFSheet sheet = null;
				int rowCur = 1;
				long tableId = 1L;
				Timestamp expirationDate = null;

				while(rs.next()) {
					String player = rs.getString("player");
					
					if(expirationDate == null) {
						expirationDate = rs.getTimestamp("expiration");
					}
					
					if(!player.equalsIgnoreCase(currentPlayer)) {

						tableId = formatSheet(sheet, rowCur, tableId);

						currentPlayer = player;
						sheet = initializeNewSheet(wb,player);
						rowCur = 1;
					}

					String baseID = rs.getString("charID");
					Unit unit = DbUtils.findUnitByID(baseID);

					if(unit == null) {
						continue;
					}

					XSSFRow row = sheet.createRow(rowCur);

					XSSFCell cell = row.createCell(0);
					if(Unit.UNIT_TYPE_SHIP == unit.getCombatType()) {
						cell.setCellValue(EXCEL_UNIT_TYPE_SHIP);
					}
					else if(Unit.UNIT_TYPE_TOON == unit.getCombatType()) {
						cell.setCellValue(EXCEL_UNIT_TYPE_TOON);
					}

					row.createCell(1).setCellValue(unit.getName());

					row.createCell(2).setCellValue(rs.getInt("rarity"));

					row.createCell(3).setCellValue(rs.getInt("power"));

					row.createCell(4).setCellValue(rs.getInt("level"));

					rowCur ++;
				}

				tableId = formatSheet(sheet, rowCur, tableId);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				wb.write(stream);
				wb.close();

				Blob blob = new SerialBlob(stream.toByteArray());

				ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

				Calendar dataDate = Calendar.getInstance();
				dataDate.setTime(expirationDate);
				dataDate.add(Calendar.HOUR_OF_DAY, -24);

				
				String fileName = String.format(EXCEL_FILENAME,dataDate);

				receivedMessage.getChannelReceiver().sendFile(inputStream,fileName);

				stream.close();
				inputStream.close();
				
				rs.close();
				stmt.close();
				
				stmt = conn.prepareStatement(SQL_INSERT_FILE_HISTORY);

				stmt.setInt(1, guildID);
				stmt.setTimestamp(3,expirationDate);
				
				logger.debug("Executing query : "+stmt.toString());
				
				stmt.setBlob(2, blob);

				stmt.executeUpdate();
			}
			catch(SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				receivedMessage.reply(ERROR_SQL);
			} catch (IOException e) {
				//Should never happen
				e.printStackTrace();
			}
			finally {
				try {
					if(rs != null) {
						rs.close();
					}
					if(stmt != null) {
						stmt.close();
					}
					if(conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.error(e.getMessage());
				}
			}

		}

		private long formatSheet(XSSFSheet sheet, int rowCur, long tableId) {
			if(sheet != null) {
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				sheet.autoSizeColumn(2);
				sheet.setColumnWidth(2, sheet.getColumnWidth(2) + 500);
				sheet.autoSizeColumn(3);
				sheet.setColumnWidth(3, sheet.getColumnWidth(3) + 500);
				sheet.autoSizeColumn(4);
				sheet.setColumnWidth(4, sheet.getColumnWidth(4) + 500);

				XSSFTable table = sheet.createTable();
				CTTable cttable = table.getCTTable();

				cttable.setId(tableId);

				cttable.setDisplayName("Table"+tableId);
				cttable.setName("Table"+tableId);

				CTTableStyleInfo tableStyle = cttable.addNewTableStyleInfo();
				tableStyle.setName("TableStyleMedium9");           

				tableStyle.setShowColumnStripes(false);
				tableStyle.setShowRowStripes(true);   

				@SuppressWarnings("deprecation")
				AreaReference dataRange = new AreaReference(new CellReference(0, 0), new CellReference(rowCur, 4));    
				cttable.setRef(dataRange.formatAsString());

				CTTableColumns columns = cttable.addNewTableColumns();
				columns.setCount(5L); 
				CTAutoFilter autofilter = cttable.addNewAutoFilter();
				for (int i = 0; i < 5; i++)
				{
					CTTableColumn column = columns.addNewTableColumn();   
					column.setName("Column" + i);      
					column.setId(i+1);

					CTFilterColumn filter = autofilter.addNewFilterColumn();
					filter.setColId(i + 1);
					filter.setShowButton(true);
				}   

				tableId++;

			}
			return tableId;
		}

		private XSSFSheet initializeNewSheet(Workbook wb, String player) {

			XSSFRow row;
			XSSFCell cell;
			XSSFSheet sheet = (XSSFSheet) wb.createSheet(StringUtils.remove(player,"'"));

			row = sheet.createRow(0);

			cell = row.createCell(0);
			cell.setCellValue(EXCEL_HEADER_UNIT_TYPE);

			cell = row.createCell(1);
			cell.setCellValue(EXCEL_HEADER_UNIT_NAME);

			cell = row.createCell(2);
			cell.setCellValue(EXCEL_HEADER_UNIT_RARITY);

			cell = row.createCell(3);
			cell.setCellValue(EXCEL_HEADER_UNIT_POWER);

			cell = row.createCell(4);
			cell.setCellValue(EXCEL_HEADER_UNIT_LEVEL);

			return sheet;
		}

	}
}
