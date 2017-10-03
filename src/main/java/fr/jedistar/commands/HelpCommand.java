package fr.jedistar.commands;

import de.btobastian.javacord.entities.message.Message;
import fr.jedistar.JediStarBotCommand;
import fr.jedistar.StaticVars;
import fr.jedistar.exception.HelpParamException;
import fr.jedistar.formats.CommandAnswer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HelpCommand implements JediStarBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JediStarBotCommand.class);

    private final static String JSON_ERROR_MESSAGE = "errorMessage";
    private final static String JSON_MODS_COMMAND = "helpCommandParameters";
    private final static String JSON_MODS_COMMAND_COMMAND = "command";
    private static final String MODS = "mods";
    private static final String RAID = "raid";
    private static final String ARENE = "arene";
    private static final String EQUILIBRAGE = "balance";

    private final static String JSON_HELP_MESSAGES = "messages";
    private static final String JSON_HELP_MESSAGE_INTRO_MESSAGE = "introMessage";
    private static final String JSON_HELP_MESSAGE_MODS_MESSAGE = "modsMessage";
    private static final String JSON_HELP_MESSAGE_RAID_MESSAGE = "raidMessage";
    private static final String JSON_HELP_MESSAGE_EQUILIBRAGE_MESSAGE = "equilibrageMessage";
    private static final String JSON_HELP_MESSAGE_ARENA_MESSAGE = "arenaMessage";

    private final static String JSON_HELP_ERROR_MESSAGES = "errorMessages";
    private final static String JSON_HELP_ERROR_MESSAGES_PARAMS_ERROR = "paramsError";
    private final static String JSON_HELP_ERROR_MESSAGES_TECHNICAL_ERROR = "technicalError";

    private static String COMMAND;
    private static String ERROR_MESSAGE;
    private static String INTRO_MESSAGE;
    private static String MODS_MESSAGE;
    private static String RAID_MESSAGE;
    private static String ARENA_MESSAGE;
    private static String EQUILIBRAGE_MESSAGE;
    private static String PARAMS_ERROR;
    private static String TECHNICAL_ERROR;

    public HelpCommand() {
        super();

        JSONObject parameters = StaticVars.jsonSettings;

        //messages de base
        ERROR_MESSAGE = parameters.getString(JSON_ERROR_MESSAGE);

        //Paramètres propres à l'équilibrage
        JSONObject modsParams = parameters.getJSONObject(JSON_MODS_COMMAND);

        COMMAND = modsParams.getString(JSON_MODS_COMMAND_COMMAND);

        //Messages
        JSONObject messages = modsParams.getJSONObject(JSON_HELP_MESSAGES);
        INTRO_MESSAGE = messages.getString(JSON_HELP_MESSAGE_INTRO_MESSAGE);
        MODS_MESSAGE = messages.getString(JSON_HELP_MESSAGE_MODS_MESSAGE);
        RAID_MESSAGE = messages.getString(JSON_HELP_MESSAGE_RAID_MESSAGE);
        EQUILIBRAGE_MESSAGE = messages.getString(JSON_HELP_MESSAGE_EQUILIBRAGE_MESSAGE);
        ARENA_MESSAGE = messages.getString(JSON_HELP_MESSAGE_ARENA_MESSAGE);

        //Messages d'erreur
        JSONObject errorMessages = modsParams.getJSONObject(JSON_HELP_ERROR_MESSAGES);
        PARAMS_ERROR = errorMessages.getString(JSON_HELP_ERROR_MESSAGES_PARAMS_ERROR);
        TECHNICAL_ERROR = errorMessages.getString(JSON_HELP_ERROR_MESSAGES_TECHNICAL_ERROR);
    }


    @Override
    public CommandAnswer answer(List<String> params, Message messageRecu, boolean isAdmin) {
        CommandAnswer response = new CommandAnswer("", null);
        try {
            if(params.size() == 0){
                response.setMessage(constructFullMessage());
            }else{
                response.setMessage(constructAppropriateMessage(params.get(0)));
            }
        } catch (HelpParamException e) {
            response.setMessage(PARAMS_ERROR);
        }
        return response;
    }

    private String constructAppropriateMessage(String s) throws HelpParamException {
        StringBuilder sb = new StringBuilder();
        sb.append(INTRO_MESSAGE);
        switch (s){
            case MODS:
                sb.append(MODS_MESSAGE);
                break;
            case ARENE:
                sb.append(ARENA_MESSAGE);
                break;
            case EQUILIBRAGE:
                sb.append(EQUILIBRAGE_MESSAGE);
                break;
            case RAID:
                sb.append(RAID_MESSAGE);
                break;
            default:
                throw new HelpParamException();
        }
        return sb.toString();
    }

    private String constructFullMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(INTRO_MESSAGE);
        sb.append(MODS_MESSAGE);
        sb.append("\r\n");
        sb.append(RAID_MESSAGE);
        sb.append("\r\n");
        sb.append(EQUILIBRAGE_MESSAGE);
        sb.append("\r\n");
        sb.append(ARENA_MESSAGE);

        return sb.toString();
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
