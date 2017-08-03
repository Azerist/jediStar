


import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

/**
 * A simple ping-pong bot.
 */
public class PingPong {

    public PingPong(String token) {
        // See "How to get the token" below
        DiscordAPI api = Javacord.getApi(token, true);
        // connect
        api.connect(new FutureCallback<DiscordAPI>() {
            
            public void onSuccess(DiscordAPI api) {
                // register listener
                api.registerListener(new MessageCreateListener() {
                    
                    public void onMessageCreate(DiscordAPI api, Message message) {
                        // check the content of the message
                        if (message.getContent().equalsIgnoreCase("ta mère")) {
                            // reply to the message
                            message.reply("connard");
                        }
                    }
                });
            }
            
            
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

}